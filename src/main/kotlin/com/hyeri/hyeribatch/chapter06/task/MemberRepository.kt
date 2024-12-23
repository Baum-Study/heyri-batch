package com.hyeri.hyeribatch.chapter06.task

import com.hyeri.hyeribatch.common.domain.member.Member
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface MemberRepository : JpaRepository<Member, Long>