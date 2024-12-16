package com.hyeri.hyeribatch.task

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
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource

//@Configuration
class PaymentWriterConfiguration(
    private val dataSource: DataSource,
) {
    private val logger = KotlinLogging.logger {}
    private val chunkSize = 1000
    private val fetchSize = 5000

    @Bean
    fun queryProvider(): PagingQueryProvider {
        return SqlPagingQueryProviderFactoryBean().apply {
            setDataSource(dataSource)
            setSelectClause("select ID, SELLER_ID, PRODUCT_ID, PRODUCT_NAME, PRICE, PAYMENT_DATE")
            setFromClause("from PAYMENT")
            setSortKeys(mutableMapOf(
                "ID" to Order.ASCENDING
            ))
        }.getObject()
    }

    @Bean
    fun paymentPagingItemReader(): JdbcPagingItemReader<Payment> {
        return JdbcPagingItemReaderBuilder<Payment>()
            .name("paymentJdbcItemReader")
            .dataSource(dataSource)
            .fetchSize(fetchSize)
            .queryProvider(queryProvider())
            .rowMapper(BeanPropertyRowMapper(Payment::class.java))
            .build()
    }

    @Bean
    fun paymentFlatFileItemWriter(): FlatFileItemWriter<Payment> {
        return FlatFileItemWriterBuilder<Payment>()
            .name("paymentFlatFileItemWriter")
            .resource(FileSystemResource("./output/chapter05/payment_all-back.csv"))
            .encoding("UTF-8")
            .delimited().delimiter(",").names(
                "id", "sellerId", "productId", "productName", "price", "paymentDate"
            )
            .headerCallback { writer ->
                writer.write("PK,판매자 아이디,상품 아이디,상품명,금액,결제일자")
            }
            .build()
    }

    @Bean
    fun chunkLoggingListener(): ChunkListener {
        return object : ChunkListener {
            override fun afterChunk(context: ChunkContext) {
                val stepExecution = context.stepContext.stepExecution
                logger.info {
                    "Chunk 완료 - 읽은 수: ${stepExecution.readCount}, 작성한 수: ${stepExecution.writeCount}, 누적 처리 수: ${stepExecution.readSkipCount + stepExecution.writeSkipCount}"
                }
            }
        }
    }

    @Bean
    fun paymentJdbcPagingStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        logger.info { "------------------ Init paymentJdbcPagingStep -----------------" }

        return StepBuilder("PaymentJdbcPagingStep", jobRepository)
            .chunk<Payment, Payment>(chunkSize, transactionManager)
            .reader(paymentPagingItemReader())
            .writer(paymentFlatFileItemWriter())
            .listener(chunkLoggingListener())
            .build()
    }

    @Bean
    fun paymentJdbcPagingJob(paymentJdbcPagingStep: Step, jobRepository: JobRepository): Job {
        logger.info { "------------------ Init paymentJdbcPagingJob -----------------" }
        return JobBuilder("PAYMENT_JDBC_PAGING_CHUNK_JOB", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(paymentJdbcPagingStep)
            .build()
    }
}