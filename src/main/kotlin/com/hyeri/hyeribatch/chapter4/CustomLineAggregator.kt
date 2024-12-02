package com.hyeri.hyeribatch.chapter4

import org.springframework.batch.item.file.transform.LineAggregator

class CustomLineAggregator : LineAggregator<Customer> {
    override fun aggregate(item: Customer): String {
        return "${item.name}:${item.age}"
    }
}