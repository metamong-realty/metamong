package com.metamong.infra.persistence.apartment.repository

import com.metamong.domain.apartment.model.ApartmentTradeEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ApartmentTradeJdbcRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : ApartmentTradeJdbcRepository {
    override fun batchUpsert(entities: List<ApartmentTradeEntity>): Int {
        if (entities.isEmpty()) return 0

        val sql =
            """
            INSERT INTO apartment_trades (
                unit_type_id, exclusive_area, price, floor, contract_year, contract_month, contract_day,
                contract_date, deal_type, is_canceled, canceled_date, raw_id,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW()) AS new_vals
            ON DUPLICATE KEY UPDATE
                exclusive_area = new_vals.exclusive_area,
                price = new_vals.price,
                floor = new_vals.floor,
                contract_day = new_vals.contract_day,
                contract_date = new_vals.contract_date,
                deal_type = new_vals.deal_type,
                is_canceled = new_vals.is_canceled,
                canceled_date = new_vals.canceled_date,
                updated_at = NOW()
            """.trimIndent()

        val batchArgs =
            entities.map { entity ->
                arrayOf(
                    entity.unitTypeId,
                    entity.exclusiveArea,
                    entity.price,
                    entity.floor,
                    entity.contractYear,
                    entity.contractMonth,
                    entity.contractDay,
                    entity.contractDate,
                    entity.dealType,
                    entity.isCanceled,
                    entity.canceledDate,
                    entity.rawId,
                )
            }

        val results = jdbcTemplate.batchUpdate(sql, batchArgs)
        return results.count { it != java.sql.Statement.EXECUTE_FAILED }
    }
}
