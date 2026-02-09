package com.metamong.batch.jobs.publicdata.sync.reader

import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.service.apartment.ApartmentComplexQueryService
import org.springframework.batch.item.ItemReader
import org.springframework.stereotype.Component

@Component
class UnmatchedLicenseRawComplexReader(
    private val apartmentComplexQueryService: ApartmentComplexQueryService,
) : ItemReader<ApartmentComplexEntity> {
    private val delegate: UnmatchedComplexItemReader =
        UnmatchedComplexItemReader { limit, offset ->
            apartmentComplexQueryService.getUnmatchedLicenseRawComplexes(limit, offset)
        }

    override fun read(): ApartmentComplexEntity? = delegate.read()
}
