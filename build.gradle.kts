plugins {
    java
    groovy
    kotlin("jvm") version "1.5.30-RC"
    id("org.springframework.boot") version "2.5.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

springBoot {
    mainClass.set("com.example.workshops_IntelliJ_IDEA.AppRunner")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-reflect:1.5.21")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.5.21")

    implementation("org.springframework.boot:spring-boot-starter-web:2.5.3")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.3")

    testImplementation("org.spockframework:spock-core:1.3-groovy-2.5")
    testImplementation("org.spockframework:spock-spring:1.3-groovy-2.5")
    testImplementation("org.springframework.boot:spring-boot-starter-test:2.5.3")
}

tasks.compileKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}

tasks.compileTestKotlin {
    kotlinOptions {
        jvmTarget = "11"
    }
}
