package algorithms;

import graph.Edge;
import graph.Graph;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class SccTest {

    /** Brute force: reach[u][v] by BFS from every vertex; u and v are in one SCC iff reach[u][v] && reach[v][u]. */
    private static boolean[][] reachability(Graph g) {
        int n = g.getVertexNumber();
        boolean[][] reach = new boolean[n][n];
        for (int s = 0; s < n; s++) {
            ArrayDeque<Integer> queue = new ArrayDeque<>();
            queue.add(s);
            reach[s][s] = true;
            while (!queue.isEmpty()) {
                int u = queue.poll();
                for (Edge e : g.getAdjList()[u]) {
                    if (!reach[s][e.getTo()]) {
                        reach[s][e.getTo()] = true;
                        queue.add(e.getTo());
                    }
                }
            }
        }
        return reach;
    }

    private static void assertMatchesBruteForce(Graph g, SccResult result) {
        int n = g.getVertexNumber();
        boolean[][] reach = reachability(g);
        int[] comp = result.getComponentOf();
        for (int u = 0; u < n; u++) {
            for (int v = 0; v < n; v++) {
                boolean same = reach[u][v] && reach[v][u];
                assertEquals(same, comp[u] == comp[v], "vertices " + u + " and " + v);
            }
        }
        java.util.Set<Integer> ids = new java.util.HashSet<>();
        for (int c : comp) ids.add(c);
        assertEquals(result.getCount(), ids.size());
    }

    @Test
    void kosarajuMatchesBruteForceOnRandomGraphs() {
        Random rnd = new Random(21);
        for (int trial = 0; trial < 150; trial++) {
            int n = 1 + rnd.nextInt(200);
            int m = rnd.nextInt(3 * n + 1);
            Graph g = TestGraphs.randomDirected(rnd, n, m);
            assertMatchesBruteForce(g, KosarajuSCC.findSCC(g));
        }
    }

    @Test
    void tarjanMatchesBruteForceAndKosaraju() {
        Random rnd = new Random(22);
        for (int trial = 0; trial < 150; trial++) {
            int n = 1 + rnd.nextInt(200);
            int m = rnd.nextInt(3 * n + 1);
            Graph g = TestGraphs.randomDirected(rnd, n, m);
            SccResult t = TarjanSCC.findSCC(g);
            assertMatchesBruteForce(g, t);
            assertTrue(t.samePartitionAs(KosarajuSCC.findSCC(g)));
        }
    }

    @Test
    void knownGraph() throws IOException {
        Graph g = Graph.fromFile("data/directed_cyclic.txt");
        for (SccResult r : new SccResult[]{KosarajuSCC.findSCC(g), TarjanSCC.findSCC(g)}) {
            assertEquals(4, r.getCount());
            int[] c = r.getComponentOf();
            assertTrue(c[0] == c[1] && c[1] == c[2]);
            assertEquals(c[3], c[4]);
            assertNotEquals(c[0], c[3]);
            assertNotEquals(c[5], c[6]);
            assertEquals(4, r.getComponents().size());
        }
    }

    @Test
    void edgeScansMatchTheory() {
        Random rnd = new Random(23);
        Graph g = TestGraphs.randomDirected(rnd, 500, 2000);
        assertEquals(2L * g.getEdgeNumber(), KosarajuSCC.findSCC(g).getMetrics().edgeScans, "Kosaraju scans 2m");
        assertEquals(g.getEdgeNumber(), TarjanSCC.findSCC(g).getMetrics().edgeScans, "Tarjan scans m");
    }

    @Test
    void selfLoopsParallelEdgesAndIsolatedVertices() {
        Graph g = new Graph(5, true);
        g.addEdge(0, 0, 1);
        g.addEdge(0, 1, 1);
        g.addEdge(0, 1, 2);
        g.addEdge(1, 0, 3);
        assertMatchesBruteForce(g, KosarajuSCC.findSCC(g));
        assertEquals(4, KosarajuSCC.findSCC(g).getCount()); // {0,1}, {2}, {3}, {4}
        assertEquals(0, KosarajuSCC.findSCC(new Graph(0, true)).getCount());
    }

    @Test
    void stackSafetyOnPathOf100000Vertices() {
        int n = 100_000;
        Graph path = TestGraphs.directedPath(n);
        SccResult k = KosarajuSCC.findSCC(path);
        assertEquals(n, k.getCount());
        assertTrue(k.getMetrics().maxDepth >= n - 1, "depth of the explicit stack is recorded");
        SccResult t = TarjanSCC.findSCC(path);
        assertEquals(n, t.getCount());
        // also a single huge cycle
        Graph cycle = TestGraphs.directedPath(n);
        cycle.addEdge(n - 1, 0, 1);
        assertEquals(1, KosarajuSCC.findSCC(cycle).getCount());
        assertEquals(1, TarjanSCC.findSCC(cycle).getCount());
    }
}
