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
package net.octyl.ts2kt.gradle.repository.npm

import net.octyl.ts2kt.gradle.repository.dependency.ClientDependency
import java.io.File

internal data class DependencyResolveInfo(val owner: ClientDependency,
                                          val groupCacheDir: File,
                                          val dependencyUrl: String) {
    val downloadTarget: File by lazy {
        groupCacheDir.resolve("./${owner.name}-${owner.version}.tgz")
    }

    val unpackTarget: File by lazy {
        groupCacheDir.resolve("./${owner.name}/${owner.version}/")
    }

    val packageRoot: File by lazy {
        unpackTarget.listFiles()?.firstOrNull(File::isDirectory)
                ?: throw IllegalStateException("No package root in `${unpackTarget.canonicalPath}`!")
    }

    val packageJsonSource: File by lazy {
        packageRoot.resolve("package.json")
    }

    val unpackedFlagFile: File by lazy {
        unpackTarget.resolve(".ts2kt-unpacked")
    }
}

internal fun ClientDependency.resolveInfo(cacheDirectory: File, registryUrl: String): DependencyResolveInfo {
    val groupCacheDir = cacheDirectory.resolve("./$group")
    val dependencyUrl = "$registryUrl/@$group/$name/-/$name-$version.tgz"

    if (!groupCacheDir.mkdirs() && !groupCacheDir.exists()) {
        throw IllegalStateException("Cannot create directory `${groupCacheDir.canonicalPath}`.")
    }

    return DependencyResolveInfo(this, groupCacheDir, dependencyUrl)
}