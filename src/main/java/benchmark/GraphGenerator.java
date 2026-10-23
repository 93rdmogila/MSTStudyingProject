package benchmark;

import graph.Graph;

import java.util.Random;

/**
 * The exact generator from the assignment. Every graph starts from a fresh {@code new Random(42)},
 * so all counters and results are identical on every machine.
 *
 * Weights are 1 + rnd.nextInt(1000), drawn right after the two ends of an edge.
 */
public final class GraphGenerator {
    public static final long SEED = 42L;
    private static final int MAX_WEIGHT = 1000;

    private GraphGenerator() { }

    /** Connected undirected graph: random spanning tree (n-1 edges) + random extra edges up to m. */
    public static Graph undirected(int n, int m) {
        Random rnd = new Random(SEED);
        Graph g = new Graph(n, false);
        int[] perm = shuffled(n, rnd);
        for (int i = 1; i < n; i++) {
            int u = perm[rnd.nextInt(i)];
            int v = perm[i];
            g.addEdge(u, v, weight(rnd));
        }
        for (int added = n - 1; added < m; added++) {
            addRandomEdge(g, n, rnd, null);
        }
        return g;
    }

    /** Directed graph: only random edges (no spanning tree), so it usually has cycles. */
    public static Graph directed(int n, int m) {
        Random rnd = new Random(SEED);
        Graph g = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            addRandomEdge(g, n, rnd, null);
        }
        return g;
    }

    /** DAG: random ranks, every random edge is turned to point from the lower rank to the higher rank. */
    public static Graph dag(int n, int m) {
        Random rnd = new Random(SEED);
        Graph g = new Graph(n, true);
        int[] rank = shuffled(n, rnd);
        for (int i = 0; i < m; i++) {
            addRandomEdge(g, n, rnd, rank);
        }
        return g;
    }

    private static void addRandomEdge(Graph g, int n, Random rnd, int[] rank) {
        int a = rnd.nextInt(n);
        int b = rnd.nextInt(n);
        while (b == a) {
            b = rnd.nextInt(n);
        }
        if (rank != null && rank[a] > rank[b]) {
            int t = a; a = b; b = t;
        }
        g.addEdge(a, b, weight(rnd));
    }

    private static int[] shuffled(int n, Random rnd) {
        int[] p = new int[n];
        for (int i = 0; i < n; i++) p[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = p[i]; p[i] = p[j]; p[j] = t;
        }
        return p;
    }

    private static int weight(Random rnd) {
        return 1 + rnd.nextInt(MAX_WEIGHT);
    }
}
