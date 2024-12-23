package com.hyeri.hyeribatch.common.domain.payment.data

import com.hyeri.hyeribatch.common.domain.payment.Payment
import java.math.BigDecimal
import java.time.LocalDate

data class DailyPaymentDTO (
    val sellerId: Long,
    val date: LocalDate,
    val price: BigDecimal,
) {
    companion object {
        fun of(payment: Payment): DailyPaymentDTO = DailyPaymentDTO(
            sellerId = payment.sellerId,
            date = payment.paymentDate.toLocalDate(),
            price = payment.price,
        )
    }
}