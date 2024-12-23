package com.hyeri.hyeribatch.chapter06.task

import com.hyeri.hyeribatch.common.ChunkLoggingListener
import com.hyeri.hyeribatch.common.domain.member.Member
import com.hyeri.hyeribatch.common.domain.member.MemberStatus
import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.criteria.CriteriaBuilder
import jakarta.persistence.criteria.CriteriaQuery
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.item.Chunk
import org.springframework.batch.item.database.JpaCursorItemReader
import org.springframework.batch.item.database.JpaItemWriter
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class JpaCursorConfiguration(
    private val entityManagerFactory: EntityManagerFactory,
) {

    private val logger = KotlinLogging.logger {}

    @Bean
    fun jpaCursorJob(
        jobRepository: JobRepository,
        jpaCursorStep: Step
    ): Job {
        return JobBuilder("deleteUserJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(jpaCursorStep)
            .build()
    }

    @Bean
    fun jpaCursorStep(
        transactionManager: PlatformTransactionManager,
        jobRepository: JobRepository,
        chunkLoggingListener: ChunkLoggingListener,
    ): Step {
        return StepBuilder("jpaCursorStep", jobRepository)
            .chunk<Member, Member>(1_000, transactionManager)
            .reader(jpaCursorItemReader())
            .writer(jpaDeleteItemWriter())
            .listener(chunkLoggingListener)
            .build()
    }

    @Bean
    fun jpaCursorItemReader(): JpaCursorItemReader<Member> {
        return JpaCursorItemReaderBuilder<Member>()
            .name("jpaCursorItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("SELECT m FROM Member m WHERE m.status = :status")
            .parameterValues(mapOf("status" to MemberStatus.INACTIVE))
            .build()
    }

    @Bean
    fun jpaDeleteItemWriter(): JpaDeleteItemWriter<Member> = JpaDeleteItemWriter(entityManagerFactory)
}

class JpaDeleteItemWriter<T>(entityManagerFactory: EntityManagerFactory) : JpaItemWriter<T>() {

    init {
        setEntityManagerFactory(entityManagerFactory)
        setUsePersist(false)
        setClearPersistenceContext(true)
    }

    override fun doWrite(entityManager: EntityManager, items: Chunk<out T>) {
        if (logger.isInfoEnabled) {
            logger.info("Writing to JPA with " + items.size() + " items.")
        }

        if (!items.isEmpty) {
            var deletedToContextCount = 0L
            val chunkIterator = items.iterator()

            for(item: T in chunkIterator) {

                entityManager.remove(
                    entityManager.merge(item)
                )
                ++deletedToContextCount
            }

            if (logger.isInfoEnabled) {
                logger.info("${deletedToContextCount} entities removed.")
                val size = items.size().toLong()
                if(size > deletedToContextCount) {
                    logger.info (
                        "${size - deletedToContextCount} entities found in persistence context."
                    )
                }
            }
        }
    }
}