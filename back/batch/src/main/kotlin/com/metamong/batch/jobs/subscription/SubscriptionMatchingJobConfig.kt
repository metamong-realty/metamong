package com.metamong.batch.jobs.subscription

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SubscriptionMatchingJobConfig {
    @Bean
    fun subscriptionMatchingJob(
        jobRepository: JobRepository,
        subscriptionMatchingStep: Step,
    ): Job =
        JobBuilder(JOB_NAME, jobRepository)
            .start(subscriptionMatchingStep)
            .build()

    companion object {
        const val JOB_NAME = "subscriptionMatchingJob"
    }
}
