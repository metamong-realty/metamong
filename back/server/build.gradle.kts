plugins {
    kotlin("kapt")
    kotlin("plugin.jpa")
    id("com.adarshr.test-logger") version "3.2.0"
    id("jacoco")
}

dependencies {
    /** common module */
    implementation(project(":back:common"))
    /** spring */
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.redisson:redisson-spring-boot-starter:3.34.1")

    /** kotlin */
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")

    /** database */
    implementation("mysql:mysql-connector-java:8.0.33")
    runtimeOnly("com.h2database:h2")
    implementation("jakarta.persistence:jakarta.persistence-api")

    /** querydsl */
    val queryDslVersion = "5.1.0:jakarta"
    implementation("com.querydsl:querydsl-jpa:$queryDslVersion")
    kapt("com.querydsl:querydsl-apt:$queryDslVersion")

    /** mapstruct */
    val mapstructVersion = "1.5.2.Final"
    implementation("org.mapstruct:mapstruct:$mapstructVersion")
    kapt("org.mapstruct:mapstruct-processor:$mapstructVersion")

    /** JWT */
    val jjwtVersion = "0.11.2"
    implementation("io.jsonwebtoken:jjwt-api:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwtVersion")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwtVersion")

    /** logging */
    implementation("io.github.oshai:kotlin-logging-jvm:5.1.1")

    /** aws */
    implementation("software.amazon.awssdk:s3:2.31.68")
    implementation("software.amazon.awssdk:core:2.31.68")

    /** swagger */
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

    /** test */
    testImplementation(testFixtures(project(":back:common")))
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.batch:spring-batch-test")
    testImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testImplementation("io.mockk:mockk:1.13.12")
    testImplementation("com.ninja-squad:springmockk:4.0.2")
    testImplementation("org.testcontainers:mysql")
    testImplementation("org.testcontainers:mongodb")
    testImplementation("com.appmattus.fixture:fixture:1.2.0")
}

tasks.bootJar {
    archiveFileName.set("metamong-server.jar")
}
