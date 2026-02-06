package com.metamong.model.document.publicdata

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.CompoundIndex
import org.springframework.data.mongodb.core.index.CompoundIndexes
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

/**
 * 주택인허가 동별개요 Raw 데이터
 */
@Document("HousingLicenseRaw")
@CompoundIndexes(
    CompoundIndex(name = "idx_sigunguCd_bjdongCd", def = "{'sigunguCd': 1, 'bjdongCd': 1}"),
)
data class HousingLicenseRawDocumentEntity(
    @Id val id: String? = null,
    @Indexed(unique = true)
    val mgmDongOulnPk: String?, // 관리동별개요PK (유니크)
    val mgmHsrgstPk: String?, // 관리주택대장PK
    val sigunguCd: String, // 시군구코드
    val bjdongCd: String, // 법정동코드
    val platGbCd: String?, // 대지구분코드 (0:대지, 1:산, 2:블록)
    val bun: String?, // 번
    val ji: String?, // 지
    val bldNm: String?, // 건물명
    val splotNm: String?, // 특수지명
    val block: String?, // 블록
    val lot: String?, // 로트
    val mainAtchGbCd: String?, // 주부속구분코드
    val mainAtchGbCdNm: String?, // 주부속구분코드명
    val dongNm: String?, // 동명칭
    val mainPurpsCd: String?, // 주용도코드
    val mainPurpsCdNm: String?, // 주용도코드명
    val hhldCntPeplRent: String?, // 세대수국민임대(세대)
    val hhldCntPubRent_5: String?, // 세대수공공임대5(세대)
    val hhldCntPubRent_10: String?, // 세대수공공임대10(세대)
    val hhldCntPubRentEtc: String?, // 세대수공공임대기타(세대)
    val hhldCntPubRentTot: String?, // 세대수공공임대계(세대)
    val hhldCntPubLotou: String?, // 세대수공공분양(세대)
    val hhldCntEmplRent: String?, // 세대수사원임대(세대)
    val hhldCntLaborWlfar: String?, // 세대수근로복지(세대)
    val hhldCntCvlRent: String?, // 세대수민간임대(세대)
    val hhldCntCvlLotou: String?, // 세대수민간분양(세대)
    val strctCd: String?, // 구조코드
    val strctCdNm: String?, // 구조코드명
    val roofCd: String?, // 지붕코드
    val roofCdNm: String?, // 지붕코드명
    val archArea: String?, // 건축면적(㎡)
    val totArea: String?, // 연면적(㎡)
    val ugrndArea: String?, // 지하면적(㎡)
    val vlRatEstmTotArea: String?, // 용적률산정연면적(㎡)
    val ugrndFlrCnt: String?, // 지하층수
    val grndFlrCnt: String?, // 지상층수
    val heit: String?, // 높이(m)
    val rideUseElvtCnt: String?, // 승용승강기수
    val emgenUseElvtCnt: String?, // 비상용승강기수
    val flrhFrom: String?, // 층고FROM
    val ceilHeit: String?, // 반자높이(m)
    val stairValidWidth: String?, // 계단유효폭
    val hwayWidth: String?, // 복도너비
    val ouwlThick: String?, // 외벽두께
    val adjHhldWallThick: String?, // 인접세대벽두께
    val platPlc: String?, // 대지위치
    val crtnDay: String?, // 생성일자
    val collectedAt: LocalDateTime = LocalDateTime.now(),
)
