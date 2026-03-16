package com.metamong.application.region.service

import com.metamong.application.region.response.EupmyeondongResponse
import com.metamong.application.region.response.RegionAllResponse
import com.metamong.application.region.response.SidoResponse
import com.metamong.application.region.response.SigunguResponse
import com.metamong.common.util.toFullSidoSigunguCode
import com.metamong.infra.persistence.apartment.repository.ApartmentComplexRepository
import com.metamong.infra.persistence.region.repository.RegionLegalCodeRepository
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class RegionQueryService(
    private val regionLegalCodeRepository: RegionLegalCodeRepository,
    private val apartmentComplexRepository: ApartmentComplexRepository,
) {
    @Cacheable("region:sido")
    fun getSidoList(): List<SidoResponse> {
        // 실제로 단지가 존재하는 시도 코드만 조회
        val existingSidoCodes = apartmentComplexRepository.findDistinctSidoCodes()

        return regionLegalCodeRepository
            .findAll()
            .filter { entity ->
                val sidoCodeInt = entity.sidoCode.code.toIntOrNull() ?: return@filter false
                existingSidoCodes.contains(sidoCodeInt)
            }.map { SidoResponse(code = it.sidoCode.code, name = it.sidoName) }
            .distinctBy { it.code }
            .sortedBy { it.code }
    }

    @Cacheable("region:sigungu", key = "#sidoCode")
    fun getSigunguList(sidoCode: String): List<SigunguResponse> {
        // 실제로 단지가 존재하는 시도시군구 코드만 조회
        val existingSidoSigunguCodes = apartmentComplexRepository.findDistinctSidoSigunguCodes()

        val sidoCodeInt = sidoCode.toIntOrNull() ?: return emptyList()

        return regionLegalCodeRepository
            .findAll()
            .filter { entity ->
                val entitySidoCode = entity.sidoCode.code.toIntOrNull() ?: return@filter false
                if (entitySidoCode != sidoCodeInt) return@filter false

                val sigunguCode = entity.sigunguCode?.code?.toIntOrNull() ?: return@filter false
                val fullCode = toFullSidoSigunguCode(entitySidoCode, sigunguCode)
                existingSidoSigunguCodes.contains(fullCode)
            }.mapNotNull { entity ->
                val code = entity.sigunguCode?.code ?: return@mapNotNull null
                val name = entity.sigunguName ?: return@mapNotNull null
                SigunguResponse(code = code, name = name)
            }.distinctBy { it.code }
            .sortedBy { it.code }
    }

    @Cacheable("region:eupmyeondong", key = "#sidoCode + '_' + #sigunguCode")
    fun getEupmyeondongList(
        sidoCode: String,
        sigunguCode: String,
    ): List<EupmyeondongResponse> {
        val sidoCodeInt = sidoCode.toIntOrNull() ?: return emptyList()
        val sigunguCodeInt = sigunguCode.toIntOrNull() ?: return emptyList()
        val sidoSigunguCode = toFullSidoSigunguCode(sidoCodeInt, sigunguCodeInt)

        // 실제로 단지가 존재하는 읍면동 코드만 조회
        val existingEupmyeondongCodes = apartmentComplexRepository.findDistinctEupmyeondongCodes(sidoSigunguCode)

        return regionLegalCodeRepository
            .findAll()
            .filter { entity ->
                val entitySidoCode = entity.sidoCode.code.toIntOrNull() ?: return@filter false
                val entitySigunguCode = entity.sigunguCode?.code?.toIntOrNull() ?: return@filter false
                val eupmyeondongCode = entity.eupmyeondongCode?.code?.toIntOrNull() ?: return@filter false

                entitySidoCode == sidoCodeInt &&
                    entitySigunguCode == sigunguCodeInt &&
                    existingEupmyeondongCodes.contains(eupmyeondongCode)
            }.mapNotNull { entity ->
                val code = entity.eupmyeondongCode?.code ?: return@mapNotNull null
                val name = entity.eupmyeondongName ?: return@mapNotNull null
                EupmyeondongResponse(code = code, name = name)
            }.distinctBy { it.code }
            .sortedBy { it.code }
    }

    @Cacheable("region:all")
    fun getAllRegions(): RegionAllResponse {
        val allRegions = regionLegalCodeRepository.findAll()
        val existingSidoCodes = apartmentComplexRepository.findDistinctSidoCodes()
        val existingSidoSigunguCodes = apartmentComplexRepository.findDistinctSidoSigunguCodes()
        val eupmyeondongCodeMap = apartmentComplexRepository.findAllDistinctSidoSigunguAndEupmyeondongCodes()

        // 시도 목록
        val sidoList =
            allRegions
                .filter { entity ->
                    val sidoCodeInt = entity.sidoCode.code.toIntOrNull() ?: return@filter false
                    existingSidoCodes.contains(sidoCodeInt)
                }.map { SidoResponse(code = it.sidoCode.code, name = it.sidoName) }
                .distinctBy { it.code }
                .sortedBy { it.code }

        // 시군구 목록 (시도별 그룹)
        val sigunguMap =
            sidoList.associate { sido ->
                val sidoCodeInt = sido.code.toIntOrNull() ?: return@associate sido.code to emptyList()
                sido.code to
                    allRegions
                        .filter { entity ->
                            val entitySidoCode = entity.sidoCode.code.toIntOrNull() ?: return@filter false
                            if (entitySidoCode != sidoCodeInt) return@filter false

                            val sigunguCode = entity.sigunguCode?.code?.toIntOrNull() ?: return@filter false
                            val fullCode = toFullSidoSigunguCode(entitySidoCode, sigunguCode)
                            existingSidoSigunguCodes.contains(fullCode)
                        }.mapNotNull { entity ->
                            val code = entity.sigunguCode?.code ?: return@mapNotNull null
                            val name = entity.sigunguName ?: return@mapNotNull null
                            SigunguResponse(code = code, name = name)
                        }.distinctBy { it.code }
                        .sortedBy { it.code }
            }

        // 읍면동 목록 (시도시군구별 그룹)
        val eupmyeondongMap = mutableMapOf<String, List<EupmyeondongResponse>>()
        sigunguMap.forEach { (sidoCode, sigunguList) ->
            val sidoCodeInt = sidoCode.toIntOrNull() ?: return@forEach
            sigunguList.forEach { sigungu ->
                val sigunguCodeInt = sigungu.code.toIntOrNull() ?: return@forEach
                val sidoSigunguCode = toFullSidoSigunguCode(sidoCodeInt, sigunguCodeInt)
                val existingEupmyeondongCodes = eupmyeondongCodeMap[sidoSigunguCode] ?: emptyList()

                val key = sidoCode + sigungu.code
                eupmyeondongMap[key] =
                    allRegions
                        .filter { entity ->
                            val entitySidoCode = entity.sidoCode.code.toIntOrNull() ?: return@filter false
                            val entitySigunguCode = entity.sigunguCode?.code?.toIntOrNull() ?: return@filter false
                            val eupmyeondongCode =
                                entity.eupmyeondongCode?.code?.toIntOrNull() ?: return@filter false

                            entitySidoCode == sidoCodeInt &&
                                entitySigunguCode == sigunguCodeInt &&
                                existingEupmyeondongCodes.contains(eupmyeondongCode)
                        }.mapNotNull { entity ->
                            val code = entity.eupmyeondongCode?.code ?: return@mapNotNull null
                            val name = entity.eupmyeondongName ?: return@mapNotNull null
                            EupmyeondongResponse(code = code, name = name)
                        }.distinctBy { it.code }
                        .sortedBy { it.code }
            }
        }

        // Kotlin의 emptyList()는 런타임에 kotlin.collections.EmptyList 싱글톤이라
        // Jackson + DefaultTyping(NON_FINAL)에서 역직렬화 실패함 → ArrayList로 보장
        return RegionAllResponse(
            sido = ArrayList(sidoList),
            sigungu = sigunguMap.mapValues { ArrayList(it.value) },
            eupmyeondong = eupmyeondongMap.mapValues { ArrayList(it.value) },
        )
    }
}
