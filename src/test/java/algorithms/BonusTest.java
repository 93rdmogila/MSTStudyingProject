package algorithms;

import graph.Graph;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class BonusTest {

    @Test
    void condensationIsAlwaysADag() {
        Random rnd = new Random(51);
        for (int trial = 0; trial < 100; trial++) {
            int n = 1 + rnd.nextInt(100);
            Graph g = TestGraphs.randomDirected(rnd, n, rnd.nextInt(4 * n));
            SccResult scc = KosarajuSCC.findSCC(g);
            Graph dag = Condensation.build(g, scc);
            assertEquals(scc.getCount(), dag.getVertexNumber());
            assertFalse(KahnTopologicalSort.sort(dag).hasCycle());
        }
    }

    @Test
    void criticalPathOnKnownGraph() throws IOException {
        // SCCs: A={0,1,2} B={3,4} C={5} D={6}; cross edges: 2->3 (2), 4->5 (3), 5->6 (1), 1->6 (4)
        // longest path A -> B -> C -> D = 2 + 3 + 1 = 6 (A -> D directly is only 4)
        Graph g = Graph.fromFile("data/directed_cyclic.txt");
        SccResult scc = KosarajuSCC.findSCC(g);
        Graph dag = Condensation.build(g, scc);
        assertEquals(4, dag.getVertexNumber());
        assertEquals(4, dag.getEdgeNumber());
        CriticalPath cp = CriticalPath.compute(dag);
        assertEquals(6, cp.getLength());
        assertEquals(4, cp.getPath().size());
        int[] comp = scc.getComponentOf();
        assertEquals(comp[0], (int) cp.getPath().get(0));
        assertEquals(comp[6], (int) cp.getPath().get(3));
    }

    @Test
    void criticalPathMatchesBruteForceOnSmallDags() {
        Random rnd = new Random(52);
        for (int trial = 0; trial < 100; trial++) {
            int n = 1 + rnd.nextInt(9);
            Graph dag = TestGraphs.randomDag(rnd, n, rnd.nextInt(2 * n + 1), 1, 9);
            long best = 0;
            for (int s = 0; s < n; s++) best = Math.max(best, longestFrom(dag, s));
            assertEquals(best, CriticalPath.compute(dag).getLength());
        }
    }

    private static long longestFrom(Graph g, int u) {
        long best = 0;
        for (graph.Edge e : g.getAdjList()[u]) best = Math.max(best, e.getWeight() + longestFrom(g, e.getTo()));
        return best;
    }
}
