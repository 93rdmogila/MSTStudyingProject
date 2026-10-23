package algorithms;

import metrics.Metrics;

/** Result of Kahn's algorithm. If hasCycle is true, order holds only the vertices that could be emitted. */
public class TopoResult {
    private final int[] order;
    private final boolean hasCycle;
    private final Metrics metrics;

    public TopoResult(int[] order, boolean hasCycle, Metrics metrics) {
        this.order = order;
        this.hasCycle = hasCycle;
        this.metrics = metrics;
    }

    /** Vertices in topological order (all n of them when there is no cycle). */
    public int[] getOrder() { return order; }
    public boolean hasCycle() { return hasCycle; }
    public Metrics getMetrics() { return metrics; }
}
