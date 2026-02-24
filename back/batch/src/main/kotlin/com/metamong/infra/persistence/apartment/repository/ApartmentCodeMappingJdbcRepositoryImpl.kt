package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentCodeMappingEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ApartmentCodeMappingJdbcRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : ApartmentCodeMappingJdbcRepository {
    override fun batchInsert(entities: List<ApartmentCodeMappingEntity>): Int {
        if (entities.isEmpty()) return 0

        val sql =
            """
            INSERT INTO apartment_code_mappings (
                complex_id, code_type, code_value,
                created_at, updated_at
            ) VALUES (?, ?, ?, NOW(), NOW())
            """.trimIndent()

        val batchArgs =
            entities.map { mapping ->
                arrayOf(
                    mapping.complexId,
                    mapping.codeType.name,
                    mapping.codeValue,
                )
            }

        val results = jdbcTemplate.batchUpdate(sql, batchArgs)
        return results.count { it != java.sql.Statement.EXECUTE_FAILED }
    }

    // 멱등성 보장을 위함 (Unique key -> INSERT IGNORE 쓰면 이미 있으면 스킵, 없으면 삽입)
    override fun batchInsertIgnore(entities: List<ApartmentCodeMappingEntity>): Int {
        if (entities.isEmpty()) return 0

        val sql =
            """
            INSERT IGNORE INTO apartment_code_mappings (
                complex_id, code_type, code_value,
                created_at, updated_at
            ) VALUES (?, ?, ?, NOW(), NOW())
            """.trimIndent()

        val batchArgs =
            entities.map { mapping ->
                arrayOf(
                    mapping.complexId,
                    mapping.codeType.name,
                    mapping.codeValue,
                )
            }

        val results = jdbcTemplate.batchUpdate(sql, batchArgs)
        return results.count { it != java.sql.Statement.EXECUTE_FAILED }
    }
}
