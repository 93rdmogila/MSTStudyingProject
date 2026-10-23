package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.List;

/**
 * Kahn's algorithm: in-degrees + a queue of vertices with in-degree 0. Removing a vertex lowers the
 * in-degree of its neighbours. If fewer than n vertices leave the queue, the rest lie on or behind a
 * cycle, so the graph is NOT a DAG - we report that instead of returning a wrong order.
 *
 * O(V + E) time, O(V) extra space. In-degrees come from {@link Graph#copyInDegrees()} (the graph
 * maintains them on insertion), so edgeScans = E exactly: every edge is looked at once when its
 * tail leaves the queue.
 */
public final class KahnTopologicalSort {
    private KahnTopologicalSort() { }

    public static TopoResult sort(Graph graph) {
        if (!graph.isDirected()) {
            throw new IllegalArgumentException("Topological order needs a directed graph");
        }
        int n = graph.getVertexNumber();
        List<Edge>[] adj = graph.getAdjList();
        Metrics metrics = new Metrics();

        int[] inDegree = graph.copyInDegrees();
        int[] queue = new int[n];
        int head = 0, tail = 0;
        for (int v = 0; v < n; v++) {
            if (inDegree[v] == 0) {
                queue[tail++] = v;
            }
        }
        while (head < tail) {
            int u = queue[head++];
            for (Edge e : adj[u]) {
                metrics.edgeScans++;
                if (--inDegree[e.getTo()] == 0) {
                    queue[tail++] = e.getTo();
                }
            }
        }
        boolean cycle = tail != n;
        int[] order = cycle ? java.util.Arrays.copyOf(queue, tail) : queue;
        return new TopoResult(order, cycle, metrics);
    }

    /**
     * Validation: order is a permutation of 0..n-1 and pos[u] &lt; pos[v] for EVERY edge u -> v
     * (self-loops can never satisfy this, as they should not in a DAG).
     */
    public static boolean isValidOrder(Graph graph, int[] order) {
        int n = graph.getVertexNumber();
        if (order.length != n) {
            return false;
        }
        int[] pos = new int[n];
        java.util.Arrays.fill(pos, -1);
        for (int i = 0; i < n; i++) {
            int v = order[i];
            if (v < 0 || v >= n || pos[v] != -1) {
                return false;
            }
            pos[v] = i;
        }
        for (Edge e : graph.getAllEdges()) {
            if (pos[e.getFrom()] >= pos[e.getTo()]) {
                return false;
            }
        }
        return true;
    }
}
