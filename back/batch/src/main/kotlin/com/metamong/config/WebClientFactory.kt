package com.metamong.config

import io.netty.channel.ChannelOption
import io.netty.handler.timeout.ReadTimeoutHandler
import io.netty.handler.timeout.WriteTimeoutHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.client.reactive.ReactorClientHttpConnector
import org.springframework.web.reactive.function.client.WebClient
import reactor.netty.http.client.HttpClient
import java.time.Duration
import java.util.concurrent.TimeUnit

@Configuration
class WebClientFactory {
    @Bean
    fun defaultWebClient(): WebClient =
        WebClient
            .builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(ReactorClientHttpConnector(httpClient()))
            .build()

    @Bean
    fun slackClient(): WebClient =
        WebClient
            .builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(ReactorClientHttpConnector(httpClient()))
            .baseUrl(SLACK_WEBHOOK_BASE_URL)
            .build()

    // Memory Buffer 초과 방지를 위해 2MB로 증가 (deafult 256kb), 포트원 대사 500개 쿼리시 최대 0.9MB
    @Bean
    fun portoneClient(): WebClient =
        WebClient
            .builder()
            .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient
                        .create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                        .responseTimeout(Duration.ofMillis(30000))
                        .doOnConnected { conn ->
                            conn
                                .addHandlerLast(ReadTimeoutHandler(30000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(WriteTimeoutHandler(30000, TimeUnit.MILLISECONDS))
                        },
                ),
            ).codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(2 * 1024 * 1024) }
            .baseUrl(PORTONE_BASE_URL)
            .build()

    private fun httpClient(): HttpClient =
        HttpClient
            .create()
            .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 3000)
            .responseTimeout(Duration.ofMillis(3000))
            .doOnConnected { conn ->
                conn
                    .addHandlerLast(ReadTimeoutHandler(3000, TimeUnit.MILLISECONDS))
                    .addHandlerLast(WriteTimeoutHandler(3000, TimeUnit.MILLISECONDS))
            }

    @Bean
    fun publicDataClient(): WebClient =
        WebClient
            .builder()
            .clientConnector(
                ReactorClientHttpConnector(
                    HttpClient
                        .create()
                        .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 30000)
                        .responseTimeout(Duration.ofMillis(60000))
                        .doOnConnected { conn ->
                            conn
                                .addHandlerLast(ReadTimeoutHandler(60000, TimeUnit.MILLISECONDS))
                                .addHandlerLast(WriteTimeoutHandler(30000, TimeUnit.MILLISECONDS))
                        },
                ),
            ).codecs { configurer -> configurer.defaultCodecs().maxInMemorySize(10 * 1024 * 1024) }
            .baseUrl(PUBLIC_DATA_BASE_URL)
            .build()

    companion object {
        private const val SLACK_WEBHOOK_BASE_URL = "https://hooks.slack.com"
        private const val PORTONE_BASE_URL = "https://api.portone.io"
        private const val SLACK_BASE_URL = "https://slack.com"
        private const val PUBLIC_DATA_BASE_URL = "https://apis.data.go.kr"
    }
}
