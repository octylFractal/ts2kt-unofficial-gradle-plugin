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
package net.octyl.ts2kt.gradle.sourceset

import net.octyl.ts2kt.gradle.Ts2ktUnofficialExtension
import net.octyl.ts2kt.gradle.repository.configuration.ClientConfiguration
import net.octyl.ts2kt.gradle.tasks.ConvertTypescriptToKotlin
import net.octyl.ts2kt.gradle.tasks.DiscoverTs2ktExecutable
import net.octyl.ts2kt.gradle.util.registerInfer
import org.gradle.api.Project
import org.gradle.api.file.Directory
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.*
import org.gradle.plugins.ide.idea.GenerateIdeaModule
import org.gradle.plugins.ide.idea.IdeaPlugin
import org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile
import java.util.concurrent.Callable

class Ts2ktNewSourceSetConfiguration(
        private val project: Project,
        private val discoverTaskProvider: TaskProvider<DiscoverTs2ktExecutable>,
        private val sourceSet: SourceSet) {

    fun configure() {
        val configuration = addConfiguration()
        addConversionTask(configuration)
    }

    private fun addConfiguration(): ClientConfiguration {
        val ext = project.extensions.getByType<Ts2ktUnofficialExtension>()

        return ext.getOrCreateClientConfiguration(sourceSet.configurationName("ts2ktUnofficial"))
    }

    private fun addConversionTask(configuration: ClientConfiguration): TaskProvider<ConvertTypescriptToKotlin> {
        val taskName = sourceSet.getTaskName("convert", "TypescriptToKotlin")
        val outputDirectoryProvider = project.layout.buildDirectory.dir("generated/source/ts2kt/${sourceSet.name}/")

        return project.tasks.registerInfer<ConvertTypescriptToKotlin>(taskName) {
            description = "Converts Typescript files in source set ${sourceSet.name} to Kotlin."

            val discoverTask = discoverTaskProvider.get()

            ts2ktScriptProperty.set(discoverTask.ts2ktScriptProperty)
            typescriptFilesProperty.setFrom(Callable { configuration.allFiles })
            outputDirectoryProperty.set(outputDirectoryProvider)
        }.also { task ->
            hookIntellij(task, outputDirectoryProvider)
            sourceSet.kotlin.srcDir(outputDirectoryProvider)
            project.tasks
                    .withType<Kotlin2JsCompile>()
                    .named(sourceSet.getTaskName("compile", "Kotlin2Js"))
                    .configure {
                        val convTask = task.get()
                        dependsOn(convTask)
                    }
        }
    }

    private fun hookIntellij(task: TaskProvider<ConvertTypescriptToKotlin>,
                             outputDirectoryProperty: Provider<Directory>) {
        project.plugins.withType<IdeaPlugin> {
            val idea = model
            idea.module.generatedSourceDirs.add(outputDirectoryProperty.get().asFile)

            project.tasks.withType<GenerateIdeaModule>().configureEach {
                dependsOn(task)
            }
        }
    }
}
