package ru.haykor.pacman.npm

import kotlinx.io.buffered
import kotlinx.io.files.Path
import kotlinx.io.files.SystemFileSystem
import kotlinx.io.readString
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class NpmPackumentParserTest {
    private val document = readFixture("tests/fixtures/npm/registry/sample-package")

    @Test
    fun parsesLatestVersionAndManifests() {
        val info = NpmPackumentParser.parse(document)

        assertEquals("sample-package", info.name)
        assertEquals("2.1.0", info.latestVersion)
        assertEquals(setOf("2.0.0", "2.1.0"), info.manifests.keys)
        assertEquals("2.1.0", info.latest.version)
    }

    @Test
    fun readsOnlyStringDependencies() {
        val dependencies = NpmPackumentParser.parse(document).latest.dependencies

        assertEquals(
            mapOf(
                "zeta" to "^2.0.0",
                "alpha" to "~1.2.3",
                "optional-style" to ">=3.0.0 <4.0.0",
            ),
            dependencies,
        )
    }

    @Test
    fun treatsMissingDependenciesAsEmpty() {
        val info = NpmPackumentParser.parse(scopedDocument())

        assertEquals(emptyMap(), info.latest.dependencies)
    }

    @Test
    fun reportsMissingLatestManifest() {
        val text = document.replace("\"latest\": \"2.1.0\"", "\"latest\": \"9.9.9\"")
        val error = assertFailsWith<RegistryException> { NpmPackumentParser.parse(text).latest }

        assertContains(error.message.orEmpty(), "no manifest for version '9.9.9'")
    }

    @Test
    fun rejectsInvalidJson() {
        assertParseFails("is not valid JSON", "not json at all")
    }

    @Test
    fun rejectsNonObjectDocument() {
        assertParseFails("not a JSON object", "[1, 2, 3]")
    }

    @Test
    fun rejectsDocumentWithoutName() {
        assertParseFails("no 'name' field", """{"dist-tags":{"latest":"1.0.0"},"versions":{}}""")
    }

    @Test
    fun rejectsDocumentWithoutLatestTag() {
        assertParseFails(
            "no 'dist-tags.latest' field",
            """{"name":"a","dist-tags":{},"versions":{"1.0.0":{}}}""",
        )
    }

    @Test
    fun rejectsDocumentWithoutVersions() {
        assertParseFails(
            "no 'versions' field",
            """{"name":"a","dist-tags":{"latest":"1.0.0"}}""",
        )
    }

    @Test
    fun rejectsManifestThatIsNotAnObject() {
        assertParseFails(
            "is not an object",
            """{"name":"a","dist-tags":{"latest":"1.0.0"},"versions":{"1.0.0":"text"}}""",
        )
    }

    private fun assertParseFails(expectedFragment: String, text: String) {
        val error = assertFailsWith<RegistryException> { NpmPackumentParser.parse(text) }

        assertContains(error.message.orEmpty(), expectedFragment)
    }

    private fun scopedDocument(): String = """
        {
          "name": "@scope/pkg",
          "dist-tags": { "latest": "1.0.0" },
          "versions": {
            "1.0.0": { "name": "@scope/pkg", "version": "1.0.0" }
          }
        }
    """.trimIndent()

    private fun readFixture(path: String): String =
        SystemFileSystem.source(Path(path)).buffered().use { source -> source.readString() }
}
