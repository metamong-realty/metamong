package com.metamong.application.region.service

import com.metamong.application.region.response.EupmyeondongResponse
import com.metamong.application.region.response.SidoResponse
import com.metamong.application.region.response.SigunguResponse
import com.metamong.infra.persistence.region.repository.RegionLegalCodeRepository
import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service

@Service
class RegionQueryService(
    private val regionLegalCodeRepository: RegionLegalCodeRepository,
) {
    private lateinit var sidoList: List<SidoResponse>
    private lateinit var sigunguMap: Map<String, List<SigunguResponse>>
    private lateinit var eupmyeondongMap: Map<String, List<EupmyeondongResponse>>

    @PostConstruct
    fun init() {
        val entities = regionLegalCodeRepository.findAll()

        sidoList =
            entities
                .map { SidoResponse(code = it.sidoCode.code, name = it.sidoName) }
                .distinctBy { it.code }
                .sortedBy { it.code }

        sigunguMap =
            entities
                .mapNotNull { entity ->
                    val code = entity.sigunguCode ?: return@mapNotNull null
                    val name = entity.sigunguName ?: return@mapNotNull null
                    entity.sidoCode.code to SigunguResponse(code = code.code, name = name)
                }.distinctBy { (sidoCode, sigungu) -> "$sidoCode${sigungu.code}" }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, v) -> v.sortedBy { it.code } }

        eupmyeondongMap =
            entities
                .mapNotNull { entity ->
                    val sigunguCode = entity.sigunguCode ?: return@mapNotNull null
                    val eupmyeondongCode = entity.eupmyeondongCode ?: return@mapNotNull null
                    val eupmyeondongName = entity.eupmyeondongName ?: return@mapNotNull null
                    val key = "${entity.sidoCode.code}${sigunguCode.code}"
                    key to EupmyeondongResponse(code = eupmyeondongCode.code, name = eupmyeondongName)
                }.distinctBy { (key, emd) -> "$key${emd.code}" }
                .groupBy({ it.first }, { it.second })
                .mapValues { (_, v) -> v.sortedBy { it.code } }
    }

    fun getSidoList(): List<SidoResponse> = sidoList

    fun getSigunguList(sidoCode: String): List<SigunguResponse> = sigunguMap[sidoCode].orEmpty()

    fun getEupmyeondongList(
        sidoCode: String,
        sigunguCode: String,
    ): List<EupmyeondongResponse> = eupmyeondongMap["$sidoCode$sigunguCode"].orEmpty()
}
