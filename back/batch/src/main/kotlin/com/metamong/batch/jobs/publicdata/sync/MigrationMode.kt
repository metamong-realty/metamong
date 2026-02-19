package com.metamong.batch.jobs.publicdata.sync

import java.time.LocalDate
import java.time.LocalDateTime

enum class MigrationMode {
    HISTORICAL,
    DAILY,
    ;

    fun getCutoffDate(): LocalDateTime? =
        when (this) {
            HISTORICAL -> null
            DAILY -> LocalDate.now().minusDays(1).atStartOfDay()
        }

    companion object {
        fun fromJobName(jobName: String?): MigrationMode =
            when {
                jobName?.contains("Daily", ignoreCase = true) == true -> DAILY
                else -> HISTORICAL
            }
    }
}
