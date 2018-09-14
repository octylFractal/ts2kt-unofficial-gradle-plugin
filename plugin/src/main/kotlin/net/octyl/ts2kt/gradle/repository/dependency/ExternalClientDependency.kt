package net.octyl.ts2kt.gradle.repository.dependency

/**
 * A dependency that comes from an external server, such as the NPM registry.
 */
data class ExternalClientDependency(override val group: String?,
                                    override val name: String,
                                    override val version: String?) : ClientDependency