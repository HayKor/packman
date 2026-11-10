package ru.haykor.pacman.config

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ConfigLoaderTest {
    private val loader = ConfigLoader()

    @Test
    fun loadsValidRegistryConfiguration() {
        val config = loader.loadFromText(validRegistryText())

        assertEquals("express", config.packageName)
        assertEquals("https://registry.npmjs.org", config.repository)
        assertEquals(RepositoryMode.REGISTRY, config.repositoryMode)
        assertEquals("graph.png", config.imageFile)
        assertFalse(config.asciiTree)
        assertEquals("", config.filter)
    }

    @Test
    fun loadsValidTestConfiguration() {
        val config = loader.loadFromText(
            """
            package: A
            repository: tests/fixtures/repos/linear.graph
            repository_mode: test
            image_file: graph.png
            ascii_tree: false
            filter: ""
            """.trimIndent(),
        )

        assertEquals("A", config.packageName)
        assertEquals(RepositoryMode.TEST, config.repositoryMode)
    }

    @Test
    fun acceptsScopedPackageName() {
        val config = loader.loadFromText(validRegistryText(packageName = "@angular/core"))

        assertEquals("@angular/core", config.packageName)
    }

    @Test
    fun acceptsUppercaseBooleanValue() {
        val config = loader.loadFromText(validRegistryText(asciiTree = "TRUE"))

        assertTrue(config.asciiTree)
    }

    @Test
    fun rejectsEmptyConfiguration() {
        assertInvalid("configuration file is empty", "")
    }

    @Test
    fun rejectsBrokenYaml() {
        val text = validRegistryText().replace("package: express", "package: [express")

        assertInvalid("not valid YAML", text)
    }

    @Test
    fun rejectsUnquotedUrl() {
        val text = validRegistryText()
            .replace("\"https://registry.npmjs.org\"", "https://registry.npmjs.org")

        assertInvalid("must be quoted", text)
    }

    @Test
    fun rejectsUnknownParameter() {
        assertInvalid("unknown parameter", validRegistryText() + "\npakage: express\n")
    }

    @Test
    fun rejectsMissingParameter() {
        val text = validRegistryText()
            .lines()
            .filterNot { line -> line.startsWith("filter:") }
            .joinToString("\n")

        assertInvalid("parameter 'filter' is required", text)
    }

    @Test
    fun rejectsNonScalarParameter() {
        assertInvalid("must be a scalar value", validRegistryText(filter = "[a, b]"))
    }

    @Test
    fun rejectsInvalidRegistryPackageName() {
        assertInvalid("is not a valid npm package name", validRegistryText(packageName = "Express"))
    }

    @Test
    fun rejectsEmptyPackageName() {
        assertInvalid("must not be empty", validRegistryText(packageName = "\"\""))
    }

    @Test
    fun rejectsLowercaseTestPackageName() {
        assertInvalid("is not a test repository package name", validTestText(packageName = "a"))
    }

    @Test
    fun rejectsUnknownRepositoryMode() {
        assertInvalid("unknown value 'remote'", validRegistryText(mode = "remote"))
    }

    @Test
    fun rejectsNonHttpRegistryUrl() {
        val text = validRegistryText(repository = "\"ftp://example.com\"")

        assertInvalid("is not a valid HTTP(S) URL", text)
    }

    @Test
    fun rejectsMissingTestRepositoryFile() {
        val text = validTestText(repository = "tests/fixtures/repos/missing.graph")

        assertInvalid("does not exist", text)
    }

    @Test
    fun rejectsImageFileWithoutPngExtension() {
        assertInvalid("must have the '.png' extension", validRegistryText(imageFile = "graph.svg"))
    }

    @Test
    fun rejectsImageFileWithDirectories() {
        assertInvalid("without directories", validRegistryText(imageFile = "out/graph.png"))
    }

    @Test
    fun rejectsInvalidBoolean() {
        assertInvalid("must be a boolean", validRegistryText(asciiTree = "yes"))
    }

    @Test
    fun rejectsTooLongFilter() {
        assertInvalid("must not be longer", validRegistryText(filter = "x".repeat(65)))
    }

    private fun assertInvalid(expectedFragment: String, text: String) {
        val error = assertFailsWith<ConfigException> { loader.loadFromText(text) }

        assertContains(error.message.orEmpty(), expectedFragment)
    }

    private fun validRegistryText(
        packageName: String = "express",
        repository: String = "\"https://registry.npmjs.org\"",
        mode: String = "registry",
        imageFile: String = "graph.png",
        asciiTree: String = "false",
        filter: String = "\"\"",
    ): String = """
        |package: $packageName
        |repository: $repository
        |repository_mode: $mode
        |image_file: $imageFile
        |ascii_tree: $asciiTree
        |filter: $filter
        |""".trimMargin()

    private fun validTestText(
        packageName: String = "A",
        repository: String = "tests/fixtures/repos/linear.graph",
    ): String = """
        |package: $packageName
        |repository: $repository
        |repository_mode: test
        |image_file: graph.png
        |ascii_tree: false
        |filter: ""
        |""".trimMargin()
}
