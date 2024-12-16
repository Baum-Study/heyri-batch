package com.hyeri.hyeribatch.dummy

import com.hyeri.hyeribatch.task.Payment
import com.hyeri.hyeribatch.task.PaymentRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.api.Randomizer
import org.jeasy.random.randomizers.text.StringRandomizer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import java.time.LocalDateTime
import java.util.stream.LongStream
import kotlin.random.Random
import kotlin.reflect.KProperty
import kotlin.test.Test

@SpringBootTest
class DummyDataCreationTest {
    @Autowired
    lateinit var paymentRepository: PaymentRepository
    private val logger = KotlinLogging.logger { }

    @Test
    fun `Payment 백만건 등록`() {
        // given
        val total = 1_000_000L
        val chunkSize = 1_000L

        // when & then
        for (i in 0 until total step chunkSize) {
            val payments = i.mapChunkParallel(chunkSize, PaymentFactory::generateRandom)
            paymentRepository.saveAllAndFlush(payments)
            logger.info { "saved ${payments.first().id} to ${payments.last().id}" }
        }

        logger.info { "saved all payments." }
    }

    internal fun <T> Long.mapChunkParallel(
        chunkSize: Long,
        block: () -> T,
    ) =
        LongStream.range(this, this + chunkSize)
            .parallel()
            .mapToObj {
                block()
            }.toList()
}

internal fun <T, R> EasyRandomParameters.randomize(
    property: KProperty<T>,
    randomizer: Randomizer<R>,
) =
    randomize(
        { it.name == property.name },
        randomizer,
    )

internal object PaymentFactory {
    fun generateRandom(): Payment {
        val parameter = EasyRandomParameters()
            .excludeField {
                it.name == Payment::id.name
            }
            .randomize(Payment::sellerId) {
                (1..100).random()
            }
            .randomize(Payment::productId) {
                (1..100).random()
            }
            .randomize(Payment::productName) {
                StringRandomizer(1, 10, Random.nextLong(1, 100)).randomValue
            }
            .randomize(Payment::price) {
                (100..1_000_000).random().toBigDecimal()
            }
            .randomize(Payment::paymentDate) {
                LocalDateTime.of(
                    Random.nextInt(2020, 2022),
                    Random.nextInt(1, 13),
                    Random.nextInt(1, 29),
                    Random.nextInt(0, 24),
                    Random.nextInt(0, 60),
                    Random.nextInt(0, 60),
                )
            }
        return EasyRandom(parameter).nextObject(Payment::class.java)
    }
}