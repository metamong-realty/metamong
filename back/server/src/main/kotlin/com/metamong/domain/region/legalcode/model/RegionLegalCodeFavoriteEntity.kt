package com.metamong.domain.region.legalcode.model

import com.metamong.common.vo.LegalCode
import com.metamong.domain.base.BaseEntity
import jakarta.persistence.Entity
import jakarta.persistence.Table

@Table
@Entity(name = "region_legal_code_favorites")
class RegionLegalCodeFavoriteEntity(
    val userId: Long,
    val legalCode: LegalCode,
) : BaseEntity() {
    companion object {
        fun initialOf(
            legalCode: LegalCode,
            userId: Long,
        ) = RegionLegalCodeFavoriteEntity(
            userId = userId,
            legalCode = legalCode,
        )
    }
}
