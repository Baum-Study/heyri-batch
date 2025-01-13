package com.hyeri.hyeribatch.chapter09

import com.hyeri.hyeribatch.chapter09.task.MybatisProductConfiguration
import com.hyeri.hyeribatch.common.domain.product.DeliveryStatus
import com.hyeri.hyeribatch.common.domain.product.Product
import com.hyeri.hyeribatch.common.domain.product.ProductRepository
import com.hyeri.hyeribatch.dummy.BigDecimalRangeRandomizer
import com.hyeri.hyeribatch.dummy.mapChunkParallel
import com.hyeri.hyeribatch.dummy.mapParallel
import com.hyeri.hyeribatch.dummy.randomize
import org.assertj.core.api.Assertions
import org.jeasy.random.EasyRandom
import org.jeasy.random.EasyRandomParameters
import org.jeasy.random.randomizers.range.LocalDateTimeRangeRandomizer
import org.jeasy.random.randomizers.range.LongRangeRandomizer
import org.jeasy.random.randomizers.text.StringRandomizer
import org.junit.jupiter.api.Test
import org.springframework.batch.core.BatchStatus
import org.springframework.batch.test.JobLauncherTestUtils
import org.springframework.batch.test.context.SpringBatchTest
import org.springframework.boot.test.context.SpringBootTest
import java.math.BigDecimal
import java.time.LocalDateTime
import java.time.Month

@SpringBatchTest
@SpringBootTest(classes=[MybatisProductConfiguration::class])
class Chapter09Test(
    val productRepository: ProductRepository,
    val jobLauncherTestUtils: JobLauncherTestUtils,
) {

    @Test
    fun task() {
        // given
        productRepository.saveAll(
            10_000L.mapParallel { createRandomProduct() }
        )

        // when
        val jobExecution = jobLauncherTestUtils.launchJob()

        // then
        Assertions.assertThat(jobExecution.status).isEqualTo(BatchStatus.COMPLETED)
    }

    fun createRandomProduct(): Product {
        val parameters = EasyRandomParameters()
            .excludeField {
                it.name == Product::id.name
            }
            .randomize(Product::memberId, LongRangeRandomizer(3L, 10000L))
            .randomize(Product::name, StringRandomizer(10))
            .randomize(
                Product::price, BigDecimalRangeRandomizer(
                BigDecimal("1000.00"), BigDecimal("50000.00")
            )
            )
            .randomize(Product::deliveryStatus) {
                DeliveryStatus.values().random()
            }
            .randomize(
                Product::createdAt,
                LocalDateTimeRangeRandomizer(
                    LocalDateTime.of(2020, Month.JANUARY, 1, 0, 0),
                    LocalDateTime.of(2025, Month.DECEMBER, 31, 23, 59)
                )
            )

        return EasyRandom(parameters).nextObject(Product::class.java)
    }
}