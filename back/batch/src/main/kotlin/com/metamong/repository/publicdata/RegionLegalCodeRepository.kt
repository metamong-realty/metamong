package com.metamong.repository.publicData

import com.metamong.common.vo.LegalCode
import com.metamong.domain.region.legalcode.model.RegionLegalCodeEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface RegionLegalCodeRepository : JpaRepository<RegionLegalCodeEntity, Long> {
    fun existsByLegalCode(legalCode: LegalCode): Boolean

    fun findByLegalCode(legalCode: LegalCode): RegionLegalCodeEntity?

    @Query("SELECT r.legalCode FROM RegionLegalCodeEntity r WHERE r.sidoCode = :sidoCode AND r.sigunguCode = :sigunguCode")
    fun findLegalCodesBySidoAndSigungu(
        sidoCode: LegalCode.SidoCode,
        sigunguCode: LegalCode.SigunguCode,
    ): List<LegalCode>
}
