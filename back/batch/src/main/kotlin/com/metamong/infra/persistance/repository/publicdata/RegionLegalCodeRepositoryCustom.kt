package com.metamong.infra.persistance.repository.publicdata

import com.metamong.common.vo.LegalCode

interface RegionLegalCodeRepositoryCustom {
    fun findLegalCodesBySidoAndSigungu(
        sidoCode: LegalCode.SidoCode,
        sigunguCode: LegalCode.SigunguCode,
    ): List<LegalCode>
}
