package ru.haykor.pacman.npm

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive

/**
 * Parser for npm registry documents (packuments).
 */
object NpmPackumentParser {
    /**
     * Parses a registry document.
     *
     * @param text raw JSON returned by the registry.
     * @return parsed package information.
     * @throws RegistryException when the document is malformed or lacks mandatory fields.
     */
    fun parse(text: String): PackageInfo {
        val document = parseJsonObject(text)
        val name = document.stringOrNull(FIELD_NAME)
            ?: throw RegistryException("registry response has no '$FIELD_NAME' field")
        val latestVersion = document[FIELD_DIST_TAGS].objectOrNull()?.stringOrNull(FIELD_LATEST)
            ?: throw RegistryException("registry response has no 'dist-tags.latest' field")
        val versions = document[FIELD_VERSIONS].objectOrNull()
            ?: throw RegistryException("registry response has no '$FIELD_VERSIONS' field")
        val manifests = versions.entries.associate { (version, element) ->
            version to parseManifest(name, version, element)
        }
        return PackageInfo(name = name, latestVersion = latestVersion, manifests = manifests)
    }

    private fun parseJsonObject(text: String): JsonObject {
        val element = try {
            Json.parseToJsonElement(text)
        } catch (error: Exception) {
            throw RegistryException(
                "registry response is not valid JSON: ${error.message?.trimEnd()}",
            )
        }
        return element as? JsonObject
            ?: throw RegistryException("registry response is not a JSON object")
    }

    private fun parseManifest(
        defaultName: String,
        defaultVersion: String,
        element: JsonElement,
    ): PackageManifest {
        val manifest = element as? JsonObject
            ?: throw RegistryException("manifest of version '$defaultVersion' is not an object")
        return PackageManifest(
            name = manifest.stringOrNull(FIELD_NAME) ?: defaultName,
            version = manifest.stringOrNull(FIELD_VERSION) ?: defaultVersion,
            dependencies = readDependencies(manifest[FIELD_DEPENDENCIES]),
        )
    }

    private fun readDependencies(element: JsonElement?): Map<String, String> {
        val dependencies = element as? JsonObject ?: return emptyMap()
        return dependencies.entries.mapNotNull { (name, value) ->
            val range = (value as? JsonPrimitive)?.stringContentOrNull() ?: return@mapNotNull null
            name to range
        }.toMap()
    }

    private fun JsonElement?.objectOrNull(): JsonObject? = this as? JsonObject

    private fun JsonObject.stringOrNull(key: String): String? =
        (this[key] as? JsonPrimitive)?.stringContentOrNull()

    private fun JsonPrimitive.stringContentOrNull(): String? =
        if (isString) content else null

    private const val FIELD_NAME: String = "name"
    private const val FIELD_VERSION: String = "version"
    private const val FIELD_DIST_TAGS: String = "dist-tags"
    private const val FIELD_LATEST: String = "latest"
    private const val FIELD_VERSIONS: String = "versions"
    private const val FIELD_DEPENDENCIES: String = "dependencies"
}
