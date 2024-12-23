package com.hyeri.hyeribatch.common.domain.member

import jakarta.persistence.*

@Entity
@Table(name = "MEMBER")
class Member(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val name: String,
    val status: MemberStatus,

    @OneToMany(mappedBy = "member", cascade = [CascadeType.REMOVE], orphanRemoval = true)
    val memberImages: List<MemberImage> = mutableListOf()
)