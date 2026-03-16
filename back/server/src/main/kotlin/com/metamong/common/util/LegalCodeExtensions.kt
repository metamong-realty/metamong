package com.metamong.common.util

/**
 * 시도 코드와 시군구 코드를 합쳐서 전체 시도시군구 코드 생성
 * 예: 시도=11, 시군구=170 → 11170
 */
fun toFullSidoSigunguCode(
    sidoCode: Int,
    sigunguCode: Int,
): Int = sidoCode * 1000 + sigunguCode

/**
 * 시도시군구 코드에서 시도 코드 추출
 * 예: 11170 → 11
 */
fun extractSidoCode(sidoSigunguCode: Int): Int = sidoSigunguCode / 1000

/**
 * 시도시군구 코드에서 시군구 코드 추출
 * 예: 11170 → 170
 */
fun extractSigunguCode(sidoSigunguCode: Int): Int = sidoSigunguCode % 1000
