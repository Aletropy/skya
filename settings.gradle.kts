plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
rootProject.name = "Skya"

include("skya-api", "skya-processor", "skya-menus", "skya-plugin")