package com.hyeri.hyeribatch.common

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.ChunkListener
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.stereotype.Component

@Component
class ChunkLoggingListener : ChunkListener {
    private val logger = KotlinLogging.logger {}

    override fun afterChunk(context: ChunkContext) {
        val stepExecution = context.stepContext.stepExecution

        logger.info {
            "Chunk - Read: ${stepExecution.readCount}, Write: ${stepExecution.writeCount}"
        }
    }
}