package com.hyeri.hyeribatch.chapter03

import org.springframework.batch.item.ItemProcessor

class MyItemProcessor : ItemProcessor<String, String> {
    override fun process(item: String): String {
        println("Input: ${item}")
        val processedItem = item.uppercase()
        println("Output: ${processedItem}")
        return processedItem
    }
}