plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    kotlin("plugin.jpa") version "1.9.25"
    kotlin("kapt") version "1.9.25"
    id("org.springframework.boot") version "3.3.5"
    id("io.spring.dependency-management") version "1.1.6"
}

configurations {
    compileOnly {
        extendsFrom(configurations.annotationProcessor.get())
    }
}

group = "com.hyeri"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-batch")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.springframework.batch:spring-batch-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("com.h2database:h2:2.2.224")
    implementation("io.github.oshai:kotlin-logging-jvm:7.0.0")
    implementation("org.mybatis.spring.boot:mybatis-spring-boot-starter:3.0.4")
    implementation("org.springframework.boot:spring-boot-starter-web")

    // easyRandom
    testImplementation("org.jeasy:easy-random-core:5.0.0")

    // querydsl
    implementation("com.querydsl:querydsl-jpa:5.1.0:jakarta")
    implementation("com.querydsl:querydsl-apt:5.1.0:jakarta")
    implementation("jakarta.persistence:jakarta.persistence-api")
    implementation("jakarta.annotation:jakarta.annotation-api")

    kapt("com.querydsl:querydsl-apt:5.1.0:jakarta")
    kapt("org.springframework.boot:spring-boot-configuration-processor")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}