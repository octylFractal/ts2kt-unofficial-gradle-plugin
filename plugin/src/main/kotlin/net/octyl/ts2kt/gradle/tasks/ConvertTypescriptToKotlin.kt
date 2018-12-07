/*
 * This file is part of ts2kt-unofficial-gradle-plugin-plugin, licensed under the MIT License (MIT).
 *
 * Copyright (c) Kenzie Togami <https://octyl.net>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.octyl.ts2kt.gradle.tasks

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import net.octyl.ts2kt.gradle.util.PartialPackageInfo
import net.octyl.ts2kt.gradle.util.field
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.workers.WorkerExecutor
import java.io.File
import javax.inject.Inject

open class ConvertTypescriptToKotlin @Inject constructor(
        private val workerExecutor: WorkerExecutor
) : DefaultTask() {

    @get:InputFile
    val ts2ktScriptProperty = project.objects.fileProperty()
    @get:Internal
    var ts2ktScript by ts2ktScriptProperty.field

    @get:InputFiles
    @get:SkipWhenEmpty
    val typescriptFilesProperty = project.files()
    @get:Internal
    var typescriptFiles by typescriptFilesProperty.field

    @get:OutputDirectory
    val outputDirectoryProperty = project.objects.directoryProperty()
    @get:Internal
    var outputDirectory by outputDirectoryProperty.field

    private val mapper = ObjectMapper().registerModules(KotlinModule())

    @TaskAction
    fun convert() {
        val files = typescriptFiles.files
        val outputDir = outputDirectoryProperty.asFile.get()
        if (!outputDir.mkdirs() && !outputDir.exists()) {
            throw IllegalStateException("Could not create output directory `${outputDir.canonicalPath}`")
        }

        files.map { tsFile ->
            workerExecutor.submit(RunTs2ktForPackage::class.java) {
                setParams(ts2ktScript?.asFile,
                        tsFile,
                        outputDir.resolve(tsFile.nodePackageName))
            }
        }
    }

    private val File.nodePackageName: String
        get() {
            val pkgJsonFile = parentPackageJson
            return when (pkgJsonFile) {
                null -> parentFile.name
                else -> mapper.readValue<PartialPackageInfo>(pkgJsonFile).name
            }
        }
    private val File.parentPackageJson: File?
        get() {
            var directory: File? = parentFile
            while (directory != null) {
                val pkgJson = directory.listFiles().firstOrNull { it.name == "package.json" }
                when {
                    pkgJson != null -> return pkgJson
                    else -> directory = directory.parentFile
                }
            }

            return null
        }
}