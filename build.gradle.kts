plugins {
    java
    groovy
    kotlin("jvm") version "2.0.21"
    id("org.springframework.boot") version "3.3.4"
    id("io.spring.dependency-management") version "1.1.6"
    id("org.jetbrains.kotlin.plugin.spring") version "2.0.21"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_21
}

springBoot {
    mainClass.set("com.example.workshops_IntelliJ_IDEA.AppRunner")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib")

    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    testImplementation("org.spockframework:spock-core:2.3-groovy-4.0")
    testImplementation("org.spockframework:spock-spring:2.3-groovy-4.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.jupiter")
        exclude(group = "org.junit.vintage")
    }
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "21"
    }
}

tasks.test {
    // Remove useJUnitPlatform() since we're using Spock Framework
    testLogging {
        events("passed", "skipped", "failed")
    }
}

tasks.wrapper {
    gradleVersion = "8.10.2"
}