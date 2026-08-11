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

package com.wildfire

import groovy.json.JsonException
import groovy.json.JsonSlurper
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.SetProperty
import org.gradle.api.tasks.*
import java.io.File
import java.util.Collections
import javax.inject.Inject

abstract class ValidateJson : DefaultTask() {

    private val json = JsonSlurper()

    init {
        description = "Validates JSON files and translation completeness."
        group = "verification"
    }

    @get:Inject
    abstract val layout: ProjectLayout

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val criticalFiles: ConfigurableFileCollection

    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val rootTranslation: RegularFileProperty

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val translationFiles: ConfigurableFileCollection

    @get:Input
    abstract val nonExhaustiveLocales: SetProperty<String>

    fun relativePath(file: File): String {
        return layout.projectDirectory.asFile.toPath().relativize(file.toPath()).toString()
    }

    @TaskAction
    fun validate() {
        for (criticalFile in criticalFiles) {
            try {
                json.parse(criticalFile)
            } catch (e: JsonException) {
                //# JSON decode errors in any of these files should cause a build to fail
                logger.lifecycle("::error file=${relativePath(criticalFile)}::${e.message}")
                throw GradleException("Critical JSON files contain syntax errors.", e)
            }
        }

        @Suppress("UNCHECKED_CAST")
        val rootStrings = (json.parse(rootTranslation.get().asFile) as Map<String, *>).keys

        for (file in translationFiles) {
            if (file.name == "en_us.json") {
                continue
            }

            val strings = try {
                @Suppress("UNCHECKED_CAST")
                (json.parse(file) as Map<String, *>).keys
            } catch (_: JsonException) {
                continue
            }

            val missingFromRoot = strings - rootStrings
            val missingFromTranslation : Collection<String>

            if (nonExhaustiveLocales.get().contains(file.nameWithoutExtension)) {
                missingFromTranslation = Collections.emptySet()
            } else {
                missingFromTranslation = rootStrings - strings
            }

            logger.lifecycle("::group::${file.name}")

            if (missingFromRoot.isNotEmpty()) {
                logger.lifecycle("::notice file=${relativePath(file)}::Has ${missingFromRoot.size} extra translation strings")
                missingFromRoot.sorted().forEach {
                    logger.lifecycle("  - $it")
                }
            }

            if (missingFromTranslation.isNotEmpty()) {
                logger.lifecycle("::notice file=${relativePath(file)}::Missing ${missingFromTranslation.size} translation strings")
                missingFromTranslation.sorted().forEach {
                    logger.lifecycle("  - $it")
                }
            }

            if (missingFromRoot.isEmpty() && missingFromTranslation.isEmpty()) {
                logger.lifecycle("No missing translations!")
            }

            logger.lifecycle("::endgroup::")
        }
    }
}
