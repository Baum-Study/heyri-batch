package com.hyeri.hyeribatch.chapter09.task

import com.hyeri.hyeribatch.chapter08.After20YearsItemProcessor
import com.hyeri.hyeribatch.chapter08.LowerCaseItemProcessor
import com.hyeri.hyeribatch.common.ChunkLoggingListener
import com.hyeri.hyeribatch.common.domain.customer.Customer
import com.hyeri.hyeribatch.common.domain.product.DeliveryStatus
import com.hyeri.hyeribatch.common.domain.product.Product
import com.hyeri.hyeribatch.common.domain.product.ProductRepository
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
import org.springframework.transaction.PlatformTransactionManager
import java.time.LocalDateTime

@Configuration
class MybatisProductConfiguration(
    private val sqlSessionFactory: SqlSessionFactory,
    private val chunkLoggingListener: ChunkLoggingListener
) {
    private val chunkSize = 1_000
    private val queryIdPrefix = "com.hyeri.hyeribatch.mapper.ProductMapper"

    @Bean
    fun productItemReader(): MyBatisPagingItemReader<Product> {
        return MyBatisPagingItemReaderBuilder<Product>()
            .sqlSessionFactory(sqlSessionFactory)
            .pageSize(chunkSize)
            .queryId("${queryIdPrefix}.selectProduct")
            .build()
    }

    @Bean
    fun productItemWriter(): MyBatisBatchItemWriter<Product> {
        return MyBatisBatchItemWriterBuilder<Product>()
            .sqlSessionFactory(sqlSessionFactory)
            .statementId("${queryIdPrefix}.updateDeliveryStatus")
            .build()
    }

    @Bean
    fun mybatisProductStep(jobRepository: JobRepository, transactionManager: PlatformTransactionManager): Step {
        return StepBuilder("mybatisProductStep", jobRepository)
            .chunk<Product, Product>(chunkSize, transactionManager)
            .reader(productItemReader())
            .processor { item ->
                item.copy(
                    deliveryStatus = DeliveryStatus.AUTO_COMPLETE
                )
            }
            .writer(productItemWriter())
            .listener(chunkLoggingListener)
            .build()
    }

    @Bean
    fun mybatisProductJob(mybatisProductStep: Step, jobRepository: JobRepository): Job {
        return JobBuilder("mybatisProductJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(mybatisProductStep)
            .build()
    }
}