package com.metamong.batch.jobs.subscription.reader

import com.metamong.domain.apartment.model.ApartmentTradeEntity
import com.metamong.domain.subscription.model.TradeType
import com.metamong.infra.persistence.apartment.repository.ApartmentTradeRepository
import com.metamong.infra.persistence.subscription.repository.SubscriptionMatchingCheckpointRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import org.springframework.batch.item.ItemReader
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Component

@Component
class NewTradeItemReader(
    private val apartmentTradeRepository: ApartmentTradeRepository,
    private val checkpointRepository: SubscriptionMatchingCheckpointRepository,
) : ItemReader<ApartmentTradeEntity> {
    private var currentPage = 0
    private var currentItems: MutableList<ApartmentTradeEntity> = mutableListOf()
    private var lastProcessedTradeId: Long = 0
    private var initialized = false

    @PostConstruct
    fun initialize() {
        val checkpoint = checkpointRepository.findByTradeType(TradeType.TRADE)
        lastProcessedTradeId = checkpoint?.lastProcessedTradeId ?: 0
        logger.info { "구독 매칭 시작 - 마지막 처리 Trade ID: $lastProcessedTradeId" }
    }

    override fun read(): ApartmentTradeEntity? {
        if (!initialized) {
            initialized = true
            fetchNextPage()
        }

        if (currentItems.isEmpty()) {
            return null
        }

        val item = currentItems.removeFirst()

        if (currentItems.isEmpty()) {
            fetchNextPage()
        }

        return item
    }

    private fun fetchNextPage() {
        val pageable = PageRequest.of(currentPage, PAGE_SIZE, Sort.by("id").ascending())
        val page = apartmentTradeRepository.findByIdGreaterThan(lastProcessedTradeId, pageable)
        currentItems = page.content.toMutableList()
        if (page.hasNext()) {
            currentPage++
        }
    }

    companion object {
        // 메모리 사용량과 DB 부하 균형: 100건 기준 예상 메모리 ~10MB, 운영 환경에서 모니터링 후 조정
        private const val PAGE_SIZE = 100
        private val logger = KotlinLogging.logger {}
    }
}
