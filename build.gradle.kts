plugins {
  // Replace `<...>` with the plugin name appropriate for your target environment
    kotlin("jvm") version "2.3.0"
  // For example, if your target environment is JVM:
  // kotlin("jvm") version "2.3.0"
  // If your target is Kotlin Multiplatform:
  // kotlin("multiplatform") version "2.3.0"
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
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
