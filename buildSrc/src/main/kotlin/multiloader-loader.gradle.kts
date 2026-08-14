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

import gradle.kotlin.dsl.accessors._3c984467cfe6063166439ec0710b6c00.compileOnly

plugins {
    id("multiloader-common")
}

val commonJava by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val commonResources by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

dependencies {
    val commonPath = stonecutterBuild.node.sibling("common")!!.hierarchy.toString()
    compileOnly(project(path = commonPath)) {
        attributes {
            attribute(
                Attribute.of("io.github.mcgradleconventions.loader", String::class.java),
                "common"
            )
        }
    }

    commonJava(project(path = commonPath, configuration = "commonJava"))
    commonResources(project(path = commonPath, configuration = "commonResources"))
}

tasks.named<JavaCompile>("compileJava") {
    dependsOn(commonJava)
    source(commonJava)
}

tasks.named<ProcessResources>("processResources") {
    dependsOn(commonResources)
    from(commonResources)
}

tasks.named<Javadoc>("javadoc") {
    dependsOn(commonJava)
    source(commonJava)
}

tasks.named<Jar>("sourcesJar") {
    dependsOn(commonJava)
    from(commonJava)

    dependsOn(commonResources)
    from(commonResources)
}
