plugins {
    kotlin("jvm") version "2.1.21" apply false
    kotlin("plugin.serialization") version "2.2.10" apply false
    id("com.google.devtools.ksp") version "2.2.0-2.0.2" apply false
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18" apply false
    id("com.gradleup.shadow") version "9.1.0" apply false
}

subprojects {
    group = "com.aletropy"
    version = "0.1.1"

    repositories {
        mavenCentral()
        maven {
            name = "papermc"
            url = uri("https://repo.papermc.io/repository/maven-public/")
        }
    }
}