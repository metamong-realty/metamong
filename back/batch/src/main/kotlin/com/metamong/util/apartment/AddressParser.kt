package com.metamong.util.apartment

object AddressParser {
    private val JIBUN_PATTERN = Regex("""^(\d+)(?:-(\d+))?$""")
    private val KAPT_ADDR_JIBUN_PATTERN = Regex("""(\d+)(?:-(\d+))?\s*(?:\S+아파트|\S+$)""")
    private val ROADNM_PATTERN = Regex("""^(.+?)\s+(\d+)(?:-(\d+))?$""")

    data class JibunResult(
        val bonNo: Short,
        val buNo: Short,
    )

    data class RoadAddressResult(
        val roadName: String,
        val buildingMainNo: String,
        val buildingSubNo: String?,
    )

    fun parseJibun(jibun: String?): JibunResult? {
        if (jibun.isNullOrBlank()) return null

        val match = JIBUN_PATTERN.find(jibun.trim()) ?: return null

        val bonNo = match.groupValues[1].toShortOrNull() ?: return null
        val buNo =
            match.groupValues
                .getOrNull(2)
                ?.takeIf { it.isNotEmpty() }
                ?.toShortOrNull() ?: 0

        return JibunResult(bonNo, buNo)
    }

    fun parseKaptAddrJibun(kaptAddr: String?): JibunResult? {
        if (kaptAddr.isNullOrBlank()) return null

        val match = KAPT_ADDR_JIBUN_PATTERN.find(kaptAddr) ?: return null

        val bonNo = match.groupValues[1].toShortOrNull() ?: return null
        val buNo =
            match.groupValues
                .getOrNull(2)
                ?.takeIf { it.isNotEmpty() }
                ?.toShortOrNull() ?: 0

        return JibunResult(bonNo, buNo)
    }

    fun parseRoadnm(roadnm: String?): RoadAddressResult? {
        if (roadnm.isNullOrBlank()) return null

        val match = ROADNM_PATTERN.find(roadnm.trim()) ?: return null

        val roadName = match.groupValues[1]
        val buildingMainNo = match.groupValues[2].padStart(5, '0')
        val buildingSubNo =
            match.groupValues
                .getOrNull(3)
                ?.takeIf { it.isNotEmpty() }
                ?.padStart(5, '0')

        return RoadAddressResult(roadName, buildingMainNo, buildingSubNo)
    }

    fun buildRoadAddress(
        roadNm: String?,
        roadNmBonbun: String?,
        roadNmBubun: String?,
    ): String? {
        if (roadNm.isNullOrBlank()) return null

        val mainNo = roadNmBonbun?.trimStart('0')?.takeIf { it.isNotEmpty() } ?: return null
        val subNo = roadNmBubun?.trimStart('0')?.takeIf { it.isNotEmpty() && it != "0" }

        return if (subNo != null) {
            "$roadNm $mainNo-$subNo"
        } else {
            "$roadNm $mainNo"
        }
    }

    fun buildJibunAddress(
        umdNm: String?,
        jibun: String?,
    ): String? {
        if (umdNm.isNullOrBlank() || jibun.isNullOrBlank()) return null
        return "$umdNm $jibun"
    }

    fun extractEupmyeondongRiCodeFromBjdCode(bjdCode: String?): Int? {
        if (bjdCode.isNullOrBlank() || bjdCode.length < 10) return null
        return bjdCode.substring(5, 10).toIntOrNull()
    }

    fun extractSidoSigunguCodeFromBjdCode(bjdCode: String?): Int? {
        if (bjdCode.isNullOrBlank() || bjdCode.length < 5) return null
        return bjdCode.substring(0, 5).toIntOrNull()
    }
}
