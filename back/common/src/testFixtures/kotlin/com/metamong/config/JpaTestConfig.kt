package com.metamong.config

import org.springframework.boot.autoconfigure.EnableAutoConfiguration
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@Configuration
@EntityScan("com.metamong.*")
@EnableJpaRepositories(basePackages = ["com.metamong.infra.persistence"])
@Import(value = [TestDataSourceConfig::class, DataCleaner::class])
@EnableAutoConfiguration
class JpaTestConfig
