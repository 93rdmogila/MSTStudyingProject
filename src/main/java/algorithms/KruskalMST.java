package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;

/**
 * Kruskal: sort edges by weight, take an edge if its ends are in different DSU sets.
 * O(E log E) for the sort + O(E alpha(V)) for the DSU. A disconnected graph gives a minimum
 * spanning FOREST (no exception); {@link MSTResult#getComponents()} tells how many trees.
 *
 * Counters: comparisons = weight comparisons in the sort; edgeScans = edges examined in the main
 * loop (the loop stops as soon as V-1 edges are chosen, so on dense graphs far fewer than E);
 * dsuOps = find + successful union calls.
 */
public final class KruskalMST {
    private KruskalMST() { }

    public static MSTResult findMST(Graph graph) {
        if (graph.isDirected()) {
            throw new IllegalArgumentException("MST is defined for undirected graphs");
        }
        int n = graph.getVertexNumber();
        Metrics metrics = new Metrics();

        Edge[] sorted = graph.getAllEdges().toArray(new Edge[0]);
        EdgeSort.sort(sorted, metrics);

        DSU dsu = new DSU(n);
        List<Edge> tree = new ArrayList<>();
        long total = 0;
        for (Edge e : sorted) {
            if (tree.size() == n - 1) {
                break; // spanning tree is complete
            }
            metrics.edgeScans++;
            // union() = find(u) != find(v) check followed by the merge
            if (dsu.union(e.getFrom(), e.getTo())) {
                tree.add(e);
                total += e.getWeight();
            }
        }
        metrics.dsuOps = dsu.getOps();
        return new MSTResult(tree, total, dsu.components(), metrics);
    }
}
