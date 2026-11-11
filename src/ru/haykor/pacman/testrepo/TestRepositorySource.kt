package ru.haykor.pacman.testrepo

import ru.haykor.pacman.ExecutionException
import ru.haykor.pacman.source.DependencySource
import ru.haykor.pacman.source.PackageRequest
import ru.haykor.pacman.source.ResolvedPackage

/**
 * Dependency source backed by a test repository description.
 *
 * @property repository parsed description of the test repository.
 */
class TestRepositorySource(private val repository: TestRepository) : DependencySource {
    override suspend fun resolve(request: PackageRequest): ResolvedPackage {
        val dependencies = repository.dependenciesOf(request.name)
            ?: throw ExecutionException(
                "package '${request.name}' is not declared in the test repository",
            )
        return ResolvedPackage(
            name = request.name,
            version = "",
            dependencies = dependencies.associateWith { TEST_RANGE },
        )
    }

    private companion object {
        const val TEST_RANGE: String = "*"
    }
}
