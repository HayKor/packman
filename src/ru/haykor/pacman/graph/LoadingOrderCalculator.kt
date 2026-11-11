package ru.haykor.pacman.graph

/**
 * Computes the order in which dependencies must be loaded.
 *
 * Dependencies always precede their dependents, the analysed package goes last.
 * The order is deterministic: dependencies of a node are visited in
 * lexicographic order. Back edges of cycles are skipped, so the result is
 * always a full permutation of the graph nodes.
 */
object LoadingOrderCalculator {
    /**
     * Calculates the loading order of the graph.
     *
     * @param graph dependency graph.
     * @return node identifiers with dependencies first.
     */
    fun calculate(graph: DependencyGraph): List<String> {
        val visited = mutableSetOf<String>()
        val order = mutableListOf<String>()
        val startNodes = listOf(graph.rootId) + graph.nodes.keys.sorted()
        startNodes.forEach { start ->
            if (start !in visited) {
                visitFrom(graph, start, visited, order)
            }
        }
        return order
    }

    private fun visitFrom(
        graph: DependencyGraph,
        start: String,
        visited: MutableSet<String>,
        order: MutableList<String>,
    ) {
        val path = mutableSetOf(start)
        val stack = ArrayDeque<Frame>()
        visited += start
        stack.addLast(Frame(start, graph.dependenciesOf(start).sorted()))
        while (stack.isNotEmpty()) {
            val frame = stack.last()
            if (frame.index >= frame.children.size) {
                stack.removeLast()
                path -= frame.id
                order += frame.id
                continue
            }
            val child = frame.children[frame.index]
            frame.index++
            if (child in path || child in visited) {
                continue
            }
            visited += child
            path += child
            stack.addLast(Frame(child, graph.dependenciesOf(child).sorted()))
        }
    }

    /**
     * Stack frame of the iterative post-order traversal.
     *
     * @property id node identifier.
     * @property children sorted dependency identifiers.
     * @property index next child to visit.
     */
    private data class Frame(
        val id: String,
        val children: List<String>,
        var index: Int = 0,
    )
}
