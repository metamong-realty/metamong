package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.batch.jobs.publicdata.sync.DealYearMonthRange
import com.metamong.batch.jobs.publicdata.sync.MigrationMode
import com.metamong.infra.persistence.mongo.publicdata.repository.ApartmentTradeRawRepository
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import jakarta.annotation.PostConstruct
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
@StepScope
class TradeRawPagingReader(
    private val apartmentTradeRawRepository: ApartmentTradeRawRepository,
    @Value("#{jobParameters['name']}") private val jobName: String?,
    @Value("#{jobParameters['startYearMonth']}") private val startYearMonth: String?,
    @Value("#{jobParameters['endYearMonth']}") private val endYearMonth: String?,
) : ItemReader<ApartmentTradeRawDocumentEntity> {
    private var delegate: MongoPageItemReader<ApartmentTradeRawDocumentEntity>? = null

    @PostConstruct
    fun initialize() {
        val mode = MigrationMode.fromJobName(jobName)
        val cutoffDate = mode.getCutoffDate()
        val yearMonthRange = DealYearMonthRange.of(startYearMonth, endYearMonth)

        delegate =
            MongoPageItemReader(
                countFetcher = {
                    when {
                        yearMonthRange != null -> apartmentTradeRawRepository.countByDealYearMonthRange(yearMonthRange.buildCriteria())
                        cutoffDate != null -> apartmentTradeRawRepository.countByCollectedAtGreaterThanEqual(cutoffDate)
                        else -> apartmentTradeRawRepository.count()
                    }
                },
                cursorFetcher = { lastId, pageSize ->
                    when {
                        yearMonthRange != null ->
                            apartmentTradeRawRepository.findByCursorAndDealYearMonthRange(
                                lastId,
                                yearMonthRange.buildCriteria(),
                                pageSize,
                            )
                        cutoffDate != null -> apartmentTradeRawRepository.findByCursorAndCollectedAtGte(lastId, cutoffDate, pageSize)
                        else -> apartmentTradeRawRepository.findAllByCursor(lastId, pageSize)
                    }
                },
                idExtractor = { it.id },
                logPrefix = "Trade 동기화 대상",
                mode = mode,
            )
    }

    override fun read(): ApartmentTradeRawDocumentEntity? = delegate?.read()
}
