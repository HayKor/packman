package ru.haykor.pacman.npm

import ru.haykor.pacman.PacmanException

/**
 * Raised when the npm registry cannot be queried or its response cannot be understood.
 *
 * @param message description of the problem addressed to the user.
 */
class RegistryException(message: String) : PacmanException(message)

/**
 * Manifest of one version of an npm package.
 *
 * @property name package name.
 * @property version concrete version.
 * @property dependencies runtime dependencies declared by this version.
 */
data class PackageManifest(
    val name: String,
    val version: String,
    val dependencies: Map<String, String>,
)

/**
 * Registry document of an npm package.
 *
 * @property name package name.
 * @property latestVersion version referenced by the `latest` distribution tag.
 * @property manifests manifests of all published versions indexed by version.
 */
data class PackageInfo(
    val name: String,
    val latestVersion: String,
    val manifests: Map<String, PackageManifest>,
) {
    /**
     * Manifest of the version referenced by the `latest` tag.
     *
     * @throws RegistryException when the document has no manifest for that version.
     */
    val latest: PackageManifest
        get() = manifests[latestVersion]
            ?: throw RegistryException(
                "registry response for '$name' has no manifest for version '$latestVersion'",
            )
}
