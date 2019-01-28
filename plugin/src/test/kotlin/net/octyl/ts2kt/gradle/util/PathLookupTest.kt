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

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PathLookupTest {

//    private val pathLookup = PathLookup.fromSystemEnv()
//
//    @Test
//    fun canFindCommonPrograms() {
//        listOf("ls", "cat", "env", "tee", "head", "tail")
//                .forEach { pathLookup.assertCanFindProgram(it) }
//    }
//
//    @Test
//    fun doesNotFindInvalidPrograms() {
//        listOf("cd", "this isn't a program")
//                .forEach { pathLookup.assertCanFindProgram(it, shouldFind = false) }
//    }
//
//    private fun PathLookup.assertCanFindProgram(name: String, shouldFind: Boolean = true) {
//        val found = find(name) != null
//        when {
//            shouldFind -> assertTrue(found, "Did not find program `$name`.")
//            else -> assertFalse(found, "Should not have found program `$name`.")
//        }
//    }

}