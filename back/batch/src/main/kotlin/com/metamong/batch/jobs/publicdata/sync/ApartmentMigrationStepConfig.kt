package com.metamong.batch.jobs.publicdata.sync

import com.metamong.batch.jobs.publicdata.sync.listener.ApartmentMigrationRetryListener
import com.metamong.batch.jobs.publicdata.sync.listener.ApartmentMigrationSkipListener
import com.metamong.batch.jobs.publicdata.sync.listener.ApartmentMigrationStepListener
import com.metamong.batch.jobs.publicdata.sync.processor.CreateComplexFromRentProcessor
import com.metamong.batch.jobs.publicdata.sync.processor.CreateComplexProcessor
import com.metamong.batch.jobs.publicdata.sync.processor.MatchInfoRawProcessor
import com.metamong.batch.jobs.publicdata.sync.reader.RentRawDistinctAptSeqReader
import com.metamong.batch.jobs.publicdata.sync.reader.RentRawPagingReader
import com.metamong.batch.jobs.publicdata.sync.reader.TradeRawDistinctAptSeqReader
import com.metamong.batch.jobs.publicdata.sync.reader.TradeRawPagingReader
import com.metamong.batch.jobs.publicdata.sync.reader.UnmatchedInfoRawComplexReader
import com.metamong.batch.jobs.publicdata.sync.reader.UnmatchedLicenseRawComplexReader
import com.metamong.batch.jobs.publicdata.sync.tasklet.CacheWarmingTasklet
import com.metamong.batch.jobs.publicdata.sync.writer.ComplexWriter
import com.metamong.batch.jobs.publicdata.sync.writer.MatchResultWriter
import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.ApartmentRentEntity
import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.service.apartment.dto.ComplexWithApartmentSequence
import org.springframework.batch.core.Step
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemWriter
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.dao.DataAccessException
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ApartmentMigrationStepConfig {
    @Bean
    fun syncStepTaskExecutor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 4
        executor.maxPoolSize = 4
        executor.setThreadNamePrefix("sync-step-")
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(30)
        executor.initialize()
        return executor
    }

    // ===== Steps =====
    @Bean
    fun createComplexStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        tradeRawDistinctAptSeqReader: TradeRawDistinctAptSeqReader,
        createComplexProcessor: CreateComplexProcessor,
        complexWriter: ComplexWriter,
        apartmentMigrationStepListener: ApartmentMigrationStepListener,
        apartmentMigrationSkipListener: ApartmentMigrationSkipListener,
        apartmentMigrationRetryListener: ApartmentMigrationRetryListener,
    ): Step =
        StepBuilder("createComplexStep", jobRepository)
            .chunk<ApartmentTradeRawDocumentEntity, ComplexWithApartmentSequence?>(COMPLEX_CHUNK_SIZE, transactionManager)
            .reader(tradeRawDistinctAptSeqReader)
            .processor(createComplexProcessor)
            .writer(complexWriter)
            .faultTolerant()
            .skipLimit(100)
            .skip(DataAccessException::class.java)
            .skip(IllegalArgumentException::class.java)
            .retryLimit(3)
            .retry(DataAccessException::class.java)
            .listener(apartmentMigrationStepListener)
            .listener(apartmentMigrationSkipListener)
            .listener(apartmentMigrationRetryListener)
            .build()

    @Bean
    fun createComplexFromRentStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        rentRawDistinctAptSeqReader: RentRawDistinctAptSeqReader,
        createComplexFromRentProcessor: CreateComplexFromRentProcessor,
        complexWriter: ComplexWriter,
        apartmentMigrationSkipListener: ApartmentMigrationSkipListener,
        apartmentMigrationRetryListener: ApartmentMigrationRetryListener,
    ): Step =
        StepBuilder("createComplexFromRentStep", jobRepository)
            .chunk<ApartmentRentRawDocumentEntity, ComplexWithApartmentSequence?>(COMPLEX_CHUNK_SIZE, transactionManager)
            .reader(rentRawDistinctAptSeqReader)
            .processor(createComplexFromRentProcessor)
            .writer(complexWriter)
            .faultTolerant()
            .skipLimit(100)
            .skip(DataAccessException::class.java)
            .skip(IllegalArgumentException::class.java)
            .retryLimit(3)
            .retry(DataAccessException::class.java)
            .listener(apartmentMigrationSkipListener)
            .listener(apartmentMigrationRetryListener)
            .build()

    @Bean
    fun matchInfoRawStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        unmatchedInfoRawComplexReader: UnmatchedInfoRawComplexReader,
        matchInfoRawProcessor: MatchInfoRawProcessor,
        matchResultWriter: MatchResultWriter,
    ): Step =
        StepBuilder("matchInfoRawStep", jobRepository)
            .chunk<ApartmentComplexEntity, Boolean>(MATCH_CHUNK_SIZE, transactionManager)
            .reader(unmatchedInfoRawComplexReader)
            .processor(matchInfoRawProcessor)
            .writer(matchResultWriter)
            .build()

    @Bean
    fun matchLicenseRawStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        unmatchedLicenseRawComplexReader: UnmatchedLicenseRawComplexReader,
        matchLicenseRawProcessor: ItemProcessor<ApartmentComplexEntity, Boolean>,
        matchResultWriter: MatchResultWriter,
    ): Step =
        StepBuilder("matchLicenseRawStep", jobRepository)
            .chunk<ApartmentComplexEntity, Boolean>(MATCH_CHUNK_SIZE, transactionManager)
            .reader(unmatchedLicenseRawComplexReader)
            .processor(matchLicenseRawProcessor)
            .writer(matchResultWriter)
            .build()

    @Bean
    fun syncTradeStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        tradeRawPagingReader: TradeRawPagingReader,
        syncTradeProcessor: ItemProcessor<ApartmentTradeRawDocumentEntity, ApartmentTradeEntity?>,
        tradeWriter: ItemWriter<ApartmentTradeEntity?>,
        apartmentMigrationSkipListener: ApartmentMigrationSkipListener,
        apartmentMigrationRetryListener: ApartmentMigrationRetryListener,
        syncStepTaskExecutor: TaskExecutor,
    ): Step =
        StepBuilder("syncTradeStep", jobRepository)
            .chunk<ApartmentTradeRawDocumentEntity, ApartmentTradeEntity?>(SYNC_CHUNK_SIZE, transactionManager)
            .reader(tradeRawPagingReader)
            .processor(syncTradeProcessor)
            .writer(tradeWriter)
            .taskExecutor(syncStepTaskExecutor)
            .faultTolerant()
            .skipLimit(200)
            .skip(DataAccessException::class.java)
            .skip(IllegalArgumentException::class.java)
            .retryLimit(3)
            .retry(DataAccessException::class.java)
            .listener(apartmentMigrationSkipListener)
            .listener(apartmentMigrationRetryListener)
            .build()

    @Bean
    fun syncRentStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        rentRawPagingReader: RentRawPagingReader,
        syncRentProcessor: ItemProcessor<ApartmentRentRawDocumentEntity, ApartmentRentEntity?>,
        rentWriter: ItemWriter<ApartmentRentEntity?>,
        apartmentMigrationSkipListener: ApartmentMigrationSkipListener,
        apartmentMigrationRetryListener: ApartmentMigrationRetryListener,
        syncStepTaskExecutor: TaskExecutor,
    ): Step =
        StepBuilder("syncRentStep", jobRepository)
            .chunk<ApartmentRentRawDocumentEntity, ApartmentRentEntity?>(SYNC_CHUNK_SIZE, transactionManager)
            .reader(rentRawPagingReader)
            .processor(syncRentProcessor)
            .writer(rentWriter)
            .taskExecutor(syncStepTaskExecutor)
            .faultTolerant()
            .skipLimit(200)
            .skip(DataAccessException::class.java)
            .skip(IllegalArgumentException::class.java)
            .retryLimit(3)
            .retry(DataAccessException::class.java)
            .listener(apartmentMigrationSkipListener)
            .listener(apartmentMigrationRetryListener)
            .build()

    @Bean
    fun cacheWarmingStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        cacheWarmingTasklet: CacheWarmingTasklet,
    ): Step =
        StepBuilder("cacheWarmingStep", jobRepository)
            .tasklet(cacheWarmingTasklet, transactionManager)
            .build()

    companion object {
        // Step별 최적화된 CHUNK_SIZE
        private const val COMPLEX_CHUNK_SIZE = 200 // DB 조회 많음
        private const val SYNC_CHUNK_SIZE = 1000 // 단순 배치 인서트
        private const val MATCH_CHUNK_SIZE = 300 // 중간 복잡도
    }
}
