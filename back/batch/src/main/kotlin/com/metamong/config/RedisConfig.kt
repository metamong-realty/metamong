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
    @Value("\${spring.data.redis.host:localhost}")
    private lateinit var host: String

    @Value("\${spring.data.redis.port:6379}")
    private var port: Int = 6379

    @Value("\${spring.data.redis.password:}")
    private var password: String? = null

    @Bean(destroyMethod = "shutdown")
    fun redissonClient(): RedissonClient {
        val config = Config()
        val address = "redis://$host:$port"

        config.useSingleServer().apply {
            setAddress(address)
            setConnectionMinimumIdleSize(2)
            setConnectionPoolSize(10)
            setConnectTimeout(10000)
            setTimeout(3000)
            setRetryAttempts(3)
            setRetryInterval(1500)

            // 비밀번호가 있는 경우에만 설정
            if (!password.isNullOrEmpty()) {
                setPassword(password)
            }
        }

        return Redisson.create(config)
    }

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory = RedissonConnectionFactory(redissonClient())

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = redisConnectionFactory()
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = StringRedisSerializer()
        return template
    }
}
