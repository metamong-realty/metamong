package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.batch.jobs.publicdata.sync.MigrationMode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.ItemReader

class MongoPageItemReader<T>(
    private val countFetcher: () -> Long,
    private val cursorFetcher: (lastId: String?, pageSize: Int) -> List<T>,
    private val idExtractor: (T) -> String?,
    private val logPrefix: String,
    private val mode: MigrationMode,
) : ItemReader<T> {
    private var lastId: String? = null
    private var currentPageData: List<T> = emptyList()
    private var currentIndex = 0
    private var initialized = false

    @Synchronized
    override fun read(): T? {
        if (!initialized) {
            val totalCount = countFetcher()
            logger.info { "$logPrefix: ${totalCount}건 (mode=$mode)" }
            initialized = true
        }

        if (currentIndex >= currentPageData.size) {
            currentPageData = cursorFetcher(lastId, PAGE_SIZE)
            currentIndex = 0

            if (currentPageData.isEmpty()) {
                return null
            }
        }

        val item = currentPageData[currentIndex++]
        lastId = idExtractor(item) ?: lastId
        return item
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        const val PAGE_SIZE = 1000
    }
}
