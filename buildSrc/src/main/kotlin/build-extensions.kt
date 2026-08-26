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

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import me.modmuss50.mpp.ModPublishExtension
import me.modmuss50.mpp.ReleaseType
import org.gradle.api.Project
import org.gradle.api.attributes.Attribute
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.named

val Project.stonecutterBuild get() = extensions.getByType<StonecutterBuildExtension>()

val loaderAttribute = Attribute.of("io.github.mcgradleconventions.loader", String::class.java)
//TODO: This should be the same as stonecutterBuild.branch.id, but the build stuff requires the property to be defined
val Project.loader get() = stonecutterBuild.branch.project.property("loader") as String

private val Project.modPublishExtension get() = extensions.getByType<ModPublishExtension>()

private val Project.modrinthToken get() = providers.environmentVariable("MODRINTH_TOKEN")
private val Project.cfToken get() = providers.environmentVariable("CURSEFORGE_TOKEN")

val Project.performDryRun get() = modrinthToken.getOrNull() == null && cfToken.getOrNull() == null

val Project.basePublishingOps get() = modPublishExtension.publishOptions {
    val loader = stonecutterBuild.branch.project.property("loader") as String
    val modVer: String = stonecutterBuild.properties["mod_version"]
    val verTitle: String = stonecutterBuild.properties["publish.version_title"]
    val loaderName : String = if (loader == "fabric") "Fabric" else "NeoForge"

    displayName.set("$modVer for $loaderName $verTitle")
    //Note: The version is set automatically, but maybe we want to set it to the modVer instead?
    //version.set(project.version as String)
    changelog.set(providers.fileContents(rootProject.layout.projectDirectory.file("CHANGELOG.md")).asText)
    type.set(ReleaseType.of(stonecutterBuild.properties["publish.type"]))
    modLoaders.add(loader)
    file.set(tasks.named<Jar>("jar").flatMap { it.archiveFile })
    additionalFiles.from(
        tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile },
        tasks.named<Jar>("javadocJar").flatMap { it.archiveFile }
    )
}

//TODO - Neo: Technically neo min version is different just because of min neo version required being only on 26.1.2
val Project.modrinthOps get() = modPublishExtension.modrinthOptions {
    accessToken.set(modrinthToken)
    projectId.set(stonecutterBuild.properties["publish.modrinth"] as String)
    environment.set(CLIENT_ONLY_SERVER_OPTIONAL)
    minecraftVersionRange {
        start.set(stonecutterBuild.properties["publish.min_version"] as String)
        end.set(stonecutterBuild.properties["publish.max_version"] as String)
    }
}

val Project.cfOps get() = modPublishExtension.curseforgeOptions {
    accessToken.set(cfToken)
    projectId.set(stonecutterBuild.properties["publish.curseforge"] as String)
    projectSlug.set("female-gender")
    client.set(true)
    server.set(true)
    minecraftVersionRange {
        start.set(stonecutterBuild.properties["publish.min_version"] as String)
        end.set(stonecutterBuild.properties["publish.max_version"] as String)
    }
}
