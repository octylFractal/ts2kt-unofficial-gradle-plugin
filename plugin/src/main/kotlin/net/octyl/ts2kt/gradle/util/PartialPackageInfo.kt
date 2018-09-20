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
package net.octyl.ts2kt.gradle.util

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class PartialPackageInfo(val name: String,
                              val dependencies: Map<String, String>?) {
    companion object {
        // Versions can be modified in package.json using these strings, so toss them:
        private val VERSION_MODIFIERS = setOf(
                "<=", ">="
        ) + "~<>^".asIterable().map { String(charArrayOf(it)) }
    }

    /**
     * Filter [dependencies] so that there are no extra characters in the versions.
     *
     * Node uses `^`, `@`, and others to indicate version ranges,
     * but we just want the first valid version for now.
     */
    fun getDependenciesWithDirectVersions(): Map<String, String> {
        return dependencies?.mapValues { (_, version) ->
            val versionPrefix = VERSION_MODIFIERS.firstOrNull {
                version.startsWith(it)
            }
            val startOfVersion = (versionPrefix ?: "").length

            version.substring(startOfVersion)
        } ?: mapOf()
    }

}
