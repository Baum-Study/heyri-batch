package com.hyeri.hyeribatch.common.domain.payment

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
data class Payment(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val sellerId: Long,
    val productId: Long,
    val productName: String,
    val price: BigDecimal,
    val paymentDate: LocalDateTime,
)
