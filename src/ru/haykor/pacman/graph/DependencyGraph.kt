package ru.haykor.pacman.graph

/**
 * One vertex of the dependency graph.
 *
 * @property id stable identifier, `name@version` for npm packages and `NAME` for test ones.
 * @property name package name.
 * @property version resolved version, empty for test repositories.
 */
data class GraphNode(
    val id: String,
    val name: String,
    val version: String,
)

/**
 * Directed graph of dependencies: an edge points from a package to its dependency.
 *
 * @property rootId identifier of the analysed package.
 * @property nodes vertices indexed by identifier.
 * @property edges dependency identifiers indexed by parent identifier.
 */
class DependencyGraph(
    val rootId: String,
    val nodes: Map<String, GraphNode>,
    val edges: Map<String, List<String>>,
) {
    /** Total number of edges. */
    val edgeCount: Int
        get() = edges.values.sumOf { dependencies -> dependencies.size }

    /**
     * Dependencies of a node.
     *
     * @param id node identifier.
     * @return dependency identifiers, empty when the node has no dependencies.
     */
    fun dependenciesOf(id: String): List<String> = edges[id].orEmpty()
}
