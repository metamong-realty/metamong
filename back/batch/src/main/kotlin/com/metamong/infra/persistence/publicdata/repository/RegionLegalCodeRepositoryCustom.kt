package com.metamong.infra.persistence.publicdata.repository

import com.metamong.common.vo.LegalCode

interface RegionLegalCodeRepositoryCustom {
    fun findLegalCodesBySidoAndSigungu(
        sidoCode: LegalCode.SidoCode,
        sigunguCode: LegalCode.SigunguCode,
    ): List<LegalCode>
}
