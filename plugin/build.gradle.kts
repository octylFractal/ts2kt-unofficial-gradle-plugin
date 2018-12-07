import net.researchgate.release.ReleaseExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.techshroom.incise-blue") version "0.2.1"
    id("net.researchgate.release") version "2.7.0"
    id("com.gradle.plugin-publish") version "0.10.0"
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

inciseBlue {
    license()
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions.jvmTarget = "1.6"
}

gradlePlugin {
    plugins {
        register("ts2kt-unofficial") {
            id = "${project.group}.ts2kt-unofficial"
            implementationClass = "net.octyl.ts2kt.gradle.Ts2ktUnofficialPlugin"
        }
    }
}

pluginBundle {
    website = "https://github.com/kenzierocks/ts2kt-unofficial-gradle-plugin"
    vcsUrl = website

    description = "An unofficial plugin for running `ts2kt` on NPM dependencies."
    tags = listOf("ts2kt", "typescript", "kotlin")

    plugins {
        getByName("ts2kt-unofficial") {
            displayName = "Typescript to Kotlin Gradle Plugin"
        }
    }
}

configure<ReleaseExtension> {
    versionPropertyFile = rootProject.file("gradle.properties").canonicalPath
}
tasks.named("afterReleaseBuild").configure {
    dependsOn("publishPlugins")
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

dependencies {
    val ktorVersion = "1.0.1"
    implementation(group = "io.ktor", name = "ktor-client-apache", version = ktorVersion)

    val jacksonVersion = "2.9.7"
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-core", version = jacksonVersion)
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = jacksonVersion)
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = jacksonVersion)

    // We expect these at runtime, stdlib from Gradle
    // gradle-plugin from separate application in build script
    compileOnly(embeddedKotlin("stdlib-jdk8"))
    compileOnly(embeddedKotlin("gradle-plugin"))

    testImplementation(group = "junit", name = "junit", version = "4.12")
    testImplementation(embeddedKotlin("test-junit"))
}
