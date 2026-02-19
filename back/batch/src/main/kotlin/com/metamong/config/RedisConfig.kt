package com.metamong.config

import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.metamong.common.cache.CacheType
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import org.redisson.spring.data.connection.RedissonConnectionFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.cache.CacheManager
import org.springframework.cache.annotation.EnableCaching
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.cache.RedisCacheConfiguration
import org.springframework.data.redis.cache.RedisCacheManager
import org.springframework.data.redis.connection.RedisConnectionFactory
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer
import org.springframework.data.redis.serializer.RedisSerializationContext
import org.springframework.data.redis.serializer.StringRedisSerializer
import java.time.Duration

@Configuration
@EnableCaching
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

            // URL에 비밀번호가 포함된 경우 설정 (redis://:password@host:port)
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
    fun redisTemplate(): RedisTemplate<String, Any> {
        val template = RedisTemplate<String, Any>()
        template.connectionFactory = redisConnectionFactory()
        template.keySerializer = StringRedisSerializer()
        template.valueSerializer = StringRedisSerializer()
        template.hashKeySerializer = StringRedisSerializer()
        template.hashValueSerializer = StringRedisSerializer()
        return template
    }

    @Bean
    fun cacheManager(redisConnectionFactory: RedisConnectionFactory): CacheManager {
        val objectMapper =
            ObjectMapper().apply {
                registerModule(KotlinModule.Builder().build())
                registerModule(JavaTimeModule())

                activateDefaultTyping(
                    BasicPolymorphicTypeValidator
                        .builder()
                        .allowIfSubType("com.metamong.")
                        .allowIfSubType("java.")
                        .allowIfSubType("kotlin.")
                        .build(),
                    ObjectMapper.DefaultTyping.EVERYTHING,
                    JsonTypeInfo.As.PROPERTY,
                )
            }

        val jsonSerializer = GenericJackson2JsonRedisSerializer(objectMapper)

        val defaultConfig =
            RedisCacheConfiguration
                .defaultCacheConfig()
                .entryTtl(DEFAULT_TTL)
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(jsonSerializer))

        val cacheConfigurations =
            CacheType.entries().associate { cacheType ->
                cacheType.cacheName to defaultConfig.entryTtl(cacheType.ttl)
            }

        return RedisCacheManager
            .builder(redisConnectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .build()
    }

    companion object {
        private val DEFAULT_TTL = Duration.ofHours(24)
    }
}
