package com.example.api

import com.example.common.config.DatabaseConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.domain.EntityScan
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan
import org.springframework.context.annotation.Import
import org.springframework.data.jpa.repository.config.EnableJpaRepositories

@SpringBootApplication(scanBasePackages = ["com.example.api", "com.example.common"])
class ReverseGeocodingApiApplication

fun main(args: Array<String>) {
    runApplication<ReverseGeocodingApiApplication>(*args)
} 