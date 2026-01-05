plugins {
    kotlin("jvm") version "2.1.0"
    id("com.gradleup.shadow") version "9.0.0-beta12"
}

group = "dev.osh"
version = "0.1.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    implementation(kotlin("stdlib"))
    implementation("io.javalin:javalin:6.7.0")
    implementation("tools.jackson.core:jackson-databind:3.0.3")
    implementation("tools.jackson.module:jackson-module-kotlin:3.0.3")
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks.shadowJar {
    archiveClassifier.set("")
    mergeServiceFiles()
    
    exclude("META-INF/LICENSE*")
    exclude("META-INF/NOTICE*")
    exclude("META-INF/*-LICENSE")
    exclude("META-INF/*-NOTICE")
    exclude("META-INF/DEPENDENCIES")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
