package com.hyeri.hyeribatch.chapter05

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JdbcBatchItemWriter
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.transaction.PlatformTransactionManager
import javax.sql.DataSource


@Configuration
class Chapter05ConfigurationWriter(
    private val dataSource: DataSource,
) {

    private val logger = KotlinLogging.logger {}
    private val chunkSize = 1000
    private val encoding = "UTF-8"

    @Bean
    fun flatFileItemReader(): FlatFileItemReader<Customer> {
        return FlatFileItemReaderBuilder<Customer>()
            .name("FlatFileItemReader")
            .resource(ClassPathResource("./customer.csv"))
            .encoding(encoding)
            .delimited().delimiter(",")
            .names("name", "age", "gender")
            .targetType(Customer::class.java)
            .build()
    }

    @Bean
    fun jdbcItemWriter(): JdbcBatchItemWriter<Customer> {
        return JdbcBatchItemWriterBuilder<Customer>()
            .dataSource(dataSource)
            .sql("INSERT INTO customer2 (name, age, gender) VALUES (:name, :age, :gender)")
            .itemSqlParameterSourceProvider(CustomerItemSqlParameterSourceProvider())
            .build()
    }


    @Bean
    fun flatFileStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        logger.info { "------------------ Init flatFileStep -----------------" }

        return StepBuilder("flatFileStep", jobRepository)
            .chunk<Customer, Customer>(chunkSize, transactionManager)
            .reader(flatFileItemReader())
            .writer(jdbcItemWriter())
            .build()
    }

    @Bean
    fun flatFileJob(flatFileStep: Step, jobRepository: JobRepository): Job {
        logger.info { "------------------ Init flatFileJob -----------------" }
        return JobBuilder("JDBC_BATCH_WRITER_CHUNK_JOB", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(flatFileStep)
            .build()
    }
}