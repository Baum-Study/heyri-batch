package com.hyeri.hyeribatch.dummy

import com.hyeri.hyeribatch.chapter06.task.MemberRepository
import com.hyeri.hyeribatch.common.domain.member.Member
import com.hyeri.hyeribatch.common.domain.member.MemberStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.randomizers.misc.EnumRandomizer
import org.jeasy.random.randomizers.text.StringRandomizer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import kotlin.random.Random
import kotlin.test.Test

@SpringBootTest
class DummyMemberCreationTest {
    @Autowired
    lateinit var memberRepository: MemberRepository
    private val logger = KotlinLogging.logger { }

    @Test
    fun `Member 10만건 등록`() {
        // given
        val total = 100_000L
        val chunkSize = 1_000L

        // when & then
        for (i in 0 until total step chunkSize) {
            val users = i.mapChunkParallel(chunkSize, MemberFactory::generateRandom)
            memberRepository.saveAll(users)
            logger.info { "saved ${users.first().id} to ${users.last().id}" }
        }

        logger.info { "saved all members." }
    }
}

object MemberFactory {
    fun generateRandom(): Member {
        val parameter = EasyRandomParameters()
            .excludeField {
                it.name == Member::id.name
            }
            .randomize(Member::name) {
                StringRandomizer(1, 10, Random.nextLong(1, 100)).randomValue
            }
            .randomize(Member::status) {
                EnumRandomizer(MemberStatus::class.java).randomValue
            }
        return EasyRandom(parameter).nextObject(Member::class.java)
    }
}