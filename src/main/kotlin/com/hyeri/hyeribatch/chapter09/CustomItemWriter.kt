package com.hyeri.hyeribatch.chapter09

import com.hyeri.hyeribatch.common.domain.customer.Customer
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.ItemWriter
import org.springframework.stereotype.Component

@Component
class CustomItemWriter(
    private val customService: CustomService,
) : ItemWriter<Customer> {

    override fun write(chunk: Chunk<out Customer>) {
        for(customer in chunk) {
            customService.processToOtherService(customer)
        }
    }

}