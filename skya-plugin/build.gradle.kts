plugins {
    id("java")
    kotlin("jvm") version "2.1.21"
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.18"
    id("com.gradleup.shadow") version "9.1.0"
    kotlin("plugin.serialization") version "2.2.10"
    id("com.google.devtools.ksp")
}

dependencies {
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")

    ksp(project(":skya-processor"))
    implementation(project(":skya-api"))
	implementation(project(":skya-menus"))

    paperweight.paperDevBundle("1.21.8-R0.1-SNAPSHOT")
}

tasks {
    shadowJar {
        relocate("kotlin", "${project.group}.${project.name.lowercase()}.lib.kotlin")
    }

    val deploy by registering(Exec::class) {
        dependsOn(shadowJar)
        group = "deployment"
        description = "Deploys the plugin to the sandbox server."
        commandLine(
            "wsl",
            "rsync",
            "-avz",
            "-e",
            "ssh",
            "/mnt/n/Projects/skya/skya-plugin/build/libs/skya-plugin-0.1.1-all.jar",
            "sandbox-server:~/Server/plugins/Skya-0.1.1.jar"
        )
    }

    build {
        finalizedBy(deploy)
        dependsOn(shadowJar)
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
    jvmToolchain(21)
}

ksp {
    arg("moduleName", "skya-plugin")
}