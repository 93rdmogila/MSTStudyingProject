package algorithms;

import graph.Graph;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class TopologicalOrderTest {

    @Test
    void validOrderOnRandomDags() {
        Random rnd = new Random(31);
        for (int trial = 0; trial < 100; trial++) {
            int n = 1 + rnd.nextInt(100);
            Graph dag = TestGraphs.randomDag(rnd, n, rnd.nextInt(4 * n), 1, 10);
            TopoResult r = KahnTopologicalSort.sort(dag);
            assertFalse(r.hasCycle());
            assertEquals(n, r.getOrder().length);
            assertTrue(KahnTopologicalSort.isValidOrder(dag, r.getOrder()));
            assertEquals(dag.getEdgeNumber(), r.getMetrics().edgeScans);
        }
    }

    @Test
    void cycleIsDetectedInsteadOfReturningWrongOrder() throws IOException {
        Graph cyclic = Graph.fromFile("data/directed_cyclic.txt");
        TopoResult r = KahnTopologicalSort.sort(cyclic);
        assertTrue(r.hasCycle());
        assertTrue(r.getOrder().length < cyclic.getVertexNumber());
        assertFalse(KahnTopologicalSort.isValidOrder(cyclic, r.getOrder()), "partial order must not validate");
    }

    @Test
    void selfLoopIsACycle() {
        Graph g = new Graph(2, true);
        g.addEdge(1, 1, 1);
        assertTrue(KahnTopologicalSort.sort(g).hasCycle());
    }

    @Test
    void validatorRejectsBadOrders() throws IOException {
        Graph dag = Graph.fromFile("data/directed_dag.txt");
        assertTrue(KahnTopologicalSort.isValidOrder(dag, new int[]{0, 1, 2, 3, 4, 5}));
        assertFalse(KahnTopologicalSort.isValidOrder(dag, new int[]{2, 0, 1, 3, 4, 5}), "edge 0->2 violated");
        assertFalse(KahnTopologicalSort.isValidOrder(dag, new int[]{0, 1, 2, 3, 4}), "too short");
        assertFalse(KahnTopologicalSort.isValidOrder(dag, new int[]{0, 1, 2, 3, 4, 4}), "not a permutation");
    }

    @Test
    void emptyGraphAndIsolatedVertices() {
        assertFalse(KahnTopologicalSort.sort(new Graph(0, true)).hasCycle());
        TopoResult r = KahnTopologicalSort.sort(new Graph(3, true));
        assertEquals(3, r.getOrder().length);
    }

    @Test
    void rejectsUndirectedGraph() {
        assertThrows(IllegalArgumentException.class, () -> KahnTopologicalSort.sort(new Graph(2, false)));
    }

    @Test
    void stackSafetyOnPathOf100000Vertices() {
        Graph path = TestGraphs.directedPath(100_000);
        TopoResult r = KahnTopologicalSort.sort(path);
        assertFalse(r.hasCycle());
        assertTrue(KahnTopologicalSort.isValidOrder(path, r.getOrder()));
    }
}
