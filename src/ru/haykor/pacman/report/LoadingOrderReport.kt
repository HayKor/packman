package ru.haykor.pacman.report

import ru.haykor.pacman.graph.DependencyGraph

/**
 * Text report with the order in which dependencies must be loaded.
 */
object LoadingOrderReport {
    private const val NONE: String = "none"
    private const val CYCLE_SEPARATOR: String = " -> "
    private const val CYCLE_JOINER: String = "; "
    private const val NUMBER_OFFSET: Int = 1

    /**
     * Renders the report.
     *
     * @param graph dependency graph.
     * @param order node identifiers with dependencies first.
     * @param cycles cycles found in the graph.
     * @return report lines.
     */
    fun render(
        graph: DependencyGraph,
        order: List<String>,
        cycles: List<List<String>>,
    ): List<String> {
        val lines = mutableListOf(
            "Loading order for ${graph.rootId} (dependencies first):",
        )
        order.forEachIndexed { index, id ->
            lines += "  ${index + NUMBER_OFFSET}. $id"
        }
        lines += "Total: ${order.size} packages"
        lines += ""
        lines += "Cycles: ${renderCycles(cycles)}"
        return lines
    }

    private fun renderCycles(cycles: List<List<String>>): String =
        if (cycles.isEmpty()) {
            NONE
        } else {
            cycles.joinToString(CYCLE_JOINER) { cycle -> cycle.joinToString(CYCLE_SEPARATOR) }
        }
}
