package ru.haykor.pacman.report

import ru.haykor.pacman.graph.DependencyGraph
import ru.haykor.pacman.graph.GraphNode
import kotlin.test.Test
import kotlin.test.assertEquals

class DependencyGraphReportTest {
    private val graph = DependencyGraph(
        rootId = "A",
        nodes = mapOf(
            "A" to GraphNode("A", "A", ""),
            "B" to GraphNode("B", "B", ""),
            "C" to GraphNode("C", "C", ""),
        ),
        edges = mapOf(
            "A" to listOf("B", "C"),
            "B" to listOf("C"),
            "C" to emptyList(),
        ),
    )

    @Test
    fun rendersAdjacencyList() {
        assertEquals(
            listOf(
                "Dependency graph for A",
                "Nodes: 3",
                "Edges: 3",
                "",
                "A -> B, C",
                "B -> C",
                "C -> (no dependencies)",
                "",
                "Excluded by filter: none",
                "Cycles: none",
            ),
            DependencyGraphReport.render(graph, excluded = emptyList(), cycles = emptyList()),
        )
    }

    @Test
    fun rendersExcludedPackagesAndCycles() {
        val lines = DependencyGraphReport.render(
            graph,
            excluded = listOf("D", "E"),
            cycles = listOf(listOf("A", "B", "C", "A")),
        )

        assertEquals("Excluded by filter: D, E", lines[lines.size - 2])
        assertEquals("Cycles: A -> B -> C -> A", lines.last())
    }
}
