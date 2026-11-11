package ru.haykor.pacman.cli

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ArgumentParserTest {
    @Test
    fun usesDefaultConfigurationPath() {
        val result = assertIs<ArgumentsResult.Parsed>(ArgumentParser.parse(emptyList()))

        assertEquals(ArgumentParser.DEFAULT_CONFIG_PATH, result.configPath)
    }

    @Test
    fun readsSeparateConfigOption() {
        val arguments = listOf("--config", "custom.yaml")
        val result = assertIs<ArgumentsResult.Parsed>(ArgumentParser.parse(arguments))

        assertEquals("custom.yaml", result.configPath)
    }

    @Test
    fun readsInlineConfigOption() {
        val arguments = listOf("--config=custom.yaml")
        val result = assertIs<ArgumentsResult.Parsed>(ArgumentParser.parse(arguments))

        assertEquals("custom.yaml", result.configPath)
    }

    @Test
    fun readsPrintConfigOption() {
        val result = assertIs<ArgumentsResult.Parsed>(
            ArgumentParser.parse(listOf("--print-config", "--config", "custom.yaml")),
        )

        assertEquals("custom.yaml", result.configPath)
        assertTrue(result.printConfig)
    }

    @Test
    fun recognisesHelpOption() {
        assertIs<ArgumentsResult.HelpRequested>(ArgumentParser.parse(listOf("--help")))
        assertIs<ArgumentsResult.HelpRequested>(ArgumentParser.parse(listOf("-h")))
    }

    @Test
    fun rejectsUnknownArgument() {
        val result = assertIs<ArgumentsResult.Invalid>(ArgumentParser.parse(listOf("--verbose")))

        assertEquals("unknown argument '--verbose'", result.message)
    }

    @Test
    fun rejectsConfigOptionWithoutValue() {
        val result = assertIs<ArgumentsResult.Invalid>(ArgumentParser.parse(listOf("--config")))

        assertEquals("option '--config' requires a value", result.message)
    }

    @Test
    fun rejectsEmptyConfigValue() {
        val result = assertIs<ArgumentsResult.Invalid>(ArgumentParser.parse(listOf("--config=")))

        assertEquals("option '--config' requires a non-empty value", result.message)
    }
}
