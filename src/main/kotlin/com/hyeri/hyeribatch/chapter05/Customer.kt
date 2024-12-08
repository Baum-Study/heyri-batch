package com.hyeri.hyeribatch.chapter05

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table


//class Customer(val name: String, val age: Int, val gender: String)


@Entity
class Customer(
    @Id @GeneratedValue
    var id: Long? = null,

    var name: String,

    var age: Int,

    var gender: String,
)
