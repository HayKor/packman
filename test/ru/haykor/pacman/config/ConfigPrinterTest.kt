package ru.haykor.pacman.config

import kotlin.test.Test
import kotlin.test.assertEquals

class ConfigPrinterTest {
    private val config = PacmanConfig(
        packageName = "express",
        repository = "https://registry.npmjs.org",
        repositoryMode = RepositoryMode.REGISTRY,
        imageFile = "graph.png",
        asciiTree = false,
        filter = "",
    )

    @Test
    fun rendersAllParametersAsKeyValueLines() {
        assertEquals(
            listOf(
                "package: express",
                "repository: https://registry.npmjs.org",
                "repository_mode: registry",
                "image_file: graph.png",
                "ascii_tree: false",
                "filter: \"\"",
            ),
            ConfigPrinter.render(config),
        )
    }

    @Test
    fun rendersNonEmptyFilter() {
        val lines = ConfigPrinter.render(config.copy(filter = "test"))

        assertEquals("filter: test", lines.last())
    }
}
