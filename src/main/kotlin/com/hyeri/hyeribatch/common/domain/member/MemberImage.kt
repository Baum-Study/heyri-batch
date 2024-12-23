package com.hyeri.hyeribatch.common.domain.member

import jakarta.persistence.*

@Entity
class MemberImage (
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val url: String,

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    val member: Member
)