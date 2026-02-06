package com.metamong.batch.jobs.publicdata

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 부동산 공공데이터 일별 수집 Job 설정
 *
 * 매일 스케줄링되어 실행되며, 2개 Step을 순차적으로 실행합니다.
 * - Step 1: 아파트 매매 실거래가 (apartmentTradeStep)
 * - Step 2: 아파트 전월세 실거래가 (apartmentRentStep)
 *
 * 월 1회 실행되는 Step은 PublicDataMonthlyJobConfig로 분리됨:
 * - 주택인허가정보, 공동주택 단지 목록/기본정보
 */
@Configuration
class PublicDataDailyJobConfig {
    @Bean
    fun publicDataDailyCollectionJob(
        jobRepository: JobRepository,
        apartmentTradeStep: Step,
        apartmentRentStep: Step,
    ): Job =
        JobBuilder(JOB_NAME, jobRepository)
            .start(apartmentTradeStep)
            .next(apartmentRentStep)
            .build()

    companion object {
        const val JOB_NAME = "publicDataDailyCollectionJob"
    }
}
