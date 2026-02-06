package com.metamong.repository.region

import com.metamong.common.vo.LegalCode
import com.metamong.domain.region.legalcode.model.RegionLegalCodeEntity
import org.springframework.data.jpa.repository.JpaRepository

interface RegionLegalCodeRepository : JpaRepository<RegionLegalCodeEntity, Long> {
    fun existsByLegalCode(legalCode: LegalCode): Boolean

    fun findByLegalCode(legalCode: LegalCode): RegionLegalCodeEntity?
}
