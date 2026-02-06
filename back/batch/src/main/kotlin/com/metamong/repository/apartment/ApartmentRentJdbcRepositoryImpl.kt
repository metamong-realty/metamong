package com.metamong.repository.apartment

import com.metamong.entity.apartment.ApartmentRentEntity
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class ApartmentRentJdbcRepositoryImpl(
    private val jdbcTemplate: JdbcTemplate,
) : ApartmentRentJdbcRepository {
    override fun batchUpsert(entities: List<ApartmentRentEntity>): Int {
        if (entities.isEmpty()) return 0

        val sql =
            """
            INSERT INTO apartment_rents (
                unit_type_id, rent_type, deposit, monthly_rent, floor,
                contract_year, contract_month, contract_day, contract_date,
                is_canceled, canceled_date, raw_id,
                created_at, updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
            ON DUPLICATE KEY UPDATE
                deposit = VALUES(deposit),
                monthly_rent = VALUES(monthly_rent),
                floor = VALUES(floor),
                contract_day = VALUES(contract_day),
                contract_date = VALUES(contract_date),
                is_canceled = VALUES(is_canceled),
                canceled_date = VALUES(canceled_date),
                updated_at = NOW()
            """.trimIndent()

        val batchArgs =
            entities.map { entity ->
                arrayOf(
                    entity.unitTypeId,
                    entity.rentType.name,
                    entity.deposit,
                    entity.monthlyRent,
                    entity.floor,
                    entity.contractYear,
                    entity.contractMonth,
                    entity.contractDay,
                    entity.contractDate,
                    entity.isCanceled,
                    entity.canceledDate,
                    entity.rawId,
                )
            }

        val results = jdbcTemplate.batchUpdate(sql, batchArgs)
        return results.sum()
    }
}
