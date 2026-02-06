package com.metamong

import com.metamong.config.exception.BadRequestException
import com.metamong.config.exception.ErrorCodes
import com.metamong.external.slack.SlackBatchMonitoringPayload
import com.metamong.external.slack.SlackBatchMonitoringSender
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.JobParametersBuilder
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.batch.core.launch.JobLauncher
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext
import org.springframework.data.jpa.repository.config.EnableJpaAuditing
import org.springframework.data.mongodb.config.EnableMongoAuditing
import org.springframework.scheduling.annotation.EnableScheduling
import kotlin.system.exitProcess

@SpringBootApplication
@EnableBatchProcessing
@EnableScheduling
@EnableJpaAuditing
@EnableMongoAuditing
class MetamongBatchApplication(
    private val slackSender: SlackBatchMonitoringSender,
    private val jobLauncher: JobLauncher,
    private val jobs: Map<String, Job>,
    private val applicationContext: ConfigurableApplicationContext,
) : CommandLineRunner {
    @Value("\${spring.batch.job.name:NONE}")
    private val jobName: String? = null

    override fun run(vararg args: String?) {
        val currentTimeMillis = System.currentTimeMillis()
        val name = jobName
        if (name.isNullOrBlank()) {
            logger.error { "❌ Job name parameter is required" }
            slackSender.sendWebhook(
                SlackBatchMonitoringPayload(
                    jobName = "UNKNOWN",
                    startTimeMillis = currentTimeMillis,
                    exception = BadRequestException(ErrorCodes.PARAMETER_REQUIRED),
                    commandArgs =
                        args.filterNotNull().joinToString("\n") {
                            it.split("=").let { parts ->
                                "${parts[0]} = ${parts.getOrNull(1) ?: "(값 없음)"}"
                            }
                        },
                ),
            )
            return
        }
        val job = jobs[name]
        if (job == null) {
            logger.error { "❌ Job not found: $name" }
            slackSender.sendWebhook(
                SlackBatchMonitoringPayload(
                    jobName = name,
                    startTimeMillis = currentTimeMillis,
                    exception = BadRequestException(ErrorCodes.BATCH_JOB_NOT_FOUND),
                    commandArgs =
                        args.filterNotNull().joinToString("\n") {
                            it.split("=").let { parts ->
                                "${parts[0]} = ${parts.getOrNull(1) ?: "(값 없음)"}"
                            }
                        },
                ),
            )
            return
        }

        // 커맨드라인 인수에서 Job 파라미터 동적 추출 (key=value 형태)
        val jobParameters =
            JobParametersBuilder()
                .addString("name", name)
                .addLong("time", currentTimeMillis)
                .apply {
                    args
                        .filterNotNull()
                        .filter { it.contains("=") && !it.startsWith("--") }
                        .forEach { arg ->
                            val (key, value) = arg.split("=", limit = 2)
                            addString(key, value)
                        }
                }.toJobParameters()

        val commandArgs =
            args
                .filterNotNull()
                .joinToString("\n") { it.split("=").let { parts -> "${parts[0]} = ${parts.getOrNull(1) ?: "(값 없음)"}" } }

        // 잡 실행
        val jobExecution = jobLauncher.run(job, jobParameters)

        // Slack은 예외가 나도 종료 코드에는 영향 없게 runCatching
        runCatching {
            when (jobExecution.exitStatus.exitCode) {
                ExitStatus.COMPLETED.exitCode -> {
                    slackSender.sendWebhook(
                        SlackBatchMonitoringPayload(
                            jobName = job.name,
                            startTimeMillis = currentTimeMillis,
                            commandArgs = commandArgs,
                        ),
                    )
                    logger.info { "✅ 배치 작업 완료 - 애플리케이션 종료" }
                }
                else -> {
                    val ex = jobExecution.allFailureExceptions.firstOrNull()
                    slackSender.sendWebhook(
                        SlackBatchMonitoringPayload(
                            jobName = job.name,
                            startTimeMillis = currentTimeMillis,
                            exception = ex,
                            commandArgs = commandArgs,
                        ),
                    )
                    logger.error(ex) { "❌ 배치 실행 실패 - 애플리케이션 종료" }
                }
            }
        }.onFailure { ignore ->
            // 종료 중 부가 알림 실패는 exit code에 반영하지 않음
            logger.warn(ignore) { "종료 알림 전송 중 오류(무시)" }
        }

        // exit code는 오직 잡 결과로만 결정
        val exit = if (jobExecution.exitStatus == ExitStatus.COMPLETED) 0 else 1
        logger.info { "🔄 Spring 애플리케이션 종료 시작 (exit code: $exit)" }

        // 컨텍스트 종료 중 발생하는 경미한 예외는 삼켜서 프로세스 코드는 유지
        runCatching {
            val exitCode = SpringApplication.exit(applicationContext, { exit })
            exitProcess(exitCode)
        }.onFailure { t ->
            logger.warn(t) { "컨텍스트 종료 중 예외(무시) - 프로세스 종료 강제" }
            exitProcess(exit)
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}

fun main(args: Array<String>) {
    runApplication<MetamongBatchApplication>(*args)
}
