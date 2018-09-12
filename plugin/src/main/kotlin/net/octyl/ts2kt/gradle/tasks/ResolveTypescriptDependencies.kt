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
package net.octyl.ts2kt.gradle.tasks

import net.octyl.ts2kt.gradle.repository.ResolutionResult
import net.octyl.ts2kt.gradle.repository.TsRepository
import net.octyl.ts2kt.gradle.util.ensureExhausted
import net.octyl.ts2kt.gradle.util.field
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.Dependency
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property
import java.util.LinkedList

open class ResolveTypescriptDependencies : KotlinDefaultTask() {

    @get:Internal
    val configurationProperty = project.objects.property<Configuration>()
    @get:Internal
    var configuration by configurationProperty.field

    @get:Internal
    val repositoriesProperty = project.objects.listProperty<TsRepository>()
    @get:Internal
    val repositories by repositoriesProperty.field

    fun repository(tsRepository: TsRepository) = repositoriesProperty.add(tsRepository)

    private val outputFilesInternal = project.files().builtBy(this)
    @get:OutputFiles
    val outputFiles: FileCollection = outputFilesInternal

    init {
        // opt-out of up-to-date checks -- we'll rely on repos to cache it
        outputs.upToDateWhen { false }
    }

    private data class SimpleDependencyId(val group: String?, val name: String, val version: String)

    private fun Dependency.id(): SimpleDependencyId {
        val groupFixed = when (group) {
            null, "" -> null
            else -> group
        }
        return SimpleDependencyId(group, name, version
                ?: throw IllegalStateException("Missing version for `$this`."))
    }

    @TaskAction
    fun resolveDependencies() {
        val config = configuration ?: throw IllegalStateException("No configuration provided.")

        val resolutionSuccessCache = mutableSetOf<SimpleDependencyId>()
        val dependencyStack = LinkedList(config.allDependencies)
        while (dependencyStack.isNotEmpty()) {
            val dep = dependencyStack.pop()!!

            val id = dep.id()
            if (id !in resolutionSuccessCache) {
                val transitiveDeps = resolveDependency(dep)
                resolutionSuccessCache.add(id)

                dependencyStack.addAll(transitiveDeps)
            }
        }
    }

    private fun resolveDependency(dep: Dependency): List<Dependency> {
        val errors = mutableListOf<ResolutionResult.NotFound>()
        for (repo in repositories) {
            val resolved = repo.resolveDependency(dep)
            ensureExhausted(when (resolved) {
                is ResolutionResult.Success -> {
                    outputFilesInternal.from(resolved.files)
                    return resolved.dependencies
                }
                is ResolutionResult.NotFound -> errors.add(resolved)
            })
        }

        // Throw errors on failure, with associated information in info logs.
        errors.forEach { res ->
            if (res.error != null) {
                logger.info("Resolution error for `$dep`:", res.error)
            }
        }
        throw IllegalStateException("Unable to resolve dependency `$dep`." +
                "Run with --info for more information.")
    }

}