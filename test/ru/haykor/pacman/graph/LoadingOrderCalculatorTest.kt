package ru.haykor.pacman.graph

import kotlin.test.Test
import kotlin.test.assertEquals

class LoadingOrderCalculatorTest {
    @Test
    fun ordersChainFromLeavesToRoot() {
        val order = LoadingOrderCalculator.calculate(
            graphOf(
                "A" to listOf("B"),
                "B" to listOf("C"),
                "C" to emptyList(),
            ),
        )

        assertEquals(listOf("C", "B", "A"), order)
    }

    @Test
    fun ordersDiamondDeterministically() {
        val order = LoadingOrderCalculator.calculate(
            graphOf(
                "A" to listOf("B", "C"),
                "B" to listOf("D"),
                "C" to listOf("D"),
                "D" to emptyList(),
            ),
        )

        assertEquals(listOf("D", "B", "C", "A"), order)
    }

    @Test
    fun keepsEveryNodeOnceOnCycle() {
        val order = LoadingOrderCalculator.calculate(
            graphOf(
                "A" to listOf("B"),
                "B" to listOf("C"),
                "C" to listOf("A"),
            ),
        )

        assertEquals(listOf("C", "B", "A"), order)
    }

    @Test
    fun keepsSelfDependencyOnce() {
        val order = LoadingOrderCalculator.calculate(
            graphOf(
                "A" to listOf("A", "B"),
                "B" to emptyList(),
            ),
        )

        assertEquals(listOf("B", "A"), order)
    }

    private fun graphOf(vararg edges: Pair<String, List<String>>): DependencyGraph {
        val edgesMap = edges.toMap()
        val ids = edgesMap.keys + edgesMap.values.flatten()
        val nodes = ids.associateWith { id -> GraphNode(id, id, "") }
        return DependencyGraph(rootId = "A", nodes = nodes, edges = edgesMap)
    }
}
