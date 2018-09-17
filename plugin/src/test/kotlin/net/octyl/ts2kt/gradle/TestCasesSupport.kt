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

import kotlin.test.assertEquals

typealias Generator<I, R> = (I) -> R

fun <I, R> verify(test: Generator<I, R>, inputs: TestInputs<I, R>.() -> Unit) {
    TestInputs(test).inputs()
}

class TestInputs<out I, R>(@PublishedApi internal val test: Generator<@UnsafeVariance I, R>) {

    inline fun <reified EX : Exception> failsWith(input: @UnsafeVariance I,
                                                  exceptionAssertions: (EX) -> Unit = {}) {
        try {
            test(input)
        } catch (ex: Exception) {
            when (ex) {
                is EX -> exceptionAssertions(ex)
                else -> assertEquals(EX::class.java, ex.javaClass,
                        "Wrong exception for input `$input`")
            }
        }
    }


    fun converts(pair: Pair<@UnsafeVariance I, R>) {
        val (input, result) = pair
        val actual = test(input)
        assertEquals(result, actual, "Wrong output for input `$input`")
    }

}
