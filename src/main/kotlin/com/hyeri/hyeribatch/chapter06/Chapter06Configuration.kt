package com.hyeri.hyeribatch.chapter06

import com.hyeri.hyeribatch.chapter04.Customer
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManagerFactory
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.JpaPagingItemReader
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder
import org.springframework.batch.item.database.builder.JpaPagingItemReaderBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class Chapter06Configuration(
    private val entityManagerFactory: EntityManagerFactory
) {

    private val logger = KotlinLogging.logger {}
    private val chunkSize = 1_000

    @Bean
    fun customerJpaPagingItemReaderNoBuilder(): JpaPagingItemReader<Customer> {
        return JpaPagingItemReader<Customer>().apply {
            setQueryString(
                "SELECT c FROM Customer c WHERE c.age > :age ORDER BY c.id DESC"
            )
            setEntityManagerFactory(entityManagerFactory)
            pageSize = chunkSize
            setParameterValues(mapOf("age" to 20))
        }
    }

    @Bean
    fun customerJpaPagingItemReader(): JpaPagingItemReader<Customer> {
        return JpaPagingItemReaderBuilder<Customer>()
            .name("customerJpaPagingItemReader")
            .queryString("SELECT c FROM Customer c WHERE c.age > :age ORDER BY c.id DESC")
            .pageSize(chunkSize)
            .entityManagerFactory(entityManagerFactory)
            .parameterValues(mapOf("age" to 20))
            .build()
    }

    @Bean
    fun customerJpaFlatFileItemWriter(): FlatFileItemWriter<Customer> {
        return FlatFileItemWriterBuilder<Customer>()
            .name("customerJpaFlatFileItemWriter")
            .resource(FileSystemResource("./output/chapter06/customer.csv"))
            .encoding("UTF-8")
            .delimited().delimiter("\t").names("Name", "Age", "Gender")
            .build()
    }

    @Bean
    fun flatFileItemReader(): FlatFileItemReader<Customer> {
        return FlatFileItemReaderBuilder<Customer>()
            .name("flatFileItemReader")
            .resource(ClassPathResource("./customer.csv"))
            .encoding("UTF-8")
            .delimited().delimiter(", ").names("name", "age", "gender")
            .targetType(Customer::class.java)
            .build()
    }

    @Bean
    fun jpaItemWriter(): JpaItemWriter<Customer> {
        return JpaItemWriterBuilder<Customer>()
            .entityManagerFactory(entityManagerFactory)
            .usePersist(true)
            .build()
    }

    @Bean
    fun customerJpaPagingStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
    ): Step {
        return StepBuilder("customerJpaPagingStep", jobRepository)
            .chunk<Customer, Customer>(chunkSize, transactionManager)
            .reader(flatFileItemReader())
            .processor { item ->
                logger.info { "processor ------- ${item} " }
                item
            }
            .writer(jpaItemWriter())
            .build()
    }

    @Bean
    fun customerJpaPagingJob(customerJpaPagingStep: Step, jobRepository: JobRepository): Job {
        return JobBuilder("JPA_PAGING_CHUNK_JOB", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(customerJpaPagingStep)
            .build()
    }

}