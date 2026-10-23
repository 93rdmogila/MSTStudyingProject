package algorithms;

import graph.Edge;
import metrics.Metrics;

import java.util.List;

/** Result of Kruskal / Prim: a minimum spanning forest (a tree if the graph is connected). */
public class MSTResult {
    private final List<Edge> edges;
    private final long totalWeight;
    private final int components;
    private final Metrics metrics;

    public MSTResult(List<Edge> edges, long totalWeight, int components, Metrics metrics) {
        this.edges = edges;
        this.totalWeight = totalWeight;
        this.components = components;
        this.metrics = metrics;
    }

    public List<Edge> getEdges() { return edges; }
    public long getTotalWeight() { return totalWeight; }
    /** Number of connected components; 1 means the forest is a single spanning tree. */
    public int getComponents() { return components; }
    public Metrics getMetrics() { return metrics; }
}
