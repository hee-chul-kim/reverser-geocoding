plugins {
    kotlin("jvm")
    kotlin("plugin.spring")
    kotlin("plugin.jpa")
    id("org.springframework.boot")
    id("io.spring.dependency-management")
}

repositories {
    maven { url = uri("https://repo.osgeo.org/repository/release/") }
}

dependencies {
    implementation(project(":lib-common"))
    implementation("org.springframework.boot:spring-boot-starter:3.4.4")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa:3.4.4")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.15.3")

    // 테스트 의존성 추가
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.4.4")
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("io.mockk:mockk:1.13.10")
    testImplementation("com.h2database:h2:2.2.224")
}