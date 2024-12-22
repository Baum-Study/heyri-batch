package com.hyeri.hyeribatch.chapter05.task

import com.hyeri.hyeribatch.entity.Payment
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PaymentRepository : JpaRepository<Payment, Long>