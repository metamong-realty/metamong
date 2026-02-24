plugins {
    kotlin("kapt")
    kotlin("plugin.jpa")
    id("java-library")
    id("java-test-fixtures")
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
    implementation("org.springframework:spring-web")
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

dependencies {
    val springBootVersion = "3.3.0"

    /** testFixtures 의존성 */
    testFixturesImplementation(platform("org.springframework.boot:spring-boot-dependencies:$springBootVersion"))
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-test")
    testFixturesImplementation("org.springframework.boot:spring-boot-starter-data-jpa")
    testFixturesImplementation("io.kotest:kotest-runner-junit5:5.9.1")
    testFixturesImplementation("io.kotest:kotest-assertions-core:5.9.1")
    testFixturesImplementation("io.kotest.extensions:kotest-extensions-spring:1.3.0")
    testFixturesImplementation("io.mockk:mockk:1.13.12")
    testFixturesImplementation("com.ninja-squad:springmockk:4.0.2")
    testFixturesImplementation("com.h2database:h2")
    testFixturesImplementation("com.appmattus.fixture:fixture:1.2.0")
}
