package com.metamong.batch.jobs.publicdata.sync

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.FlowBuilder
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.job.flow.Flow
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ApartmentMigrationDailyJobConfig {
    @Bean
    fun apartmentMigrationDailyJob(
        jobRepository: JobRepository,
        createComplexStep: Step,
        createComplexFromRentStep: Step,
        matchParallelFlow: Flow,
        cacheWarmingStep: Step,
        syncParallelFlow: Flow,
    ): Job {
        val createComplexFlow =
            FlowBuilder<Flow>("createComplexFlow")
                .start(createComplexStep)
                .next(createComplexFromRentStep)
                .build()

        return JobBuilder(JOB_NAME, jobRepository)
            .start(createComplexFlow)
            .next(matchParallelFlow)
            .next(cacheWarmingStep)
            .next(syncParallelFlow)
            .end()
            .build()
    }

    companion object {
        const val JOB_NAME = "apartmentMigrationDailyJob"
    }
}
