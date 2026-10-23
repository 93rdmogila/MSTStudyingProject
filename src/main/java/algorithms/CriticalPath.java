package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.List;

/**
 * Longest ("critical") path in a DAG = shortest path with the sign of every weight flipped, found by
 * the same one-pass relaxation in topological order (bonus B).
 *
 * Easy in a DAG because there are no cycles: the topological order guarantees a vertex is final when
 * it is processed. In a general graph the longest SIMPLE path is NP-hard (it contains Hamiltonian
 * path), and with a positive cycle "longest" is not even defined (the walk can grow forever).
 * In scheduling the critical path is the chain of dependent jobs that determines the minimum total
 * project time (CPM / PERT): delaying any job on it delays everything.
 */
public final class CriticalPath {
    private final long length;
    private final List<Integer> path;

    private CriticalPath(long length, List<Integer> path) {
        this.length = length;
        this.path = path;
    }

    public long getLength() { return length; }
    /** Vertices of the DAG on a longest path, from start to end. */
    public List<Integer> getPath() { return path; }

    public static CriticalPath compute(Graph dag) {
        Graph negated = new Graph(dag.getVertexNumber(), true);
        for (Edge e : dag.getAllEdges()) {
            negated.addEdge(e.getFrom(), e.getTo(), -e.getWeight());
        }
        DagShortestPath sp = DagShortestPath.compute(negated);
        int best = -1;
        for (int v = 0; v < dag.getVertexNumber(); v++) {
            if (best == -1 || sp.distanceTo(v) < sp.distanceTo(best)) {
                best = v;
            }
        }
        if (best == -1) {
            return new CriticalPath(0, java.util.Collections.emptyList());
        }
        return new CriticalPath(-sp.distanceTo(best), sp.pathTo(best));
    }
}
