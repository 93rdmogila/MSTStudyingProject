package metrics;

/**
 * Operation counters. They are incremented INSIDE the algorithms, so they are identical on every
 * machine, JVM and run - unlike wall-clock time.
 */
public class Metrics {
    /** DSU: find calls + successful union calls. */
    public long dsuOps;
    /** Edges looked at (adjacency entries or edge-list entries, depending on the algorithm). */
    public long edgeScans;
    /** Weight comparisons (sorting in Kruskal, heap comparisons in Prim, relaxations in DAG paths). */
    public long comparisons;
    /** Deepest the explicit DFS stack ever got (0 for algorithms without a stack). */
    public long maxDepth;

    public void noteDepth(long depth) {
        if (depth > maxDepth) {
            maxDepth = depth;
        }
    }

    @Override
    public String toString() {
        return "dsuOps=" + dsuOps + " edgeScans=" + edgeScans + " comparisons=" + comparisons + " maxDepth=" + maxDepth;
    }
}
