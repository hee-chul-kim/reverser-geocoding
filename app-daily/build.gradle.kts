plugins {
    application
}

dependencies {
    implementation(project(":lib-common"))
    implementation("org.springframework.boot:spring-boot-starter:3.2.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.15.3")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.15.3")
}

application {
    mainClass.set("com.example.daily.DailyApplicationKt")
} 