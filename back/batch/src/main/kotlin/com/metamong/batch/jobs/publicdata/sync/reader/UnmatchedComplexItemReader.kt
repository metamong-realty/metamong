package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.domain.apartment.model.ApartmentComplexEntity
import org.springframework.batch.item.ItemReader

class UnmatchedComplexItemReader(
    private val fetcher: (Long, Long) -> List<ApartmentComplexEntity>,
) : ItemReader<ApartmentComplexEntity> {
    private var currentPageData: List<ApartmentComplexEntity> = emptyList()
    private var currentIndex = 0
    private var lastProcessedId: Long = 0

    override fun read(): ApartmentComplexEntity? {
        if (currentIndex >= currentPageData.size) {
            currentPageData = fetcher(PAGE_SIZE.toLong(), lastProcessedId)
            currentIndex = 0
            if (currentPageData.isEmpty()) return null
        }

        val item = currentPageData[currentIndex++]
        lastProcessedId = item.id
        return item
    }

    companion object {
        const val PAGE_SIZE = 1000
    }
}
