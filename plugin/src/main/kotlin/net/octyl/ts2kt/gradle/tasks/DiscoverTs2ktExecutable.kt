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

import net.octyl.ts2kt.gradle.util.PathLookup
import net.octyl.ts2kt.gradle.util.field
import net.octyl.ts2kt.gradle.util.file
import net.octyl.ts2kt.gradle.util.ts2ktUnofficialDirectory
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.property

open class DiscoverTs2ktExecutable : KotlinDefaultTask() {

    @get:Input
    val ts2ktVersionProperty = project.objects.property<String>()
    /**
     * Version of the NPM package `ts2kt` to install.
     */
    @get:Internal
    var ts2ktVersion by ts2ktVersionProperty.field

    @get:Input
    @get:Optional
    val ts2KtProvidedExecutableProperty = project.layout.fileProperty()
    /**
     * A hard-coded executable path to use. Avoids lookup if this is set and exists.
     */
    @get:Internal
    var ts2KtProvidedExecutable by ts2KtProvidedExecutableProperty.field

    /**
     * Path for searching for executables.
     *
     * This cannot be set, and is just for caching.
     */
    @get:Nested
    internal val findPath = PathLookup.fromSystemEnv()

    @Suppress("LeakingThis")
    @get:OutputFile
    val ts2KtScriptProperty = newOutputFile().apply {
        set(project.layout.ts2ktUnofficialDirectory.file("ts2kt.sh"))
    }
    /**
     * Script that, when run, will call `ts2kt`.
     */
    @get:Internal
    var ts2KtScript by ts2KtScriptProperty.field

    @TaskAction
    fun discover() {
        val scriptFile = ts2KtScriptProperty.asFile.get()
        if (!scriptFile.parentFile.mkdirs() && !scriptFile.parentFile.exists()) {
            throw IllegalStateException("Could not create `${scriptFile.parentFile.canonicalPath}`.")
        }
        if (!scriptFile.createNewFile()) {
            throw IllegalStateException("Could not create `${scriptFile.canonicalPath}`.")
        }
        if (!scriptFile.setExecutable(true)) {
            throw IllegalStateException("Could not make `${scriptFile.canonicalPath}` executable.")
        }

        scriptFile.writeText("""
            |#!/usr/bin/env sh
            |${ts2KtInvocation()} "$@"
            """.trimMargin("|"))
    }

    fun ts2KtInvocation(): String {
        val ts2KtInstalled = ts2KtProvidedExecutableProperty.asFile.orNull ?: findPath.find("ts2kt")
        if (ts2KtInstalled != null && ts2KtInstalled.canExecute()) {
            return ts2KtInstalled.canonicalPath
        }

        findPath.find("npx")
                ?: throw IllegalStateException("`npx` is not installed. Cannot run `ts2kt`.")
        val packageOption = getPackageOption()
        return "npx -p '$packageOption' ts2kt"
    }

    private fun getPackageOption(): String {
        val version = ts2ktVersion
        return when (version) {
            null -> "ts2kt"
            else -> {
                if ("'" in version) {
                    throw IllegalStateException("Version should not contain single quotes.")
                }
                "ts2kt@$version"
            }
        }
    }

}
