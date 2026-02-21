package com.metamong.infra.persistence.repository.apartment

import com.metamong.domain.apartment.model.ApartmentUnitTypeEntity
import com.metamong.domain.base.AuditContextHolder
import org.springframework.jdbc.core.ConnectionCallback
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.Statement
import java.sql.Timestamp
import java.time.LocalDateTime

@Repository
class ApartmentUnitTypeJdbcRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : ApartmentUnitTypeJdbcRepository {
    override fun batchInsert(entities: List<ApartmentUnitTypeEntity>): List<ApartmentUnitTypeEntity> {
        if (entities.isEmpty()) return emptyList()

        val now = LocalDateTime.now()
        val userId = AuditContextHolder.getCurrentUserId() ?: "unknown"
        val auditUser = "METAMONG:$userId"

        return requireNotNull(
            jdbcTemplate.execute(
                ConnectionCallback<List<ApartmentUnitTypeEntity>> { connection: Connection ->
                    val ps =
                        connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)

                    ps.use { stmt ->
                        for (entity in entities) {
                            setParameters(stmt, entity, auditUser, now)
                            stmt.addBatch()
                        }

                        stmt.executeBatch()

                        val generatedKeys = stmt.generatedKeys
                        val ids = mutableListOf<Long>()
                        while (generatedKeys.next()) {
                            ids.add(generatedKeys.getLong(1))
                        }

                        entities.zip(ids).forEach { (entity, id) ->
                            entity.id = id
                            entity.createdBy = auditUser
                            entity.updatedBy = auditUser
                        }

                        entities
                    }
                },
            ),
        ) { "UnitType batch insert 결과가 null입니다" }
    }

    private fun setParameters(
        ps: PreparedStatement,
        entity: ApartmentUnitTypeEntity,
        auditUser: String,
        now: LocalDateTime,
    ) {
        var idx = 1
        ps.setLong(idx++, entity.complexId)
        ps.setBigDecimal(idx++, entity.exclusiveArea)
        if (entity.exclusivePyeong != null) {
            ps.setShort(idx++, entity.exclusivePyeong!!)
        } else {
            ps.setNull(idx++, java.sql.Types.SMALLINT)
        }
        ps.setString(idx++, auditUser)
        ps.setString(idx++, auditUser)
        ps.setTimestamp(idx++, Timestamp.valueOf(now))
        ps.setTimestamp(idx, Timestamp.valueOf(now))
    }

    companion object {
        private val INSERT_SQL =
            """
            INSERT INTO apartment_unit_types (
                complex_id, exclusive_area, exclusive_pyeong,
                created_by, updated_by, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
    }
}
