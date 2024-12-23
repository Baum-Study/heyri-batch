package com.hyeri.hyeribatch

import com.hyeri.hyeribatch.common.domain.member.MemberStatus

fun main() {
    println("${MemberStatus.ACTIVE.name} ${MemberStatus.ACTIVE.ordinal}")
    println("${MemberStatus.INACTIVE.name} ${MemberStatus.INACTIVE.ordinal}")
}