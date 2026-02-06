package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.batch.jobs.publicdata.sync.MigrationMode
import com.metamong.service.apartment.ApartmentComplexQueryService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.ItemReader
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

/**
 * MongoDB Raw 데이터에서 아파트 단지(Complex)를 생성할 때 사용하는 Reader.
 *
 * 동일한 aptSeq를 가진 거래 데이터가 다수 존재하므로, DISTINCT 처리가 필요함.
 * - 이미 처리한 aptSeq는 스킵 (processedApartmentSequences)
 * - DB에 이미 Complex가 존재하면 스킵 (queryService 조회)
 *
 * 결과적으로 "새로운 Complex를 생성해야 하는" 첫 번째 Raw 데이터만 반환함.
 */
class DistinctApartmentSequenceItemReader<T>(
    private val countFetcher: () -> Long,
    private val pageFetcher: (PageRequest) -> List<T>,
    private val queryService: ApartmentComplexQueryService,
    private val apartmentSequenceExtractor: (T) -> String?,
    private val logPrefix: String,
    private val mode: MigrationMode,
) : ItemReader<T> {
    private var currentPage = 0
    private var currentPageData: List<T> = emptyList()
    private var currentIndex = 0
    private val processedApartmentSequences = mutableSetOf<String>()
    private var initialized = false

    override fun read(): T? {
        if (!initialized) {
            val totalCount = countFetcher()
            logger.info { "$logPrefix: ${totalCount}건 (mode=$mode)" }
            initialized = true
        }

        while (true) {
            if (currentIndex >= currentPageData.size) {
                val pageable = PageRequest.of(currentPage, PAGE_SIZE, Sort.by("_id"))
                currentPageData = pageFetcher(pageable)
                currentIndex = 0
                currentPage++

                if (currentPageData.isEmpty()) {
                    return null
                }
            }

            val item = currentPageData[currentIndex++]
            val apartmentSequence = apartmentSequenceExtractor(item) ?: continue

            // 이번 배치 실행 중 이미 처리한 aptSeq는 스킵
            if (processedApartmentSequences.contains(apartmentSequence)) continue

            // DB에 이미 Complex가 존재하면 스킵
            if (queryService.getComplexIdByApartmentSequence(apartmentSequence) != null) {
                processedApartmentSequences.add(apartmentSequence)
                continue
            }

            processedApartmentSequences.add(apartmentSequence)
            return item
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        const val PAGE_SIZE = 1000
    }
}
