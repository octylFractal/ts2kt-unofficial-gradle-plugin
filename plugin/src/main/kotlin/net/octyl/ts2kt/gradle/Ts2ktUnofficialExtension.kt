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
package net.octyl.ts2kt.gradle

import net.octyl.ts2kt.gradle.repository.ClientRepository
import net.octyl.ts2kt.gradle.repository.configuration.ClientConfiguration
import net.octyl.ts2kt.gradle.repository.dependency.ClientDependencyHandlerScope
import net.octyl.ts2kt.gradle.util.field
import org.gradle.api.Project
import org.gradle.kotlin.dsl.property
import org.gradle.kotlin.dsl.setProperty

open class Ts2ktUnofficialExtension(private val project: Project) {

    val ts2ktVersionProperty = project.objects.property<String>().apply { set("0.1.3") }
    val ts2ktExecutableProperty = project.objects.fileProperty()

    /**
     * Version of the NPM package `ts2kt` to install.
     * Defaults to `0.1.3`.
     */
    var ts2ktVersion by ts2ktVersionProperty.field

    /**
     *  Direct path to the `ts2kt` program to run.
     */
    var ts2ktExecutable by ts2ktExecutableProperty.field

    val clientConfigurations = mutableMapOf<String, ClientConfiguration>()

    fun getClientConfiguration(name: String): ClientConfiguration {
        return clientConfigurations[name]
                ?: throw IllegalArgumentException("No configuration with name `$name`.")
    }

    fun createClientConfiguration(name: String): ClientConfiguration {
        return ClientConfiguration(name, project, clientRepositories)
                .also { clientConfigurations[it.name] = it }
    }

    fun getOrCreateClientConfiguration(name: String): ClientConfiguration {
        return clientConfigurations[name] ?: createClientConfiguration(name)
    }

    val clientRepositories = project.objects.setProperty<ClientRepository>().empty()

    fun dependencies(block: ClientDependencyHandlerScope.() -> Unit) {
        ClientDependencyHandlerScope(this).block()
    }

}
