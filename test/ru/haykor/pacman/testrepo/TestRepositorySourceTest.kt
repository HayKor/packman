package ru.haykor.pacman.testrepo

import kotlinx.coroutines.runBlocking
import ru.haykor.pacman.ExecutionException
import ru.haykor.pacman.source.PackageRequest
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TestRepositorySourceTest {
    private val repository = TestRepositoryParser.parse("A: B C\nB:\nC:\n")
    private val source = TestRepositorySource(repository)

    @Test
    fun resolvesDeclaredPackage() = runBlocking {
        val resolved = source.resolve(PackageRequest("A", "*"))

        assertEquals("A", resolved.id)
        assertEquals(mapOf("B" to "*", "C" to "*"), resolved.dependencies)
    }

    @Test
    fun reportsUndeclaredPackage() = runBlocking {
        val error = assertFailsWith<ExecutionException> {
            source.resolve(PackageRequest("Z", "*"))
        }

        assertContains(error.message.orEmpty(), "package 'Z' is not declared")
    }
}
