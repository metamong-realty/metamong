package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.batch.jobs.publicdata.sync.DealYearMonthRange
import com.metamong.batch.jobs.publicdata.sync.MigrationMode
import com.metamong.infra.persistence.mongo.publicdata.repository.ApartmentRentRawRepository
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import jakarta.annotation.PostConstruct
import org.springframework.batch.core.configuration.annotation.StepScope
import org.springframework.batch.item.ItemReader
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
@StepScope
class RentRawPagingReader(
    private val apartmentRentRawRepository: ApartmentRentRawRepository,
    @Value("#{jobParameters['name']}") private val jobName: String?,
    @Value("#{jobParameters['startYearMonth']}") private val startYearMonth: String?,
    @Value("#{jobParameters['endYearMonth']}") private val endYearMonth: String?,
) : ItemReader<ApartmentRentRawDocumentEntity> {
    private var delegate: MongoPageItemReader<ApartmentRentRawDocumentEntity>? = null

    @PostConstruct
    fun initialize() {
        val mode = MigrationMode.fromJobName(jobName)
        val cutoffDate = mode.getCutoffDate()
        val yearMonthRange = DealYearMonthRange.of(startYearMonth, endYearMonth)

        delegate =
            MongoPageItemReader(
                countFetcher = {
                    when {
                        yearMonthRange != null -> apartmentRentRawRepository.countByDealYearMonthRange(yearMonthRange.buildCriteria())
                        cutoffDate != null -> apartmentRentRawRepository.countByCollectedAtGreaterThanEqual(cutoffDate)
                        else -> apartmentRentRawRepository.count()
                    }
                },
                pageFetcher = { pageable ->
                    when {
                        yearMonthRange != null ->
                            apartmentRentRawRepository.findByDealYearMonthRange(
                                yearMonthRange.buildCriteria(),
                                pageable,
                            )
                        cutoffDate != null -> apartmentRentRawRepository.findByCollectedAtGreaterThanEqual(cutoffDate, pageable).content
                        else -> apartmentRentRawRepository.findAllBy(pageable).content
                    }
                },
                logPrefix = "Rent 동기화 대상",
                mode = mode,
            )
    }

    override fun read(): ApartmentRentRawDocumentEntity? = delegate?.read()
}
