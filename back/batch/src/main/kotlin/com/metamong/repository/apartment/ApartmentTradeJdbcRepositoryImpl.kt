package com.metamong.repository.apartment

import com.metamong.entity.apartment.ApartmentTradeEntity
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
                unit_type_id, price, floor, contract_year, contract_month, contract_day,
                contract_date, deal_type, is_canceled, canceled_date, raw_id,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                price = VALUES(price),
                floor = VALUES(floor),
                contract_day = VALUES(contract_day),
                contract_date = VALUES(contract_date),
                deal_type = VALUES(deal_type),
                is_canceled = VALUES(is_canceled),
                canceled_date = VALUES(canceled_date),
                updated_at = NOW()
            """.trimIndent()

        val batchArgs =
            entities.map { entity ->
                arrayOf(
                    entity.unitTypeId,
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
        return results.sum()
    }
}
