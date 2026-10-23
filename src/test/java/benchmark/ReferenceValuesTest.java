package benchmark;

import algorithms.*;
import graph.Graph;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/** The generator + algorithms must reproduce the reference table of the assignment (the two smallest sizes here for speed). */
class ReferenceValuesTest {

    private static void check(int n, int density) {
        int m = n * density;
        ReferenceValues.Row ref = ReferenceValues.get(n, m);
        assertNotNull(ref);

        Graph u = GraphGenerator.undirected(n, m);
        assertEquals(m, u.getEdgeNumber());
        MSTResult k = KruskalMST.findMST(u);
        MSTResult p = PrimMST.findMST(u);
        assertEquals(ref.mstWeight, k.getTotalWeight(), "Kruskal " + n + "/" + m);
        assertEquals(ref.mstWeight, p.getTotalWeight(), "Prim " + n + "/" + m);
        assertEquals(1, k.getComponents(), "generated graph is connected");

        Graph d = GraphGenerator.directed(n, m);
        SccResult scc = KosarajuSCC.findSCC(d);
        assertEquals(ref.sccCount, scc.getCount(), "SCC " + n + "/" + m);
        assertEquals(2L * m, scc.getMetrics().edgeScans);
        assertEquals(ref.sccCount, TarjanSCC.findSCC(d).getCount());

        Graph dag = GraphGenerator.dag(n, m);
        TopoResult topo = KahnTopologicalSort.sort(dag);
        assertFalse(topo.hasCycle());
        assertTrue(KahnTopologicalSort.isValidOrder(dag, topo.getOrder()));
        assertEquals(m, topo.getMetrics().edgeScans);
        DagShortestPath sp = DagShortestPath.compute(dag, topo.getOrder());
        assertEquals(ref.dagDistanceSum, sp.sumOfDistances(), "DAG " + n + "/" + m);
        assertEquals(m, sp.getMetrics().edgeScans);
    }

    @Test
    void n1000Sparse() { check(1000, 4); }

    @Test
    void n1000Dense() { check(1000, 20); }

    @Test
    void n3162Sparse() { check(3162, 4); }

    @Test
    void n3162Dense() { check(3162, 20); }

    @Test
    void generatorIsDeterministic() {
        Graph a = GraphGenerator.undirected(500, 2000);
        Graph b = GraphGenerator.undirected(500, 2000);
        for (int i = 0; i < a.getEdgeNumber(); i++) {
            assertEquals(a.getAllEdges().get(i).getFrom(), b.getAllEdges().get(i).getFrom());
            assertEquals(a.getAllEdges().get(i).getWeight(), b.getAllEdges().get(i).getWeight());
        }
    }
}
