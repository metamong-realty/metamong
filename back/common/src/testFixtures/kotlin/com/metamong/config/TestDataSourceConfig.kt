package com.metamong.config

import jakarta.persistence.EntityManagerFactory
import org.h2.jdbcx.JdbcDataSource
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.jdbc.datasource.init.DataSourceInitializer
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator
import org.springframework.orm.jpa.JpaTransactionManager
import javax.sql.DataSource

@TestConfiguration
class TestDataSourceConfig(
    @Value("\${spring.datasource.url}")
    private val url: String,
    @Value("\${spring.datasource.username}")
    private val username: String,
    @Value("\${spring.datasource.password}")
    private val password: String,
) {
    @Bean
    fun dataSource(): DataSource =
        DataSourceBuilder
            .create()
            .type(JdbcDataSource::class.java)
            .url(url)
            .username(username)
            .password(password)
            .build()

    @Bean
    fun dataSourceInitializer(dataSource: DataSource): DataSourceInitializer {
        val resourceDatabasePopulator = ResourceDatabasePopulator()
        val dataSourceInitializer = DataSourceInitializer()
        dataSourceInitializer.setDataSource(dataSource)
        dataSourceInitializer.setDatabasePopulator(resourceDatabasePopulator)
        return dataSourceInitializer
    }

    @Bean
    fun transactionManager(entityManagerFactory: EntityManagerFactory): JpaTransactionManager = JpaTransactionManager(entityManagerFactory)
}
