package com.metamong.model.document.publicdata

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime

@Document("ApartmentComplexInfoRaw")
data class ApartmentComplexInfoRawDocumentEntity(
    @Id val id: String? = null,
    @Indexed(unique = true)
    val kaptCode: String, // 단지코드
    val kaptName: String?, // 단지명
    val kaptAddr: String?, // 단지주소
    val doroJuso: String?, // 도로명주소
    val codeSaleNm: String?, // 분양구분 (분양/임대)
    val codeHeatNm: String?, // 난방방식
    val codeHallNm: String?, // 복도유형 (계단식/복도식)
    val codeMgrNm: String?, // 관리방식 (위탁관리/자치관리)
    val codeAptNm: String?, // 주택유형 (아파트)
    val kaptTarea: Double?, // 단지전용면적 (㎡)
    val kaptMarea: Double?, // 단지관리비부과면적 (㎡)
    val kaptDongCnt: String?, // 동수
    val hoCnt: Int?, // 세대수
    val kaptdaCnt: Double?, // 총 주차대수
    val kaptTopFloor: Int?, // 최고층
    val kaptBaseFloor: Int?, // 지하층수
    val ktownFlrNo: Int?, // 최고층수 (타운)
    val kaptMparea60: Double?, // 60㎡ 이하 세대수
    val kaptMparea85: Double?, // 60~85㎡ 세대수
    val kaptMparea135: Double?, // 85~135㎡ 세대수
    val kaptMparea136: Double?, // 135㎡ 초과 세대수
    val privArea: String?, // 전용면적합계
    val kaptBcompany: String?, // 시공사
    val kaptAcompany: String?, // 시행사
    val kaptTel: String?, // 관리사무소 전화번호
    val kaptFax: String?, // 관리사무소 팩스번호
    val kaptUrl: String?, // 홈페이지 URL
    val kaptUsedate: String?, // 사용승인일 (yyyyMMdd)
    val bjdCode: String?, // 법정동코드
    val kaptdEcntp: Int?, // 전기계약종별
    val zipcode: String?, // 우편번호
    val collectedAt: LocalDateTime = LocalDateTime.now(),
)
