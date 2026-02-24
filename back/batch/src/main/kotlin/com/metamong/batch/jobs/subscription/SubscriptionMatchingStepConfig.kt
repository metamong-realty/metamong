package com.metamong.batch.jobs.subscription

import com.metamong.batch.jobs.subscription.reader.NewTradeItemReader
import com.metamong.batch.jobs.subscription.writer.NotificationEventWriter
import com.metamong.domain.apartment.model.ApartmentTradeEntity
import org.springframework.batch.core.Step
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class SubscriptionMatchingStepConfig {
    @Bean
    fun subscriptionMatchingStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        newTradeItemReader: NewTradeItemReader,
        notificationEventWriter: NotificationEventWriter,
    ): Step =
        StepBuilder("subscriptionMatchingStep", jobRepository)
            // 메모리 사용량과 DB 부하 균형: 100건 기준 예상 메모리 ~10MB, 운영 환경에서 모니터링 후 조정
            .chunk<ApartmentTradeEntity, ApartmentTradeEntity>(CHUNK_SIZE, transactionManager)
            .reader(newTradeItemReader)
            .writer(notificationEventWriter)
            .build()

    companion object {
        private const val CHUNK_SIZE = 100
    }
}
