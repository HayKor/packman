package ru.haykor.pacman.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class MainApplicationTest {
    private val output = RecordingSink()
    private val errorOutput = RecordingSink()
    private val application = MainApplication(output = output, errorOutput = errorOutput)

    @Test
    fun reportsMissingConfigurationFile() {
        val exitCode = application.run(listOf("--config", "tests/fixtures/configs/missing.yaml"))

        assertEquals(ExitCode.FAILURE, exitCode)
        assertTrue(output.lines.isEmpty())
        assertTrue(errorOutput.lines.single().startsWith("error: configuration file"))
    }

    @Test
    fun analysesLinearTestRepository() {
        val arguments = listOf("--config", "tests/fixtures/configs/test-linear.yaml")
        val exitCode = application.run(arguments)

        assertEquals(ExitCode.SUCCESS, exitCode)
        assertEquals("Dependency graph for A", output.lines.first())
        assertTrue(output.lines.contains("A -> B"))
        assertTrue(errorOutput.lines.isEmpty())
    }

    @Test
    fun reportsUnknownArgumentAsUsageProblem() {
        val exitCode = application.run(listOf("--unknown"))

        assertEquals(ExitCode.USAGE, exitCode)
        assertEquals("error: unknown argument '--unknown'", errorOutput.lines.first())
        assertEquals("use '--help' to see available options", errorOutput.lines.last())
    }

    @Test
    fun printsHelpPage() {
        val exitCode = application.run(listOf("--help"))

        assertEquals(ExitCode.SUCCESS, exitCode)
        assertTrue(output.lines.first().startsWith("pacman - dependency graph visualizer"))
        assertTrue(errorOutput.lines.isEmpty())
    }

    private class RecordingSink : MessageSink {
        val lines: MutableList<String> = mutableListOf()

        override fun line(message: String) {
            lines.add(message)
        }
    }
}
