package ru.haykor.pacman.testrepo

/**
 * Description of a test repository: direct dependencies of every package.
 *
 * @property dependencies dependency names indexed by package name.
 */
data class TestRepository(val dependencies: Map<String, List<String>>) {
    /**
     * Looks up direct dependencies of a package.
     *
     * @param name package name.
     * @return dependency names or `null` when the package is not declared.
     */
    fun dependenciesOf(name: String): List<String>? = dependencies[name]
}
