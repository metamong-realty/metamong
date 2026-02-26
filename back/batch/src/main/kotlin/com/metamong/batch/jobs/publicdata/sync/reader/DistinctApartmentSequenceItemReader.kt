package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.batch.jobs.publicdata.sync.MigrationMode
import com.metamong.service.apartment.ApartmentComplexQueryService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.ItemReader

/**
 * MongoDB Raw 데이터에서 아파트 단지(Complex)를 생성할 때 사용하는 Reader.
 *
 * 동일한 aptSeq를 가진 거래 데이터가 다수 존재하므로, DISTINCT 처리가 필요함.
 * - 이미 처리한 aptSeq는 스킵 (processedApartmentSequences)
 * - DB에 이미 Complex가 존재하면 스킵 (페이지 로드 시 배치 조회)
 *
 * 결과적으로 "새로운 Complex를 생성해야 하는" 첫 번째 Raw 데이터만 반환함.
 */
class DistinctApartmentSequenceItemReader<T>(
    private val countFetcher: () -> Long,
    private val cursorFetcher: (lastId: String?, pageSize: Int) -> List<T>,
    private val idExtractor: (T) -> String?,
    private val queryService: ApartmentComplexQueryService,
    private val apartmentSequenceExtractor: (T) -> String?,
    private val logPrefix: String,
    private val mode: MigrationMode,
) : ItemReader<T> {
    private var lastId: String? = null
    private val processedApartmentSequences = mutableSetOf<String>()
    private var initialized = false
    private var filteredItems: MutableList<T> = mutableListOf()
    private var filteredIndex = 0

    @Synchronized
    override fun read(): T? {
        if (!initialized) {
            val totalCount = countFetcher()
            logger.info { "$logPrefix: ${totalCount}건 (mode=$mode)" }
            initialized = true
        }

        while (true) {
            // filteredItems에 아직 반환하지 않은 아이템이 있으면 반환
            if (filteredIndex < filteredItems.size) {
                return filteredItems[filteredIndex++]
            }

            // 새 페이지 로드 및 배치 필터링
            val pageData = cursorFetcher(lastId, PAGE_SIZE)

            if (pageData.isEmpty()) {
                return null
            }

            // cursor 위치 갱신 (필터링 전 원본 페이지 기준)
            idExtractor(pageData.last())?.let { lastId = it }

            // 페이지 내 모든 고유 aptSeq 수집
            val pageAptSeqs =
                pageData
                    .mapNotNull { apartmentSequenceExtractor(it) }
                    .filter { !processedApartmentSequences.contains(it) }
                    .toSet()

            // 1회 배치 DB 조회로 이미 존재하는 aptSeq 확인
            val existingAptSeqs = queryService.findExistingApartmentSequences(pageAptSeqs)
            processedApartmentSequences.addAll(existingAptSeqs)

            // 필터링된 아이템 리스트 구성
            filteredItems = mutableListOf()
            filteredIndex = 0

            for (item in pageData) {
                val apartmentSequence = apartmentSequenceExtractor(item) ?: continue

                if (processedApartmentSequences.contains(apartmentSequence)) continue

                processedApartmentSequences.add(apartmentSequence)
                filteredItems.add(item)
            }

            // filteredItems에 아이템이 있으면 첫 번째 반환
            if (filteredItems.isNotEmpty()) {
                return filteredItems[filteredIndex++]
            }
            // 없으면 다음 페이지로
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
        const val PAGE_SIZE = 1000
    }
}
