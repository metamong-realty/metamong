package com.metamong.batch.jobs.publicdata

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 부동산 공공데이터 과거 데이터 수집 Job 설정
 *
 * 과거 데이터를 수동으로 수집할 때 사용합니다.
 * startYearMonth, endYearMonth 파라미터로 수집 기간을 지정합니다.
 *
 * 포함된 Step (날짜 기반 실거래가 데이터만):
 * - Step 1: 아파트 매매 실거래가
 * - Step 2: 아파트 전월세 실거래가
 *
 * 제외된 Step:
 * - 주택인허가: yearMonth 파라미터를 사용하지 않음
 * - 공동주택 단지 목록/기본정보: Daily Job에서만 실행
 *
 * 실행 예시:
 * ```
 * # 2024년 1월 ~ 12월 데이터 수집
 * java -jar batch.jar --spring.batch.job.name=publicDataHistoricalCollectionJob \
 *   startYearMonth=202401 endYearMonth=202412
 *
 * # 2023년 전체 데이터 수집
 * java -jar batch.jar --spring.batch.job.name=publicDataHistoricalCollectionJob \
 *   startYearMonth=202301 endYearMonth=202312
 * ```
 */
@Configuration
class PublicDataHistoricalJobConfig {
    @Bean
    fun publicDataHistoricalCollectionJob(
        jobRepository: JobRepository,
        @Qualifier("historicalApartmentTradeStep") historicalApartmentTradeStep: Step,
        @Qualifier("historicalApartmentRentStep") historicalApartmentRentStep: Step,
    ): Job =
        JobBuilder(JOB_NAME, jobRepository)
            .start(historicalApartmentTradeStep)
            .next(historicalApartmentRentStep)
            .build()

    companion object {
        const val JOB_NAME = "publicDataHistoricalCollectionJob"
    }
}
