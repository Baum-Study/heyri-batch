package com.hyeri.hyeribatch.common.domain.customer

import jakarta.persistence.*

@Entity
class Customer(
    @Id @GeneratedValue(strategy = GenerationType.AUTO)
    var id: Long? = null,

    var name: String,

    var age: Int,

    var gender: String,
)
