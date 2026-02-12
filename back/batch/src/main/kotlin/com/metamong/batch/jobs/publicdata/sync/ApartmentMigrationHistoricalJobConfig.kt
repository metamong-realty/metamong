package com.metamong.batch.jobs.publicdata.sync

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApartmentMigrationHistoricalJobConfig {
    @Bean
    fun apartmentMigrationHistoricalJob(
        jobRepository: JobRepository,
        createComplexStep: Step,
        createComplexFromRentStep: Step,
        matchInfoRawStep: Step,
        matchLicenseRawStep: Step,
        cacheWarmingStep: Step,
        syncTradeStep: Step,
        syncRentStep: Step,
    ): Job =
        JobBuilder(JOB_NAME, jobRepository)
            .start(createComplexStep)
            .next(createComplexFromRentStep)
            .next(matchInfoRawStep)
            .next(matchLicenseRawStep)
            .next(cacheWarmingStep)
            .next(syncTradeStep)
            .next(syncRentStep)
            .build()

    companion object {
        const val JOB_NAME = "apartmentMigrationHistoricalJob"
    }
}
