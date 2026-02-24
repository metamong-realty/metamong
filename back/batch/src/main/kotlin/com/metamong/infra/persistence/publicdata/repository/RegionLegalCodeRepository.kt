package com.metamong.infra.persistence.publicdata.repository

import com.metamong.common.vo.LegalCode
import com.metamong.domain.region.RegionLegalCodeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RegionLegalCodeRepository :
    JpaRepository<RegionLegalCodeEntity, Long>,
    RegionLegalCodeRepositoryCustom {
    fun existsByLegalCode(legalCode: LegalCode): Boolean

    fun findByLegalCode(legalCode: LegalCode): RegionLegalCodeEntity?

    // @Query 대신 method naming 사용
    // 기존: @Query("SELECT r.legalCode FROM RegionLegalCodeEntity r WHERE r.sidoCode = :sidoCode AND r.sigunguCode = :sigunguCode")
    fun findBySidoCodeAndSigunguCode(
        sidoCode: LegalCode.SidoCode,
        sigunguCode: LegalCode.SigunguCode,
    ): List<RegionLegalCodeEntity>
}
