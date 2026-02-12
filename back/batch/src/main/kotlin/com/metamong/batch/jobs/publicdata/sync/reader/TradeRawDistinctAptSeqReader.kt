package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.batch.jobs.publicdata.sync.MigrationMode
import com.metamong.infra.persistence.repository.mongo.publicdata.ApartmentTradeRawRepository
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.service.apartment.ApartmentComplexQueryService
import jakarta.annotation.PostConstruct
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
@StepScope
class TradeRawDistinctAptSeqReader(
    private val apartmentTradeRawRepository: ApartmentTradeRawRepository,
    private val apartmentComplexQueryService: ApartmentComplexQueryService,
    @Value("#{jobParameters['mode']}") private val modeStr: String?,
    @Value("\${job.migration.mode:FULL}") private val defaultModeStr: String,
) : ItemReader<ApartmentTradeRawDocumentEntity> {
    private var delegate: DistinctApartmentSequenceItemReader<ApartmentTradeRawDocumentEntity>? = null

    @PostConstruct
    fun initialize() {
        val mode = MigrationMode.fromString(modeStr ?: defaultModeStr)
        val cutoffDate = mode.getCutoffDate()

        delegate =
            DistinctApartmentSequenceItemReader(
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
                queryService = apartmentComplexQueryService,
                apartmentSequenceExtractor = { it.aptSeq },
                logPrefix = "TradeRaw 전체 데이터",
                mode = mode,
            )
    }

    override fun read(): ApartmentTradeRawDocumentEntity? = delegate?.read()
}
