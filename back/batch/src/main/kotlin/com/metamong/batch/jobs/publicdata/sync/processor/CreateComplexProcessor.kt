package com.metamong.batch.jobs.publicdata.sync.processor

import com.metamong.domain.apartment.model.ApartmentComplexEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.service.apartment.dto.ComplexWithApartmentSequence
import com.metamong.util.apartment.AddressParser
import com.metamong.util.apartment.ApartmentNameNormalizer
import org.springframework.batch.item.ItemProcessor
import org.springframework.stereotype.Component

@Component
class CreateComplexProcessor : ItemProcessor<ApartmentTradeRawDocumentEntity, ComplexWithApartmentSequence?> {
    override fun process(item: ApartmentTradeRawDocumentEntity): ComplexWithApartmentSequence? {
        val apartmentSequence = item.aptSeq ?: return null
        val apartmentName = item.aptNm ?: return null
        val sidoSigunguCode = item.lawdCd.toIntOrNull() ?: return null

        val jibunResult = AddressParser.parseJibun(item.jibun)
        val roadAddress =
            AddressParser.buildRoadAddress(
                item.roadNm,
                item.roadNmBonbun,
                item.roadNmBubun,
            )
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
                addressRoad = roadAddress,
                addressJibun = jibunAddress,
            )

        return ComplexWithApartmentSequence(complex, apartmentSequence)
    }
}
