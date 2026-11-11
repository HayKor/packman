package ru.haykor.pacman.graph

import kotlinx.coroutines.runBlocking
import ru.haykor.pacman.ExecutionException
import ru.haykor.pacman.source.DependencySource
import ru.haykor.pacman.source.PackageRequest
import ru.haykor.pacman.source.ResolvedPackage
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DependencyGraphBuilderTest {
    @Test
    fun buildsLinearGraph() = runBlocking {
        val result = buildGraph(linearPackages(), filter = "")

        assertEquals("A", result.graph.rootId)
        assertEquals(setOf("A", "B", "C"), result.graph.nodes.keys)
        assertEquals(listOf("B"), result.graph.dependenciesOf("A"))
        assertEquals(2, result.graph.edgeCount)
    }

    @Test
    fun sharesDiamondDependency() = runBlocking {
        val result = buildGraph(diamondPackages(), filter = "")

        assertEquals(setOf("A", "B", "C", "D"), result.graph.nodes.keys)
        assertEquals(4, result.graph.edgeCount)
        assertEquals(listOf("D"), result.graph.dependenciesOf("B"))
    }

    @Test
    fun terminatesOnCycles() = runBlocking {
        val result = buildGraph(cyclePackages(), filter = "")

        assertEquals(setOf("A", "B"), result.graph.nodes.keys)
        assertEquals(listOf("B"), result.graph.dependenciesOf("A"))
        assertEquals(listOf("A"), result.graph.dependenciesOf("B"))
    }

    @Test
    fun excludesFilteredPackageWithItsSubtree() = runBlocking {
        val result = buildGraph(diamondPackages(), filter = "b")

        assertEquals(setOf("A", "C", "D"), result.graph.nodes.keys)
        assertEquals(listOf("B"), result.excluded)
        assertEquals(listOf("C"), result.graph.dependenciesOf("A"))
    }

    @Test
    fun rejectsFilteredRootPackage() = runBlocking {
        val error = assertFailsWith<ExecutionException> {
            buildGraph(diamondPackages(), filter = "a")
        }

        assertContains(error.message.orEmpty(), "matches filter 'a'")
    }

    @Test
    fun keepsOneEdgePerParentAndDependency() = runBlocking {
        val packages = mapOf(
            "A" to FakePackage(dependencies = mapOf("B" to "^1.0.0", "C" to "^1.0.0")),
            "B" to FakePackage(dependencies = mapOf("D" to "^1.0.0")),
            "C" to FakePackage(dependencies = mapOf("D" to "^1.0.0")),
            "D" to FakePackage(),
        )

        val result = buildGraph(packages, filter = "")

        assertEquals(4, result.graph.edgeCount)
    }

    private suspend fun buildGraph(
        packages: Map<String, FakePackage>,
        filter: String,
    ): GraphBuildResult = DependencyGraphBuilder(FakeSource(packages), PackageNameFilter(filter))
        .build(PackageRequest("A", DependencySource.LATEST_TAG))

    private fun linearPackages(): Map<String, FakePackage> = mapOf(
        "A" to FakePackage(dependencies = mapOf("B" to "^1.0.0")),
        "B" to FakePackage(dependencies = mapOf("C" to "^1.0.0")),
        "C" to FakePackage(),
    )

    private fun diamondPackages(): Map<String, FakePackage> = mapOf(
        "A" to FakePackage(dependencies = mapOf("B" to "^1.0.0", "C" to "^1.0.0")),
        "B" to FakePackage(dependencies = mapOf("D" to "^1.0.0")),
        "C" to FakePackage(dependencies = mapOf("D" to "^1.0.0")),
        "D" to FakePackage(),
    )

    private fun cyclePackages(): Map<String, FakePackage> = mapOf(
        "A" to FakePackage(dependencies = mapOf("B" to "^1.0.0")),
        "B" to FakePackage(dependencies = mapOf("A" to "^1.0.0")),
    )

    private data class FakePackage(
        val version: String = "",
        val dependencies: Map<String, String> = emptyMap(),
    )

    private class FakeSource(private val packages: Map<String, FakePackage>) : DependencySource {
        override suspend fun resolve(request: PackageRequest): ResolvedPackage {
            val resolved = packages[request.name]
                ?: throw ExecutionException("unknown package '${request.name}'")
            return ResolvedPackage(request.name, resolved.version, resolved.dependencies)
        }
    }
}
