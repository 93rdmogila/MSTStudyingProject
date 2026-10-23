package benchmark;

import algorithms.*;
import graph.Graph;

import java.io.IOException;
import java.util.Arrays;

/**
 * Runs every applicable algorithm on one input file and prints the results with their counters.
 * Usage: GraphDemo data/directed_cyclic.txt
 */
public class GraphDemo {
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Usage: GraphDemo <graph file>");
            System.exit(2);
        }
        Graph g = Graph.fromFile(args[0]);
        System.out.println(args[0] + ": n=" + g.getVertexNumber() + " m=" + g.getEdgeNumber()
                + (g.isDirected() ? " directed" : " undirected"));
        if (!g.isDirected()) {
            MSTResult k = KruskalMST.findMST(g);
            MSTResult p = PrimMST.findMST(g);
            System.out.println("Kruskal: weight=" + k.getTotalWeight() + " components=" + k.getComponents() + " " + k.getMetrics());
            System.out.println("Prim:    weight=" + p.getTotalWeight() + " components=" + p.getComponents() + " " + p.getMetrics());
            if (k.getTotalWeight() != p.getTotalWeight()) {
                throw new IllegalStateException("Kruskal and Prim disagree!");
            }
            return;
        }
        SccResult kos = KosarajuSCC.findSCC(g);
        SccResult tar = TarjanSCC.findSCC(g);
        System.out.println("Kosaraju: " + kos.getCount() + " components " + kos.getComponents() + " " + kos.getMetrics());
        System.out.println("Tarjan:   " + tar.getCount() + " components, same partition: " + tar.samePartitionAs(kos) + " " + tar.getMetrics());
        TopoResult topo = KahnTopologicalSort.sort(g);
        if (topo.hasCycle()) {
            System.out.println("Kahn: CYCLE detected, only " + topo.getOrder().length + " of " + g.getVertexNumber()
                    + " vertices could be ordered " + Arrays.toString(topo.getOrder()));
            Graph dag = Condensation.build(g, kos);
            CriticalPath cp = CriticalPath.compute(dag);
            System.out.println("Condensation: " + dag.getVertexNumber() + " vertices, " + dag.getEdgeNumber()
                    + " edges; critical path length " + cp.getLength() + " via components " + cp.getPath());
        } else {
            System.out.println("Kahn: order " + Arrays.toString(topo.getOrder())
                    + " valid=" + KahnTopologicalSort.isValidOrder(g, topo.getOrder()) + " " + topo.getMetrics());
            DagShortestPath sp = DagShortestPath.compute(g, topo.getOrder());
            System.out.println("DagShortestPath: dist=" + Arrays.toString(sp.getDistances()) + " " + sp.getMetrics());
            for (int v = 0; v < g.getVertexNumber(); v++) {
                System.out.println("  path to " + v + ": " + sp.pathTo(v) + " (length " + sp.distanceTo(v) + ")");
            }
        }
    }
}
