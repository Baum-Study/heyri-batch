package com.hyeri.hyeribatch

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@EnableBatchProcessing
@SpringBootApplication
class HyeriBatchApplication

fun main(args: Array<String>) {
    runApplication<HyeriBatchApplication>(*args)
}
