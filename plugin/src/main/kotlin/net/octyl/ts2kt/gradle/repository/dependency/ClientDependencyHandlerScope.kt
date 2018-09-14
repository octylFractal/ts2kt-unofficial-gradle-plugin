package net.octyl.ts2kt.gradle.repository.dependency

import net.octyl.ts2kt.gradle.Ts2ktUnofficialExtension

class ClientDependencyHandlerScope(private val extension: Ts2ktUnofficialExtension) {
    operator fun String.invoke(dependencyNotation: Any): ExternalClientDependency {
        return when (dependencyNotation) {
            is Map<*, *> -> {
                @Suppress("UNCHECKED_CAST")
                val asMap = dependencyNotation as Map<String, String>
                val name = asMap["name"] ?: throw IllegalArgumentException("No name specified.")

                this(asMap["group"], name, asMap["version"])
            }
            is String -> {
                val parts = dependencyNotation.split(':')
                if (parts.size < 2) {
                    throw IllegalArgumentException("Illegal string notation." +
                            " Must be at least <group>:<name>. Use :<name> if you have no group.")
                }
                val (group, name) = parts
                val version = parts.getOrNull(2)

                this(group, name, version)
            }
            else -> throw IllegalArgumentException("Illegal type for dependency notation: " +
                    dependencyNotation.javaClass.name)
        }
    }

    operator fun String.invoke(group: String?,
                               name: String,
                               version: String? = null): ExternalClientDependency {
        val groupFixed = when {
            group.isNullOrBlank() -> null
            else -> group
        }
        return ExternalClientDependency(groupFixed, name, version)
                .also { extension.getClientConfiguration(this).dependencies.add(it) }
    }
}
