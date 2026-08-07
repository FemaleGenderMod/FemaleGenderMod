import com.wildfire.ValidateJson
import org.gradle.kotlin.dsl.register

plugins {
    id("dev.kikugie.stonecutter")
    id("net.fabricmc.fabric-loom") version "1.16-SNAPSHOT" apply false
    id("me.modmuss50.mod-publish-plugin") version "1.1.0" apply false
}

stonecutter active "26.2"

tasks.register<ValidateJson>("validateJson") {
    criticalFiles.from(
        "src/main/resources/fabric.mod.json",
        "src/main/resources/wildfire_gender.mixins.json"
    )
    rootTranslation.set(layout.projectDirectory.file(
        "versions/${stonecutter.current?.project}/src/main/generated/assets/wildfire_gender/lang/en_us.json"
    ))
    translationFiles.from(fileTree(layout.projectDirectory) {
        stonecutter.versions.forEach { version ->
            include("versions/${version.project}/src/main/generated/assets/wildfire_gender/lang/*.json")
        }
        include("src/main/resources/assets/wildfire_gender/lang/*.json")

    })
    nonExhaustiveLocales.set(setOf(
        //Only generates the relevant overrides as missing lang entries fall back to en_us
        "en_au",
        "en_ca",
        "en_gb",
        //Exact matches such as "N" does not generate the duplicate file
        "en_ud"
    ))
}
