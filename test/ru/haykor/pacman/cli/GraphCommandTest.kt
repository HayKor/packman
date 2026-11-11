package ru.haykor.pacman.cli

import kotlinx.coroutines.runBlocking
import ru.haykor.pacman.ExecutionException
import ru.haykor.pacman.config.PacmanConfig
import ru.haykor.pacman.config.RepositoryMode
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class GraphCommandTest {
    private val output = RecordingSink()

    @Test
    fun analysesTestRepositoryFile() {
        runBlocking {
            GraphCommand(output).execute(configFor("tests/fixtures/repos/diamond.graph", ""))
        }

        assertEquals("Dependency graph for A", output.lines.first())
        assertTrue(output.lines.contains("A -> B, C"))
        assertEquals("Cycles: none", output.lines.last())
    }

    @Test
    fun printsLoadingOrderWhenRequested() {
        runBlocking {
            GraphCommand(output).execute(
                configFor("tests/fixtures/repos/diamond.graph", filter = "", loadOrder = true),
            )
        }

        assertEquals("Loading order for A (dependencies first):", output.lines.first())
        assertEquals(listOf("  1. D", "  2. B", "  3. C", "  4. A"), output.lines.subList(1, 5))
        assertEquals("Total: 4 packages", output.lines[5])
    }

    @Test
    fun reportsFilteredRootPackage() {
        val error = assertFailsWith<ExecutionException> {
            runBlocking {
                GraphCommand(output).execute(configFor("tests/fixtures/repos/diamond.graph", "a"))
            }
        }

        assertContains(error.message.orEmpty(), "matches filter 'a'")
    }

    @Test
    fun reportsMissingTestRepositoryFile() {
        assertFailsWith<ExecutionException> {
            runBlocking {
                GraphCommand(output).execute(configFor("tests/fixtures/repos/missing.graph", ""))
            }
        }
    }

    private fun configFor(
        repository: String,
        filter: String,
        loadOrder: Boolean = false,
    ): PacmanConfig = PacmanConfig(
        packageName = "A",
        repository = repository,
        repositoryMode = RepositoryMode.TEST,
        imageFile = "graph.png",
        asciiTree = false,
        filter = filter,
        loadOrder = loadOrder,
    )

    private class RecordingSink : MessageSink {
        val lines: MutableList<String> = mutableListOf()

        override fun line(message: String) {
            lines.add(message)
        }
    }
}
