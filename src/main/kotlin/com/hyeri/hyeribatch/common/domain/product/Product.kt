package com.hyeri.hyeribatch.common.domain.product

import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDateTime

@Entity
data class Product(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    val memberId: Long,

    val name: String,

    val price: BigDecimal,

    @Enumerated(EnumType.STRING)
    val deliveryStatus: DeliveryStatus,

    val createdAt: LocalDateTime,
)