package ru.haykor.pacman.cli

import ru.haykor.pacman.config.PacmanConfig
import ru.haykor.pacman.config.RepositoryMode
import ru.haykor.pacman.graph.CycleDetector
import ru.haykor.pacman.graph.DependencyGraphBuilder
import ru.haykor.pacman.graph.PackageNameFilter
import ru.haykor.pacman.npm.KtorNpmRegistryClient
import ru.haykor.pacman.npm.NpmDependencySource
import ru.haykor.pacman.report.DependencyGraphReport
import ru.haykor.pacman.source.DependencySource
import ru.haykor.pacman.source.PackageRequest
import ru.haykor.pacman.testrepo.TestRepositoryLoader
import ru.haykor.pacman.testrepo.TestRepositorySource

/**
 * Command that builds and prints the transitive dependency graph.
 *
 * @property output sink for the report.
 */
class GraphCommand(private val output: MessageSink) {
    /**
     * Analyses the configured package and prints the graph.
     *
     * @param config validated configuration.
     */
    suspend fun execute(config: PacmanConfig) {
        val result = withSource(config) { source ->
            DependencyGraphBuilder(source, PackageNameFilter(config.filter))
                .build(PackageRequest(config.packageName, DependencySource.LATEST_TAG))
        }
        val cycles = CycleDetector.findCycles(result.graph)
        DependencyGraphReport.render(result.graph, result.excluded, cycles).forEach(output::line)
    }

    private suspend fun <T> withSource(
        config: PacmanConfig,
        block: suspend (DependencySource) -> T,
    ): T = when (config.repositoryMode) {
        RepositoryMode.TEST -> block(
            TestRepositorySource(TestRepositoryLoader().load(config.repository)),
        )

        RepositoryMode.REGISTRY -> withRegistry(config.repository, block)
    }

    private suspend fun <T> withRegistry(
        baseUrl: String,
        block: suspend (DependencySource) -> T,
    ): T {
        val client = KtorNpmRegistryClient(baseUrl)
        return try {
            block(NpmDependencySource(client))
        } finally {
            client.close()
        }
    }
}
