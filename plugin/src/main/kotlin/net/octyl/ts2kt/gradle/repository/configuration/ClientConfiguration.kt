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
package net.octyl.ts2kt.gradle.repository.configuration

import net.octyl.ts2kt.gradle.repository.ClientRepository
import net.octyl.ts2kt.gradle.repository.ResolutionResult
import net.octyl.ts2kt.gradle.repository.dependency.ClientDependency
import net.octyl.ts2kt.gradle.util.ensureExhausted
import org.apache.log4j.Logger
import org.gradle.api.Project
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.SetProperty
import java.io.PrintWriter
import java.io.StringWriter
import java.util.LinkedList

class ClientConfiguration(val name: String,
                          private val project: Project,
                          private val repositories: SetProperty<ClientRepository>) {

    private val logger = Logger.getLogger(javaClass)

    val dependencies = mutableSetOf<ClientDependency>()

    val allFiles: FileCollection by lazy {
        val deps = project.files()

        val remainingDependencies = LinkedList(dependencies)
        val processedDependencies = mutableSetOf<ClientDependency>()

        while (remainingDependencies.isNotEmpty()) {
            val next = remainingDependencies.pollFirst()!!

            val transDeps = resolveDependency(next, deps)

            processedDependencies.add(next)

            remainingDependencies += HashSet(transDeps).apply {
                removeIf(processedDependencies::contains)
            }
        }

        return@lazy deps
    }

    private fun resolveDependency(dep: ClientDependency,
                                  outputFiles: ConfigurableFileCollection): List<ClientDependency> {
        val errors = LinkedHashMap<ClientRepository, ResolutionResult.Error>()
        for (repo in repositories.get()) {
            val resolved = repo.resolveDependency(dep)
            ensureExhausted(when (resolved) {
                is ResolutionResult.Success -> {
                    outputFiles.from(resolved.files)
                    return resolved.dependencies
                }
                is ResolutionResult.Error -> errors.put(repo, resolved)
            })
        }

        // Throw errors on failure, with associated information in info logs.
        errors.forEach { repo, res ->
            when (res.error) {
                null -> logger.warn("$repo: Could not find `$dep`.")
                else -> {
                    val stack = StringWriter()
                            .use {
                                res.error.printStackTrace(PrintWriter(it, true))
                                it.toString()
                            }
                    logger.warn("$repo: Resolution error for `$dep`: $stack")
                }
            }
        }
        throw IllegalStateException("Unable to resolve dependency `$dep`.")
    }

}