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
package net.octyl.ts2kt.gradle.repository.dependency

import net.octyl.ts2kt.gradle.TestInputs
import net.octyl.ts2kt.gradle.verify
import kotlin.test.Test
import kotlin.test.assertTrue

class DependencyNotationTest {

    private fun dep(group: String?, name: String, version: String?) =
            ExternalClientDependency(group, name, version)

    private val groupNameVersion = dep("group", "name", "version")
    private val groupName = groupNameVersion.copy(version = null)
    private val nameVersion = groupNameVersion.copy(group = null)
    private val name = groupNameVersion.copy(group = null, version = null)

    @Test
    fun createAnyNotationTest() {
        verify(DependencyFactory::createFromAnyNotation) {
            gradleNotationSuccessInputs()
            pkgJsonNotationSuccessInputs()
            mapNotationSuccessInputs()

            pkgJsonNotationFailInputs()
            mapNotationFailInputs()
            failsWith<IllegalArgumentException>(Unit) { ex ->
                assertTrue("Invalid type for dependency notation" in (ex.message ?: ""))
            }
        }
    }

    // mostly to validate that "name" is actually valid in this mode
    @Test
    fun generalStringNotation() {
        verify(DependencyFactory::stringDepNotation) {
            gradleNotationSuccessInputs()
            pkgJsonNotationSuccessInputs()

            pkgJsonNotationFailInputs()
        }
    }

    @Test
    fun gradleStringNotation() {
        verify(DependencyFactory::gradleStringDepNotation) {
            gradleNotationSuccessInputs()

            failsWith<IllegalArgumentException>("name") { ex ->
                assertTrue("Invalid dependency notation" in (ex.message ?: ""))
            }
        }
    }

    private fun TestInputs<Any, ExternalClientDependency>.gradleNotationSuccessInputs() {
        converts("group:name:version" to groupNameVersion)
        converts(":name:version" to nameVersion)
        converts(":name" to name)
        converts(":name:" to name)
        converts("group:name" to groupName)
    }

    @Test
    fun pkgJsonStringNotation() {
        verify(DependencyFactory::pkgJsonStringDepNotation) {
            pkgJsonNotationSuccessInputs()

            pkgJsonNotationFailInputs()
        }
    }

    private fun TestInputs<Any, ExternalClientDependency>.pkgJsonNotationSuccessInputs() {
        converts("name" to name)
        converts("name@version" to nameVersion)
        converts("@group/name" to groupName)
        converts("@group/name@version" to groupNameVersion)
    }

    private fun TestInputs<Any, ExternalClientDependency>.pkgJsonNotationFailInputs() {
        failsWith<IllegalArgumentException>("@group") { ex ->
            assertTrue("Invalid dependency notation" in (ex.message ?: ""))
        }
        failsWith<IllegalArgumentException>("@group/") { ex ->
            assertTrue("Invalid dependency notation" in (ex.message ?: ""))
        }
        failsWith<IllegalArgumentException>("@version") { ex ->
            assertTrue("Invalid dependency notation" in (ex.message ?: ""))
        }
        failsWith<IllegalArgumentException>("/name@version") { ex ->
            assertTrue("Invalid dependency notation" in (ex.message ?: ""))
        }
    }

    private fun mapNotation(vararg keys: String): Map<String, String> {
        return HashMap<String, String>().also {
            keys.forEach { key -> it[key] = key }
        }
    }

    @Test
    fun mapNotation() {
        verify(DependencyFactory::mapDepNotation) {
            mapNotationSuccessInputs()

            mapNotationFailInputs()
        }
    }

    private fun TestInputs<Any, ExternalClientDependency>.mapNotationSuccessInputs() {
        converts(mapNotation("name") to name)
        converts(mapNotation("name", "version") to nameVersion)
        converts(mapNotation("group", "name") to groupName)
        converts(mapNotation("group", "name", "version") to groupNameVersion)
    }

    private fun TestInputs<Any, ExternalClientDependency>.mapNotationFailInputs() {
        failsWith<IllegalArgumentException>(mapNotation("group")) { ex ->
            assertTrue("Invalid dependency notation" in (ex.message ?: ""))
        }
        failsWith<IllegalArgumentException>(mapNotation("version")) { ex ->
            assertTrue("Invalid dependency notation" in (ex.message ?: ""))
        }
        failsWith<IllegalArgumentException>(mapNotation("group", "version")) { ex ->
            assertTrue("Invalid dependency notation" in (ex.message ?: ""))
        }
    }
}
