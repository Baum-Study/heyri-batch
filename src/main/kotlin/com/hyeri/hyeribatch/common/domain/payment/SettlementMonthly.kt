package com.hyeri.hyeribatch.common.domain.payment

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import java.math.BigDecimal
import java.time.YearMonth

@Entity
data class SettlementMonthly(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val sellerId: Long,
    val settlementDate: YearMonth,
    var totalAmount: BigDecimal,
)