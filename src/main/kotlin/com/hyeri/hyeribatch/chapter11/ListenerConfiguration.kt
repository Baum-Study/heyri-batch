package com.hyeri.hyeribatch.chapter11

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.transaction.PlatformTransactionManager

@Configuration
class ListenerConfiguration {

    private val logger = KotlinLogging.logger{ }

    @Bean
    fun jobExecutionListener() = object : JobExecutionListener {
        override fun beforeJob(jobExecution: JobExecution) {
            logger.info { "before execution ${jobExecution.jobInstance.jobName}" }
        }

        override fun afterJob(jobExecution: JobExecution) {
            logger.info { "after execution ${jobExecution.jobInstance.jobName}" }
        }
    }

    @Bean
    fun stepExecutionListener() = object : StepExecutionListener {
        override fun beforeStep(stepExecution: StepExecution) {
            logger.info { "before execution ${stepExecution.stepName}" }
        }

        override fun afterStep(stepExecution: StepExecution): ExitStatus {
            logger.info { "after execution ${stepExecution.stepName}" }
            return stepExecution.exitStatus
        }
    }

    @Bean
    fun listenerStep(
        jobRepository: JobRepository,
        transactionManager: PlatformTransactionManager,
    ): Step {
        return StepBuilder("listenerStep", jobRepository)
            .tasklet({ _, _ ->
                logger.info{ "Execute listenerStep Tasklet ..." }
                RepeatStatus.FINISHED
            }, transactionManager)
            .listener(stepExecutionListener())
            .build()
    }

    @Bean
    fun listenerJob(
        jobRepository: JobRepository,
        listenerStep: Step,
    ): Job {
        return JobBuilder("listenerJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(listenerStep)
            .listener(jobExecutionListener())
            .build()
    }

}