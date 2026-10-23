package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.List;

/**
 * Kosaraju's algorithm, fully iterative (own int[] stack, so 100 000 vertices cannot overflow the JVM stack).
 *
 *   pass 1: DFS on G, record vertices in order of finishing;
 *   reverse the graph;
 *   pass 2: DFS on G^R starting from vertices in DECREASING finish order - each DFS tree is one SCC.
 *
 * Time O(V + E), extra space O(V + E) (the reversed graph).
 * edgeScans = 2E exactly (every edge is looked at once per pass; building the reversed graph is not
 * counted); maxDepth = deepest explicit stack in either pass.
 */
public final class KosarajuSCC {
    private KosarajuSCC() { }

    public static SccResult findSCC(Graph graph) {
        int n = graph.getVertexNumber();
        List<Edge>[] adj = graph.getAdjList();
        Metrics metrics = new Metrics();

        int[] stack = new int[n];      // each vertex is pushed at most once per pass
        int[] pointer = new int[n];    // next adjacency index to examine, per vertex
        boolean[] visited = new boolean[n];
        int[] finishOrder = new int[n];
        int finished = 0;

        // ---- pass 1: finishing order on G
        for (int s = 0; s < n; s++) {
            if (visited[s]) continue;
            int sp = 0;
            stack[sp++] = s;
            visited[s] = true;
            metrics.noteDepth(sp);
            while (sp > 0) {
                int u = stack[sp - 1];
                List<Edge> out = adj[u];
                if (pointer[u] < out.size()) {
                    int v = out.get(pointer[u]++).getTo();
                    metrics.edgeScans++;
                    if (!visited[v]) {
                        visited[v] = true;
                        stack[sp++] = v;
                        metrics.noteDepth(sp);
                    }
                } else {
                    sp--;
                    finishOrder[finished++] = u;
                }
            }
        }

        // ---- reverse the graph (CSR arrays: revStart[v] .. revStart[v+1] index into revTo)
        int[] revStart = new int[n + 1];
        for (int u = 0; u < n; u++) {
            for (Edge e : adj[u]) {
                revStart[e.getTo() + 1]++;
            }
        }
        for (int v = 0; v < n; v++) {
            revStart[v + 1] += revStart[v];
        }
        int[] revTo = new int[revStart[n]];
        int[] fill = new int[n];
        for (int u = 0; u < n; u++) {
            for (Edge e : adj[u]) {
                int v = e.getTo();
                revTo[revStart[v] + fill[v]++] = u;
            }
        }

        // ---- pass 2: DFS on G^R in decreasing finish order
        java.util.Arrays.fill(visited, false);
        for (int v = 0; v < n; v++) {
            pointer[v] = revStart[v];
        }
        int[] componentOf = new int[n];
        int count = 0;
        for (int k = n - 1; k >= 0; k--) {
            int s = finishOrder[k];
            if (visited[s]) continue;
            int sp = 0;
            stack[sp++] = s;
            visited[s] = true;
            componentOf[s] = count;
            metrics.noteDepth(sp);
            while (sp > 0) {
                int u = stack[sp - 1];
                if (pointer[u] < revStart[u + 1]) {
                    int v = revTo[pointer[u]++];
                    metrics.edgeScans++;
                    if (!visited[v]) {
                        visited[v] = true;
                        componentOf[v] = count;
                        stack[sp++] = v;
                        metrics.noteDepth(sp);
                    }
                } else {
                    sp--;
                }
            }
            count++;
        }
        return new SccResult(componentOf, count, metrics);
    }
}
