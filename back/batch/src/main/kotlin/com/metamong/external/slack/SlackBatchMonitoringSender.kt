package com.metamong.external.slack

import com.metamong.config.WebClientFactory
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class SlackBatchMonitoringPayload(
    val jobName: String,
    val startTimeMillis: Long,
    val exception: Throwable? = null,
    val commandArgs: String,
)

@Component
class SlackBatchMonitoringSender(
    private val environment: Environment,
    private val webClientFactory: WebClientFactory,
) {
    fun sendWebhook(payload: SlackBatchMonitoringPayload) {
        if (isNotProduction()) {
            println("🔔 Slack 알림 (로컬 환경): ${payload.jobName} - ${if (payload.exception == null) "성공" else "실패"}")
            return
        }

        val startDate = getFormattedDateByNow()
        val duration =
            ((System.currentTimeMillis() - payload.startTimeMillis) / 1000.0)
                .let { String.format("%.1f", it) }

        val message: Map<String, Any>
        if (payload.exception == null) {
            message = buildSuccessMessage(payload.jobName, startDate, duration, payload.commandArgs)
        } else {
            message = buildErrorMessage(payload.jobName, startDate, duration, payload.exception, payload.commandArgs)
        }

        sendSlackMessage(
            "/services/YOUR_SLACK_WEBHOOK_PATH", // TODO: 실제 Slack Webhook URL 설정
            message,
        )
    }

    private fun sendSlackMessage(
        uri: String,
        payload: Map<String, Any>,
    ) {
        val client: WebClient = webClientFactory.slackClient()

        runCatching {
            val response =
                client
                    .post()
                    .uri(uri)
                    .body(
                        BodyInserters.fromValue(payload),
                    ).retrieve()
                    .bodyToMono(String::class.java)
                    .block()
            println("Slack message sent successfully: $response")
        }.onFailure { error ->
            println("Failed to send Slack message: ${error.message}")
        }
    }

    fun buildSuccessMessage(
        jobName: String,
        dateTime: String,
        duration: String,
        commandArgs: String? = null,
    ): Map<String, Any> =
        mapOf(
            "attachments" to
                listOf(
                    mapOf(
                        "color" to "#36a64f",
                        "blocks" to
                            listOf(
                                mapOf(
                                    "type" to "section",
                                    "text" to
                                        mapOf(
                                            "type" to "mrkdwn",
                                            "text" to "*Name : $jobName ✅*",
                                        ),
                                ),
                                mapOf(
                                    "type" to "section",
                                    "text" to
                                        mapOf(
                                            "type" to "mrkdwn",
                                            "text" to commandArgs,
                                        ),
                                ),
                                mapOf(
                                    "type" to "section",
                                    "text" to
                                        mapOf(
                                            "type" to "mrkdwn",
                                            "text" to "$dateTime | duration: ${duration}s",
                                        ),
                                ),
                            ),
                    ),
                ),
        )

    fun buildErrorMessage(
        jobName: String,
        dateTime: String,
        duration: String,
        throwable: Throwable?,
        commandArgs: String,
    ): Map<String, Any> =
        mapOf(
            "attachments" to
                listOf(
                    mapOf(
                        "color" to "#ff0000",
                        "blocks" to
                            listOf(
                                mapOf(
                                    "type" to "section",
                                    "text" to
                                        mapOf(
                                            "type" to "mrkdwn",
                                            "text" to "*Name : $jobName ❌*",
                                        ),
                                ),
                                mapOf(
                                    "type" to "section",
                                    "text" to
                                        mapOf(
                                            "type" to "mrkdwn",
                                            "text" to commandArgs,
                                        ),
                                ),
                                mapOf(
                                    "type" to "section",
                                    "text" to
                                        mapOf(
                                            "type" to "mrkdwn",
                                            "text" to "$dateTime | duration: ${duration}s",
                                        ),
                                ),
                                mapOf(
                                    "type" to "section",
                                    "text" to
                                        mapOf(
                                            "type" to "mrkdwn",
                                            "text" to "Exception: ${throwable?.message}",
                                        ),
                                ),
                            ),
                    ),
                ),
        )

    private fun getFormattedDateByNow(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return LocalDateTime.now().format(formatter)
    }

    private fun isNotProduction(): Boolean = !environment.activeProfiles.contains("production")
}
