plugins {
    kotlin("kapt")
    kotlin("plugin.jpa")
    id("java-library")
}

dependencies {
    // Spring Boot dependency management를 사용하여 버전 관리
    val springBootVersion = "3.3.0"
    implementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))

    /** kotlin */
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    /** spring */
    implementation("org.springframework:spring-context")
    implementation("org.springframework.data:spring-data-commons")
    implementation("org.springframework.data:spring-data-jpa")

    /** database */
    implementation("jakarta.persistence:jakarta.persistence-api")

    /** jackson */
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.1")

    /** querydsl */
    val queryDslVersion = "5.1.0"
    implementation("com.querydsl:querydsl-jpa:$queryDslVersion:jakarta")
    implementation("com.querydsl:querydsl-core:$queryDslVersion")
    kapt("com.querydsl:querydsl-apt:$queryDslVersion:jakarta")
}

// common 모듈은 bootJar가 필요 없음
tasks.jar {
    enabled = true
}
