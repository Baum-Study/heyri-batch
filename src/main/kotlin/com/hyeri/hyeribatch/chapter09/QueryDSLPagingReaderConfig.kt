package com.hyeri.hyeribatch.chapter09

import com.hyeri.hyeribatch.common.chunkSize
import com.hyeri.hyeribatch.common.domain.customer.Customer
import com.hyeri.hyeribatch.common.domain.customer.QCustomer
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class QueryDSLPagingReaderConfig(
    private val entityManagerFactory: EntityManagerFactory,
    private val customItemWriter: CustomItemWriter,
) {
    private val jobName = "customerQueryDslPagingJob"

    @Bean
    fun customerQuerydslPagingItemReader(): QuerydslPagingItemReader<Customer> {
        return QuerydslPagingItemReader(
            name = "customerQuerydslPagingItemReader",
            entityManagerFactory = entityManagerFactory,
            querySupplier = { jpaQueryFactory ->
                jpaQueryFactory
                    .select(QCustomer.customer)
                    .from(QCustomer.customer)
                    .where(QCustomer.customer.age.gt(20))
            },
            alwaysReadFromZero = false,
            chunkSize = 2,
        )
    }

    @Bean
    fun customerQueryDslFlatFileItemWriter(): FlatFileItemWriter<Customer> {
        return FlatFileItemWriterBuilder<Customer>()
            .name("customerQueryDslFlatFileItemWriter")
            .resource(FileSystemResource("./output/chapter09/customer.csv"))
            .encoding("UTF-8")
            .delimited().delimiter("\t")
            .names("name", "age", "gender")
            .build()
    }

    @Bean
    fun customerQueryDslPagingStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
    ): Step {
        return StepBuilder("customerQueryDslPagingStep", jobRepository)
            .chunk<Customer, Customer>(chunkSize, transactionManager)
            .reader(customerQuerydslPagingItemReader())
            .writer(customItemWriter)
            .build()
    }

    @Bean
    fun customerQueryDslPagingJob(
        customerQueryDslPagingStep: Step,
        jobRepository: JobRepository,
    ): Job {
        return JobBuilder(jobName, jobRepository)
            .incrementer(RunIdIncrementer())
            .start(customerQueryDslPagingStep)
            .build()
    }
}