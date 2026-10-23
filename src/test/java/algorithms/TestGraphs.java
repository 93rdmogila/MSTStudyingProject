package algorithms;

import graph.Graph;

import java.util.Random;

/** Random graph builders shared by the tests. */
final class TestGraphs {
    private TestGraphs() { }

    /** Connected undirected graph: random tree + extra random edges (self-loops and parallel edges allowed). */
    static Graph randomConnected(Random rnd, int n, int extra) {
        Graph g = new Graph(n, false);
        for (int v = 1; v < n; v++) {
            g.addEdge(rnd.nextInt(v), v, 1 + rnd.nextInt(20));
        }
        for (int i = 0; i < extra; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(20));
        }
        return g;
    }

    static Graph randomUndirected(Random rnd, int n, int m) {
        Graph g = new Graph(n, false);
        for (int i = 0; i < m; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(20));
        }
        return g;
    }

    static Graph randomDirected(Random rnd, int n, int m) {
        Graph g = new Graph(n, true);
        for (int i = 0; i < m; i++) {
            g.addEdge(rnd.nextInt(n), rnd.nextInt(n), 1 + rnd.nextInt(20));
        }
        return g;
    }

    /** Random DAG (edges go from lower to higher rank); weights in [minW, maxW], may be negative. */
    static Graph randomDag(Random rnd, int n, int m, int minW, int maxW) {
        int[] rank = new int[n];
        for (int i = 0; i < n; i++) rank[i] = i;
        for (int i = n - 1; i > 0; i--) {
            int j = rnd.nextInt(i + 1);
            int t = rank[i]; rank[i] = rank[j]; rank[j] = t;
        }
        Graph g = new Graph(n, true);
        for (int i = 0; i < m && n > 1; i++) {
            int a = rnd.nextInt(n), b = rnd.nextInt(n);
            if (a == b) continue;
            if (rank[a] > rank[b]) { int t = a; a = b; b = t; }
            g.addEdge(a, b, minW + rnd.nextInt(maxW - minW + 1));
        }
        return g;
    }

    static Graph directedPath(int n) {
        Graph g = new Graph(n, true);
        for (int i = 0; i + 1 < n; i++) g.addEdge(i, i + 1, 1);
        return g;
    }
}
