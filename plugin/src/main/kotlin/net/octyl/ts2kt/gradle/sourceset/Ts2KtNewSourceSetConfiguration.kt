/*
 * This file is part of plugin, licensed under the MIT License (MIT).
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

import net.octyl.ts2kt.gradle.repository.npm.NpmTsRepository
import net.octyl.ts2kt.gradle.tasks.ConvertTypescriptToKotlin
import net.octyl.ts2kt.gradle.tasks.DiscoverTs2ktExecutable
import net.octyl.ts2kt.gradle.tasks.ResolveTypescriptDependencies
import net.octyl.ts2kt.gradle.util.registerInfer
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.internal.HasConvention
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.getPluginByName
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet

class Ts2KtNewSourceSetConfiguration(
        private val project: Project,
        private val discoverTaskProvider: TaskProvider<DiscoverTs2ktExecutable>,
        private val sourceSet: SourceSet) {

    fun configure() {
        val configuration = addConfiguration()
        val resolveTask = addResolveTask(configuration)
        addConversionTask(resolveTask)
    }

    private fun addConfiguration(): Configuration {
        return project.configurations.create(sourceSet.configurationName("ts2ktUnofficial"))
                .apply {
                    isVisible = false
                    description = "TypeScript dependencies to be converted to Kotlin " +
                            "of source set `${sourceSet.name}`."
                }
    }

    private fun addResolveTask(configuration: Configuration): TaskProvider<ResolveTypescriptDependencies> {
        val taskName = sourceSet.getTaskName("resolve", "TypescriptDependencies")
        return project.tasks.registerInfer(taskName) {
            description = "Resolves dependencies of the ${configuration.name} configuration."
            group = "resolution"

            configurationProperty.set(configuration)
            repository(NpmTsRepository(project))
        }
    }

    private fun addConversionTask(resolveTaskProvider: TaskProvider<ResolveTypescriptDependencies>): TaskProvider<ConvertTypescriptToKotlin> {
        val taskName = sourceSet.getTaskName("convert", "TypescriptToKotlin")
        return project.tasks.registerInfer(taskName) {
            description = "Converts Typescript files in source set ${sourceSet.name} to Kotlin."

            val resolveTask = resolveTaskProvider.get()
            val discoverTask = discoverTaskProvider.get()

            ts2ktScriptProperty.set(discoverTask.ts2KtScriptProperty)
            typescriptFilesProperty.setFrom(resolveTask.outputFiles)
            outputDirectoryProperty.set(project.layout.buildDirectory.dir("generated/source/ts2kt/${sourceSet.name}/"))

            // Add output to equivalent Kotlin source set.
            // I don't really like using the internal API here, but Kotlin does...
            val kotlinSourceSet = (sourceSet as HasConvention).convention.getPluginByName<KotlinSourceSet>("kotlin")
            kotlinSourceSet.kotlin.srcDir(outputDirectoryProperty)

            addToIntellijGeneratedSources(outputDirectoryProperty)
        }
    }

    private fun addToIntellijGeneratedSources(outputDirectoryProperty: DirectoryProperty) {
        val idea = (project.convention.findByName("idea") as IdeaModel?) ?: return
        idea.module.generatedSourceDirs.add(outputDirectoryProperty.asFile.get())
    }
}
