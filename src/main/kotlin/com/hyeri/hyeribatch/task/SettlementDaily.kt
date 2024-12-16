package com.hyeri.hyeribatch.task

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.LocalDate

@Entity
data class SettlementDaily(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,
    val sellerId: Long,
    val settlementDate: LocalDate,
    var totalAmount: BigDecimal,
)

