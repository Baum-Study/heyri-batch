package com.hyeri.hyeribatch.dummy

import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.api.Randomizer
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

fun <T, R> EasyRandomParameters.randomize(
    property: KProperty<T>,
    randomizer: Randomizer<R>,
): EasyRandomParameters = randomize(
        { it.name == property.name },
        randomizer,
    )