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

import com.squareup.kotlinpoet.ClassName
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.BeforeClass
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import java.nio.file.Files
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for some simple real-world examples.
 */
class RealExampleTest {
    @Rule
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
        buildFile = testProjectDir.newFile("build.gradle.kts")!!
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
        buildFile.writeGradleSetupAnd("""
            ts2ktUnofficial {
                dependencies {
                    "ts2ktUnofficial"("@types/add2home@2.0.29")
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

        assertEquals(TaskOutcome.SUCCESS, result.task(":getStub")!!.outcome)
        assertEquals(1, countFiles(testProjectDir.root.resolve("build/kt-stubs")))
    }

    private fun countFiles(dir: File): Long {
        return Files.walk(dir.toPath())
                .filter { Files.isRegularFile(it) }
                .count()
    }

    @Test
    fun testBigIntStubs() {
        buildFile.writeGradleSetupAnd("""
            configure<${Ts2ktUnofficialExtension::class.java.name}> {
                dependencies {
                    "ts2ktUnofficial"("big-integer@1.6.36")
                }
            }
        """.trimIndent())

        // types for the code below:
        @Suppress("LocalVariableName")
        val BigInteger = ClassName.bestGuess("bigInt.BigInteger")

        testProjectDir.writeSrcFile(
                "TestBigIntStubs",
                weirdImports = setOf("bigInt")) {
            `fun`("add5ToInput") {
                addParameter("input", Int::class)
                returns(BigInteger)
                addCode("""
                        |val inputBigInt = bigInt(input)
                        |return inputBigInt.add(5)
                        """.trimMargin())
            }
        }

        val result = runGradle {
            withArguments("build", "-S")
        }

        assertEquals(TaskOutcome.SUCCESS, result.task(":build")!!.outcome)
    }

}