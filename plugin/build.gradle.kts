import com.techshroom.inciseblue.InciseBluePluginApplication
import net.researchgate.release.ReleaseExtension
import nl.javadude.gradle.plugins.license.DownloadLicensesExtension.license
import nl.javadude.gradle.plugins.license.LicenseExtension
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmCompile
import org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.techshroom.incise-blue") version "0.0.12"
    id("net.researchgate.release") version "2.7.0"
    `java-gradle-plugin`
    `kotlin-dsl`
    kotlin("jvm") version embeddedKotlinVersion
}

inciseBlue {
    plugins {
        license()
    }
}

configure<LicenseExtension> {
    include("**/*.kt")
    mapping(mapOf("kt" to "SLASHSTAR_STYLE"))
}

tasks.withType<KotlinJvmCompile> {
    kotlinOptions.jvmTarget = "1.6"
}

gradlePlugin {
    plugins {
        register("ts2kt-unofficial-gradle-plugin") {
            id = "ts2kt-unofficial-gradle-plugin"
            implementationClass = "net.octyl.ts2kt.gradle.Ts2ktUnofficialPlugin"
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
    val ktorVersion = "0.9.4"
    implementation(group = "io.ktor", name = "ktor-client-apache", version = ktorVersion)

    val jacksonVersion = "2.9.6"
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-core", version = jacksonVersion)
    implementation(group = "com.fasterxml.jackson.core", name = "jackson-databind", version = jacksonVersion)
    implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-kotlin", version = jacksonVersion)

    implementation(kotlin("stdlib-jdk8"))
    compileOnly(embeddedKotlin("gradle-plugin"))

    testImplementation(group = "junit", name = "junit", version = "4.12")
    testImplementation(embeddedKotlin("test-junit"))
}

configure<KotlinJvmProjectExtension> {
    experimental.coroutines = Coroutines.ENABLE
}
