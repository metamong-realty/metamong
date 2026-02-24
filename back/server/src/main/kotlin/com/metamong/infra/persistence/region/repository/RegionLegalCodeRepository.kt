package com.metamong.infra.persistence.region.repository

import com.metamong.common.vo.LegalCode
import com.metamong.domain.region.RegionLegalCodeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RegionLegalCodeRepository : JpaRepository<RegionLegalCodeEntity, Long> {
    fun existsByLegalCode(legalCode: LegalCode): Boolean
}
