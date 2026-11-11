package ru.haykor.pacman.report

import ru.haykor.pacman.graph.DependencyGraph

/**
 * Text report with the dependency graph rendered as an adjacency list.
 */
object DependencyGraphReport {
    private const val NONE: String = "none"
    private const val NO_DEPENDENCIES: String = "(no dependencies)"
    private const val CYCLE_SEPARATOR: String = " -> "
    private const val ITEM_SEPARATOR: String = ", "
    private const val CYCLE_JOINER: String = "; "

    /**
     * Renders the report.
     *
     * @param graph dependency graph.
     * @param excluded names of packages removed by the filter.
     * @param cycles cycles found in the graph.
     * @return report lines in a stable order.
     */
    fun render(
        graph: DependencyGraph,
        excluded: List<String>,
        cycles: List<List<String>>,
    ): List<String> {
        val lines = mutableListOf(
            "Dependency graph for ${graph.rootId}",
            "Nodes: ${graph.nodes.size}",
            "Edges: ${graph.edgeCount}",
            "",
        )
        graph.nodes.keys.sorted().forEach { id ->
            lines += renderEdges(graph, id)
        }
        lines += ""
        lines += "Excluded by filter: ${renderExcluded(excluded)}"
        lines += "Cycles: ${renderCycles(cycles)}"
        return lines
    }

    private fun renderEdges(graph: DependencyGraph, id: String): String {
        val dependencies = graph.dependenciesOf(id).sorted()
        val target = if (dependencies.isEmpty()) {
            NO_DEPENDENCIES
        } else {
            dependencies.joinToString(ITEM_SEPARATOR)
        }
        return "$id -> $target"
    }

    private fun renderExcluded(excluded: List<String>): String =
        if (excluded.isEmpty()) NONE else excluded.joinToString(ITEM_SEPARATOR)

    private fun renderCycles(cycles: List<List<String>>): String =
        if (cycles.isEmpty()) {
            NONE
        } else {
            cycles.joinToString(CYCLE_JOINER) { cycle -> cycle.joinToString(CYCLE_SEPARATOR) }
        }
}
