package com.metamong.infra.persistance.repository.publicdata

import com.metamong.common.vo.LegalCode
import com.metamong.domain.region.QRegionLegalCodeEntity
import com.metamong.domain.region.RegionLegalCodeEntity
import com.metamong.support.QuerydslRepositorySupport

class RegionLegalCodeRepositoryCustomImpl :
    QuerydslRepositorySupport(RegionLegalCodeEntity::class.java),
    RegionLegalCodeRepositoryCustom {
    val regionLegalCode: QRegionLegalCodeEntity = QRegionLegalCodeEntity.regionLegalCodeEntity

    override fun findLegalCodesBySidoAndSigungu(
        sidoCode: LegalCode.SidoCode,
        sigunguCode: LegalCode.SigunguCode,
    ): List<LegalCode> =
        from(regionLegalCode)
            .select(regionLegalCode.legalCode)
            .where(
                regionLegalCode.sidoCode.eq(sidoCode.code),
                regionLegalCode.sigunguCode.eq(sigunguCode.code),
            ).fetch()
            .map { LegalCode(it) }
}
