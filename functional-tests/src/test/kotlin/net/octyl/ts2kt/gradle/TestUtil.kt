/*
 * This file is part of ts2kt-unofficial-gradle-plugin-functional-tests, licensed under the MIT License (MIT).
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
package net.octyl.ts2kt.gradle

import com.squareup.kotlinpoet.FileSpec
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

fun cleanTs2ktRepositoryCaches() {
    val tmpDir = System.getProperties()["java.io.tmpdir"]
    val user = System.getProperties()["user.name"]
    File("$tmpDir/.gradle-test-kit-$user/caches/ts2kt-unoff-1").deleteRecursively()
}

const val PKG_BASE = "testsrc"

val TemporaryFolder.kotlinSrcDir: File
    get() = newFolder(*"src/main/kotlin".split("/").toTypedArray())

fun TemporaryFolder.writeSrcFile(name: String,
                                 extraPkgName: String = "",
                                 weirdImports: Set<String> = setOf(),
                                 specBlock: FileSpec.Builder.() -> Unit) {
    val pkgName = when {
        extraPkgName.isBlank() -> PKG_BASE
        else -> "$PKG_BASE.${extraPkgName.trim()}"
    }
    val spec = FileSpec.builder(pkgName, name)
            .apply(specBlock)
            .build()


    val content = StringBuilder()
    spec.writeTo(content)

    val pkgStart = content.indexOf("package")
    val afterPkgLine = content.indexOf("\n", pkgStart) + 1
    content.insert(afterPkgLine, weirdImports.asSequence()
            .sorted()
            .map { "import $it" }
            .joinToString(separator = "\n"))

    val outputFile = spec.getOutputFile(kotlinSrcDir.toPath())
    Files.newBufferedWriter(outputFile).use { it.write(content.toString()) }
}

private fun FileSpec.getOutputFile(directory: Path): Path {
    require(Files.notExists(directory) || Files.isDirectory(directory)) {
        "path $directory exists but is not a directory."
    }
    var outputDirectory = directory
    if (packageName.isNotEmpty()) {
        for (packageComponent in packageName.split('.').dropLastWhile { it.isEmpty() }) {
            outputDirectory = outputDirectory.resolve(packageComponent)
        }
        Files.createDirectories(outputDirectory)
    }

    return outputDirectory.resolve("$name.kt")
}
