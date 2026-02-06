package com.metamong.batch.jobs.publicdata

import com.metamong.batch.jobs.publicdata.processor.ApartmentComplexInfoItemProcessor
import com.metamong.batch.jobs.publicdata.processor.ApartmentComplexListItemProcessor
import com.metamong.batch.jobs.publicdata.processor.ApartmentRentItemProcessor
import com.metamong.batch.jobs.publicdata.processor.ApartmentTradeItemProcessor
import com.metamong.batch.jobs.publicdata.processor.HistoricalApartmentRentItemProcessor
import com.metamong.batch.jobs.publicdata.processor.HistoricalApartmentTradeItemProcessor
import com.metamong.batch.jobs.publicdata.processor.HousingLicenseItemProcessor
import com.metamong.batch.jobs.publicdata.reader.RegionCodeItemReader
import com.metamong.batch.jobs.publicdata.reader.RegionCodeYearMonthItemReader
import com.metamong.batch.jobs.publicdata.writer.PublicDataMongoFastWriter
import com.metamong.batch.jobs.publicdata.writer.PublicDataMongoWriter
import com.metamong.external.publicdata.dto.RegionCode
import com.metamong.external.publicdata.dto.RegionCodeWithYearMonth
import com.metamong.infra.persistance.repository.mongo.publicdata.ApartmentComplexListRawRepository
import com.metamong.model.document.publicdata.ApartmentComplexInfoRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentComplexListRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.model.document.publicdata.HousingLicenseRawDocumentEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.core.configuration.support.DefaultBatchConfiguration
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.core.step.skip.SkipPolicy
import org.springframework.batch.item.ItemReader
import org.springframework.batch.support.transaction.ResourcelessTransactionManager
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.TaskExecutor
import org.springframework.data.mongodb.UncategorizedMongoDbException
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor

@Configuration
class PublicDataStepConfig : DefaultBatchConfiguration() {
    @Bean
    fun batchTaskExecutor(): TaskExecutor {
        val executor = ThreadPoolTaskExecutor()
        executor.corePoolSize = 3
        executor.maxPoolSize = 5 // API 서버 부하 고려하여 조정
        executor.queueCapacity = 25
        executor.setThreadNamePrefix("historical-batch-")
        executor.setWaitForTasksToCompleteOnShutdown(true)
        executor.setAwaitTerminationSeconds(30)
        executor.initialize()
        return executor
    }

    @Bean
    fun apartmentTradeStep(
        jobRepository: JobRepository,
        regionCodeItemReader: RegionCodeItemReader,
        apartmentTradeItemProcessor: ApartmentTradeItemProcessor,
        publicDataMongoWriter: PublicDataMongoWriter,
    ): Step =
        StepBuilder("apartmentTradeStep", jobRepository)
            .chunk<RegionCode, List<ApartmentTradeRawDocumentEntity>>(
                APARTMENT_TRADE_CHUNK_SIZE,
                ResourcelessTransactionManager(),
            ).reader(regionCodeItemReader.createReader())
            .processor(apartmentTradeItemProcessor)
            .writer(publicDataMongoWriter.apartmentTradeWriter())
            .faultTolerant()
            .skipPolicy(mongoSkipPolicy)
            .build()

    @Bean
    fun apartmentRentStep(
        jobRepository: JobRepository,
        regionCodeItemReader: RegionCodeItemReader,
        apartmentRentItemProcessor: ApartmentRentItemProcessor,
        publicDataMongoWriter: PublicDataMongoWriter,
    ): Step =
        StepBuilder("apartmentRentStep", jobRepository)
            .chunk<RegionCode, List<ApartmentRentRawDocumentEntity>>(
                APARTMENT_RENT_CHUNK_SIZE,
                ResourcelessTransactionManager(),
            ).reader(regionCodeItemReader.createReader())
            .processor(apartmentRentItemProcessor)
            .writer(publicDataMongoWriter.apartmentRentWriter())
            .faultTolerant()
            .skipPolicy(mongoSkipPolicy)
            .build()

    @Bean
    fun housingLicenseStep(
        jobRepository: JobRepository,
        regionCodeItemReader: RegionCodeItemReader,
        housingLicenseItemProcessor: HousingLicenseItemProcessor,
        publicDataMongoWriter: PublicDataMongoWriter,
    ): Step =
        StepBuilder("housingLicenseStep", jobRepository)
            .chunk<RegionCode, List<HousingLicenseRawDocumentEntity>>(
                HOUSING_LICENSE_CHUNK_SIZE,
                ResourcelessTransactionManager(),
            ).reader(regionCodeItemReader.createReader())
            .processor(housingLicenseItemProcessor)
            .writer(publicDataMongoWriter.housingLicenseWriter())
            .faultTolerant()
            .skipPolicy(mongoSkipPolicy)
            .build()

    @Bean
    fun apartmentComplexListStep(
        jobRepository: JobRepository,
        regionCodeItemReader: RegionCodeItemReader,
        apartmentComplexListItemProcessor: ApartmentComplexListItemProcessor,
        publicDataMongoWriter: PublicDataMongoWriter,
    ): Step =
        StepBuilder("apartmentComplexListStep", jobRepository)
            .chunk<RegionCode, List<ApartmentComplexListRawDocumentEntity>>(
                APARTMENT_COMPLEX_LIST_CHUNK_SIZE,
                ResourcelessTransactionManager(),
            ).reader(regionCodeItemReader.createReader())
            .processor(apartmentComplexListItemProcessor)
            .writer(publicDataMongoWriter.apartmentComplexListWriter())
            .faultTolerant()
            .skipPolicy(mongoSkipPolicy)
            .build()

    @Bean
    @StepScope
    fun kaptCodeItemReader(apartmentComplexListRawRepository: ApartmentComplexListRawRepository): ItemReader<String> {
        var kaptCodes: Iterator<String>? = null
        return ItemReader {
            if (kaptCodes == null) {
                val codes =
                    apartmentComplexListRawRepository
                        .findAllKaptCodesOnly()
                logger.info { "공동주택 기본 정보 조회 대상: ${codes.size}개 단지" }
                kaptCodes = codes.iterator()
            }

            if (kaptCodes!!.hasNext()) {
                kaptCodes!!.next()
            } else {
                null
            }
        }
    }

    @Bean
    fun apartmentComplexInfoStep(
        jobRepository: JobRepository,
        kaptCodeItemReader: ItemReader<String>,
        apartmentComplexInfoItemProcessor: ApartmentComplexInfoItemProcessor,
        publicDataMongoWriter: PublicDataMongoWriter,
    ): Step =
        StepBuilder("apartmentComplexInfoStep", jobRepository)
            .chunk<String, ApartmentComplexInfoRawDocumentEntity?>(
                APARTMENT_COMPLEX_INFO_CHUNK_SIZE,
                ResourcelessTransactionManager(),
            ).reader(kaptCodeItemReader)
            .processor(apartmentComplexInfoItemProcessor)
            .writer(publicDataMongoWriter.apartmentComplexInfoWriter())
            .faultTolerant()
            .skipPolicy(mongoSkipPolicy)
            .build()

    // ========== Historical Job용 Step (startYearMonth ~ endYearMonth 범위 처리) ==========

    @Bean
    fun historicalApartmentTradeStep(
        jobRepository: JobRepository,
        regionCodeYearMonthItemReader: RegionCodeYearMonthItemReader,
        historicalApartmentTradeItemProcessor: HistoricalApartmentTradeItemProcessor,
        publicDataMongoFastWriter: PublicDataMongoFastWriter,
        batchTaskExecutor: TaskExecutor,
    ): Step =
        StepBuilder("historicalApartmentTradeStep", jobRepository)
            .chunk<RegionCodeWithYearMonth, List<ApartmentTradeRawDocumentEntity>>(
                HISTORICAL_APARTMENT_TRADE_CHUNK_SIZE,
                ResourcelessTransactionManager(),
            ).reader(regionCodeYearMonthItemReader)
            .processor(historicalApartmentTradeItemProcessor)
            .writer(publicDataMongoFastWriter.apartmentTradeWriter())
            .taskExecutor(batchTaskExecutor)
            .faultTolerant()
            .skipPolicy(mongoSkipPolicy)
            .build()

    @Bean
    fun historicalApartmentRentStep(
        jobRepository: JobRepository,
        regionCodeYearMonthItemReader: RegionCodeYearMonthItemReader,
        historicalApartmentRentItemProcessor: HistoricalApartmentRentItemProcessor,
        publicDataMongoFastWriter: PublicDataMongoFastWriter,
        batchTaskExecutor: TaskExecutor,
    ): Step =
        StepBuilder("historicalApartmentRentStep", jobRepository)
            .chunk<RegionCodeWithYearMonth, List<ApartmentRentRawDocumentEntity>>(
                HISTORICAL_APARTMENT_RENT_CHUNK_SIZE,
                ResourcelessTransactionManager(),
            ).reader(regionCodeYearMonthItemReader)
            .processor(historicalApartmentRentItemProcessor)
            .writer(publicDataMongoFastWriter.apartmentRentWriter())
            .taskExecutor(batchTaskExecutor)
            .faultTolerant()
            .skipPolicy(mongoSkipPolicy)
            .build()

    companion object {
        private val logger = KotlinLogging.logger {}

        // Step별 Chunk Size
        private const val DEFAULT_CHUNK_SIZE = 5
        private const val APARTMENT_TRADE_CHUNK_SIZE = DEFAULT_CHUNK_SIZE
        private const val APARTMENT_RENT_CHUNK_SIZE = 3
        private const val HOUSING_LICENSE_CHUNK_SIZE = DEFAULT_CHUNK_SIZE
        private const val APARTMENT_COMPLEX_LIST_CHUNK_SIZE = DEFAULT_CHUNK_SIZE
        private const val APARTMENT_COMPLEX_INFO_CHUNK_SIZE = DEFAULT_CHUNK_SIZE

        // Historical 배치용 최적화된 Chunk Size (중복 체크 없이 처리하므로 더 큰 배치 가능)
        private const val HISTORICAL_APARTMENT_TRADE_CHUNK_SIZE = 10
        private const val HISTORICAL_APARTMENT_RENT_CHUNK_SIZE = 8

        /**
         * MongoDB 에러 발생 시 skip하고 다음 chunk로 진행하는 SkipPolicy
         * - 11601 (operation was interrupted): Transaction 타임아웃
         * - 112 (Write Conflict): 동시 쓰기 충돌
         */
        val mongoSkipPolicy =
            SkipPolicy { throwable, _ ->
                when (throwable) {
                    is UncategorizedMongoDbException -> {
                        logger.error(throwable) { "MongoDB 에러 발생, skip 처리: ${throwable.message}" }
                        true
                    }

                    else -> false
                }
            }
    }
}
