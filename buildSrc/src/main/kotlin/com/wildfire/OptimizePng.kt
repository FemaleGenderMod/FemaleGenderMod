package com.wildfire

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileType
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import org.gradle.work.ChangeType
import org.gradle.work.Incremental
import org.gradle.work.InputChanges
import java.util.*
import javax.inject.Inject

/**
 * From <a href="https://github.com/mekanism/Mekanism/blob/26.2/buildSrc/src/main/groovy/mekanism/OptimizePng.groovy">Mekanism</a>
 *
 * License: <a href="https://github.com/mekanism/Mekanism/blob/26.2/LICENSE">MIT</a>
 */
abstract class OptimizePng : DefaultTask() {

    init {
        description = "Optimize png file size"
        outputs.upToDateWhen { true }
    }

    @get:InputFiles
    @get:Incremental
    abstract val inputFiles: ConfigurableFileCollection

    @get:Inject
    protected abstract val execOperations: ExecOperations

    @TaskAction
    fun execute(inputChanges: InputChanges) {
        for (fileChange in inputChanges.getFileChanges(inputFiles)) {
            if (fileChange.changeType == ChangeType.REMOVED ||
                fileChange.fileType != FileType.FILE
            ) {
                continue
            }

            val file = fileChange.file
            val size = file.length()

            execOperations.exec {
                commandLine("optipng", "-q", "-o7", "-zm1-9", "-strip", "all", file)
            }

            val newSize = file.length()

            if (newSize < size) {
                System.out.format(
                    Locale.ROOT,
                    "Reduced File size of %s from %d bytes to %d bytes (reduced by %.2f%%)%n",
                    file,
                    size,
                    newSize,
                    (size - newSize).toDouble() / size.toDouble() * 100.0
                )
            }
        }
    }
}
