package com.hyeri.hyeribatch.chapter05

import com.hyeri.hyeribatch.entity.Customer
import org.springframework.batch.item.database.ItemSqlParameterSourceProvider
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource
import org.springframework.jdbc.core.namedparam.SqlParameterSource


class CustomerItemSqlParameterSourceProvider : ItemSqlParameterSourceProvider<Customer> {
    override fun createSqlParameterSource(item: Customer): SqlParameterSource {
        return BeanPropertySqlParameterSource(item)
    }
}