package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.batch.jobs.publicdata.sync.MigrationMode
import com.metamong.infra.persistence.repository.mongo.publicdata.ApartmentRentRawRepository
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class RentRawPagingReader(
    private val apartmentRentRawRepository: ApartmentRentRawRepository,
    @Value("\${job.migration.mode:FULL}") private val defaultModeStr: String,
) : ItemReader<ApartmentRentRawDocumentEntity> {
    private var delegate: MongoPageItemReader<ApartmentRentRawDocumentEntity>? = null

    fun initialize(modeStr: String?) {
        val mode = MigrationMode.fromString(modeStr ?: defaultModeStr)
        val cutoffDate = mode.getCutoffDate()

        delegate =
            MongoPageItemReader(
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
                logPrefix = "Rent 동기화 대상",
                mode = mode,
            )
    }

    override fun read(): ApartmentRentRawDocumentEntity? = delegate?.read()
}
