package com.hyeri.hyeribatch.common.config

import com.hyeri.hyeribatch.chapter09.task.EnumTypeHandler
import com.hyeri.hyeribatch.common.domain.product.DeliveryStatus
import org.mybatis.spring.boot.autoconfigure.ConfigurationCustomizer
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MyBatisConfig {
    @Bean
    fun configurationCustomizer(): ConfigurationCustomizer {
        return ConfigurationCustomizer { configuration ->
            configuration.typeHandlerRegistry.register(DeliveryStatus::class.java, EnumTypeHandler(DeliveryStatus::class.java))
        }
    }
}
