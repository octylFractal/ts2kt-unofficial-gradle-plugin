# ts2kt-unofficial-gradle-plugin
An **unofficial** Gradle plugin for [ts2kt].

Example (Kotlin DSL):
```kotlin
configure<Ts2KtUnofficialExtension> {
    dependencies {
        // request NPM package `@types/jquery` (for separately packaged types):
        definitelyTyped("jquery")
        // request NPM package `react` (for types in packages):
        npm("react")
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

## Future plans

It would be nice to leverage Gradle's repository system, rather than
hacking out the dependency objects and resolving it totally differently.
The current pattern leaves an unresolveable configuration that can't be
used normally.


[ts2kt]: https://github.com/Kotlin/ts2kt
[Gradle Plugin Portal]: https://plugins.gradle.org
