package com.metamong.batch.jobs.publicdata.sync.listener

import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.SkipListener
import org.springframework.stereotype.Component

@Component
class ApartmentMigrationSkipListener : SkipListener<Any, Any> {

    override fun onSkipInRead(t: Throwable) {
        logger.warn { "읽기 중 Skip 발생: ${t.javaClass.simpleName} - ${t.message}" }
    }

    override fun onSkipInWrite(item: Any, t: Throwable) {
        logger.warn { "쓰기 중 Skip 발생 - Item: $item, Error: ${t.javaClass.simpleName} - ${t.message}" }
    }

    override fun onSkipInProcess(item: Any, t: Throwable) {
        when (item) {
            is ApartmentTradeRawDocumentEntity -> {
                logger.warn { 
                    "처리 중 Skip 발생 [매매] - " +
                    "아파트시퀀스: ${item.aptSeq}, " +
                    "단지명: ${item.aptNm}, " +
                    "거래금액: ${item.dealAmount}, " +
                    "Error: ${t.javaClass.simpleName} - ${t.message}"
                }
            }
            is ApartmentRentRawDocumentEntity -> {
                logger.warn {
                    "처리 중 Skip 발생 [전월세] - " +
                    "아파트시퀀스: ${item.aptSeq}, " +
                    "단지명: ${item.aptNm}, " +
                    "보증금: ${item.deposit}, 월세: ${item.monthlyRent}, " +
                    "Error: ${t.javaClass.simpleName} - ${t.message}"
                }
            }
            else -> {
                logger.warn { "처리 중 Skip 발생 - Item: $item, Error: ${t.javaClass.simpleName} - ${t.message}" }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}