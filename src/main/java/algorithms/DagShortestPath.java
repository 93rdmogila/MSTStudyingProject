package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Single-pass shortest paths in a DAG, O(V + E): relax the outgoing edges of every vertex in
 * topological order.
 *
 * Virtual super-source: EVERY vertex with in-degree 0 starts at distance 0, all others at infinity.
 * In a DAG each vertex is reachable from some in-degree-0 vertex, so every distance is finite and the
 * answer does not depend on which vertex "happened" to be chosen as the source.
 *
 * Negative weights are fine: when u is processed, all its predecessors are already final (induction
 * on the topological order), so no distance is ever improved after it has been used. There is no
 * "the smallest vertex is final" assumption as in Dijkstra, and no cycle that could shrink a path forever.
 *
 * Counters: edgeScans = E, comparisons = E (one "dist[u] + w &lt; dist[v]" per edge).
 */
public class DagShortestPath {
    public static final long INF = Long.MAX_VALUE;

    private final long[] dist;
    private final int[] parent;
    private final Metrics metrics;

    private DagShortestPath(long[] dist, int[] parent, Metrics metrics) {
        this.dist = dist;
        this.parent = parent;
        this.metrics = metrics;
    }

    /** Runs Kahn first; throws IllegalArgumentException if the graph has a cycle. */
    public static DagShortestPath compute(Graph graph) {
        TopoResult topo = KahnTopologicalSort.sort(graph);
        if (topo.hasCycle()) {
            throw new IllegalArgumentException("Graph contains a cycle: shortest paths by one pass need a DAG");
        }
        return compute(graph, topo.getOrder());
    }

    /** Relaxation pass over a given valid topological order (its cost is NOT included in the metrics). */
    public static DagShortestPath compute(Graph graph, int[] topoOrder) {
        int n = graph.getVertexNumber();
        List<Edge>[] adj = graph.getAdjList();
        Metrics metrics = new Metrics();

        long[] dist = new long[n];
        int[] parent = new int[n];
        java.util.Arrays.fill(dist, INF);
        java.util.Arrays.fill(parent, -1);
        for (int v = 0; v < n; v++) {
            if (graph.getInDegree(v) == 0) {
                dist[v] = 0; // virtual super-source with 0-weight edges to every in-degree-0 vertex
            }
        }
        for (int u : topoOrder) {
            if (dist[u] == INF) {
                continue; // unreachable from any source (cannot happen in a DAG)
            }
            for (Edge e : adj[u]) {
                metrics.edgeScans++;
                metrics.comparisons++;
                long candidate = dist[u] + e.getWeight();
                if (candidate < dist[e.getTo()]) {
                    dist[e.getTo()] = candidate;
                    parent[e.getTo()] = u;
                }
            }
        }
        return new DagShortestPath(dist, parent, metrics);
    }

    public long[] getDistances() { return dist; }
    public int[] getParent() { return parent; }
    public Metrics getMetrics() { return metrics; }
    public long distanceTo(int v) { return dist[v]; }

    /** Sum of all finite distances (the "result" column of the benchmark). */
    public long sumOfDistances() {
        long sum = 0;
        for (long d : dist) {
            if (d != INF) sum += d;
        }
        return sum;
    }

    /** Vertices of a shortest path from its (in-degree-0) start to v, restored through parent[]. */
    public List<Integer> pathTo(int v) {
        if (dist[v] == INF) {
            return Collections.emptyList();
        }
        List<Integer> path = new ArrayList<>();
        for (int cur = v; cur != -1; cur = parent[cur]) {
            path.add(cur);
        }
        Collections.reverse(path);
        return path;
    }
}
