package ru.haykor.pacman.graph

import kotlin.test.Test
import kotlin.test.assertEquals

class CycleDetectorTest {
    @Test
    fun reportsNoCyclesForAcyclicGraph() {
        val graph = graphOf(mapOf("A" to listOf("B"), "B" to emptyList()))
        val cycles = CycleDetector.findCycles(graph)

        assertEquals(emptyList(), cycles)
    }

    @Test
    fun findsSimpleCycle() {
        val cycles = CycleDetector.findCycles(
            graphOf(
                mapOf(
                    "A" to listOf("B"),
                    "B" to listOf("C"),
                    "C" to listOf("A"),
                ),
            ),
        )

        assertEquals(listOf(listOf("A", "B", "C", "A")), cycles)
    }

    @Test
    fun findsSelfCycle() {
        val cycles = CycleDetector.findCycles(graphOf(mapOf("A" to listOf("A"))))

        assertEquals(listOf(listOf("A", "A")), cycles)
    }

    @Test
    fun findsIndependentCyclesOnce() {
        val cycles = CycleDetector.findCycles(
            graphOf(
                mapOf(
                    "A" to listOf("B", "X"),
                    "B" to listOf("A"),
                    "X" to listOf("Y"),
                    "Y" to listOf("X"),
                ),
            ),
        )

        assertEquals(2, cycles.size)
        assertEquals(listOf("A", "B", "A"), cycles[0])
        assertEquals(listOf("X", "Y", "X"), cycles[1])
    }

    @Test
    fun doesNotReportDiamondAsCycle() {
        val graph = graphOf(
            mapOf(
                "A" to listOf("B", "C"),
                "B" to listOf("D"),
                "C" to listOf("D"),
                "D" to emptyList(),
            ),
        )

        assertEquals(emptyList(), CycleDetector.findCycles(graph))
    }

    private fun graphOf(edges: Map<String, List<String>>): DependencyGraph {
        val ids = edges.keys + edges.values.flatten()
        val nodes = ids.associateWith { id -> GraphNode(id, id, "") }
        return DependencyGraph(rootId = "A", nodes = nodes, edges = edges)
    }
}
