package ru.haykor.pacman.source

/**
 * Request for one dependency: package name and declared version range.
 *
 * @property name package name.
 * @property versionRange declared range or the registry tag.
 */
data class PackageRequest(val name: String, val versionRange: String)

/**
 * Dependency resolved to a concrete version.
 *
 * @property name package name.
 * @property version resolved version, empty for test repositories.
 * @property dependencies direct dependencies declared by this version.
 */
data class ResolvedPackage(
    val name: String,
    val version: String,
    val dependencies: Map<String, String>,
) {
    /** Stable node identifier used inside the dependency graph. */
    val id: String
        get() = if (version.isEmpty()) name else "$name@$version"
}

/**
 * Source of resolved dependencies.
 */
interface DependencySource {
    /**
     * Resolves a package request.
     *
     * @param request package name and version range.
     * @return resolved package with its direct dependencies.
     */
    suspend fun resolve(request: PackageRequest): ResolvedPackage

    companion object {
        /** Range that selects the `latest` distribution tag. */
        const val LATEST_TAG: String = "latest"
    }
}
