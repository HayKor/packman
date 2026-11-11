package ru.haykor.pacman.report

import ru.haykor.pacman.graph.DependencyGraph
import ru.haykor.pacman.graph.GraphNode
import kotlin.test.Test
import kotlin.test.assertEquals

class LoadingOrderReportTest {
    private val graph = DependencyGraph(
        rootId = "A",
        nodes = mapOf(
            "A" to GraphNode("A", "A", ""),
            "B" to GraphNode("B", "B", ""),
        ),
        edges = mapOf("A" to listOf("B"), "B" to emptyList()),
    )

    @Test
    fun rendersNumberedOrder() {
        assertEquals(
            listOf(
                "Loading order for A (dependencies first):",
                "  1. B",
                "  2. A",
                "Total: 2 packages",
                "",
                "Cycles: none",
            ),
            LoadingOrderReport.render(graph, order = listOf("B", "A"), cycles = emptyList()),
        )
    }

    @Test
    fun rendersCycles() {
        val lines = LoadingOrderReport.render(
            graph,
            order = listOf("B", "A"),
            cycles = listOf(listOf("A", "B", "A")),
        )

        assertEquals("Cycles: A -> B -> A", lines.last())
    }
}
