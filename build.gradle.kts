plugins {
    id("java")
}

group = "dev.thesourcecode"
version = "2.0.0"

layout.buildDirectory = file("target")

repositories {
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}
