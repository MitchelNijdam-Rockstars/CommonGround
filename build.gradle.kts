import com.github.gradle.node.npm.task.NpmTask

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.6"
    id("io.spring.dependency-management") version "1.1.7"
    id("com.github.node-gradle.node") version "7.0.2"
    kotlin("plugin.jpa") version "2.2.21"
}

group = "com.mitchelnijdam"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")

    runtimeOnly("org.postgresql:postgresql")

    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
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

// --- Build the angular frontend & copy into resources/static of the SB app
node {
    version.set("24.15.0")
    download.set(true)
    nodeProjectDir.set(file("${projectDir}/frontend"))
}

val buildAngular = tasks.register<NpmTask>("buildAngular") {
    dependsOn("npmInstall")
    args.set(listOf("run", "build"))

    // Caching: Only rerun if these files changed
    inputs.dir(file("${projectDir}/frontend/src"))
    inputs.dir(file("${projectDir}/frontend/public")) // If using Angular 18+
    outputs.dir(file("${projectDir}/frontend/dist"))
}

val copyAngular = tasks.register<Copy>("copyAngular") {
    dependsOn(buildAngular)
    from(file("${projectDir}/frontend/dist/frontend/browser"))
    into(layout.buildDirectory.dir("resources/main/static"))
}

tasks.processResources {
    dependsOn(copyAngular)
}