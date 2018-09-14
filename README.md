# ts2kt-unofficial-gradle-plugin
An **unofficial** Gradle plugin for [ts2kt].

Example (Kotlin DSL):
```kotlin
configure<Ts2ktUnofficialExtension> {
    dependencies {
        // request NPM package `@types/jquery@3.3.6`:
        "ts2ktUnofficial"("types", "jquery", "3.3.6")
        // request NPM package `react@16.5.1`:
        "ts2ktUnofficial"("", "react", "16.5.1")
    }
}
```

Note: This plugin uses longer names to avoid conflicts if an official plugin is
developed.

## Installation

The plugin is available on the [Gradle Plugin Portal]. Installation
instructions are available there.

The plugin uses `npx` to run `ts2kt` if it isn't on the path.
It will either use the locally installed version (`./node_modules/`)
 or a version specified by the plugin.

You can configure the version of `ts2kt` used by default by setting
`ts2ktUnofficial.ts2ktVersion`.

[ts2kt]: https://github.com/Kotlin/ts2kt
[Gradle Plugin Portal]: https://plugins.gradle.org/plugin/net.octyl.ts2kt-unofficial
