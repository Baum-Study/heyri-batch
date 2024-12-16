package com.hyeri.hyeribatch.task

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal

@Entity
data class SettlementMonthly(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val sellerId: Long,
    val settlementDate: String,
    var totalAmount: BigDecimal,
)