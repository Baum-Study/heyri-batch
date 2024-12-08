package com.hyeri.hyeribatch.chapter05

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JdbcPagingItemReader
import org.springframework.batch.item.database.Order
import org.springframework.batch.item.database.PagingQueryProvider
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.boot.autoconfigure.batch.BatchProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.jdbc.core.BeanPropertyRowMapper
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource


@Configuration
class Chapter05Configuration(
    private val dataSource: DataSource,
) {

    private val logger = KotlinLogging.logger {}
    private val chunkSize = 1000


    @Bean
    fun queryProvider(): PagingQueryProvider {
        return SqlPagingQueryProviderFactoryBean().apply {
            setDataSource(dataSource)
            setSelectClause("select id, name, age, gender")
            setFromClause("from customer")
            setWhereClause("where age >= :age")
            setSortKeys(mutableMapOf(
                "id" to Order.DESCENDING,
            ))
        }.getObject()
    }

    @Bean
    fun jdbcPagingItemReader(): JdbcPagingItemReader<Customer> {
        val parameterValue = mapOf(
            "age" to 20
        )

        return JdbcPagingItemReaderBuilder<Customer>()
            .name("jdbcPagingItemReader")
            .fetchSize(chunkSize)
            .dataSource(dataSource)
            .rowMapper(BeanPropertyRowMapper(Customer::class.java))
            .queryProvider(queryProvider())
            .parameterValues(parameterValue)
            .build()
    }

    @Bean
    fun customerFlatFileItemWriter(): FlatFileItemWriter<Customer> {
        return FlatFileItemWriterBuilder<Customer>()
            .name("customerFlatFileItemWriter")
            .resource(FileSystemResource("./output/customer_new_v1.csv"))
            .encoding("UTF-8")
            .delimited().delimiter("\t")
            .names("name", "age", "gender")
            .build()
    }


    @Bean
    @Throws(Exception::class)
    fun customerJdbcPagingStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        logger.info { "------------------ Init customerJdbcPagingStep -----------------" }

        return StepBuilder("customerJdbcPagingStep", jobRepository)
            .chunk<Customer, Customer>(chunkSize, transactionManager)
            .reader(jdbcPagingItemReader())
            .processor { item ->
                logger.info { "processor: ${item.name}, ${item.age}, ${item.gender}" }
                item
            }
            .writer(customerFlatFileItemWriter())
            .build()
    }

    @Bean
    fun customerJdbcPagingJob(customerJdbcPagingStep: Step, jobRepository: JobRepository): Job {
        logger.info { "------------------ Init customerJdbcPagingJob -----------------" }
        return JobBuilder("JDBC_PAGING_CHUNK_JOB", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(customerJdbcPagingStep)
            .build()
    }

}