package com.hyeri.hyeribatch.chapter10

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.batch.core.ExitStatus
import org.springframework.batch.core.Job
import org.springframework.batch.core.Step
import org.springframework.batch.core.job.builder.JobBuilder
import org.springframework.batch.core.launch.support.RunIdIncrementer
import org.springframework.batch.core.repository.JobRepository
import org.springframework.batch.core.step.builder.StepBuilder
import org.springframework.batch.repeat.RepeatStatus
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.transaction.PlatformTransactionManager


@Configuration
class FlowControlConfiguration(
    val transactionManager: PlatformTransactionManager
) {

    val logger = KotlinLogging.logger {}

    @Bean(name = ["step01"])
    fun step01(jobRepository: JobRepository): Step {
        logger.info{ "------------------ Init step01  -----------------" }

        return StepBuilder("step01", jobRepository)
            .tasklet({ _, _ ->
                logger.info {"Execute Step 01 Tasklet ..." }
                throw RuntimeException("Step01 Error")
            }, transactionManager)
            .build()
    }

    @Bean(name = ["step02"])
    fun step02(jobRepository: JobRepository): Step {
        logger.info{ "------------------ Init step02 -----------------" }

        return StepBuilder("step02", jobRepository)
            .tasklet({ _, _ ->
                logger.info{ "Execute Step 02 Tasklet ..." }
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    @Bean(name = ["step03"])
    fun step03(jobRepository: JobRepository): Step {
        logger.info{ "------------------ Init step03 -----------------" }

        return StepBuilder("step03", jobRepository)
            .tasklet({ _, _ ->
                logger.info{ "Execute Step 03 Tasklet ..." }
                RepeatStatus.FINISHED
            }, transactionManager)
            .build()
    }

    @Bean
    fun flowControlJob(step01: Step, step02: Step, step03: Step, jobRepository: JobRepository): Job {
        logger.info { "------------------ Init myJob -----------------" }
        return JobBuilder("flowControlJob", jobRepository)
            .incrementer(RunIdIncrementer())
            .start(step01)
            .next(step02)
            .on(ExitStatus.FAILED.exitCode)
            .to(step03)
            .end()
            .build()
    }

}