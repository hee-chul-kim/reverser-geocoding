package com.example.daily.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "app.file")
class FileConfig {
    var baseDir: String = "data"
} 