package com.metamong.config

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.data.mongodb.MongoDatabaseFactory
import org.springframework.data.mongodb.MongoTransactionManager
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.SimpleMongoClientDatabaseFactory
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories
import org.springframework.transaction.annotation.EnableTransactionManagement

@Configuration
@EnableTransactionManagement
@Profile("!test")
@EnableMongoRepositories(basePackages = ["com.metamong.infra.persistence.repository"])
class DocumentDatabaseConfig(
    @Value("\${spring.datasource.document.primary.uri}") private val mongoUri: String,
    @Value("\${spring.datasource.document.database}") private val databaseName: String,
) {
    @Bean
    fun mongoClient(): MongoClient = MongoClients.create(mongoUri)

    @Bean
    fun mongoDatabaseFactory(mongoClient: MongoClient): MongoDatabaseFactory = SimpleMongoClientDatabaseFactory(mongoClient, databaseName)

    @Bean
    fun mongoTemplate(mongoDatabaseFactory: MongoDatabaseFactory): MongoTemplate = MongoTemplate(mongoDatabaseFactory)

    @Bean
    fun documentTransactionManager(mongoDatabaseFactory: MongoDatabaseFactory): MongoTransactionManager =
        MongoTransactionManager(mongoDatabaseFactory)
}
