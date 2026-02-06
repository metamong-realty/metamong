package com.metamong.batch.jobs.publicdata.writer

import com.metamong.model.document.publicdata.ApartmentComplexInfoRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentComplexListRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentRentRawDocumentEntity
import com.metamong.model.document.publicdata.ApartmentTradeRawDocumentEntity
import com.metamong.model.document.publicdata.HousingLicenseRawDocumentEntity
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import org.bson.Document
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.data.mongodb.core.BulkOperations
import org.springframework.data.mongodb.core.MongoTemplate
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Component
class PublicDataMongoWriter(
    private val mongoTemplate: MongoTemplate,
) {
    /**
     * Entity를 Update 객체로 변환
     * MongoTemplate의 converter를 사용하여 Entity의 모든 필드를 자동으로 변환
     */
    private fun <T : Any> toUpdate(entity: T): Update {
        val document = Document()
        mongoTemplate.converter.write(entity, document)
        document.remove("_id")
        return Update.fromDocument(Document("\$set", document))
    }

    /**
     * 단일 배치 실행 (병렬 처리용)
     * @return Pair(insertedCount, modifiedCount)
     */
    private fun <T : Any> executeSingleBatch(
        batch: List<T>,
        batchIndex: Int,
        totalBatches: Int,
        entityClass: KClass<T>,
        queryBuilder: (T) -> Query,
        operationName: String,
    ): Pair<Int, Int> {
        val batchStartTime = System.currentTimeMillis()
        logger.info { "$operationName 배치 ${batchIndex + 1}/$totalBatches 시작 (${batch.size}건)" }

        val bulkOps =
            mongoTemplate.bulkOps(
                BulkOperations.BulkMode.UNORDERED,
                entityClass.java,
            )

        val prepareStartTime = System.currentTimeMillis()
        batch.forEach { doc ->
            bulkOps.upsert(queryBuilder(doc), toUpdate(doc))
        }
        val prepareTime = System.currentTimeMillis() - prepareStartTime

        val executeStartTime = System.currentTimeMillis()
        val result = bulkOps.execute()
        val executeTime = System.currentTimeMillis() - executeStartTime

        val totalTime = System.currentTimeMillis() - batchStartTime
        logger.info {
            "$operationName 배치 ${batchIndex + 1}/$totalBatches 완료: " +
                "신규 ${result.insertedCount}건, 매칭 ${result.matchedCount}건, 수정 ${result.modifiedCount}건 | " +
                "준비 ${prepareTime}ms, 실행 ${executeTime}ms, 총 ${totalTime}ms"
        }

        return Pair(result.insertedCount, result.modifiedCount)
    }

    /**
     * 대량 문서를 BULK_BATCH_SIZE 단위로 나누어 병렬로 BulkOps 실행
     * MongoDB 타임아웃 방지를 위해 배치 분할
     */
    private fun <T : Any> executeBulkUpsert(
        documents: List<T>,
        entityClass: KClass<T>,
        queryBuilder: (T) -> Query,
        keyExtractor: (T) -> String,
        operationName: String,
    ) {
        if (documents.isEmpty()) return

        // 중복 키 제거 (마지막 문서만 유지) - Write Conflict 방지
        val uniqueDocuments = documents.associateBy(keyExtractor).values.toList()
        if (uniqueDocuments.size < documents.size) {
            logger.warn {
                "$operationName 중복 키 제거: ${documents.size}건 → ${uniqueDocuments.size}건 (${documents.size - uniqueDocuments.size}건 중복)"
            }
        }

        val batches = uniqueDocuments.chunked(BULK_BATCH_SIZE)
        val totalBatches = batches.size

        logger.info { "$operationName 병렬 처리 시작: 총 ${uniqueDocuments.size}건, ${totalBatches}개 배치" }

        val overallStartTime = System.currentTimeMillis()

        val results =
            runBlocking {
                batches
                    .mapIndexed { batchIndex, batch ->
                        CoroutineScope(Dispatchers.IO).async {
                            executeSingleBatch(
                                batch = batch,
                                batchIndex = batchIndex,
                                totalBatches = totalBatches,
                                entityClass = entityClass,
                                queryBuilder = queryBuilder,
                                operationName = operationName,
                            )
                        }
                    }.awaitAll()
            }

        val totalInserted = results.sumOf { it.first }
        val totalModified = results.sumOf { it.second }
        val overallTime = System.currentTimeMillis() - overallStartTime

        logger.info {
            "$operationName 저장 완료: 신규 ${totalInserted}건, 수정 ${totalModified}건 (총 ${uniqueDocuments.size}건) | " +
                "전체 소요시간 ${overallTime}ms"
        }
    }

    fun apartmentTradeWriter(): ItemWriter<List<ApartmentTradeRawDocumentEntity>> =
        ItemWriter { chunk: Chunk<out List<ApartmentTradeRawDocumentEntity>> ->
            executeBulkUpsert(
                documents = chunk.items.flatten(),
                entityClass = ApartmentTradeRawDocumentEntity::class,
                queryBuilder = { doc ->
                    Query(
                        org.springframework.data.mongodb.core.query.Criteria
                            .where("compositeKey")
                            .`is`(doc.compositeKey),
                    )
                },
                keyExtractor = { doc -> doc.compositeKey },
                operationName = "아파트 매매 실거래가",
            )
        }

    fun apartmentRentWriter(): ItemWriter<List<ApartmentRentRawDocumentEntity>> =
        ItemWriter { chunk: Chunk<out List<ApartmentRentRawDocumentEntity>> ->
            executeBulkUpsert(
                documents = chunk.items.flatten(),
                entityClass = ApartmentRentRawDocumentEntity::class,
                queryBuilder = { doc ->
                    Query(
                        org.springframework.data.mongodb.core.query.Criteria
                            .where("compositeKey")
                            .`is`(doc.compositeKey),
                    )
                },
                keyExtractor = { doc -> doc.compositeKey },
                operationName = "아파트 전월세 실거래가",
            )
        }

    fun housingLicenseWriter(): ItemWriter<List<HousingLicenseRawDocumentEntity>> =
        ItemWriter { chunk: Chunk<out List<HousingLicenseRawDocumentEntity>> ->
            val validDocuments = chunk.items.flatten().filter { it.mgmDongOulnPk != null }
            executeBulkUpsert(
                documents = validDocuments,
                entityClass = HousingLicenseRawDocumentEntity::class,
                queryBuilder = { doc ->
                    Query(
                        org.springframework.data.mongodb.core.query.Criteria
                            .where("mgmDongOulnPk")
                            .`is`(doc.mgmDongOulnPk),
                    )
                },
                keyExtractor = { doc -> "${doc.mgmDongOulnPk}" },
                operationName = "주택인허가정보",
            )
        }

    fun apartmentComplexListWriter(): ItemWriter<List<ApartmentComplexListRawDocumentEntity>> =
        ItemWriter { chunk: Chunk<out List<ApartmentComplexListRawDocumentEntity>> ->
            executeBulkUpsert(
                documents = chunk.items.flatten(),
                entityClass = ApartmentComplexListRawDocumentEntity::class,
                queryBuilder = { doc ->
                    Query(
                        org.springframework.data.mongodb.core.query.Criteria
                            .where("kaptCode")
                            .`is`(doc.kaptCode),
                    )
                },
                keyExtractor = { doc -> "${doc.kaptCode}" },
                operationName = "공동주택 단지 목록",
            )
        }

    fun apartmentComplexInfoWriter(): ItemWriter<ApartmentComplexInfoRawDocumentEntity?> =
        ItemWriter { chunk: Chunk<out ApartmentComplexInfoRawDocumentEntity?> ->
            executeBulkUpsert(
                documents = chunk.items.filterNotNull(),
                entityClass = ApartmentComplexInfoRawDocumentEntity::class,
                queryBuilder = { doc ->
                    Query(
                        org.springframework.data.mongodb.core.query.Criteria
                            .where("kaptCode")
                            .`is`(doc.kaptCode),
                    )
                },
                keyExtractor = { doc -> "${doc.kaptCode}" },
                operationName = "공동주택 기본 정보",
            )
        }

    companion object {
        private val logger = KotlinLogging.logger {}
        private const val BULK_BATCH_SIZE = 300
    }
}
