import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.techshroom.incise-blue") version "0.2.1"
    kotlin("jvm") version embeddedKotlinVersion
}

inciseBlue {
    license()
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

repositories {
    jcenter()
    gradlePluginPortal()
    maven {
        name = "KotlinX"
        url = uri("https://kotlin.bintray.com/kotlinx/")
    }
    maven {
        name = "Ktor"
        url = uri("https://kotlin.bintray.com/ktor/")
    }
}

val sourceSetMain = sourceSets["main"]!!

dependencies {
    implementation(project(":${rootProject.name}-plugin"))
    implementation(embeddedKotlin("gradle-plugin"))

    testImplementation(gradleTestKit())
    testImplementation(group = "junit", name = "junit", version = "4.12")
    testImplementation(embeddedKotlin("test-junit"))
    testImplementation(group = "com.squareup", name = "kotlinpoet", version = "1.0.0-RC3") {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib-jdk7")
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-reflect")
    }
}

val createClasspathManifest by tasks.registering {
    val outputDir = file("$buildDir/$name-test-manifest")
    val outputFile = file("$outputDir/plugin-classpath.txt")

    inputs.files(sourceSetMain.runtimeClasspath)
    outputs.dir(outputDir)

    doLast {
        outputFile.writer().use {
            sourceSetMain.runtimeClasspath.forEach { entry ->
                it.write(entry.canonicalPath + "\n")
            }
        }
    }
}

dependencies {
    testRuntime(files(createClasspathManifest))
}
