package net.octyl.ts2kt.gradle.repository.dependency

import net.octyl.ts2kt.gradle.repository.ClientRepository

/**
 * Represents an ID for a set of artifacts that can be acquired by asking a [ClientRepository]
 * to collect them.
 */
interface ClientDependency {
    val group: String?
    val name: String
    val version: String?
}