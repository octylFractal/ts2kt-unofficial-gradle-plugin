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

import java.io.File
import javax.inject.Inject

class RunTs2ktForPackage @Inject constructor(
        private val ts2ktScript: File,
        private val inputFile: File,
        private val outputDirectory: File
) : Runnable {
    override fun run() {
        if (!outputDirectory.mkdirs() && !outputDirectory.exists()) {
            throw IllegalStateException("Failed to create ${outputDirectory.canonicalPath}.")
        }

        val process = ProcessBuilder(ts2ktScript.canonicalPath,
                "-d", ".",
                inputFile.canonicalPath)
                .directory(outputDirectory)
                .inheritIO()
                .start()
        if (process.waitFor() != 0) {
            throw IllegalStateException("Failed to run ts2kt, exit code ${process.exitValue()}." +
                    " See logs for more detail.")
        }
    }
}
