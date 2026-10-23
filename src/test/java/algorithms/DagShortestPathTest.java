package algorithms;

import graph.Edge;
import graph.Graph;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DagShortestPathTest {

    /** Bellman-Ford with the same sources: every in-degree-0 vertex starts at 0. */
    private static long[] bellmanFord(Graph g) {
        int n = g.getVertexNumber();
        long[] dist = new long[n];
        Arrays.fill(dist, DagShortestPath.INF);
        for (int v = 0; v < n; v++) if (g.getInDegree(v) == 0) dist[v] = 0;
        for (int round = 0; round < n; round++) {
            boolean changed = false;
            for (Edge e : g.getAllEdges()) {
                if (dist[e.getFrom()] != DagShortestPath.INF && dist[e.getFrom()] + e.getWeight() < dist[e.getTo()]) {
                    dist[e.getTo()] = dist[e.getFrom()] + e.getWeight();
                    changed = true;
                }
            }
            if (!changed) break;
        }
        return dist;
    }

    @Test
    void matchesBellmanFordOnRandomDagsWithNegativeWeights() {
        Random rnd = new Random(41);
        for (int trial = 0; trial < 200; trial++) {
            int n = 1 + rnd.nextInt(60);
            Graph dag = TestGraphs.randomDag(rnd, n, rnd.nextInt(4 * n), -10, 15);
            DagShortestPath sp = DagShortestPath.compute(dag);
            assertArrayEquals(bellmanFord(dag), sp.getDistances(), "trial " + trial);
        }
    }

    @Test
    void negativeWeightsKnownGraph() throws IOException {
        Graph dag = Graph.fromFile("data/directed_dag.txt");
        DagShortestPath sp = DagShortestPath.compute(dag);
        assertArrayEquals(new long[]{0, 0, -3, -1, -4, -3}, sp.getDistances());
        assertEquals(-11, sp.sumOfDistances());
        assertEquals(List.of(1, 2, 4, 5), sp.pathTo(5));
        assertEquals(List.of(0), sp.pathTo(0), "a source's path is just itself");
    }

    @Test
    void restoredPathHasTheReportedLength() {
        Random rnd = new Random(42);
        Graph dag = TestGraphs.randomDag(rnd, 80, 300, -5, 20);
        DagShortestPath sp = DagShortestPath.compute(dag);
        for (int v = 0; v < 80; v++) {
            List<Integer> path = sp.pathTo(v);
            assertEquals(0, dag.getInDegree(path.get(0)), "path starts at a source");
            assertEquals(v, (int) path.get(path.size() - 1));
            long length = 0;
            for (int i = 0; i + 1 < path.size(); i++) {
                int a = path.get(i), b = path.get(i + 1);
                long best = Long.MAX_VALUE;
                for (Edge e : dag.getAdjList()[a]) if (e.getTo() == b) best = Math.min(best, e.getWeight());
                assertNotEquals(Long.MAX_VALUE, best, "path uses a real edge");
                length += best;
            }
            assertEquals(sp.distanceTo(v), length);
        }
    }

    @Test
    void everyVertexGetsAFiniteDistance() {
        Random rnd = new Random(43);
        Graph dag = TestGraphs.randomDag(rnd, 200, 600, 1, 9);
        for (long d : DagShortestPath.compute(dag).getDistances()) {
            assertNotEquals(DagShortestPath.INF, d);
        }
    }

    @Test
    void countersAreExactlyM() {
        Random rnd = new Random(44);
        Graph dag = TestGraphs.randomDag(rnd, 100, 500, 1, 9);
        Metrics m = new Metrics(DagShortestPath.compute(dag));
        assertEquals(dag.getEdgeNumber(), m.scans);
        assertEquals(dag.getEdgeNumber(), m.comparisons);
    }

    private static final class Metrics {
        final long scans, comparisons;
        Metrics(DagShortestPath sp) {
            scans = sp.getMetrics().edgeScans;
            comparisons = sp.getMetrics().comparisons;
        }
    }

    @Test
    void cyclicGraphIsRejected() throws IOException {
        Graph cyclic = Graph.fromFile("data/directed_cyclic.txt");
        assertThrows(IllegalArgumentException.class, () -> DagShortestPath.compute(cyclic));
    }

    @Test
    void emptyGraph() {
        DagShortestPath sp = DagShortestPath.compute(new Graph(0, true));
        assertEquals(0, sp.sumOfDistances());
    }
}
