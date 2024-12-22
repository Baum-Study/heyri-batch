package com.hyeri.hyeribatch.chapter05.task

import com.hyeri.hyeribatch.data.MonthlyPaymentDTO
import com.hyeri.hyeribatch.entity.Payment
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class MonthlySettlementConfiguration(
    private val dataSource: DataSource,
) {
    private val chunkSize = 1_000

    @Bean
    fun monthlySettlementWriter(): JdbcBatchItemWriter<MonthlyPaymentDTO> {
        return JdbcBatchItemWriterBuilder<MonthlyPaymentDTO>()
            .dataSource(dataSource)
            .sql("""
                MERGE INTO SETTLEMENT_MONTHLY AS t
                USING DUAL
                ON t.seller_id = :sellerId AND t.settlement_date = :date
                WHEN MATCHED THEN
                    UPDATE SET t.total_amount = t.total_amount + :price
                WHEN NOT MATCHED THEN
                    INSERT (seller_id, settlement_date, total_amount)
                    VALUES (:sellerId, :date, :price)
            """.trimIndent())
            .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
            .build()
    }

    @Bean
    fun monthlySettlementStep(
        paymentFlatFileReader: FlatFileItemReader<Payment>,
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
    ): Step {
        return StepBuilder("monthlySettlementStep", jobRepository)
            .chunk<Payment, MonthlyPaymentDTO>(chunkSize, transactionManager)
            .reader(paymentFlatFileReader)
            .processor(MonthlyPaymentDTO::of)
            .writer(monthlySettlementWriter())
            .build()
    }

    @Bean
    fun monthlySettlementJob(dailySettlementStep: Step, jobRepository: JobRepository): Job {
        return JobBuilder("monthlySettlementJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(dailySettlementStep)
            .build()
    }

}

