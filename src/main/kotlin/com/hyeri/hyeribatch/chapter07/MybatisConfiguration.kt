package com.hyeri.hyeribatch.chapter07

import com.hyeri.hyeribatch.chapter08.After20YearsItemProcessor
import com.hyeri.hyeribatch.chapter08.LowerCaseItemProcessor
import com.hyeri.hyeribatch.common.domain.customer.Customer
import org.apache.ibatis.session.SqlSessionFactory
import org.mybatis.spring.batch.MyBatisBatchItemWriter
import org.mybatis.spring.batch.MyBatisPagingItemReader
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.batch.item.support.CompositeItemProcessor
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.FileSystemResource
import org.springframework.transaction.PlatformTransactionManager


@Configuration
class MybatisConfiguration(
    private val sqlSessionFactory: SqlSessionFactory,
) {
    private val chunkSize = 1_000
    private val queryIdPrefix = "com.hyeri.hyeribatch.mapper.CustomerMapper"

    @Bean
    fun myBatisItemReader(): MyBatisPagingItemReader<Customer> {
        return MyBatisPagingItemReaderBuilder<Customer>()
            .sqlSessionFactory(sqlSessionFactory)
            .pageSize(chunkSize)
            .queryId("${queryIdPrefix}.selectCustomers")
            .parameterValues(mapOf("age" to 20))
            .build()
    }

    @Bean
    fun customerFlatFileItemWriter(): FlatFileItemWriter<Customer> {
        return FlatFileItemWriterBuilder<Customer>()
            .name("customerFlatFileItemWriter")
            .resource(FileSystemResource("./output/chapter08/customer_new.csv"))
            .encoding("UTF-8")
            .delimited().delimiter("\t")
            .names("name", "age", "gender")
            .build()
    }

    @Bean
    fun mybatisItemWriter(): MyBatisBatchItemWriter<Customer> {
        return MyBatisBatchItemWriterBuilder<Customer>()
            .sqlSessionFactory(sqlSessionFactory)
            .statementId("${queryIdPrefix}.insertCustomer2")
            .itemToParameterConverter({ item ->
                with(item) {
                    mapOf(
                        "name" to name,
                        "age" to age,
                        "gender" to gender,
                    )
                }
            })
            .build()
    }

    @Bean
    fun compositeItemProcessor(): CompositeItemProcessor<Customer, Customer> {
        return CompositeItemProcessorBuilder<Customer, Customer>()
            .delegates(listOf(
                LowerCaseItemProcessor(),
                After20YearsItemProcessor(),
            ))
            .build()
    }

    @Bean
    fun mybatisReaderStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        return StepBuilder("mybatisReaderStep", jobRepository)
            .chunk<Customer, Customer>(chunkSize, transactionManager)
            .reader(myBatisItemReader())
            .writer(customerFlatFileItemWriter())
            .build()
    }

    @Bean
    fun mybatisStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        return StepBuilder("mybatisStep", jobRepository)
            .chunk<Customer, Customer>(chunkSize, transactionManager)
            .reader(myBatisItemReader())
            .processor(compositeItemProcessor())
            .writer(customerFlatFileItemWriter())
            .build()
    }

    @Bean
    fun mybatisJob(mybatisStep: Step, jobRepository: JobRepository): Job {
        return JobBuilder("mybatisJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(mybatisStep)
            .build()
    }
}