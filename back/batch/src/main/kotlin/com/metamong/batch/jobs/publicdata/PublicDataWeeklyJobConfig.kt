package com.metamong.batch.jobs.publicdata

import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.repository.JobRepository
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

/**
 * 부동산 공공데이터 주별 수집 Job 설정
 *
 * 매주 1회 스케줄링되어 실행되며, 3개 Step을 순차적으로 실행합니다.
 * - Step 1: 주택인허가정보 (housingLicenseStep)
 * - Step 2: 공동주택 단지 목록 (apartmentComplexListStep)
 * - Step 3: 공동주택 기본 정보 (apartmentComplexInfoStep)
 *
 * 참고: apartmentComplexInfoStep은 apartmentComplexListStep의 결과(kaptCode)를 사용하므로
 * 반드시 순서대로 실행되어야 합니다.
 */
@Configuration
class PublicDataWeeklyJobConfig {
    @Bean
    fun publicDataWeeklyCollectionJob(
        jobRepository: JobRepository,
        housingLicenseStep: Step,
        apartmentComplexListStep: Step,
        apartmentComplexInfoStep: Step,
    ): Job =
        JobBuilder(JOB_NAME, jobRepository)
            .start(housingLicenseStep)
            .next(apartmentComplexListStep)
            .next(apartmentComplexInfoStep)
            .build()

    companion object {
        const val JOB_NAME = "publicDataWeeklyCollectionJob"
    }
}
