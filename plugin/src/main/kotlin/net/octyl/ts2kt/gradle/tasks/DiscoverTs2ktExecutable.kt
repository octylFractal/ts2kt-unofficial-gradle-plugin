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

import net.octyl.ts2kt.gradle.util.PathLookup
import net.octyl.ts2kt.gradle.util.field
import net.octyl.ts2kt.gradle.util.file
import net.octyl.ts2kt.gradle.util.ts2ktUnofficialDirectory
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.os.OperatingSystem
import org.gradle.kotlin.dsl.*

open class DiscoverTs2ktExecutable : DefaultTask() {

    @get:Input
    val ts2ktVersionProperty = project.objects.property<String>()
    /**
     * Version of the NPM package `ts2kt` to install.
     */
    @get:Internal
    var ts2ktVersion by ts2ktVersionProperty.field

    @get:Input
    @get:Optional
    val ts2ktProvidedExecutableProperty = project.objects.fileProperty()
    /**
     * A hard-coded executable path to use. Avoids lookup if this is set and exists.
     */
    @get:Internal
    var ts2ktProvidedExecutable by ts2ktProvidedExecutableProperty.field

    /**
     * Path for searching for executables.
     *
     * This cannot be set, and is just for caching.
     */
    @get:Nested
    internal val findPath = PathLookup.fromSystemEnv()

    @Suppress("LeakingThis")
    @get:OutputFile
    val ts2ktScriptProperty = project.objects.fileProperty().apply {
        set(project.layout.ts2ktUnofficialDirectory.file(ts2ktScriptDescriptor().fileName))
    }
    /**
     * Script that, when run, will call `ts2kt`.
     */
    @get:Internal
    var ts2ktScript by ts2ktScriptProperty.field

    @TaskAction
    fun discover() {
        val scriptFile = ts2ktScriptProperty.asFile.get()
        if (!scriptFile.parentFile.mkdirs() && !scriptFile.parentFile.exists()) {
            throw IllegalStateException("Could not create `${scriptFile.parentFile.canonicalPath}`.")
        }
        if (!scriptFile.createNewFile() && !scriptFile.exists()) {
            throw IllegalStateException("Could not create `${scriptFile.canonicalPath}`.")
        }
        if (!scriptFile.setExecutable(true)) {
            throw IllegalStateException("Could not make `${scriptFile.canonicalPath}` executable.")
        }

        scriptFile.writeText(ts2ktScriptDescriptor().fileContent(ts2ktInvocation()))
    }

    private fun ts2ktInvocation(): String {
        val ts2ktInstalled = ts2ktProvidedExecutableProperty.asFile.orNull ?: findPath.find("ts2kt")
        if (ts2ktInstalled != null && ts2ktInstalled.canExecute()) {
            return ts2ktInstalled.canonicalPath
        }

        findPath.find("npx") ?: throw IllegalStateException("`npx` is not installed. Cannot run `ts2kt`.")
        return ts2ktScriptDescriptor().npxInvocation(getPackageOption())
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

/**
 * Information on what to name the ts2kt script and what to put inside, based on the build platform
 */
private fun ts2ktScriptDescriptor() = with(OperatingSystem.current()) {
    when {
        isWindows -> Script.Bat
        isUnix -> Script.Sh
        else -> throw IllegalStateException("Unknown edge case: Platform that is neither Windows not Unix")
    }
}


private sealed class Script(
        val fileName: String,
        val npxInvocation: (packageOption: String) -> String,
        val fileContent: (ts2ktInvocation: String) -> String
) {
    object Bat : Script(
            "ts2kt.bat",
            { packageOption -> "npx -p $packageOption ts2kt" },
            { ts2ktInvocation ->
                  "@echo off\r\n" +
                  "$ts2ktInvocation \"%*\"\r\n"
            }
    )
    object Sh : Script(
            "ts2kt.sh",
            { packageOption -> "npx -p '$packageOption' ts2kt" },
            { ts2ktInvocation -> """
                    |#!/usr/bin/env sh
                    |$ts2ktInvocation "$@"
                """.trimMargin("|") })
}
