package com.metamong.batch.jobs.publicdata.writer

import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

/**
 * Historical 배치 전용 MongoDB Writer
 * - 중복 체크 없이 직접 insert 수행
 * - upsert 대신 insert 사용으로 성능 최적화
 * - Historical 데이터는 전체 기간을 한 번에 가져오므로 중복 불가
 */
@Component
class PublicDataMongoFastWriter(
    private val mongoTemplate: MongoTemplate,
) {
    /**
     * 단일 배치 실행 (병렬 처리용) - insert 전용
     * @return insertedCount
     */
    private fun <T : Any> executeSingleBatch(
        batch: List<T>,
        batchIndex: Int,
        totalBatches: Int,
        entityClass: KClass<T>,
        operationName: String,
    ): Int {
        val batchStartTime = System.currentTimeMillis()
        logger.info { "$operationName Fast 배치 ${batchIndex + 1}/$totalBatches 시작 (${batch.size}건)" }

        val bulkOps =
            mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                entityClass.java,
            )

        val prepareStartTime = System.currentTimeMillis()
        batch.forEach { doc ->
            bulkOps.insert(doc)
        }
        val prepareTime = System.currentTimeMillis() - prepareStartTime

        val executeStartTime = System.currentTimeMillis()
        val result = bulkOps.execute()
        val executeTime = System.currentTimeMillis() - executeStartTime

        val totalTime = System.currentTimeMillis() - batchStartTime
        
        // 메모리 사용량 체크
        val runtime = Runtime.getRuntime()
        val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 // MB
        val maxMemory = runtime.maxMemory() / 1024 / 1024 // MB
        val memoryUsagePercent = (usedMemory.toDouble() / maxMemory * 100).toInt()
        
        logger.info {
            "$operationName Fast 배치 ${batchIndex + 1}/$totalBatches 완료: " +
                "신규 ${result.insertedCount}건 | " +
                "준비 ${prepareTime}ms, 실행 ${executeTime}ms, 총 ${totalTime}ms | " +
                "메모리 ${usedMemory}/${maxMemory}MB (${memoryUsagePercent}%)"
        }
        
        if (memoryUsagePercent > 80) {
            logger.warn { "메모리 사용률 높음: ${memoryUsagePercent}% - GC 권장" }
        }

        return result.insertedCount
    }

    /**
     * 대량 문서를 BULK_BATCH_SIZE 단위로 나누어 병렬로 BulkOps 실행 (insert 전용)
     * MongoDB 타임아웃 방지를 위해 배치 분할
     */
    private fun <T : Any> executeBulkInsert(
        documents: List<T>,
        entityClass: KClass<T>,
        keyExtractor: (T) -> String,
        operationName: String,
    ) {
        if (documents.isEmpty()) return

        // Historical은 중복이 없다고 가정하지만, 안전장치로 중복 제거
        val uniqueDocuments = documents.associateBy(keyExtractor).values.toList()
        if (uniqueDocuments.size < documents.size) {
            logger.warn {
                "$operationName Fast 중복 키 제거: ${documents.size}건 → ${uniqueDocuments.size}건 (${documents.size - uniqueDocuments.size}건 중복)"
            }
        }

        val batches = uniqueDocuments.chunked(BULK_BATCH_SIZE)
        val totalBatches = batches.size

        logger.info { "$operationName Fast 병렬 처리 시작: 총 ${uniqueDocuments.size}건, ${totalBatches}개 배치 (배치크기: $BULK_BATCH_SIZE)" }

        val overallStartTime = System.currentTimeMillis()

        val semaphore = Semaphore(MAX_CONCURRENT_BATCHES)
        
        val results =
            runBlocking {
                batches
                    .mapIndexed { batchIndex, batch ->
                        CoroutineScope(Dispatchers.IO).async {
                            semaphore.withPermit {
                                executeSingleBatch(
                                    batch = batch,
                                    batchIndex = batchIndex,
                                    totalBatches = totalBatches,
                                    entityClass = entityClass,
                                    operationName = operationName,
                                )
                            }
                        }
                    }.awaitAll()
            }

        val totalInserted = results.sum()
        val overallTime = System.currentTimeMillis() - overallStartTime
        val throughputPerSecond = if (overallTime > 0) (totalInserted * 1000 / overallTime) else 0
        
        // 최종 메모리 상태
        val runtime = Runtime.getRuntime()
        val finalUsedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024 // MB
        val finalMaxMemory = runtime.maxMemory() / 1024 / 1024 // MB
        val finalMemoryUsagePercent = (finalUsedMemory.toDouble() / finalMaxMemory * 100).toInt()

        logger.info {
            "$operationName Fast 저장 완료: 신규 ${totalInserted}건 (총 ${uniqueDocuments.size}건) | " +
                "전체 소요시간 ${overallTime}ms (평균 ${overallTime / totalBatches}ms/배치) | " +
                "처리량 ${throughputPerSecond}건/초 | " +
                "최종 메모리 ${finalUsedMemory}/${finalMaxMemory}MB (${finalMemoryUsagePercent}%) | " +
                "병렬도 ${MAX_CONCURRENT_BATCHES}개"
        }
        
        if (finalMemoryUsagePercent > 85) {
            logger.warn { "최종 메모리 사용률 매우 높음: ${finalMemoryUsagePercent}% - 시스템 모니터링 필요" }
        }
    }

    fun apartmentTradeWriter(): ItemWriter<List<ApartmentTradeRawDocumentEntity>> =
        ItemWriter { chunk: Chunk<out List<ApartmentTradeRawDocumentEntity>> ->
            executeBulkInsert(
                documents = chunk.items.flatten(),
                entityClass = ApartmentTradeRawDocumentEntity::class,
                keyExtractor = { doc -> doc.compositeKey },
                operationName = "아파트 매매 실거래가",
            )
        }

    fun apartmentRentWriter(): ItemWriter<List<ApartmentRentRawDocumentEntity>> =
        ItemWriter { chunk: Chunk<out List<ApartmentRentRawDocumentEntity>> ->
            executeBulkInsert(
                documents = chunk.items.flatten(),
                entityClass = ApartmentRentRawDocumentEntity::class,
                keyExtractor = { doc -> doc.compositeKey },
                operationName = "아파트 전월세 실거래가",
            )
        }

    companion object {
        private val logger = KotlinLogging.logger {}

        // MongoDB 연결 안정성을 위해 배치 사이즈 축소 (500 → 200)
        private const val BULK_BATCH_SIZE = 200
        
        // 병렬 처리 제한 (무제한 → 3개로 제한)
        private const val MAX_CONCURRENT_BATCHES = 3
    }
}
