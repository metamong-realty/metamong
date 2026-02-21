package com.metamong.infra.persistence.repository.apartment

import com.metamong.domain.apartment.model.ApartmentComplexEntity
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
class ApartmentComplexJdbcRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : ApartmentComplexJdbcRepository {
    override fun batchInsert(entities: List<ApartmentComplexEntity>): List<ApartmentComplexEntity> {
        if (entities.isEmpty()) return emptyList()

        val now = LocalDateTime.now()
        val userId = AuditContextHolder.getCurrentUserId() ?: "unknown"
        val auditUser = "METAMONG:$userId"

        return jdbcTemplate.execute(
            ConnectionCallback<List<ApartmentComplexEntity>> { connection: Connection ->
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
        )!!
    }

    private fun setParameters(
        ps: PreparedStatement,
        entity: ApartmentComplexEntity,
        auditUser: String,
        now: LocalDateTime,
    ) {
        var idx = 1
        ps.setInt(idx++, entity.sidoSigunguCode)
        ps.setNullableString(idx++, entity.addressRoad)
        ps.setNullableString(idx++, entity.addressJibun)
        ps.setNullableInt(idx++, entity.eupmyeondongRiCode)
        ps.setString(idx++, entity.platType.name)
        ps.setNullableShort(idx++, entity.bonNo)
        ps.setNullableShort(idx++, entity.buNo)
        ps.setString(idx++, entity.nameRaw)
        ps.setNullableString(idx++, entity.nameNormalized)
        ps.setNullableShort(idx++, entity.builtYear)
        ps.setNullableInt(idx++, entity.totalHousehold)
        ps.setNullableInt(idx++, entity.totalBuilding)
        ps.setNullableInt(idx++, entity.totalParking)
        ps.setNullableBigDecimal(idx++, entity.floorAreaRatio)
        ps.setNullableBigDecimal(idx++, entity.buildingCoverageRatio)
        ps.setNullableString(idx++, entity.heatingType)
        ps.setString(idx++, auditUser)
        ps.setString(idx++, auditUser)
        ps.setTimestamp(idx++, Timestamp.valueOf(now))
        ps.setTimestamp(idx, Timestamp.valueOf(now))
    }

    private fun PreparedStatement.setNullableString(
        index: Int,
        value: String?,
    ) {
        if (value != null) setString(index, value) else setNull(index, java.sql.Types.VARCHAR)
    }

    private fun PreparedStatement.setNullableInt(
        index: Int,
        value: Int?,
    ) {
        if (value != null) setInt(index, value) else setNull(index, java.sql.Types.INTEGER)
    }

    private fun PreparedStatement.setNullableShort(
        index: Int,
        value: Short?,
    ) {
        if (value != null) setShort(index, value) else setNull(index, java.sql.Types.SMALLINT)
    }

    private fun PreparedStatement.setNullableBigDecimal(
        index: Int,
        value: java.math.BigDecimal?,
    ) {
        if (value != null) setBigDecimal(index, value) else setNull(index, java.sql.Types.DECIMAL)
    }

    companion object {
        private val INSERT_SQL =
            """
            INSERT INTO apartment_complexes (
                sido_sigungu_code, address_road, address_jibun, eupmyeondong_ri_code,
                plat_type, bon_no, bu_no, name_raw, name_normalized, built_year,
                total_household, total_building, total_parking, floor_area_ratio,
                building_coverage_ratio, heating_type,
                created_by, updated_by, created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
    }
}
