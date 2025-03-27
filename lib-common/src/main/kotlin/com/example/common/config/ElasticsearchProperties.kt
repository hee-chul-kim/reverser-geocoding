package com.example.common.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "elasticsearch")
data class ElasticsearchProperties(
    val host: String = "localhost",
    val port: Int = 9200,
    val scheme: String = "http",
    val username: String? = null,
    val password: String? = null,
    val connectTimeout: Int = 5000,
    val socketTimeout: Int = 60000,
    val maxRetryTimeoutMillis: Int = 60000,
    val indexSettings: IndexSettings = IndexSettings()
) {
    data class IndexSettings(
        val numberOfShards: Int = 1,
        val numberOfReplicas: Int = 1
    )
} 