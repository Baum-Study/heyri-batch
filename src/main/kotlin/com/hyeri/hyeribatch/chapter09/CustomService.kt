package com.hyeri.hyeribatch.chapter09

import com.hyeri.hyeribatch.common.domain.customer.Customer
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.stereotype.Service

@Service
class CustomService {

    private val logger = KotlinLogging.logger {}

    fun processToOtherService(customer: Customer): Map<String, String> {
        logger.info { "Call API to OtherService....." }
        return mapOf("code" to "200", "message" to "ok")
    }
}