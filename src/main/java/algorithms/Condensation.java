package algorithms;

import graph.Edge;
import graph.Graph;

/**
 * Condensation graph (bonus B): every SCC becomes one vertex, an edge u -> v of the original graph
 * between two DIFFERENT components becomes an edge between their component vertices (weight kept,
 * parallel edges kept). The result is always a DAG: a cycle between components would merge them.
 */
public final class Condensation {
    private Condensation() { }

    public static Graph build(Graph graph, SccResult scc) {
        Graph dag = new Graph(scc.getCount(), true);
        int[] comp = scc.getComponentOf();
        for (Edge e : graph.getAllEdges()) {
            int a = comp[e.getFrom()];
            int b = comp[e.getTo()];
            if (a != b) {
                dag.addEdge(a, b, e.getWeight());
            }
        }
        return dag;
    }
}
