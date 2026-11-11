package ru.haykor.pacman.cli

import ru.haykor.pacman.config.PacmanConfig
import ru.haykor.pacman.config.RepositoryMode
import ru.haykor.pacman.graph.CycleDetector
import ru.haykor.pacman.graph.DependencyGraphBuilder
import ru.haykor.pacman.graph.LoadingOrderCalculator
import ru.haykor.pacman.graph.PackageNameFilter
import ru.haykor.pacman.npm.KtorNpmRegistryClient
import ru.haykor.pacman.npm.NpmDependencySource
import ru.haykor.pacman.report.DependencyGraphReport
import ru.haykor.pacman.report.LoadingOrderReport
import ru.haykor.pacman.source.DependencySource
import ru.haykor.pacman.source.PackageRequest
import ru.haykor.pacman.testrepo.TestRepositoryLoader
import ru.haykor.pacman.testrepo.TestRepositorySource

/**
 * Command that analyses the configured package and prints the result.
 *
 * Depending on the configuration the command prints either the dependency
 * graph or the order in which dependencies are loaded.
 *
 * @property output sink for the report.
 */
class GraphCommand(private val output: MessageSink) {
    /**
     * Analyses the configured package.
     *
     * @param config validated configuration.
     */
    suspend fun execute(config: PacmanConfig) {
        val result = withSource(config) { source ->
            DependencyGraphBuilder(source, PackageNameFilter(config.filter))
                .build(PackageRequest(config.packageName, DependencySource.LATEST_TAG))
        }
        val cycles = CycleDetector.findCycles(result.graph)
        if (config.loadOrder) {
            val order = LoadingOrderCalculator.calculate(result.graph)
            LoadingOrderReport.render(result.graph, order, cycles).forEach(output::line)
        } else {
            DependencyGraphReport.render(result.graph, result.excluded, cycles)
                .forEach(output::line)
        }
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
