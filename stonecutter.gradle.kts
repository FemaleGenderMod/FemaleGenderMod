import com.wildfire.ValidateJson

plugins {
    id("dev.kikugie.stonecutter")
    id("net.neoforged.moddev") version "2.0.143" apply false
    id("net.fabricmc.fabric-loom") version "1.17-SNAPSHOT" apply false
    id("me.modmuss50.mod-publish-plugin") version "2.2.0" apply false
}

stonecutter active "26.2"

tasks.register("generatePackageInfos") {
}

tasks.register<ValidateJson>("validateJson") {
    criticalFiles.from(
        "fabric/src/main/resources/fabric.mod.json",
        "fabric/src/main/resources/wildfire_gender.mixins.json"
    )
    rootTranslation.set(layout.projectDirectory.file(
        "fabric/versions/${stonecutter.current?.project}/src/main/generated/assets/wildfire_gender/lang/en_us.json"
    ))
    translationFiles.from(fileTree(layout.projectDirectory) {
        include("*/versions/*/src/main/generated/assets/wildfire_gender/lang/*.json")
        include("common/src/main/resources/assets/wildfire_gender/lang/*.json")
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
