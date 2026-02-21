package com.metamong.config

import jakarta.annotation.PostConstruct
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
@Profile("test")
class DataCleaner(
    private val environment: Environment,
) {
    companion object {
        private const val FOREIGN_KEY_CHECK_FORMAT = "SET FOREIGN_KEY_CHECKS %d"
        private const val TRUNCATE_FORMAT = "TRUNCATE TABLE %s"
    }

    private val tableNames = mutableListOf<String>()

    @PersistenceContext
    private lateinit var entityManager: EntityManager

    private var isH2Database: Boolean = false

    @PostConstruct
    fun findDatabaseTableNames() {
        val dbUrl = environment.getProperty("spring.datasource.url")
        isH2Database = dbUrl?.contains("h2", ignoreCase = true) ?: false

        if (isH2Database) {
            val tableInfos = entityManager.createNativeQuery("SHOW TABLES").resultList as List<Array<Any>>
            for (tableInfo in tableInfos) {
                val tableName = tableInfo[0] as String
                tableNames.add(tableName)
            }
        }
    }

    @Transactional
    fun clear() {
        if (isH2Database) {
            entityManager.clear()
            truncate()
        }
    }

    private fun truncate() {
        entityManager.createNativeQuery(String.format(FOREIGN_KEY_CHECK_FORMAT, 0)).executeUpdate()
        for (tableName in tableNames) {
            entityManager.createNativeQuery(String.format(TRUNCATE_FORMAT, tableName)).executeUpdate()
        }
        entityManager.createNativeQuery(String.format(FOREIGN_KEY_CHECK_FORMAT, 1)).executeUpdate()
    }
}
