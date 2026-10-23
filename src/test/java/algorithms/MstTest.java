package algorithms;

import graph.Edge;
import graph.Graph;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class MstTest {

    @Test
    void kruskalAndPrimAgreeOn100RandomConnectedGraphs() {
        Random rnd = new Random(7);
        for (int i = 0; i < 100; i++) {
            int n = 2 + rnd.nextInt(60);
            Graph g = TestGraphs.randomConnected(rnd, n, rnd.nextInt(4 * n));
            MSTResult k = KruskalMST.findMST(g);
            MSTResult p = PrimMST.findMST(g);
            assertEquals(k.getTotalWeight(), p.getTotalWeight(), "graph #" + i);
            assertEquals(n - 1, k.getEdges().size());
            assertEquals(n - 1, p.getEdges().size());
            assertEquals(1, k.getComponents());
            assertEquals(1, p.getComponents());
        }
    }

    @Test
    void treeIsAcyclicAndSpanning() {
        Random rnd = new Random(8);
        Graph g = TestGraphs.randomConnected(rnd, 80, 200);
        for (MSTResult r : new MSTResult[]{KruskalMST.findMST(g), PrimMST.findMST(g)}) {
            DSU dsu = new DSU(80);
            for (Edge e : r.getEdges()) {
                assertTrue(dsu.union(e.getFrom(), e.getTo()), "tree edge closes a cycle");
            }
            assertEquals(1, dsu.components());
        }
    }

    @Test
    void matchesBruteForceOnTinyGraphs() {
        // brute force: try every subset of n-1 edges, keep the lightest one that spans
        Random rnd = new Random(9);
        for (int trial = 0; trial < 60; trial++) {
            int n = 2 + rnd.nextInt(5);
            Graph g = TestGraphs.randomConnected(rnd, n, rnd.nextInt(5));
            java.util.List<Edge> edges = g.getAllEdges();
            int m = edges.size();
            long best = Long.MAX_VALUE;
            for (int mask = 0; mask < (1 << m); mask++) {
                if (Integer.bitCount(mask) != n - 1) continue;
                DSU dsu = new DSU(n);
                long w = 0;
                for (int i = 0; i < m; i++) {
                    if ((mask >> i & 1) == 1) { dsu.union(edges.get(i).getFrom(), edges.get(i).getTo()); w += edges.get(i).getWeight(); }
                }
                if (dsu.components() == 1) best = Math.min(best, w);
            }
            assertEquals(best, KruskalMST.findMST(g).getTotalWeight());
            assertEquals(best, PrimMST.findMST(g).getTotalWeight());
        }
    }

    @Test
    void knownSmallGraph() throws IOException {
        Graph g = Graph.fromFile("data/undirected_connected.txt");
        assertEquals(16, KruskalMST.findMST(g).getTotalWeight());
        assertEquals(16, PrimMST.findMST(g).getTotalWeight());
    }

    @Test
    void disconnectedGraphGivesForestInsteadOfException() throws IOException {
        Graph g = Graph.fromFile("data/undirected_disconnected.txt");
        MSTResult k = KruskalMST.findMST(g);
        MSTResult p = PrimMST.findMST(g);
        assertEquals(6, k.getTotalWeight());
        assertEquals(6, p.getTotalWeight());
        assertEquals(3, k.getComponents());
        assertEquals(3, p.getComponents());
        assertEquals(3, k.getEdges().size()); // n - components = 6 - 3
        assertEquals(3, p.getEdges().size());
    }

    @Test
    void randomForestsAgree() {
        Random rnd = new Random(10);
        for (int i = 0; i < 100; i++) {
            int n = 1 + rnd.nextInt(40);
            Graph g = TestGraphs.randomUndirected(rnd, n, rnd.nextInt(2 * n));
            MSTResult k = KruskalMST.findMST(g);
            MSTResult p = PrimMST.findMST(g);
            assertEquals(k.getTotalWeight(), p.getTotalWeight());
            assertEquals(k.getComponents(), p.getComponents());
            assertEquals(n - k.getComponents(), k.getEdges().size());
        }
    }

    @Test
    void trivialGraphs() {
        assertEquals(0, KruskalMST.findMST(new Graph(0, false)).getTotalWeight());
        assertEquals(0, PrimMST.findMST(new Graph(0, false)).getComponents());
        Graph one = new Graph(1, false);
        one.addEdge(0, 0, 9);
        assertEquals(0, KruskalMST.findMST(one).getTotalWeight());
        assertEquals(1, PrimMST.findMST(one).getComponents());
        Graph isolated = new Graph(4, false);
        assertEquals(4, KruskalMST.findMST(isolated).getComponents());
        assertEquals(4, PrimMST.findMST(isolated).getComponents());
    }

    @Test
    void rejectsDirectedGraph() {
        assertThrows(IllegalArgumentException.class, () -> KruskalMST.findMST(new Graph(2, true)));
        assertThrows(IllegalArgumentException.class, () -> PrimMST.findMST(new Graph(2, true)));
    }

    @Test
    void kruskalCountersAreCountedInsideTheAlgorithm() {
        Random rnd = new Random(11);
        Graph g = TestGraphs.randomConnected(rnd, 100, 400);
        MSTResult k = KruskalMST.findMST(g);
        assertTrue(k.getMetrics().edgeScans <= g.getEdgeNumber());
        assertTrue(k.getMetrics().edgeScans >= 99);
        assertTrue(k.getMetrics().dsuOps >= 2 * k.getMetrics().edgeScans);
        assertTrue(k.getMetrics().comparisons > 0);
        MSTResult p = PrimMST.findMST(g);
        assertEquals(2L * g.getEdgeNumber() - selfLoops(g), p.getMetrics().edgeScans);
    }

    private static long selfLoops(Graph g) {
        return g.getAllEdges().stream().filter(e -> e.getFrom() == e.getTo()).count();
    }

    @Test
    void edgeSortIsSortedAndStable() {
        Random rnd = new Random(12);
        for (int trial = 0; trial < 20; trial++) {
            int n = rnd.nextInt(300);
            Edge[] edges = new Edge[n];
            for (int i = 0; i < n; i++) edges[i] = new Edge(i, 0, rnd.nextInt(10));
            metrics.Metrics m = new metrics.Metrics();
            EdgeSort.sort(edges, m);
            for (int i = 1; i < n; i++) {
                assertTrue(edges[i - 1].getWeight() <= edges[i].getWeight());
                if (edges[i - 1].getWeight() == edges[i].getWeight()) {
                    assertTrue(edges[i - 1].getFrom() < edges[i].getFrom(), "stable");
                }
            }
        }
    }
}
