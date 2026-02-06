package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.batch.jobs.publicdata.sync.MigrationMode
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.ItemReader
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

class MongoPageItemReader<T>(
    private val countFetcher: () -> Long,
    private val pageFetcher: (PageRequest) -> List<T>,
    private val logPrefix: String,
    private val mode: MigrationMode,
) : ItemReader<T> {
    private var currentPage = 0
    private var currentPageData: List<T> = emptyList()
    private var currentIndex = 0
    private var initialized = false

    override fun read(): T? {
        if (!initialized) {
            val totalCount = countFetcher()
            logger.info { "$logPrefix: ${totalCount}건 (mode=$mode)" }
            initialized = true
        }

        if (currentIndex >= currentPageData.size) {
            val pageable = PageRequest.of(currentPage, PAGE_SIZE, Sort.by("_id"))
            currentPageData = pageFetcher(pageable)
            currentIndex = 0
            currentPage++

            if (currentPageData.isEmpty()) {
                return null
            }
        }

        return currentPageData[currentIndex++]
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        const val PAGE_SIZE = 1000
    }
}
