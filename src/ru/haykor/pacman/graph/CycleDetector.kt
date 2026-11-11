package ru.haykor.pacman.graph

/**
 * Finds cycles in a dependency graph with an iterative depth-first search.
 */
object CycleDetector {
    private const val SEPARATOR: String = " -> "

    /**
     * Enumerates cycles reachable from the graph nodes.
     *
     * Each cycle is reported once in the order of the first discovery, the
     * first node is repeated at the end, for example `A -> B -> A`.
     *
     * @param graph dependency graph to inspect.
     * @return discovered cycles, empty when the graph is acyclic.
     */
    fun findCycles(graph: DependencyGraph): List<List<String>> {
        val colors = mutableMapOf<String, VisitColor>()
        val cycles = mutableListOf<List<String>>()
        val seen = mutableSetOf<String>()
        graph.nodes.keys.sorted().forEach { start ->
            if ((colors[start] ?: VisitColor.NEW) == VisitColor.NEW) {
                collectCycles(graph, start, colors, cycles, seen)
            }
        }
        return cycles
    }

    private fun collectCycles(
        graph: DependencyGraph,
        start: String,
        colors: MutableMap<String, VisitColor>,
        cycles: MutableList<List<String>>,
        seen: MutableSet<String>,
    ) {
        val path = mutableListOf(start)
        val stack = ArrayDeque<Frame>()
        colors[start] = VisitColor.ACTIVE
        stack.addLast(Frame(start, graph.dependenciesOf(start).sorted()))
        while (stack.isNotEmpty()) {
            val frame = stack.last()
            if (frame.index >= frame.children.size) {
                stack.removeLast()
                path.removeAt(path.lastIndex)
                colors[frame.id] = VisitColor.VISITED
                continue
            }
            val child = frame.children[frame.index]
            frame.index++
            when (colors[child] ?: VisitColor.NEW) {
                VisitColor.NEW -> {
                    colors[child] = VisitColor.ACTIVE
                    path += child
                    stack.addLast(Frame(child, graph.dependenciesOf(child).sorted()))
                }

                VisitColor.ACTIVE -> recordCycle(child, path, cycles, seen)
                VisitColor.VISITED -> Unit
            }
        }
    }

    private fun recordCycle(
        nodeId: String,
        path: List<String>,
        cycles: MutableList<List<String>>,
        seen: MutableSet<String>,
    ) {
        val startIndex = path.indexOf(nodeId)
        if (startIndex < 0) {
            return
        }
        val cycle = path.subList(startIndex, path.size) + nodeId
        if (seen.add(canonicalForm(cycle))) {
            cycles += cycle
        }
    }

    private fun canonicalForm(cycle: List<String>): String {
        val body = cycle.dropLast(1)
        if (body.isEmpty()) {
            return cycle.joinToString(SEPARATOR)
        }
        val rotations = body.indices.map { index -> body.drop(index) + body.take(index) }
        return rotations.minOf { rotation -> rotation.joinToString(SEPARATOR) }
    }

    /**
     * Traversal state of a node.
     */
    private enum class VisitColor {
        /** Node is not visited yet. */
        NEW,

        /** Node is in the current DFS path. */
        ACTIVE,

        /** Node and all its descendants are fully processed. */
        VISITED,
    }

    /**
     * Stack frame of the iterative depth-first search.
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
