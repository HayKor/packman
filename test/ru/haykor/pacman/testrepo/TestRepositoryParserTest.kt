package ru.haykor.pacman.testrepo

import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestRepositoryParserTest {
    @Test
    fun parsesDeclarationsAndDependencies() {
        val repository = TestRepositoryParser.parse(
            """
            # comment
            A: B, C
            B:   C
            C:
            """.trimIndent(),
        )

        assertEquals(listOf("B", "C"), repository.dependenciesOf("A"))
        assertEquals(listOf("C"), repository.dependenciesOf("B"))
        assertEquals(emptyList(), repository.dependenciesOf("C"))
    }

    @Test
    fun ignoresBlankLinesAndInlineComments() {
        val repository = TestRepositoryParser.parse("A: B # only B\n\nB:\n")

        assertEquals(setOf("A", "B"), repository.dependencies.keys)
        assertEquals(listOf("B"), repository.dependenciesOf("A"))
    }

    @Test
    fun removesDuplicateDependencies() {
        val repository = TestRepositoryParser.parse("A: B B, B\nB:\n")

        assertEquals(listOf("B"), repository.dependenciesOf("A"))
    }

    @Test
    fun rejectsDuplicateDeclaration() {
        assertParseFails("is declared twice", "A:\nA:\n", line = 2)
    }

    @Test
    fun rejectsLowercaseName() {
        assertParseFails("invalid package name 'a'", "a: B\nB:\n", line = 1)
    }

    @Test
    fun rejectsInvalidDependencyName() {
        assertParseFails("invalid dependency name 'b'", "A: b\n", line = 1)
    }

    @Test
    fun rejectsLineWithoutSeparator() {
        assertParseFails("expected '<NAME>: <DEPENDENCIES>'", "A B\n", line = 1)
    }

    @Test
    fun acceptsSelfDependency() {
        val repository = TestRepositoryParser.parse("A: A\n")

        assertEquals(listOf("A"), repository.dependenciesOf("A"))
    }

    private fun assertParseFails(fragment: String, text: String, line: Int) {
        val error = assertFailsWith<TestRepositoryException> { TestRepositoryParser.parse(text) }

        assertContains(error.message.orEmpty(), fragment)
        assertContains(error.message.orEmpty(), "line $line")
    }
}
