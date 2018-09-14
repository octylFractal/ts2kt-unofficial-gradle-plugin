/*
 * This file is part of functional-tests, licensed under the MIT License (MIT).
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

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.BeforeClass
import org.junit.rules.TemporaryFolder
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for some simple real-world examples.
 */
class RealExampleTest {
    //    @Rule
    @JvmField
    val testProjectDir = TemporaryFolder()

    private lateinit var buildFile: File

    companion object {
        @BeforeClass
        @JvmStatic
        fun cleanupRepositoryCaches() {
            cleanTs2ktRepositoryCaches()
        }
    }

    @BeforeTest
    fun initialize() {
        testProjectDir.create()
        buildFile = testProjectDir.newFile("build.gradle.kts")!!
    }

    private fun buildFileWithPlugins(moreText: String) {
        buildFile.writeText("""
            plugins {
                kotlin("jvm") version embeddedKotlinVersion
                id("ts2kt-unofficial-gradle-plugin")
            }

            repositories {
                jcenter()
            }
        """.trimIndent() + "\n" + moreText)
    }

    private fun runGradle(configBlock: GradleRunner.() -> Unit): BuildResult {
        return GradleRunner.create()
                .withProjectDir(testProjectDir.root)
                .withTs2ktPluginClasspath()
                .apply(configBlock)
                .build()
    }

    @Test
    fun generateAdd2HomeStubs() {
        buildFileWithPlugins("""
            ts2ktUnofficial {
                dependencies {
                    "ts2ktUnofficial"("types:add2home:2.0.29")
                }
            }

            val getStub by tasks.registering(Copy::class) {
                from(tasks.named("convertTypescriptToKotlin"))
                into("${'$'}buildDir/kt-stubs/")
            }
        """.trimIndent())
        val result = runGradle {
            withArguments("getStub", "-S")
        }

        println(result.output)
        assertEquals(TaskOutcome.SUCCESS, result.task(":getStub")!!.outcome)
    }

}