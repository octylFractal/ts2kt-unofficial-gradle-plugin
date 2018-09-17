# ts2kt-unofficial-gradle-plugin
An **unofficial** Gradle plugin for [ts2kt].

[![Gradle Plugin Portal][GPPBadge]][Gradle Plugin Portal]
[![Build Status][TCIBadge]][Travis CI]

Example (Kotlin DSL):
```kotlin
configure<Ts2ktUnofficialExtension> {
    dependencies {
        // request NPM package `@types/jquery@3.3.6`:
        "ts2ktUnofficial"("@types/jquery@3.3.6")
        // request NPM package `react@16.5.1`:
        "ts2ktUnofficial"("react@16.5.1")
    }
}
```

Note: This plugin uses longer names to avoid conflicts if an official plugin is
developed.

## Installation

The plugin is available on the [Gradle Plugin Portal]. Installation
instructions are available there.

The plugin uses `npx` to run `ts2kt` if `ts2kt` isn't on the path.
`npx` will either use the locally installed version (`./node_modules/`)
 or a version specified by the plugin.

You can configure the version of `ts2kt` used when running it through `npx`
 by setting `ts2ktUnofficial.ts2ktVersion`.

## Dependency Resolution

The dependency notation can be one of the following:

- The typical Gradle string notation, `group:name:version`
- The typical Gradle map notation, `mapOf("group" to "<group>", "name" to "<name>", ...)`
- `package.json`-like notation, `@group/name@version` or `name@version`
- Individual elements, `group = "group", name = "name", version = "version"`

Only the three elements, `group/name/version` are used, and `name`
must be specified in all notations. Changing versions are not
supported, such as `-SNAPSHOT`, dynamic versions, or NPM's tags. PRs are
welcome for additional resolution support.

Note that while `version` is not a required element, it is necessary
for the current resolver to find the download, even if the dependency
has its version specified by another, since a dependency graph is
not constructed and version info never transfers between dependencies.
A better resolution system may be written by me, @kenzierocks, if I 
find I need it, but otherwise it will have to be added by someone
else, since I don't have the time to do so.

The dependency resolver also uses a simplistic method of selecting the
dependent version, that won't merge packages of the same name and different
version. It simply takes the version listed in the `dependencies` section
of the `package.json`, and strips away anything that doesn't look like
a version number. PRs are welcome for changing this to a more `npm`-like
resolution, to avoid surprises.

[ts2kt]: https://github.com/Kotlin/ts2kt
[Gradle Plugin Portal]: https://plugins.gradle.org/plugin/net.octyl.ts2kt-unofficial
[GPPBadge]: https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/net/octyl/ts2kt-unofficial/net.octyl.ts2kt-unofficial.gradle.plugin/maven-metadata.xml.svg?label=Gradle%20Plugin%20Portal
[Travis CI]: https://travis-ci.com/kenzierocks/ts2kt-unofficial-gradle-plugin
[TCIBadge]: https://travis-ci.com/kenzierocks/ts2kt-unofficial-gradle-plugin.svg?branch=master
