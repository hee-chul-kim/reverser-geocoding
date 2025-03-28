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

repositories {
    mavenCentral()
    maven { url = uri("https://repo.osgeo.org/repository/release/") }
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.2.2")
    implementation("org.postgresql:postgresql:42.7.1")
    implementation("org.hibernate.orm:hibernate-core:6.4.1.Final")
    implementation("com.zaxxer:HikariCP:5.1.0")
    implementation("org.flywaydb:flyway-core:10.7.1")

    implementation("co.elastic.clients:elasticsearch-java:8.17.4")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.17.0")

    implementation("org.locationtech.proj4j:proj4j:1.3.0")
    implementation("org.locationtech.proj4j:proj4j-epsg:1.3.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test:3.2.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.h2database:h2:2.2.224")
    testImplementation(kotlin("test"))
}

tasks {
    withType<BootJar> {
        enabled = false
    }

    withType<Test> {
        useJUnitPlatform()
    }
}
