/*
 * Wildfire's Female Gender Mod is a female gender mod created for Minecraft.
 * Copyright (C) 2023-present WildfireRomeo
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

import gradle.kotlin.dsl.accessors._3c984467cfe6063166439ec0710b6c00.main
import gradle.kotlin.dsl.accessors._3c984467cfe6063166439ec0710b6c00.publishing
import gradle.kotlin.dsl.accessors._3c984467cfe6063166439ec0710b6c00.sourceSets

plugins {
    `java-library`
    `maven-publish`
}

base {
    //TODO: This is what jared's multiloader template uses (but do we care about having the extra information?), but maybe the id is similar
    //archivesName.set("$mod_id-${project.name}-$minecraft_version")
    archivesName.set(commonMod.hyphenedName)
}

//TODO: ?? Also do we need to be getting the mc version in the same way this does?
version = "${loader}-${commonMod.version}+mc${stonecutterBuild.current.version}"

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(commonProject.prop("java.version")!!))
    withSourcesJar()
    withJavadocJar()
}

val generatePackageInfos = tasks.register<neoforge.GeneratePackageInfos>("generatePackageInfos") {
    files.from(sourceSets.main.get().java.srcDirTrees)
}

tasks.named("test").configure {//Ensure validateJson has to be ran in order for build to pass
    dependsOn(rootProject.tasks.named("validateJson"))
}

rootProject.tasks.named("generatePackageInfos").configure {
    dependsOn(generatePackageInfos)
}

tasks.withType<Jar>().configureEach {
    from(rootProject.file("LICENSE")) {
        rename { "${it}_${commonMod.hyphenedName}" }
    }
}

tasks.named<Jar>("jar") {
    manifest {
        attributes(
            mapOf(
                "Specification-Title" to commonMod.name,
                "Specification-Vendor" to "WildfireRomeo, celeste, pupnewfster",
                "Specification-Version" to archiveVersion.get(),
                "Implementation-Title" to project.name,
                "Implementation-Version" to archiveVersion.get(),
                "Implementation-Vendor" to "WildfireRomeo, celeste, pupnewfster",
                "Built-On-Minecraft" to commonMod.mc
            )
        )
    }
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(":common:${commonMod.prop("major_minecraft_version")}:stonecutterGenerate")
    val expandProps = mapOf(
        "version" to commonMod.version,
        "group" to project.group,//Else we target the task's group.
        "minecraft_version" to commonMod.mc,
        "major_minecraft_version" to commonMod.prop("major_minecraft_version"),
        "fabric_version" to commonMod.dep("fabric_api"),
        "fabric_loader_version" to commonMod.dep("fabric_loader"),
        "mod_name" to commonMod.name,
        "mod_id" to commonMod.id,
        "license" to commonMod.modProp("license"),
        "source_code" to commonMod.modProp("source"),
        "issue_tracker" to commonMod.modProp("issue_tracker"),
        "description" to commonMod.modProp("description"),//TODO: Jared's template uses: project.description,
        "neoforge_version" to commonMod.dep("min_neoforge"),
        "java_version" to commonProject.prop("java.version")
    )

    val jsonExpandProps = expandProps.mapValues { (_, value) ->
        if (value is String) value.replace("\n", "\\n") else value
    }

    filesMatching("META-INF/neoforge.mods.toml") {
        expand(expandProps)
    }

    filesMatching(listOf("pack.mcmeta", "fabric.mod.json", "*.mixins.json")) {
        expand(jsonExpandProps)
    }

    inputs.properties(expandProps)
}

publishing {
    publications {
        register<MavenPublication>("mavenJava") {
            artifactId = base.archivesName.get()
            from(components["java"])
        }
    }
}
