package ru.haykor.pacman.report

import ru.haykor.pacman.config.PacmanConfig
import ru.haykor.pacman.npm.PackageInfo

/**
 * Text report with the direct dependencies of the analysed package.
 */
object DirectDependenciesReport {
    /**
     * Renders the report.
     *
     * @param config configuration used for the analysis.
     * @param info registry document of the analysed package.
     * @return report lines in a stable order.
     */
    fun render(config: PacmanConfig, info: PackageInfo): List<String> {
        val manifest = info.latest
        val dependencies = manifest.dependencies.entries.sortedBy { entry -> entry.key }
        val lines = mutableListOf(
            "Package: ${manifest.name}",
            "Repository: ${config.repository}",
            "Version: ${manifest.version}",
            "Direct dependencies: ${dependencies.size}",
        )
        dependencies.forEach { (name, range) -> lines += "  $name: $range" }
        return lines
    }
}
