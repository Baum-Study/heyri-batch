package com.hyeri.hyeribatch.chapter4

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.file.FlatFileItemReader
import org.springframework.batch.item.file.FlatFileItemWriter
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.FileSystemResource
import org.springframework.transaction.PlatformTransactionManager
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

@Configuration
class Chapter04Configuration {

    private val log = KotlinLogging.logger {}
    private val encoding = "UTF-8"
    private val chunkSize = 100
    private val aggregateCustomers = ConcurrentHashMap<String, Int>()

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
    fun flatFileItemWriter(): FlatFileItemWriter<Customer> {
        return FlatFileItemWriterBuilder<Customer>()
            .name("FlatFileItemWriter")
            .resource(FileSystemResource("./output/cusomer_new.csv"))
            .encoding(encoding)
            .delimited().delimiter("\t")
            .names("Name", "Age", "Gender")
            .append(false)
            .lineAggregator(CustomLineAggregator())
            .headerCallback(CustomHeader())
            .footerCallback(CustomFooter(aggregateCustomers))
            .build()
    }

    @Bean
    fun flatFileStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        log.info { "------------------ init flatFileStep --------------------------" }

        return StepBuilder("flatFileStep", jobRepository)
            .chunk<Customer, Customer>(chunkSize, transactionManager)
            .reader(flatFileItemReader())
            .processor { item ->
                aggregateCustomers.putIfAbsent("TOTAL_CUSTOMERS", 0)
                aggregateCustomers.putIfAbsent("TOTAL_AGES", 0)

                aggregateCustomers["TOTAL_CUSTOMERS"] = aggregateCustomers["TOTAL_CUSTOMERS"]!! + 1
                aggregateCustomers["TOTAL_AGES"] = aggregateCustomers["TOTAL_AGES"]!! + item.age!!
                item
            }
            .writer(flatFileItemWriter())
            .build()
    }

    @Bean
    fun flatFileJob(flatFileStep: Step, jobRepository: JobRepository): Job {
        log.info { "--------------------- Init flatFileJob -----------------------------------" }

        return JobBuilder("FLAT_FILE_CHUNK_JOB", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(flatFileStep)
            .build()
    }
}