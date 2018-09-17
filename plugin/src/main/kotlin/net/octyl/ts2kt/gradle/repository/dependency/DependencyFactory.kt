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
package net.octyl.ts2kt.gradle.repository.dependency

object DependencyFactory {
    fun createDependency(group: String?,
                         name: String,
                         version: String? = null): ExternalClientDependency {
        val groupFixed = when {
            group.isNullOrBlank() -> null
            else -> group
        }
        val versionFixed = when {
            version.isNullOrBlank() -> null
            else -> version
        }
        return ExternalClientDependency(groupFixed, name, versionFixed)
    }

    fun createFromAnyNotation(dependencyNotation: Any): ExternalClientDependency {
        return when (dependencyNotation) {
            is ExternalClientDependency -> dependencyNotation
            is Map<*, *> -> mapDepNotation(dependencyNotation)
            is String -> stringDepNotation(dependencyNotation)
            else -> throw IllegalArgumentException("Invalid type for dependency notation: " +
                    dependencyNotation.javaClass.name)
        }
    }

    internal fun mapDepNotation(dependencyNotation: Any): ExternalClientDependency {
        @Suppress("UNCHECKED_CAST")
        val asMap = dependencyNotation as Map<String, String>
        val name = asMap["name"]
                ?: throw IllegalArgumentException("Invalid dependency notation: `$asMap`, missing name.")

        return createDependency(asMap["group"], name, asMap["version"])
    }

    internal fun stringDepNotation(dependencyNotation: String) = when {
        ":" in dependencyNotation -> gradleStringDepNotation(dependencyNotation)
        else -> pkgJsonStringDepNotation(dependencyNotation)
    }

    internal fun gradleStringDepNotation(dependencyNotation: String): ExternalClientDependency {
        val parts = dependencyNotation.split(':')
        if (parts.size < 2) {
            throw IllegalArgumentException("Invalid dependency notation: `$dependencyNotation`")
        }
        val (group, name) = parts
        val version = parts.getOrNull(2)

        return createDependency(group, name, version)
    }

    private val pkgJsonRegex = Regex(
            "^(?:@([^@/]+?)/)?" // 1. Group matcher, @<group>/
                    + "([^@/]+?)" // 2. Name
                    + "(?:@([^@/]+?))?$" // 3. Version
    )

    internal fun pkgJsonStringDepNotation(dependencyNotation: String): ExternalClientDependency {
        val match = pkgJsonRegex.find(dependencyNotation)
                ?: throw IllegalArgumentException("Invalid dependency notation: `$dependencyNotation`")
        return match.destructured
                .let { (group, name, version) ->
                    createDependency(group, name, version)
                }
    }
}