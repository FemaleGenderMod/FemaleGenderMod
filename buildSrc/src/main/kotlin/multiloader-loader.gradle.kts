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

import gradle.kotlin.dsl.accessors._04dc8d3a90e2cfd35b03275ab207a42a.compileOnly
import gradle.kotlin.dsl.accessors._04dc8d3a90e2cfd35b03275ab207a42a.main
import gradle.kotlin.dsl.accessors._04dc8d3a90e2cfd35b03275ab207a42a.sourceSets

plugins {
    id("multiloader-common")
}

val commonJava = configurations.register("commonJava") {
    isCanBeResolved = true
    isCanBeConsumed = false
}
val commonResources = configurations.register("commonResources") {
    isCanBeResolved = true
    isCanBeConsumed = false
}
val commonDataJava = configurations.register("commonDataJava") {
    extendsFrom(commonJava)
    isCanBeResolved = true
    isCanBeConsumed = false
}

sourceSets.main {
    resources {
        //Add the generated main module resources
        srcDir("src/generated/resources")
        //But exclude the cache of the generated data from what gets built
        exclude(".cache")
    }
}

val commonPath = stonecutterBuild.node.sibling("common")!!.hierarchy.toString()

sourceSets.configureEach {
    //println("Adding $name sourceset to $loader for mc version ${stonecutterBuild.current.project}")
    if (name == "datagen") {
        dependencies {
            add(implementationConfigurationName, project(path = commonPath, configuration = "commonDataJava"))
        }
        tasks.named<JavaCompile>(compileJavaTaskName) {
            dependsOn(commonDataJava)
            source(commonDataJava)
        }
    }
}

dependencies {
    compileOnly(project(path = commonPath)) {
        attributes {
            attribute(loaderAttribute, "common")
        }
    }
    commonJava(project(path = commonPath, configuration = "commonJava"))
    commonResources(project(path = commonPath, configuration = "commonResources"))
    commonDataJava(project(path = commonPath, configuration = "commonDataJava"))
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
    dependsOn(commonJava, commonResources)
    from(commonJava, commonResources)
}
