package com.metamong.batch.jobs.publicdata.sync.listener

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.StepExecution
import org.springframework.batch.core.StepExecutionListener
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class ApartmentMigrationStepListener : StepExecutionListener {
    
    override fun beforeStep(stepExecution: StepExecution) {
        val stepName = stepExecution.stepName
        logger.info { "[$stepName] Step 시작" }
    }

    override fun afterStep(stepExecution: StepExecution): ExitStatus? {
        val stepName = stepExecution.stepName
        val duration = if (stepExecution.endTime != null && stepExecution.startTime != null) {
            Duration.between(stepExecution.startTime, stepExecution.endTime).toMillis()
        } else {
            0L
        }
        val readCount = stepExecution.readCount
        val writeCount = stepExecution.writeCount
        val skipCount = stepExecution.skipCount
        val rollbackCount = stepExecution.rollbackCount
        val commitCount = stepExecution.commitCount
        val exitStatus = stepExecution.exitStatus

        logger.info {
            "[$stepName] Step 완료: " +
            "실행시간=${duration}ms, " +
            "읽기=${readCount}건, " +
            "쓰기=${writeCount}건, " +
            "스킵=${skipCount}건, " +
            "롤백=${rollbackCount}건, " +
            "커밋=${commitCount}건, " +
            "상태=${exitStatus.exitCode}"
        }

        // 처리량 계산 (처리건수/초)
        if (duration > 0) {
            val throughput = (writeCount * 1000.0) / duration
            logger.info { "[$stepName] 처리량: ${String.format("%.2f", throughput)} 건/초" }
        }

        // 경고 조건 체크
        if (skipCount > writeCount * 0.1) { // 스킵이 10% 이상
            logger.warn { "[$stepName] 스킵 건수가 높습니다: $skipCount (전체의 ${String.format("%.1f", (skipCount.toDouble() / (writeCount + skipCount)) * 100)}%)" }
        }

        if (rollbackCount > commitCount * 0.05) { // 롤백이 5% 이상
            logger.warn { "[$stepName] 롤백 건수가 높습니다: $rollbackCount (커밋의 ${String.format("%.1f", (rollbackCount.toDouble() / commitCount) * 100)}%)" }
        }

        return null // 기본 ExitStatus 유지
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}