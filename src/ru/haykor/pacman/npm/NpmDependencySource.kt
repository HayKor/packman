package ru.haykor.pacman.npm

import ru.haykor.pacman.source.DependencySource
import ru.haykor.pacman.source.PackageRequest
import ru.haykor.pacman.source.ResolvedPackage
import ru.haykor.pacman.semver.VersionResolver

/**
 * Dependency source backed by an npm registry.
 *
 * Registry documents are requested at most once per package name.
 *
 * @property registry client used to fetch package documents.
 */
class NpmDependencySource(private val registry: NpmRegistryClient) : DependencySource {
    private val documents = mutableMapOf<String, PackageInfo>()

    override suspend fun resolve(request: PackageRequest): ResolvedPackage {
        val document = documents[request.name] ?: registry.fetchPackage(request.name).also {
            documents[request.name] = it
        }
        val version = resolveVersion(document, request)
        val manifest = document.manifests[version]
            ?: throw RegistryException(
                "registry response for '${request.name}' has no manifest for version '$version'",
            )
        return ResolvedPackage(
            name = manifest.name,
            version = manifest.version,
            dependencies = manifest.dependencies,
        )
    }

    private fun resolveVersion(document: PackageInfo, request: PackageRequest): String {
        if (request.versionRange == DependencySource.LATEST_TAG) {
            return document.latestVersion
        }
        return VersionResolver.resolve(document.manifests.keys, request.versionRange)
    }
}
