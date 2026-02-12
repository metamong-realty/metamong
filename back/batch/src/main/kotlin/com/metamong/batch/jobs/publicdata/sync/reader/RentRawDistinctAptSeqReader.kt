package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.batch.jobs.publicdata.sync.MigrationMode
import com.metamong.infra.persistence.repository.mongo.publicdata.ApartmentRentRawRepository
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.service.apartment.ApartmentComplexQueryService
import jakarta.annotation.PostConstruct
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
@StepScope
class RentRawDistinctAptSeqReader(
    private val apartmentRentRawRepository: ApartmentRentRawRepository,
    private val apartmentComplexQueryService: ApartmentComplexQueryService,
    @Value("#{jobParameters['mode']}") private val modeStr: String?,
    @Value("\${job.migration.mode:FULL}") private val defaultModeStr: String,
) : ItemReader<ApartmentRentRawDocumentEntity> {
    private var delegate: DistinctApartmentSequenceItemReader<ApartmentRentRawDocumentEntity>? = null

    @PostConstruct
    fun initialize() {
        val mode = MigrationMode.fromString(modeStr ?: defaultModeStr)
        val cutoffDate = mode.getCutoffDate()

        delegate =
            DistinctApartmentSequenceItemReader(
                countFetcher = {
                    if (cutoffDate != null) {
                        apartmentRentRawRepository.countByCollectedAtGreaterThanEqual(cutoffDate)
                    } else {
                        apartmentRentRawRepository.count()
                    }
                },
                pageFetcher = { pageable ->
                    if (cutoffDate != null) {
                        apartmentRentRawRepository.findByCollectedAtGreaterThanEqual(cutoffDate, pageable).content
                    } else {
                        apartmentRentRawRepository.findAllBy(pageable).content
                    }
                },
                queryService = apartmentComplexQueryService,
                apartmentSequenceExtractor = { it.aptSeq },
                logPrefix = "RentRaw 전체 데이터 (Complex 생성용)",
                mode = mode,
            )
    }

    override fun read(): ApartmentRentRawDocumentEntity? = delegate?.read()
}
