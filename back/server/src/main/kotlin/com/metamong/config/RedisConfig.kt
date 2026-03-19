package com.metamong.config

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.StringRedisSerializer

@Configuration
@Profile("!test")
class RedisConfig {
    @Value("\${spring.data.redis.url:redis://localhost:6379}")
    private lateinit var redisUrl: String

    @Bean(destroyMethod = "shutdown")
    fun redissonClient(): RedissonClient {
        val config = Config()

        // redis://[user:password@]host:port 또는 rediss://... 형식 파싱
        // java.net.URI는 특수문자 비밀번호에서 실패하므로 정규식으로 직접 파싱
        val regex = Regex("^(rediss?)://(?:[^:@]+:([^@]*)@)?([^:]+):(\\d+)$")
        val match =
            regex.matchEntire(redisUrl)
                ?: throw IllegalArgumentException("Invalid Redis URL format: $redisUrl")

        val scheme = match.groupValues[1] // redis 또는 rediss
        val password = match.groupValues[2].ifEmpty { null }
        val host = match.groupValues[3]
        val port = match.groupValues[4]

        config.useSingleServer().apply {
            setAddress("$scheme://$host:$port")
            password?.let { setPassword(it) }
            setConnectionMinimumIdleSize(2)
            setConnectionPoolSize(10)
            setConnectTimeout(10000)
            setTimeout(3000)
            setRetryAttempts(3)
            setRetryInterval(1500)
        }

        return Redisson.create(config)
    }

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory = RedissonConnectionFactory(redissonClient())

    @Bean
    fun redisTemplate(): RedisTemplate<String, String> {
        val template = RedisTemplate<String, String>()
        template.connectionFactory = redisConnectionFactory()
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = StringRedisSerializer()
        return template
    }
}
