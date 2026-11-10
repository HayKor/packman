package ru.haykor.pacman.report

import ru.haykor.pacman.config.PacmanConfig
import ru.haykor.pacman.config.RepositoryMode
import ru.haykor.pacman.npm.PackageInfo
import ru.haykor.pacman.npm.PackageManifest
import kotlin.test.Test
import kotlin.test.assertEquals

class DirectDependenciesReportTest {
    private val config = PacmanConfig(
        packageName = "sample",
        repository = "https://registry.npmjs.org",
        repositoryMode = RepositoryMode.REGISTRY,
        imageFile = "graph.png",
        asciiTree = false,
        filter = "",
    )

    @Test
    fun rendersHeaderAndSortedDependencies() {
        val info = PackageInfo(
            name = "sample",
            latestVersion = "1.0.0",
            manifests = mapOf(
                "1.0.0" to PackageManifest(
                    name = "sample",
                    version = "1.0.0",
                    dependencies = mapOf("zeta" to "^2.0.0", "alpha" to "~1.0.0"),
                ),
            ),
        )

        assertEquals(
            listOf(
                "Package: sample",
                "Repository: https://registry.npmjs.org",
                "Version: 1.0.0",
                "Direct dependencies: 2",
                "  alpha: ~1.0.0",
                "  zeta: ^2.0.0",
            ),
            DirectDependenciesReport.render(config, info),
        )
    }

    @Test
    fun rendersPackageWithoutDependencies() {
        val info = PackageInfo(
            name = "sample",
            latestVersion = "1.0.0",
            manifests = mapOf(
                "1.0.0" to PackageManifest("sample", "1.0.0", emptyMap()),
            ),
        )

        assertEquals(
            listOf(
                "Package: sample",
                "Repository: https://registry.npmjs.org",
                "Version: 1.0.0",
                "Direct dependencies: 0",
            ),
            DirectDependenciesReport.render(config, info),
        )
    }
}
