pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.fabricmc.net/")
        maven("https://maven.neoforged.net/releases/")
    }
}

plugins {
    id("dev.kikugie.stonecutter") version providers.gradleProperty("stonecutter_version").get()
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

val fabricVersions = providers.gradleProperty("stonecutter_enabled_fabric_versions").orNull?.split(",")?.map { it.trim() } ?: emptyList()
val neoforgeVersions = providers.gradleProperty("stonecutter_enabled_neoforge_versions").orNull?.split(",")?.map { it.trim() } ?: emptyList()
val commonVersions = listOf(fabricVersions, neoforgeVersions).flatten().distinct()

stonecutter {
    create(rootProject) {
        versions(commonVersions)
        branch("common") {
            versions(commonVersions)
        }
        branch("fabric") {
            versions(fabricVersions)
        }
        branch("neoforge") {
            versions(neoforgeVersions)
        }
    }
}

rootProject.name = "female-gender-mod"
