package com.metamong.config

import com.querydsl.jpa.impl.JPAQueryFactory
import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.DependsOn
import org.springframework.context.annotation.FilterType
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

const val MASTER_HIKARI_CONFIG_PREFIX = "spring.datasource.aurora.master.hikari"
const val SLAVE_HIKARI_CONFIG_PREFIX = "spring.datasource.aurora.slave.hikari"

const val MASTER_DATASOURCE = "masterDataSource"
const val SLAVE_DATASOURCE = "slaveDataSource"

@Configuration
@Profile("!test")
@EnableJpaRepositories(
    basePackages = ["com.metamong.infra.persistence.repository"],
    excludeFilters = [
        ComponentScan.Filter(
            type = FilterType.REGEX,
            pattern = [
                "com\\.metamong\\.infra\\.persistence\\.repository\\.mongo\\..*",
            ],
        ),
    ],
    entityManagerFactoryRef = "entityManagerFactory",
    transactionManagerRef = "transactionManager",
)
class DatabaseConfig {
    @Bean
    @ConfigurationProperties(prefix = MASTER_HIKARI_CONFIG_PREFIX)
    fun masterDataSource(): DataSource = DataSourceBuilder.create().type(HikariDataSource::class.java).build()

    @Bean
    @ConfigurationProperties(prefix = SLAVE_HIKARI_CONFIG_PREFIX)
    fun slaveDataSource(): DataSource = DataSourceBuilder.create().type(HikariDataSource::class.java).build()

    @Bean
    @DependsOn(MASTER_DATASOURCE, SLAVE_DATASOURCE)
    fun routingDataSource(
        @Qualifier(MASTER_DATASOURCE) masterDataSource: DataSource,
        @Qualifier(SLAVE_DATASOURCE) slaveDataSource: DataSource,
    ): DataSource {
        val routingDataSource = RoutingDataSource()
        val dataSourceMap =
            hashMapOf<Any, Any>(
                "master" to masterDataSource,
                "slave" to slaveDataSource,
            )
        routingDataSource.setTargetDataSources(dataSourceMap)
        routingDataSource.setDefaultTargetDataSource(masterDataSource)
        return routingDataSource
    }

    @Bean
    @Primary
    @DependsOn("routingDataSource")
    fun dataSource(routingDataSource: DataSource): LazyConnectionDataSourceProxy = LazyConnectionDataSourceProxy(routingDataSource)

    @Bean
    @Primary
    fun entityManagerFactory(
        entityManagerFactoryBuilder: EntityManagerFactoryBuilder,
        @Qualifier("dataSource") dataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean =
        entityManagerFactoryBuilder
            .dataSource(dataSource)
            .packages(
                "com.metamong.domain",
                "com.metamong.batch.domain",
            ).persistenceUnit("metamong")
            .build()

    @Bean
    @Primary
    fun transactionManager(
        @Qualifier("entityManagerFactory") entityManagerFactory: EntityManagerFactory,
    ): PlatformTransactionManager = JpaTransactionManager(entityManagerFactory)

    @Bean
    @Primary
    fun entityManager(
        @Qualifier("entityManagerFactory") entityManagerFactory: EntityManagerFactory,
    ): EntityManager = entityManagerFactory.createEntityManager()

    @Bean
    fun jpaQueryFactory(
        @Qualifier("entityManager") entityManager: EntityManager,
    ): JPAQueryFactory = JPAQueryFactory(entityManager)

    @Bean
    fun jdbcTemplate(
        @Qualifier(MASTER_DATASOURCE) masterDataSource: DataSource,
    ): JdbcTemplate = JdbcTemplate(masterDataSource)
}
