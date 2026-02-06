package com.metamong.domain.region.legalcode.model

import com.metamong.common.vo.LegalCode
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Table
@Entity(name = "region_legal_codes")
class RegionLegalCodeEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val legalCode: LegalCode,
    val regionName: String,
    val sidoCode: LegalCode.SidoCode,
    val sidoName: String,
    val sidoAlias: String?,
    val sigunguCode: LegalCode.SigunguCode?,
    val sigunguName: String?,
    val eupmyeondongCode: LegalCode.EupmyeondongCode?,
    val eupmyeondongName: String?,
    val riCode: LegalCode.RiCode?,
    val riName: String?,
)
