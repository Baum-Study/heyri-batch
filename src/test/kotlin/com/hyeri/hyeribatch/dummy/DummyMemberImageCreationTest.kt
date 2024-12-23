package com.hyeri.hyeribatch.dummy

import com.hyeri.hyeribatch.chapter06.task.MemberImageRepository
import com.hyeri.hyeribatch.chapter06.task.MemberRepository
import com.hyeri.hyeribatch.common.domain.member.Member
import com.hyeri.hyeribatch.common.domain.member.MemberImage
import com.hyeri.hyeribatch.common.domain.member.MemberStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.randomizers.misc.EnumRandomizer
import org.jeasy.random.randomizers.text.StringRandomizer
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.random.Random
import kotlin.test.Test

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@SpringBootTest
class DummyMemberImageCreationTest {
    @Autowired
    lateinit var memberRepository: MemberRepository

    @Autowired
    lateinit var memberImageRepository: MemberImageRepository

    private val logger = KotlinLogging.logger { }

    private lateinit var members: List<Member>

    @BeforeAll
    fun setUp() {
        members = memberRepository.findAll()
    }

    @Test
    fun `MemberImage 100만건 등록`() {
        // given
        val total = 1_000_000L
        val chunkSize = 1_000L

        // when & then
        for (i in 0 until total step chunkSize) {
            val memberImages = i.mapChunkParallel(chunkSize) {
                MemberImageFactory.generateRandom(members.random())
            }
            memberImageRepository.saveAll(memberImages)
            logger.info { "saved ${memberImages.first().id} to ${memberImages.last().id}" }
        }

        logger.info { "saved all memberImages." }
    }
}

object MemberImageFactory {
    fun generateRandom(member: Member): MemberImage {
        val parameter = EasyRandomParameters()
            .excludeField {
                it.name == MemberImage::id.name
            }
            .randomize(MemberImage::url) {
                StringRandomizer(1, 10, Random.nextLong(1, 100)).randomValue
            }
            .randomize(MemberImage::member) {
                member
            }
        return EasyRandom(parameter).nextObject(MemberImage::class.java)
    }
}

