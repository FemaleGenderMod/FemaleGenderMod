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

/*
 * From: https://github.com/cosmiclabstudio/multiloader-stonecutter/blob/main/buildSrc/src/main/kotlin/build-extensions.kt
 *
 * License (CC0): https://github.com/cosmiclabstudio/multiloader-stonecutter/blob/main/LICENSE
 */

import dev.kikugie.stonecutter.build.StonecutterBuildExtension
import org.gradle.api.Project
import org.gradle.kotlin.dsl.*

val Project.mod: ModData get() = ModData(this)
fun Project.prop(key: String): String? = findProperty(key)?.toString()

val Project.stonecutterBuild get() = extensions.getByType<StonecutterBuildExtension>()

val Project.common get() = requireNotNull(stonecutterBuild.node.sibling("common")) {
    "No common project for $project"
}
val Project.commonProject get() = rootProject.project(stonecutterBuild.current.project)
val Project.commonMod get() = commonProject.mod

val Project.loader: String? get() = prop("loader")

@JvmInline
value class ModData(private val project: Project) {
    val id: String get() = modProp("id")
    val name: String get() = modProp("name")
    val version: String get() = modProp("version")

    fun modProp(key: String) = requireNotNull(project.prop("mod.$key")) { "Missing 'mod.$key'" }
    fun dep(key: String) = requireNotNull(project.prop("deps.$key")?.takeIf { it.isNotEmpty() && it != "" }) { "Missing 'deps.$key'" }
}
