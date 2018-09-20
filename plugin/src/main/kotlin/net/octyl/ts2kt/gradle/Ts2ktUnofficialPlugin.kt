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
package net.octyl.ts2kt.gradle

import net.octyl.ts2kt.gradle.repository.npm.NpmClientRepository
import net.octyl.ts2kt.gradle.sourceset.Ts2ktNewSourceSetConfiguration
import net.octyl.ts2kt.gradle.tasks.DiscoverTs2ktExecutable
import net.octyl.ts2kt.gradle.util.registerInfer
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.getByType

class Ts2ktUnofficialPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.plugins.withId("kotlin2js") {
            project.doApply()
        }
    }

    private fun Project.doApply() {
        val ext = addExtension()

        ext.clientRepositories.add(NpmClientRepository(this))

        val discoverTaskProvider = addDiscoverTask(ext)

        val sourceSets = extensions.getByType<SourceSetContainer>()
        sourceSets.all { Ts2ktNewSourceSetConfiguration(this@doApply, discoverTaskProvider, this@all).configure() }
    }

    private fun Project.addExtension(): Ts2ktUnofficialExtension {
        return extensions.create("ts2ktUnofficial", this)
    }

    private fun Project.addDiscoverTask(ext: Ts2ktUnofficialExtension): TaskProvider<DiscoverTs2ktExecutable> {
        return project.tasks.registerInfer("discoverTs2ktExecutable") {
            description = "Finds the `ts2kt` executable and saves it for other tasks."
            group = "discovery"

            ts2ktVersionProperty.set(ext.ts2ktVersionProperty)
            ts2ktProvidedExecutableProperty.set(ext.ts2ktExecutableProperty)
        }
    }

}