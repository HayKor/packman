package ru.haykor.pacman.npm

/**
 * Source of npm package documents.
 */
interface NpmRegistryClient {
    /**
     * Requests the registry document of a package.
     *
     * @param name package name.
     * @return parsed registry document.
     * @throws RegistryException when the package cannot be fetched.
     */
    suspend fun fetchPackage(name: String): PackageInfo
}
