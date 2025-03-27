package com.example.common.config

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(basePackages = ["com.example.common.repository"])
class DatabaseConfig {

    @Bean
    fun dataSource(): DataSource {
        val config = HikariConfig().apply {
            driverClassName = "org.postgresql.Driver"
            jdbcUrl = "jdbc:postgresql://localhost:5432/t1100555"
            username = "t1100555"
            password = ""
            maximumPoolSize = 10
        }
        return HikariDataSource(config)
    }

    @Bean
    fun entityManagerFactory(): LocalContainerEntityManagerFactoryBean {
        val vendorAdapter = HibernateJpaVendorAdapter().apply {
            setGenerateDdl(true)
            setShowSql(true)
        }

        return LocalContainerEntityManagerFactoryBean().apply {
            dataSource = dataSource()
            setPackagesToScan("com.example.common.entity")
            jpaVendorAdapter = vendorAdapter
            setJpaProperties(hibernateProperties())
        }
    }

    @Bean
    fun transactionManager(): PlatformTransactionManager {
        return JpaTransactionManager(entityManagerFactory().`object`!!)
    }

    private fun hibernateProperties() = java.util.Properties().apply {
        setProperty("hibernate.hbm2ddl.auto", "update")
        setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect")
        setProperty("hibernate.format_sql", "true")
    }
} 