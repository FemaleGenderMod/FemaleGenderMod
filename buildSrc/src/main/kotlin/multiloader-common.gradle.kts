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
    archivesName.set(commonMod.name.replace(' ', '-'))
}

version = "${loader}-${commonMod.version}+mc${stonecutterBuild.current.project}"

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

val licenseFile = rootProject.layout.projectDirectory.file("LICENSE")
tasks.withType<Jar>().configureEach {
    inputs.property("name", commonMod.name)
    from(licenseFile) {
        rename("LICENSE", "LICENSE_${commonMod.name.replace(' ', '_')}")
    }
}

tasks.withType<Javadoc>().configureEach {
    val opts = options as StandardJavadocDocletOptions
    opts.encoding = "UTF-8"
    opts.addBooleanOption("-no-fonts", true)
    opts.tags = (opts.tags ?: mutableListOf()).apply {
        add("apiNote:a:API Note:")
        add("implSpec:a:Implementation Requirements:")
        add("implNote:a:Implementation Note:")
    }
}

tasks.named<Jar>("jar") {
    manifest.attributes(mapOf(
        "Specification-Title" to commonMod.name,
        "Specification-Vendor" to "WildfireRomeo, celeste, pupnewfster",
        "Specification-Version" to archiveVersion.get(),
        "Implementation-Title" to commonMod.name,
        "Implementation-Vendor" to "WildfireRomeo, celeste, pupnewfster",
        "Implementation-Version" to archiveVersion.get(),
        "Built-On-Minecraft" to stonecutterBuild.current.version
    ))
    inputs.property("name", commonMod.name)
    inputs.property("version", archiveVersion.get())
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(":common:${stonecutterBuild.current.project}:stonecutterGenerate")
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(":common:${stonecutterBuild.current.project}:stonecutterGenerate")
    val expandProps = mapOf(
        "version" to commonMod.version,
        "group" to commonMod.modProp("group"),//Else we target the task's group.
        "minecraft_version" to stonecutterBuild.current.version,
        "major_minecraft_version" to stonecutterBuild.current.project,
        "fabric_version" to commonMod.dep("fabric_api"),
        "fabric_loader_version" to commonMod.dep("fabric_loader"),
        "mod_name" to commonMod.name,
        "mod_id" to commonMod.id,
        "license" to commonMod.modProp("license"),
        "source_code" to commonMod.modProp("source"),
        "issue_tracker" to commonMod.modProp("issue_tracker"),
        "description" to commonMod.modProp("description"),
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
