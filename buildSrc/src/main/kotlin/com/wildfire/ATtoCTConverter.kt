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

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

abstract class ATtoCTConverter : DefaultTask() {

    init {
        description = "Converts an Access Transformer file into an Access Widener file"
    }

    //TODO: If we ever extend any enums or inject interfaces that we care about propagating to dev time of addons
    // add support for those as potential inputs that then get used as well when creating the class tweaker file
    @get:InputFile
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val atPath: RegularFileProperty

    @get:OutputFile
    abstract val ctPath: RegularFileProperty

    @TaskAction
    fun convert() {
        val input = atPath.asFile.get()
        if (!input.exists()) {
            throw GradleException("Access Transformer file does not exist: $input")
        }
        val output = ctPath.asFile.get()
        output.parentFile.mkdirs()

        input.useLines { lines ->
            output.bufferedWriter().use { writer ->
                //https://github.com/FabricMC/fabric-tooling/blob/main/class-tweaker/src/main/java/net/fabricmc/classtweaker/reader/ClassTweakerReaderImpl.java#L220
                writer.appendLine("classTweaker  v2  official")
                writer.appendLine()
                lines.forEach { line ->
                    writer.appendLine(transformLine(line))
                }
            }
        }
    }

    private fun transformLine(line: String): String {
        // A line beginning with # is still transformed, but retains its #.
        val isComment = line.startsWith("#")
        var result = if (isComment) line.substring(1) else line

        val isPublic = result.startsWith("public")
        val isProtected = result.startsWith("protected")
        if (!isPublic && !isProtected) {
            //Don't do any transformations
            return line
        }
        // Transform access modifiers.

        //Remove public/protected from the start of the string
        result = result.substring(if (isPublic) 6 else 9)

        val targetType : String = when {
            //A comment at the end represents a field
            '#' in result -> "field"
            //Parentheses indicate a method
            '(' in result -> "method"
            // Otherwise it's a class
            else -> "class"
        }

        val access : String
        if (result.startsWith("-f")) {
            result = result.substring(2)
            access = if (targetType == "field") "mutable" else "extendable"
        } else {
            access = "accessible"
        }
        //Remove the leading space
        result = result.trimStart()

        // Replace periods with slashes for transformed lines.
        result = result.replace('.', '/')

        if (targetType == "field") {//Remove the comment symbol to let the field type be added
            result = result.replace("#", "")
        } else if (targetType == "method") {
            //Ensure there is a space before the method signature
            result = result.replace("(", " (")
        }
        result = "$access $targetType $result"
        return if (isComment) "#$result" else result
    }
}
