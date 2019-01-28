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
package net.octyl.ts2kt.gradle.repository.npm

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import io.ktor.client.HttpClient
import io.ktor.client.call.call
import io.ktor.client.engine.apache.Apache
import io.ktor.client.request.url
import io.ktor.client.response.readText
import io.ktor.http.HttpMethod
import io.ktor.util.cio.writeChannel
import kotlinx.coroutines.io.close
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.io.copyTo
import net.octyl.ts2kt.gradle.repository.ClientRepository
import net.octyl.ts2kt.gradle.repository.ResolutionResult
import net.octyl.ts2kt.gradle.repository.dependency.ClientDependency
import net.octyl.ts2kt.gradle.repository.dependency.ExternalClientDependency
import net.octyl.ts2kt.gradle.util.PartialPackageInfo
import org.gradle.api.Project
import org.gradle.internal.os.OperatingSystem
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

class NpmClientRepository(private val project: Project) : ClientRepository {

    private val logger = LoggerFactory.getLogger(javaClass)

    private val cacheVersion = 1
    private val cacheDirectory = project.gradle.gradleUserHomeDir
            .resolve("caches/ts2kt-unoff-$cacheVersion/")

    private val mapper = ObjectMapper()
            .registerModules(KotlinModule())

    private val registryUrl: String by lazy {
        val cap = ByteArrayOutputStream()

        val npm = with (OperatingSystem.current()) {
            when {
                // on Windows, NPM is a .bat file that needs to run under cmd.exe
                isWindows -> arrayOf("cmd", "/c", "npm")
                isLinux -> arrayOf("npm")
                else -> {
                    // A good default, but this command may not work unless you have tested the platform. Warn the user.
                    project.logger.warn("Executing 'npm', this command may not be correct on the current build platform.")
                    arrayOf("npm")
                }
            }
        }

        project.exec {
            commandLine(*npm, "config", "get", "registry")
            standardOutput = cap
        }

        cap.toString(StandardCharsets.UTF_8.name())
                .trim()
                .trimEnd('/')
    }

    private val client = HttpClient(Apache) {
    }

    override fun resolveDependency(dependency: ClientDependency): ResolutionResult {
        return when {
            (dependency is ExternalClientDependency
                    && dependency.version != null) -> runBlocking { doResolve(dependency) }
            else -> ResolutionResult.Error()
        }
    }

    private suspend fun doResolve(dependency: ExternalClientDependency): ResolutionResult {
        val resolveInfo = dependency.resolveInfo(cacheDirectory, registryUrl)

        if (!resolveInfo.downloadTarget.exists()) {
            resolveInfo.downloadPackage()
                    .let { err ->
                        when {
                            err != null -> return err
                            else -> Unit
                        }
                    }
        }
        if (!resolveInfo.unpackedFlagFile.exists()) {
            resolveInfo.unpackPackage()

            if (!resolveInfo.unpackedFlagFile.createNewFile()) {
                logger.warn("Unable to create unpack caching marker." +
                        " This will prevent caching of NPM packages.")
            }
        }

        val packageInfo = mapper.readValue<PartialPackageInfo>(resolveInfo.packageJsonSource)

        val files = project.fileTree(resolveInfo.packageRoot) {
            include("**/*.d.ts")
        }
        val dependencies = packageInfo.getGradleDependencies()

        return ResolutionResult.Success(files, dependencies)
    }

    private fun PartialPackageInfo.getGradleDependencies(): List<ClientDependency> {
        return getDependenciesWithDirectVersions()
                .map { (name, version) ->
                    var group: String? = null
                    var nameFixed = name
                    if (name.firstOrNull() == '@') {
                        val (g, n) = name.substring(1).split("/")
                        group = g
                        nameFixed = n
                    }
                    ExternalClientDependency(group, nameFixed, version)
                }
    }

    private suspend fun DependencyResolveInfo.downloadPackage(): ResolutionResult? {
        val call = client.call {
            method = HttpMethod.Get
            url(dependencyUrl)
        }
        when (call.response.status.value) {
            in 200..299 -> Unit
            404, 405 -> return ResolutionResult.Error(NoSuchElementException(call.request.url.toString()))
            in 500..599 -> return ResolutionResult.Error(RuntimeException(call.response.readText()))
        }

        // copy to temporary file first
        val temporaryFile = downloadTarget.resolveSibling(".dl.${downloadTarget.name}")
        val writeChannel = temporaryFile.writeChannel()
        // default arguments don't work here due to KT-24461
        call.response.content.copyTo(writeChannel, Long.MAX_VALUE)
        writeChannel.flush()
        writeChannel.close()
        // then do a move to the actual file, which is less likely to be interrupted
        try {
            // Platform-independent rename/move operation
            Files.move(temporaryFile.toPath(), downloadTarget.toPath(), StandardCopyOption.REPLACE_EXISTING)
        } catch (t: Throwable) {
            // More descriptive file rename error information
            return ResolutionResult.Error(t)
        }
        return null
    }

    private fun DependencyResolveInfo.unpackPackage() {
        with(project) {
            copy {
                from(tarTree(downloadTarget))

                include("**/*.d.ts")
                include("*/package.json")

                into(unpackTarget)
            }
        }
    }

    override fun toString(): String {
        return "${javaClass.simpleName}[registry=$registryUrl]"
    }

}