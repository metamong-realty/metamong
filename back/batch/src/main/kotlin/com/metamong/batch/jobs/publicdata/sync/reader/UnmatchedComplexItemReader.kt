package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.domain.apartment.model.ApartmentComplexEntity
import org.springframework.batch.item.ItemReader

class UnmatchedComplexItemReader(
    private val fetcher: (Long, Long) -> List<ApartmentComplexEntity>,
) : ItemReader<ApartmentComplexEntity> {
    private var currentPage = 0
    private var currentPageData: List<ApartmentComplexEntity> = emptyList()
    private var currentIndex = 0

    override fun read(): ApartmentComplexEntity? {
        if (currentIndex >= currentPageData.size) {
            currentPageData = fetcher(PAGE_SIZE.toLong(), (currentPage * PAGE_SIZE).toLong())
            currentIndex = 0
            currentPage++

            if (currentPageData.isEmpty()) {
                return null
            }
        }

        return currentPageData[currentIndex++]
    }

    companion object {
        const val PAGE_SIZE = 1000
    }
}
