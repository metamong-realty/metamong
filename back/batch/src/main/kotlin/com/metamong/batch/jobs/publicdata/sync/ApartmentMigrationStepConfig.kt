package com.metamong.batch.jobs.publicdata.sync

import com.metamong.batch.jobs.publicdata.sync.reader.RentRawDistinctAptSeqReader
import com.metamong.batch.jobs.publicdata.sync.reader.RentRawPagingReader
import com.metamong.batch.jobs.publicdata.sync.reader.TradeRawDistinctAptSeqReader
import com.metamong.batch.jobs.publicdata.sync.reader.TradeRawPagingReader
import com.metamong.batch.jobs.publicdata.sync.reader.UnmatchedInfoRawComplexReader
import com.metamong.batch.jobs.publicdata.sync.reader.UnmatchedLicenseRawComplexReader
import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.domain.apartment.model.ApartmentRentEntity
import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
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
class ApartmentMigrationStepConfig {
    // ===== Readers =====
    @Bean
    @StepScope
    fun tradeRawDistinctAptSeqReader(
        tradeRawDistinctAptSeqReader: TradeRawDistinctAptSeqReader,
        @Value("#{jobParameters['mode']}") modeStr: String?,
    ): ItemReader<ApartmentTradeRawDocumentEntity> {
        tradeRawDistinctAptSeqReader.initialize(modeStr)
        return tradeRawDistinctAptSeqReader
    }

    @Bean
    @StepScope
    fun rentRawDistinctAptSeqReader(
        rentRawDistinctAptSeqReader: RentRawDistinctAptSeqReader,
        @Value("#{jobParameters['mode']}") modeStr: String?,
    ): ItemReader<ApartmentRentRawDocumentEntity> {
        rentRawDistinctAptSeqReader.initialize(modeStr)
        return rentRawDistinctAptSeqReader
    }

    @Bean
    @StepScope
    fun unmatchedInfoRawComplexReader(unmatchedInfoRawComplexReader: UnmatchedInfoRawComplexReader): ItemReader<ApartmentComplexEntity> =
        unmatchedInfoRawComplexReader

    @Bean
    @StepScope
    fun unmatchedLicenseRawComplexReader(
        unmatchedLicenseRawComplexReader: UnmatchedLicenseRawComplexReader,
    ): ItemReader<ApartmentComplexEntity> = unmatchedLicenseRawComplexReader

    @Bean
    @StepScope
    fun tradeRawPagingReader(
        tradeRawPagingReader: TradeRawPagingReader,
        @Value("#{jobParameters['mode']}") modeStr: String?,
    ): ItemReader<ApartmentTradeRawDocumentEntity> {
        tradeRawPagingReader.initialize(modeStr)
        return tradeRawPagingReader
    }

    @Bean
    @StepScope
    fun rentRawPagingReader(
        rentRawPagingReader: RentRawPagingReader,
        @Value("#{jobParameters['mode']}") modeStr: String?,
    ): ItemReader<ApartmentRentRawDocumentEntity> {
        rentRawPagingReader.initialize(modeStr)
        return rentRawPagingReader
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
