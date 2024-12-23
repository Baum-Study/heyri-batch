package com.hyeri.hyeribatch.chapter05.task

import com.hyeri.hyeribatch.common.ChunkLoggingListener
import com.hyeri.hyeribatch.common.domain.payment.data.DailyPaymentDTO
import com.hyeri.hyeribatch.common.domain.payment.Payment
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
class DailySettlementConfiguration(
    private val dataSource: DataSource,
) {
    private val chunkSize = 1_000

    @Bean
    fun dailySettlementWriter(): JdbcBatchItemWriter<DailyPaymentDTO> {
        return JdbcBatchItemWriterBuilder<DailyPaymentDTO>()
            .dataSource(dataSource)
            .sql("""
                MERGE INTO SETTLEMENT_DAILY AS s
                USING DUAL
                ON s.seller_id = :sellerId AND s.settlement_date = :date
                WHEN MATCHED THEN
                    UPDATE SET s.total_amount = s.total_amount + :price
                WHEN NOT MATCHED THEN
                    INSERT (seller_id, settlement_date, total_amount)
                    VALUES (:sellerId, :date, :price)
            """.trimIndent())
            .itemSqlParameterSourceProvider(BeanPropertyItemSqlParameterSourceProvider())
            .build()
    }

    @Bean
    fun dailySettlementStep(
        chunkLoggingListener: ChunkLoggingListener,
        paymentFlatFileReader: FlatFileItemReader<Payment>,
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
    ): Step {
        return StepBuilder("dailySettlementStep", jobRepository)
            .chunk<Payment, DailyPaymentDTO>(chunkSize, transactionManager)
            .reader(paymentFlatFileReader)
            .processor { item -> DailyPaymentDTO.of(item) }
            .writer(dailySettlementWriter())
            .listener(chunkLoggingListener)
            .build()
    }

    @Bean
    fun dailySettlementJob(dailySettlementStep: Step, jobRepository: JobRepository): Job {
        return JobBuilder("dailySettlementJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(dailySettlementStep)
            .build()
    }

}

