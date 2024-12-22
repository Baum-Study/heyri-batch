package com.hyeri.hyeribatch.chapter05.task

import com.hyeri.hyeribatch.common.ChunkLoggingListener
import com.hyeri.hyeribatch.entity.Payment
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.ChunkListener
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.scope.context.ChunkContext
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.Order
import org.springframework.batch.item.database.PagingQueryProvider
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

@Configuration
class PaymentFlatFileConfiguration(
    private val dataSource: DataSource,
) {
    private val logger = KotlinLogging.logger {}
    private val chunkSize = 1000
    private val fetchSize = 5000

    @Bean
    fun paymentFlatFileReader(): FlatFileItemReader<Payment> {
        return FlatFileItemReaderBuilder<Payment>()
            .name("paymentFlatFileReader")
            .resource(ClassPathResource("payment_all.csv"))
            .linesToSkip(1)
            .lineMapper(PaymentCsvContext.csvLineMapper)
            .encoding("UTF-8")
            .targetType(Payment::class.java)
            .build()
    }

    @Bean
    fun paymentQueryProvider(): PagingQueryProvider {
        return SqlPagingQueryProviderFactoryBean().apply {
            setDataSource(dataSource)
            setSelectClause("select ID, SELLER_ID, PRODUCT_ID, PRODUCT_NAME, PRICE, PAYMENT_DATE")
            setFromClause("from PAYMENT")
            setSortKeys(
                mapOf("ID" to Order.ASCENDING)
            )
        }.getObject()
    }

    @Bean
    fun paymentPagingItemReader(): JdbcPagingItemReader<Payment> {
        return JdbcPagingItemReaderBuilder<Payment>()
            .name("paymentJdbcItemReader")
            .dataSource(dataSource)
            .fetchSize(fetchSize)
            .queryProvider(paymentQueryProvider())
            .rowMapper(BeanPropertyRowMapper(Payment::class.java))
            .build()
    }

    @Bean
    fun paymentFlatFileItemWriter(): FlatFileItemWriter<Payment> {
        return FlatFileItemWriterBuilder<Payment>()
            .name("paymentFlatFileItemWriter")
            .resource(FileSystemResource("./output/chapter05/payment_all.csv"))
            .encoding("UTF-8")
            .delimited().delimiter(",").names(*PaymentCsvContext.headers)
            .headerCallback { writer ->
                writer.write(PaymentCsvContext.headers.joinToString(","))
            }
            .build()
    }

    @Bean
    fun paymentDbToFlatFileStep(
        chunkLoggingListener: ChunkLoggingListener,
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager
    ): Step {
        return StepBuilder("paymentDbToFlatFileStep", jobRepository)
            .chunk<Payment, Payment>(chunkSize, transactionManager)
            .reader(paymentPagingItemReader())
            .writer(paymentFlatFileItemWriter())
            .listener(chunkLoggingListener)
            .build()
    }

    @Bean
    fun paymentDbToFlatFileJob(paymentDbToFlatFileStep: Step, jobRepository: JobRepository): Job {
        return JobBuilder("paymentDbToFlatFileJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(paymentDbToFlatFileStep)
            .build()
    }
}