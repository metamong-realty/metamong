package com.metamong.batch.jobs.publicdata.sync

import com.metamong.batch.jobs.publicdata.sync.reader.DistinctApartmentSequenceItemReader
import com.metamong.batch.jobs.publicdata.sync.reader.MongoPageItemReader
import com.metamong.batch.jobs.publicdata.sync.reader.UnmatchedComplexItemReader
import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.ApartmentRentEntity
import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.infra.persistance.repository.mongo.publicdata.ApartmentRentRawRepository
import com.metamong.infra.persistance.repository.mongo.publicdata.ApartmentTradeRawRepository
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.service.apartment.ApartmentComplexQueryService
import com.metamong.service.apartment.dto.ComplexWithApartmentSequence
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemProcessor
import org.springframework.batch.item.ItemReader
import org.springframework.batch.item.ItemWriter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ApartmentMigrationDailyStepConfig {
    // ===== Readers =====
    @Bean
    @StepScope
    fun tradeRawDistinctAptSeqReader(
        apartmentTradeRawRepository: ApartmentTradeRawRepository,
        apartmentComplexQueryService: ApartmentComplexQueryService,
        @Value("#{jobParameters['mode']}") modeStr: String?,
    ): ItemReader<ApartmentTradeRawDocumentEntity> {
        val mode = MigrationMode.fromString(modeStr)
        val cutoffDate = mode.getCutoffDate()

        return DistinctApartmentSequenceItemReader(
            countFetcher = {
                if (cutoffDate != null) {
                    apartmentTradeRawRepository.countByCollectedAtGreaterThanEqual(cutoffDate)
                } else {
                    apartmentTradeRawRepository.count()
                }
            },
            pageFetcher = { pageable ->
                if (cutoffDate != null) {
                    apartmentTradeRawRepository.findByCollectedAtGreaterThanEqual(cutoffDate, pageable).content
                } else {
                    apartmentTradeRawRepository.findAllBy(pageable).content
                }
            },
            queryService = apartmentComplexQueryService,
            apartmentSequenceExtractor = { it.aptSeq },
            logPrefix = "TradeRaw 전체 데이터",
            mode = mode,
        )
    }

    @Bean
    @StepScope
    fun rentRawDistinctAptSeqReader(
        apartmentRentRawRepository: ApartmentRentRawRepository,
        apartmentComplexQueryService: ApartmentComplexQueryService,
        @Value("#{jobParameters['mode']}") modeStr: String?,
    ): ItemReader<ApartmentRentRawDocumentEntity> {
        val mode = MigrationMode.fromString(modeStr)
        val cutoffDate = mode.getCutoffDate()

        return DistinctApartmentSequenceItemReader(
            countFetcher = {
                if (cutoffDate != null) {
                    apartmentRentRawRepository.countByCollectedAtGreaterThanEqual(cutoffDate)
                } else {
                    apartmentRentRawRepository.count()
                }
            },
            pageFetcher = { pageable ->
                if (cutoffDate != null) {
                    apartmentRentRawRepository.findByCollectedAtGreaterThanEqual(cutoffDate, pageable).content
                } else {
                    apartmentRentRawRepository.findAllBy(pageable).content
                }
            },
            queryService = apartmentComplexQueryService,
            apartmentSequenceExtractor = { it.aptSeq },
            logPrefix = "RentRaw 전체 데이터 (Complex 생성용)",
            mode = mode,
        )
    }

    @Bean
    @StepScope
    fun unmatchedInfoRawComplexReader(apartmentComplexQueryService: ApartmentComplexQueryService): ItemReader<ApartmentComplexEntity> =
        UnmatchedComplexItemReader { limit, offset ->
            apartmentComplexQueryService.getUnmatchedInfoRawComplexes(limit, offset)
        }

    @Bean
    @StepScope
    fun unmatchedLicenseRawComplexReader(apartmentComplexQueryService: ApartmentComplexQueryService): ItemReader<ApartmentComplexEntity> =
        UnmatchedComplexItemReader { limit, offset ->
            apartmentComplexQueryService.getUnmatchedLicenseRawComplexes(limit, offset)
        }

    @Bean
    @StepScope
    fun tradeRawPagingReader(
        apartmentTradeRawRepository: ApartmentTradeRawRepository,
        @Value("#{jobParameters['mode']}") modeStr: String?,
    ): ItemReader<ApartmentTradeRawDocumentEntity> {
        val mode = MigrationMode.fromString(modeStr)
        val cutoffDate = mode.getCutoffDate()

        return MongoPageItemReader(
            countFetcher = {
                if (cutoffDate != null) {
                    apartmentTradeRawRepository.countByCollectedAtGreaterThanEqual(cutoffDate)
                } else {
                    apartmentTradeRawRepository.count()
                }
            },
            pageFetcher = { pageable ->
                if (cutoffDate != null) {
                    apartmentTradeRawRepository.findByCollectedAtGreaterThanEqual(cutoffDate, pageable).content
                } else {
                    apartmentTradeRawRepository.findAllBy(pageable).content
                }
            },
            logPrefix = "Trade 동기화 대상",
            mode = mode,
        )
    }

    @Bean
    @StepScope
    fun rentRawPagingReader(
        apartmentRentRawRepository: ApartmentRentRawRepository,
        @Value("#{jobParameters['mode']}") modeStr: String?,
    ): ItemReader<ApartmentRentRawDocumentEntity> {
        val mode = MigrationMode.fromString(modeStr)
        val cutoffDate = mode.getCutoffDate()

        return MongoPageItemReader(
            countFetcher = {
                if (cutoffDate != null) {
                    apartmentRentRawRepository.countByCollectedAtGreaterThanEqual(cutoffDate)
                } else {
                    apartmentRentRawRepository.count()
                }
            },
            pageFetcher = { pageable ->
                if (cutoffDate != null) {
                    apartmentRentRawRepository.findByCollectedAtGreaterThanEqual(cutoffDate, pageable).content
                } else {
                    apartmentRentRawRepository.findAllBy(pageable).content
                }
            },
            logPrefix = "Rent 동기화 대상",
            mode = mode,
        )
    }

    // ===== Steps =====
    @Bean
    fun createComplexStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        tradeRawDistinctAptSeqReader: ItemReader<ApartmentTradeRawDocumentEntity>,
        createComplexProcessor: ItemProcessor<ApartmentTradeRawDocumentEntity, ComplexWithApartmentSequence?>,
        complexWriter: ItemWriter<ComplexWithApartmentSequence?>,
    ): Step =
        StepBuilder("createComplexStep", jobRepository)
            .chunk<ApartmentTradeRawDocumentEntity, ComplexWithApartmentSequence?>(CHUNK_SIZE, transactionManager)
            .reader(tradeRawDistinctAptSeqReader)
            .processor(createComplexProcessor)
            .writer(complexWriter)
            .build()

    @Bean
    fun createComplexFromRentStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        rentRawDistinctAptSeqReader: ItemReader<ApartmentRentRawDocumentEntity>,
        createComplexFromRentProcessor: ItemProcessor<ApartmentRentRawDocumentEntity, ComplexWithApartmentSequence?>,
        complexWriter: ItemWriter<ComplexWithApartmentSequence?>,
    ): Step =
        StepBuilder("createComplexFromRentStep", jobRepository)
            .chunk<ApartmentRentRawDocumentEntity, ComplexWithApartmentSequence?>(CHUNK_SIZE, transactionManager)
            .reader(rentRawDistinctAptSeqReader)
            .processor(createComplexFromRentProcessor)
            .writer(complexWriter)
            .build()

    @Bean
    fun matchInfoRawStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        unmatchedInfoRawComplexReader: ItemReader<ApartmentComplexEntity>,
        matchInfoRawProcessor: ItemProcessor<ApartmentComplexEntity, Boolean>,
        matchResultWriter: ItemWriter<Boolean>,
    ): Step =
        StepBuilder("matchInfoRawStep", jobRepository)
            .chunk<ApartmentComplexEntity, Boolean>(CHUNK_SIZE, transactionManager)
            .reader(unmatchedInfoRawComplexReader)
            .processor(matchInfoRawProcessor)
            .writer(matchResultWriter)
            .build()

    @Bean
    fun matchLicenseRawStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        unmatchedLicenseRawComplexReader: ItemReader<ApartmentComplexEntity>,
        matchLicenseRawProcessor: ItemProcessor<ApartmentComplexEntity, Boolean>,
        matchResultWriter: ItemWriter<Boolean>,
    ): Step =
        StepBuilder("matchLicenseRawStep", jobRepository)
            .chunk<ApartmentComplexEntity, Boolean>(CHUNK_SIZE, transactionManager)
            .reader(unmatchedLicenseRawComplexReader)
            .processor(matchLicenseRawProcessor)
            .writer(matchResultWriter)
            .build()

    @Bean
    fun syncTradeStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        tradeRawPagingReader: ItemReader<ApartmentTradeRawDocumentEntity>,
        syncTradeProcessor: ItemProcessor<ApartmentTradeRawDocumentEntity, ApartmentTradeEntity?>,
        tradeWriter: ItemWriter<ApartmentTradeEntity?>,
    ): Step =
        StepBuilder("syncTradeStep", jobRepository)
            .chunk<ApartmentTradeRawDocumentEntity, ApartmentTradeEntity?>(CHUNK_SIZE, transactionManager)
            .reader(tradeRawPagingReader)
            .processor(syncTradeProcessor)
            .writer(tradeWriter)
            .build()

    @Bean
    fun syncRentStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
        rentRawPagingReader: ItemReader<ApartmentRentRawDocumentEntity>,
        syncRentProcessor: ItemProcessor<ApartmentRentRawDocumentEntity, ApartmentRentEntity?>,
        rentWriter: ItemWriter<ApartmentRentEntity?>,
    ): Step =
        StepBuilder("syncRentStep", jobRepository)
            .chunk<ApartmentRentRawDocumentEntity, ApartmentRentEntity?>(CHUNK_SIZE, transactionManager)
            .reader(rentRawPagingReader)
            .processor(syncRentProcessor)
            .writer(rentWriter)
            .build()

    companion object {
        private const val CHUNK_SIZE = 500
    }
}
