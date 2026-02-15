package com.metamong.batch.jobs.publicdata.sync

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain

class DealYearMonthRangeTest :
    BehaviorSpec({

        Given("DealYearMonthRange.of()") {

            When("startYearMonth와 endYearMonth 모두 null이면") {
                val result = DealYearMonthRange.of(null, null)

                Then("null을 반환한다") {
                    result.shouldBeNull()
                }
            }

            When("startYearMonth만 제공되면") {
                Then("IllegalArgumentException이 발생한다") {
                    shouldThrow<IllegalArgumentException> {
                        DealYearMonthRange.of("202301", null)
                    }
                }
            }

            When("endYearMonth만 제공되면") {
                Then("IllegalArgumentException이 발생한다") {
                    shouldThrow<IllegalArgumentException> {
                        DealYearMonthRange.of(null, "202306")
                    }
                }
            }

            When("유효한 범위로 요청하면") {
                val result = DealYearMonthRange.of("202301", "202306")

                Then("정상적으로 생성된다") {
                    result.shouldNotBeNull()
                }

                Then("toString()이 범위를 보여준다") {
                    result.toString() shouldContain "202301"
                    result.toString() shouldContain "202306"
                }
            }

            When("단일 월 범위로 요청하면") {
                val result = DealYearMonthRange.of("202301", "202301")

                Then("정상적으로 생성된다") {
                    result.shouldNotBeNull()
                }
            }

            When("년도를 걸치는 범위로 요청하면") {
                val result = DealYearMonthRange.of("202310", "202402")

                Then("정상적으로 생성된다") {
                    result.shouldNotBeNull()
                }
            }

            When("start가 end보다 크면") {
                Then("IllegalArgumentException이 발생한다") {
                    val exception =
                        shouldThrow<IllegalArgumentException> {
                            DealYearMonthRange.of("202306", "202301")
                        }
                    exception.message shouldContain "202306"
                    exception.message shouldContain "202301"
                }
            }

            When("yyyyMM 형식이 아닌 startYearMonth가 주어지면") {
                Then("IllegalArgumentException이 발생한다") {
                    shouldThrow<IllegalArgumentException> {
                        DealYearMonthRange.of("2023-01", "202306")
                    }
                }
            }

            When("yyyyMM 형식이 아닌 endYearMonth가 주어지면") {
                Then("IllegalArgumentException이 발생한다") {
                    shouldThrow<IllegalArgumentException> {
                        DealYearMonthRange.of("202301", "2023006")
                    }
                }
            }
        }
    })
