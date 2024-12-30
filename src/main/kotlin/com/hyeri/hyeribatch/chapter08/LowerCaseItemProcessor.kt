package com.hyeri.hyeribatch.chapter08

import com.hyeri.hyeribatch.common.domain.customer.Customer
import org.springframework.batch.item.ItemProcessor

class LowerCaseItemProcessor : ItemProcessor<Customer, Customer> {
    override fun process(item: Customer): Customer {
        return item.apply {
            name = name.lowercase()
            gender = gender.lowercase()
        }
    }
}