package com.hyeri.hyeribatch.data

import com.hyeri.hyeribatch.entity.Payment
import java.math.BigDecimal
import java.time.YearMonth

data class MonthlyPaymentDTO (
    val sellerId: Long,
    val date: YearMonth,
    val price: BigDecimal,
) {
    companion object {
        fun of(payment: Payment): MonthlyPaymentDTO = MonthlyPaymentDTO(
            sellerId = payment.sellerId,
            date = YearMonth.from(payment.paymentDate),
            price = payment.price,
        )
    }
}