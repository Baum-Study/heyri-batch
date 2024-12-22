package com.hyeri.hyeribatch.chapter05.task

import com.hyeri.hyeribatch.entity.Payment
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldSet
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object PaymentCsvContext {
    val headers: Array<String> = arrayOf("id", "sellerId", "productId", "productName", "price", "paymentDate")

    val csvLineMapper = DefaultLineMapper<Payment>().apply {
        val tokenizer = DelimitedLineTokenizer()
        tokenizer.setNames( "id", "sellerId", "productId", "productName", "price", "paymentDate" )
        setLineTokenizer(tokenizer)
        setFieldSetMapper(PaymentFieldSetMapper)
    }

    private object PaymentFieldSetMapper : FieldSetMapper<Payment> {
        override fun mapFieldSet(fieldSet: FieldSet): Payment {
            return Payment(
                id = fieldSet.readLong("id"),
                sellerId = fieldSet.readLong("sellerId"),
                productId = fieldSet.readLong("productId"),
                productName = fieldSet.readString("productName"),
                price = fieldSet.readBigDecimal("price"),
                paymentDate = LocalDateTime.parse(fieldSet.readString("paymentDate"), DateTimeFormatter.ISO_DATE_TIME)
            )
        }
    }
}
