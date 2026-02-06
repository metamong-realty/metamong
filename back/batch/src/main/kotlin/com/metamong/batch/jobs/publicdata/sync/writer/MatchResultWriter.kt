package com.metamong.batch.jobs.publicdata.sync.writer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class MatchResultWriter : ItemWriter<Boolean> {
    override fun write(chunk: org.springframework.batch.item.Chunk<out Boolean>) {
        val matched = chunk.items.count { it == true }
        val failed = chunk.items.count { it == false }
        logger.info { "매칭 완료 - 성공: ${matched}건, 실패: ${failed}건" }
    }

    companion object {
        private val logger = KotlinLogging.logger {}
    }
}
