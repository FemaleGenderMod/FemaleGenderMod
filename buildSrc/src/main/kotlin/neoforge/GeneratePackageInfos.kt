package neoforge

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.io.File

/**
 * From <a href="https://github.com/neoforged/NeoForge/blob/26.1.x/buildSrc/src/main/groovy/neoforge.formatting-conventions.gradle">NeoForge</a>
 *
 * License: <a href="https://github.com/neoforged/NeoForge/blob/26.1.x/LICENSE.txt">LGPL 2.1</a>
 */
abstract class GeneratePackageInfos : DefaultTask() {

    init {
        description = "Generates package-info files for any packages that are missing them"
    }

    @get:InputFiles
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val files: ConfigurableFileCollection

    @TaskAction
    fun generatePackageInfos() {
        files.forEach { javaFile ->
            val packageInfoFile = File(javaFile.parentFile, "package-info.java")
            if (!packageInfoFile.exists()) {
                var pkgName = javaFile.invariantSeparatorsPath
                pkgName = pkgName.substring(
                    pkgName.indexOf("com/wildfire/"),
                    pkgName.lastIndexOf('/')
                ).replace('/', '.')

                val pkgInfoText = """
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

                    @NullMarked
                    package $pkgName;

                    import org.jspecify.annotations.NullMarked;
                """.trimIndent()

                packageInfoFile.writeText(pkgInfoText)
            }
        }
    }
}
