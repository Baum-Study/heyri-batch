package com.hyeri.hyeribatch.dummy

import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.api.Randomizer
import java.math.BigDecimal
import java.util.*
import java.util.stream.LongStream
import kotlin.reflect.KProperty

fun <T> Long.mapChunkParallel(
    chunkSize: Long,
    block: () -> T,
): List<T> = LongStream.range(this, this + chunkSize)
        .parallel()
        .mapToObj {
            block()
        }.toList()

fun <T> Long.mapParallel(
    block: () -> T,
): List<T> = LongStream.range(1L, this)
    .parallel()
    .mapToObj {
        block()
    }.toList()

fun <T, R> EasyRandomParameters.randomize(
    property: KProperty<T>,
    randomizer: Randomizer<R>,
): EasyRandomParameters = randomize(
        { it.name == property.name },
        randomizer,
    )

class BigDecimalRangeRandomizer(
    private val min: BigDecimal,
    private val max: BigDecimal,
    private val random: Random = Random()
) : Randomizer<BigDecimal> {

    override fun getRandomValue(): BigDecimal {
        val range = max.subtract(min)
        val randomValue = range.multiply(BigDecimal(random.nextDouble()))
        return min.add(randomValue).setScale(2, BigDecimal.ROUND_HALF_UP)
    }
}