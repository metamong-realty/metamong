package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.batch.jobs.publicdata.sync.MigrationMode
import com.metamong.infra.persistence.repository.mongo.publicdata.ApartmentTradeRawRepository
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TradeRawPagingReader(
    private val apartmentTradeRawRepository: ApartmentTradeRawRepository,
    @Value("\${job.migration.mode:FULL}") private val defaultModeStr: String,
) : ItemReader<ApartmentTradeRawDocumentEntity> {
    private var delegate: MongoPageItemReader<ApartmentTradeRawDocumentEntity>? = null

    fun initialize(modeStr: String?) {
        val mode = MigrationMode.fromString(modeStr ?: defaultModeStr)
        val cutoffDate = mode.getCutoffDate()

        delegate =
            MongoPageItemReader(
                countFetcher = {
                    if (cutoffDate != null) {
                        apartmentTradeRawRepository.countByCollectedAtGreaterThanEqual(cutoffDate)
                    } else {
                        apartmentTradeRawRepository.count()
                    }
                },
                pageFetcher = { pageable ->
                    if (cutoffDate != null) {
                        apartmentTradeRawRepository.findByCollectedAtGreaterThanEqual(cutoffDate, pageable).content
                    } else {
                        apartmentTradeRawRepository.findAllBy(pageable).content
                    }
                },
                logPrefix = "Trade 동기화 대상",
                mode = mode,
            )
    }

    override fun read(): ApartmentTradeRawDocumentEntity? = delegate?.read()
}
