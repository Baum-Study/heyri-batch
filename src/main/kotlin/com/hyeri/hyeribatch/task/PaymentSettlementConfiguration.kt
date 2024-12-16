package com.hyeri.hyeribatch.task

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.ItemWriter
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.mapping.DefaultLineMapper
import org.springframework.batch.item.file.mapping.FieldSetMapper
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer
import org.springframework.batch.item.file.transform.FieldSet
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Configuration
class PaymentSettlementConfiguration(
    private val entityManager: EntityManager
) {
    private val chunkSize = 1_000
    private val logger = KotlinLogging.logger {}
    private val yearMonthFormatter = DateTimeFormatter.ofPattern("yyyy-MM")

    @Bean
    fun paymentFlatFileReader(): FlatFileItemReader<Payment> {
        return FlatFileItemReaderBuilder<Payment>()
            .name("paymentFlatFileReader")
            .resource(ClassPathResource("payment_all.csv"))
            .linesToSkip(1) // header
            .lineMapper(DefaultLineMapper<Payment>().apply {
                setLineTokenizer(DelimitedLineTokenizer(",").apply {
                    setNames("id", "sellerId", "productId", "productName", "price", "paymentDate")
                })
                setFieldSetMapper(PaymentFieldSetMapper())
            })
            .encoding("UTF-8")
            .targetType(Payment::class.java)
            .build()
    }

    // ItemWriter (집계된 데이터 저장)
    @Bean
    @Transactional
    fun settlementWriter(): ItemWriter<Payment> {
        return ItemWriter { items ->
            // 일별 집계
//            val dailySettlement = items.groupingBy { item ->
//                Pair(item.sellerId, item.paymentDate.toLocalDate())
//            }.fold(BigDecimal.ZERO) { acc, payment ->
//                acc + payment.price
//            }.map {
//                SettlementDaily(
//                    id = TODO(),
//                    sellerId = TODO(),
//                    settlementDate = TODO(),
//                    totalAmount = TODO(),
//                    createAt = TODO(),
//                    updateAt = TODO()
//                )
//            }.toList()

            items.forEach { item ->
                val date = item.paymentDate.toLocalDate()
                val existingDaily = entityManager.createQuery(
                        "SELECT s FROM SettlementDaily s WHERE s.sellerId = :sellerId AND s.settlementDate = :date",
                        SettlementDaily::class.java
                    ).setParameter("date", date)
                    .setParameter("sellerId", item.sellerId)
                    .resultList
                    .firstOrNull()

                if (existingDaily != null) {
                    existingDaily.totalAmount = existingDaily.totalAmount.add(item.price)
                    entityManager.merge(existingDaily)
                } else {
                    val settlementDaily = SettlementDaily(
                        sellerId = item.sellerId,
                        settlementDate = date,
                        totalAmount = item.price
                    )
                    entityManager.persist(settlementDaily)
                }

                // 월별 집계 처리
                val month = date.format(yearMonthFormatter)
                val existingMonthly = entityManager.createQuery(
                    "SELECT s FROM SettlementMonthly s WHERE s.sellerId = :sellerId AND s.settlementDate = :month",
                        SettlementMonthly::class.java
                    ).setParameter("sellerId", item.sellerId)
                        .setParameter("month", month)
                        .resultList
                        .firstOrNull()

                if (existingMonthly != null) {
                    existingMonthly.totalAmount = existingMonthly.totalAmount.add(item.price)
                    entityManager.merge(existingMonthly)
                } else {
                    val settlementMonthly = SettlementMonthly(
                        sellerId = item.sellerId,
                        settlementDate = month,
                        totalAmount = item.price
                    )
                    entityManager.persist(settlementMonthly)
                }
            }
            logger.info { "write to ${items.size()}" }
        }
    }

    @Bean
    fun settlementReadProcessStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
    ): Step {
        return StepBuilder("settlementReadProcessStep", jobRepository)
            .chunk<Payment, Payment>(chunkSize, transactionManager)
            .reader(paymentFlatFileReader())
//            .processor(settlementProcessor()) // 집계 처리
            .writer(settlementWriter()) // 결과 저장
            .build()
    }

    @Bean
    fun settlementJob(settlementReadProcessStep: Step, jobRepository: JobRepository): Job {
        return JobBuilder("settlementJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(settlementReadProcessStep)
            .build()
    }

}

class PaymentFieldSetMapper : FieldSetMapper<Payment> {
    override fun mapFieldSet(fieldSet: FieldSet): Payment {
        return Payment(
            id = fieldSet.readLong("id"),  // "id"는 CSV 파일에서 해당 컬럼의 이름
            sellerId = fieldSet.readLong("sellerId"),
            productId = fieldSet.readLong("productId"),
            productName = fieldSet.readString("productName"),
            price = fieldSet.readBigDecimal("price"),
            paymentDate = LocalDateTime.parse(fieldSet.readString("paymentDate"), DateTimeFormatter.ISO_DATE_TIME)
        )
    }
}