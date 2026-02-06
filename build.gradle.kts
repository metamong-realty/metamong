import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.api.plugins.JavaPluginExtension

plugins {
    kotlin("jvm") version "2.0.0" apply false
    kotlin("plugin.spring") version "2.0.0" apply false
    kotlin("plugin.jpa") version "2.0.0" apply false
    kotlin("kapt") version "2.0.0" apply false
    id("org.springframework.boot") version "3.3.0" apply false
    id("io.spring.dependency-management") version "1.1.5" apply false
    id("org.jlleitschuh.gradle.ktlint") version "12.2.0" apply false
}

group = "com.metamong"
version = "0.0.1-SNAPSHOT"

allprojects {
    repositories {
        mavenCentral()
        maven("https://jitpack.io")
    }
}

subprojects {
    apply(plugin = "kotlin")
    apply(plugin = "kotlin-spring")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    
    // Spring Boot 플러그인은 실행 가능한 jar가 필요한 모듈에만 적용
    if (project.path == ":back:server" || project.path == ":back:batch") {
        apply(plugin = "org.springframework.boot")
    }

    configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
        version.set("1.5.0")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "21"
        }
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}