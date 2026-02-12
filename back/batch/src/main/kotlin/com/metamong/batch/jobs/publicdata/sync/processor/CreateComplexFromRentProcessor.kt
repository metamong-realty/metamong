package com.metamong.batch.jobs.publicdata.sync.processor

import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.service.apartment.dto.ComplexWithApartmentSequence
import com.metamong.util.apartment.AddressParser
import com.metamong.util.apartment.ApartmentNameNormalizer
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class CreateComplexFromRentProcessor : ItemProcessor<ApartmentRentRawDocumentEntity, ComplexWithApartmentSequence?> {
    override fun process(item: ApartmentRentRawDocumentEntity): ComplexWithApartmentSequence? {
        val apartmentSequence = item.aptSeq ?: return null
        val apartmentName = item.aptNm ?: return null
        val sidoSigunguCode = item.lawdCd.toIntOrNull() ?: return null

        val jibunResult = AddressParser.parseJibun(item.jibun)
        val jibunAddress = AddressParser.buildJibunAddress(item.umdNm, item.jibun)
        val builtYear = item.buildYear?.toShortOrNull()

        val complex =
            ApartmentComplexEntity.create(
                sidoSigunguCode = sidoSigunguCode,
                nameRaw = apartmentName,
                nameNormalized = ApartmentNameNormalizer.normalize(apartmentName),
                builtYear = builtYear,
                bonNo = jibunResult?.bonNo,
                buNo = jibunResult?.buNo,
                addressRoad = item.roadnm,
                addressJibun = jibunAddress,
            )

        return ComplexWithApartmentSequence(complex, apartmentSequence)
    }
}
