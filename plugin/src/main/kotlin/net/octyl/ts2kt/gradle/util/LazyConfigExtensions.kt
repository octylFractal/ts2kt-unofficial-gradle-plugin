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
package net.octyl.ts2kt.gradle.util

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.Directory
import org.gradle.api.file.FileCollection
import org.gradle.api.file.ProjectLayout
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import kotlin.reflect.KProperty

open class ProviderField<T>(private val provider: Provider<T>) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = this.provider.orNull
}

val <T> Provider<T>.field
    get() = ProviderField(this)

class PropertyField<T>(private val property: Property<T>) : ProviderField<T>(property) {
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) = this.property.set(value)
}

val <T> Property<T>.field
    get() = PropertyField(this)

class ListPropertyField<T>(private val property: ListProperty<T>) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>) = this.property.get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: List<T>) = this.property.set(value)
}

val <T> ListProperty<T>.field
    get() = ListPropertyField(this)

class ConfigurableFileCollectionField(
        private val configurableFileCollection: ConfigurableFileCollection) {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): FileCollection = configurableFileCollection
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: FileCollection) = configurableFileCollection.setFrom(value)
}

val ConfigurableFileCollection.field
    get() = ConfigurableFileCollectionField(this)

inline fun <reified T : Task> TaskContainer.registerInfer(name: String, configure: Action<in T>): TaskProvider<T> {
    return register(name, T::class.java).apply { configure(configure) }
}

val ProjectLayout.ts2ktUnofficialDirectory: Provider<Directory>
    get() = buildDirectory.dir("ts2kt-unofficial")

fun Provider<Directory>.file(file: String): Provider<RegularFile> {
    return this.map { it.file(file) }
}
