package ru.haykor.pacman.graph

import ru.haykor.pacman.ExecutionException
import ru.haykor.pacman.source.DependencySource
import ru.haykor.pacman.source.PackageRequest
import ru.haykor.pacman.source.ResolvedPackage

/**
 * Result of a breadth-first graph traversal.
 *
 * @property graph dependency graph reachable from the analysed package.
 * @property excluded names of packages removed by the filter.
 */
data class GraphBuildResult(
    val graph: DependencyGraph,
    val excluded: List<String>,
)

/**
 * Builds the dependency graph with a non-recursive breadth-first search.
 *
 * @property source source of resolved dependencies.
 * @property filter filter applied to package names.
 */
class DependencyGraphBuilder(
    private val source: DependencySource,
    private val filter: PackageNameFilter,
) {
    /**
     * Traverses dependencies starting from the analysed package.
     *
     * @param root request for the analysed package.
     * @return graph and the names excluded by the filter.
     * @throws ExecutionException when the analysed package itself is filtered out.
     */
    suspend fun build(root: PackageRequest): GraphBuildResult {
        if (filter.matches(root.name)) {
            throw ExecutionException(
                "package '${root.name}' matches filter '${filter.value}', nothing to analyse",
            )
        }
        val queue = ArrayDeque<PendingDependency>()
        val nodes = mutableMapOf<String, GraphNode>()
        val edges = mutableMapOf<String, MutableSet<String>>()
        val excluded = mutableSetOf<String>()
        var rootId: String? = null
        queue.add(PendingDependency(parentId = null, request = root))
        while (queue.isNotEmpty()) {
            val pending = queue.removeFirst()
            val resolved = source.resolve(pending.request)
            val parentId = pending.parentId
            if (parentId == null) {
                rootId = resolved.id
            } else {
                edges.addEdge(parentId, resolved.id)
            }
            if (nodes.containsKey(resolved.id)) {
                continue
            }
            nodes[resolved.id] = GraphNode(resolved.id, resolved.name, resolved.version)
            enqueueDependencies(resolved, resolved.id, queue, excluded)
        }
        return GraphBuildResult(
            graph = DependencyGraph(
                rootId = rootId ?: throw ExecutionException("dependency graph is empty"),
                nodes = nodes,
                edges = edges.mapValues { (_, children) -> children.toList() },
            ),
            excluded = excluded.sorted(),
        )
    }

    private fun enqueueDependencies(
        resolved: ResolvedPackage,
        parentId: String,
        queue: ArrayDeque<PendingDependency>,
        excluded: MutableSet<String>,
    ) {
        val dependencies = resolved.dependencies.entries
            .sortedBy { entry -> entry.key }
        for ((name, range) in dependencies) {
            if (filter.matches(name)) {
                excluded += name
                continue
            }
            queue.add(PendingDependency(parentId, PackageRequest(name, range)))
        }
    }
}

/**
 * Queue item of the breadth-first traversal.
 *
 * @property parentId identifier of the dependent package, `null` for the root.
 * @property request dependency request to resolve.
 */
private data class PendingDependency(
    val parentId: String?,
    val request: PackageRequest,
)

private fun MutableMap<String, MutableSet<String>>.addEdge(parentId: String, childId: String) {
    getOrPut(parentId) { mutableSetOf() }.add(childId)
}
