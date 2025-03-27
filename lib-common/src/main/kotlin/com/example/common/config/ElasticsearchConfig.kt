package com.example.common.config

import co.elastic.clients.elasticsearch.ElasticsearchClient
import co.elastic.clients.json.jackson.JacksonJsonpMapper
import co.elastic.clients.transport.rest_client.RestClientTransport
import org.apache.http.HttpHost
import org.apache.http.auth.AuthScope
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.impl.client.BasicCredentialsProvider
import org.elasticsearch.client.RestClient
import org.elasticsearch.client.RestClientBuilder
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@EnableConfigurationProperties(ElasticsearchProperties::class)
open class ElasticsearchConfig(
    private val properties: ElasticsearchProperties
) {

    @Bean
    open fun elasticsearchClient(): ElasticsearchClient {
        val restClientBuilder = RestClient.builder(
            HttpHost(properties.host, properties.port, properties.scheme)
        )

        configureRestClientBuilder(restClientBuilder)

        val restClient = restClientBuilder.build()
        val transport = RestClientTransport(restClient, JacksonJsonpMapper())

        return ElasticsearchClient(transport)
    }

    private fun configureRestClientBuilder(builder: RestClientBuilder) {
        // 기본 타임아웃 설정
        builder.setRequestConfigCallback { requestConfigBuilder ->
            requestConfigBuilder
                .setConnectTimeout(properties.connectTimeout)
                .setSocketTimeout(properties.socketTimeout)
        }

        // 최대 재시도 타임아웃 설정
        builder.setHttpClientConfigCallback { httpClientBuilder ->
            //httpClientBuilder.setMaxRetryTimeoutMillis(properties.maxRetryTimeoutMillis)

            // 인증 정보가 있는 경우에만 설정
            if (!properties.username.isNullOrBlank() && !properties.password.isNullOrBlank()) {
                val credentialsProvider = BasicCredentialsProvider()
                credentialsProvider.setCredentials(
                    AuthScope.ANY,
                    UsernamePasswordCredentials(properties.username, properties.password)
                )
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider)
            }

            httpClientBuilder
        }
    }
} 