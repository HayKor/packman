package ru.haykor.pacman.cli

import ru.haykor.pacman.ExecutionException
import ru.haykor.pacman.config.PacmanConfig
import ru.haykor.pacman.config.RepositoryMode
import ru.haykor.pacman.npm.KtorNpmRegistryClient
import ru.haykor.pacman.report.DirectDependenciesReport

/**
 * Command that prints the direct dependencies of the configured package.
 *
 * @property output sink for the report.
 */
class DirectDependenciesCommand(private val output: MessageSink) {
    /**
     * Fetches the package document and prints its direct dependencies.
     *
     * @param config validated configuration.
     */
    suspend fun execute(config: PacmanConfig) {
        if (config.repositoryMode != RepositoryMode.REGISTRY) {
            throw ExecutionException(
                "repository_mode '${config.repositoryMode.configValue}' is not supported yet",
            )
        }
        val client = KtorNpmRegistryClient(config.repository)
        try {
            val info = client.fetchPackage(config.packageName)
            DirectDependenciesReport.render(config, info).forEach(output::line)
        } finally {
            client.close()
        }
    }
}
