package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Lazy Prim with a PriorityQueue: start at vertex 0, repeatedly take the lightest edge leaving the
 * tree, skip it if its end is already in the tree. O(E log E) = O(E log V).
 * If the graph is disconnected, Prim is restarted from the next unvisited vertex, so the result is a
 * minimum spanning forest.
 *
 * Counters: edgeScans = adjacency entries looked at (every vertex's list is scanned once, so 2E in an
 * undirected graph); comparisons = every comparison made inside the PriorityQueue.
 */
public final class PrimMST {
    private PrimMST() { }

    public static MSTResult findMST(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("MST is defined for undirected graphs");
        }
        int n = graph.getVertexNumber();
        List<Edge>[] adj = graph.getAdjList();
        Metrics metrics = new Metrics();

        boolean[] inTree = new boolean[n];
        List<Edge> tree = new ArrayList<>();
        long total = 0;
        int components = 0;

        PriorityQueue<Edge> pq = new PriorityQueue<>((a, b) -> {
            metrics.comparisons++;
            return Integer.compare(a.getWeight(), b.getWeight());
        });

        for (int start = 0; start < n; start++) { // start = 0 first; later starts only for other components
            if (inTree[start]) {
                continue;
            }
            components++;
            addVertex(start, adj, inTree, pq, metrics);
            while (!pq.isEmpty()) {
                Edge e = pq.poll();
                if (inTree[e.getTo()]) {
                    continue; // both ends already in the tree - skip
                }
                tree.add(e);
                total += e.getWeight();
                addVertex(e.getTo(), adj, inTree, pq, metrics);
            }
        }
        return new MSTResult(tree, total, components, metrics);
    }

    private static void addVertex(int v, List<Edge>[] adj, boolean[] inTree, PriorityQueue<Edge> pq, Metrics metrics) {
        inTree[v] = true;
        for (Edge e : adj[v]) {
            metrics.edgeScans++;
            if (!inTree[e.getTo()]) {
                pq.add(e);
            }
        }
    }
}
