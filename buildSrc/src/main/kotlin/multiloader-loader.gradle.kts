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

// These source-sets only exist for the sake of assembling a dependency graph
// for IntelliJ. Each of these will become an IntelliJ module when the project is imported,
// and will be set as the "main module" for the IntelliJ runs. A compile dependency
// on the other source sets is enough, since runtime classpath is managed by MDG anyway.
val runMain = sourceSets.register("runMain") {
    val main = sourceSets.main.get()
    compileClasspath += main.output
    runtimeClasspath += main.runtimeClasspath
}
val runData = sourceSets.register("runData") {
    val mainRun = runMain.get()
    val data = sourceSets.named("datagen").get()
    compileClasspath += mainRun.compileClasspath + data.output
    runtimeClasspath += mainRun.runtimeClasspath + data.runtimeClasspath
}

dependencies {
    val commonPath = stonecutterBuild.node.sibling("common")!!.hierarchy.toString()
    compileOnly(project(path = commonPath)) {
        attributes {
            attribute(loaderAttribute, "common")
        }
    }
    add("datagenImplementation", project(path = commonPath, configuration = "commonDataJava"))
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

tasks.named<JavaCompile>("compileDatagenJava") {
    dependsOn(commonDataJava)
    source(commonDataJava)
}
