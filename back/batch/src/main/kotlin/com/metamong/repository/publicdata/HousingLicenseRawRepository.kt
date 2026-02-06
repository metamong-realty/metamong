package com.metamong.repository.publicdata

import com.metamong.model.document.publicdata.HousingLicenseRawDocumentEntity
import org.springframework.data.mongodb.repository.MongoRepository

interface HousingLicenseRawRepository : MongoRepository<HousingLicenseRawDocumentEntity, String> {
    fun findBySigunguCdAndBjdongCd(
        sigunguCd: String,
        bjdongCd: String,
    ): List<HousingLicenseRawDocumentEntity>
}
