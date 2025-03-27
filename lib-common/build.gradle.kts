import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

apply(plugin = "kotlin-jpa")

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.2")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.hibernate.orm:hibernate-core:6.4.1.Final")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.flywaydb:flyway-core:10.7.1")

    implementation("co.elastic.clients:elasticsearch-java:8.17.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")
}

tasks {
    withType<BootJar> {
        enabled = false
    }
}
