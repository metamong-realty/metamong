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
import com.metamong.infra.persistence.repository.mongo.publicdata.ApartmentComplexListRawRepository
import com.metamong.model.document.publicdata.ApartmentComplexInfoRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentComplexListRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.model.document.publicdata.HousingLicenseRawDocumentEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.Step
import org.springframework.batch.core.configuration.annotation.StepScope
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
class PublicDataStepConfig {
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
            .retryLimit(3) // MongoDB 연결 에러에 대한 재시도
            .retry(org.springframework.dao.DataAccessResourceFailureException::class.java)
            .retry(org.springframework.data.mongodb.UncategorizedMongoDbException::class.java)
            .retry(java.net.SocketException::class.java)
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
            .retryLimit(3) // MongoDB 연결 에러에 대한 재시도
            .retry(org.springframework.dao.DataAccessResourceFailureException::class.java)
            .retry(org.springframework.data.mongodb.UncategorizedMongoDbException::class.java)
            .retry(java.net.SocketException::class.java)
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

        // Historical 배치용 최적화된 Chunk Size (MongoDB 안정성을 위해 축소)
        private const val HISTORICAL_APARTMENT_TRADE_CHUNK_SIZE = 5
        private const val HISTORICAL_APARTMENT_RENT_CHUNK_SIZE = 4

        /**
         * MongoDB 에러 발생 시 skip하고 다음 chunk로 진행하는 확장된 SkipPolicy
         * - Connection reset: 네트워크 연결 문제
         * - Socket timeout: 소켓 타임아웃
         * - DataAccessResourceFailureException: MongoDB 리소스 문제
         * - ExhaustedRetryException: 재시도 소진
         * - MongoSocketReadException: MongoDB 소켓 읽기 에러
         * - UncategorizedMongoDbException: 기타 MongoDB 예외
         */
        val mongoSkipPolicy =
            SkipPolicy { throwable, _ ->
                val shouldSkip =
                    when (throwable) {
                        // MongoDB 연결 관련 예외들
                        is org.springframework.dao.DataAccessResourceFailureException -> {
                            val message = throwable.message ?: ""
                            val isMongoError =
                                message.contains("Connection reset") ||
                                    message.contains("Exception receiving message") ||
                                    message.contains("MongoSocketReadException") ||
                                    message.contains("Socket") ||
                                    message.contains("connection")

                            if (isMongoError) {
                                logger.warn(throwable) { "MongoDB 연결 에러 발생, skip 처리: $message" }
                                true
                            } else {
                                false
                            }
                        }

                        // 재시도 소진 예외
                        is org.springframework.retry.ExhaustedRetryException -> {
                            logger.warn(throwable) { "재시도 소진, skip 처리하여 배치 계속 진행: ${throwable.message}" }
                            true
                        }

                        // 기존 MongoDB 예외
                        is org.springframework.data.mongodb.UncategorizedMongoDbException -> {
                            logger.warn(throwable) { "MongoDB 에러 발생, skip 처리: ${throwable.message}" }
                            true
                        }

                        // Java 네트워크 예외
                        is java.net.SocketException -> {
                            logger.warn(throwable) { "네트워크 소켓 에러 발생, skip 처리: ${throwable.message}" }
                            true
                        }

                        // Java IO 예외 (Connection reset 포함)
                        is java.io.IOException -> {
                            val message = throwable.message ?: ""
                            if (message.contains("Connection reset") || message.contains("Broken pipe")) {
                                logger.warn(throwable) { "네트워크 연결 에러 발생, skip 처리: $message" }
                                true
                            } else {
                                false
                            }
                        }

                        else -> false
                    }

                if (shouldSkip) {
                    logger.info { "청크 건너뛰기 적용됨 - 배치 작업 계속 진행" }
                }

                shouldSkip
            }
    }
}
