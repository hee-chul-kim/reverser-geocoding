package com.example.daily

import com.example.common.config.DatabaseConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Import

@SpringBootApplication
@Import(DatabaseConfig::class)
class DailyApplication

fun main(args: Array<String>) {
    runApplication<DailyApplication>(*args)
} 