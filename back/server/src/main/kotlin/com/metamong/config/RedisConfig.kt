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
        val uri = java.net.URI(redisUrl)

        config.useSingleServer().apply {
            setAddress("redis://${uri.host}:${uri.port}")
            setConnectionMinimumIdleSize(2)
            setConnectionPoolSize(10)
            setConnectTimeout(10000)
            setTimeout(3000)
            setRetryAttempts(3)
            setRetryInterval(1500)

            uri.userInfo?.let { userInfo ->
                val password = if (userInfo.contains(":")) userInfo.substringAfter(":") else userInfo
                if (password.isNotEmpty()) {
                    setPassword(password)
                }
            }
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
