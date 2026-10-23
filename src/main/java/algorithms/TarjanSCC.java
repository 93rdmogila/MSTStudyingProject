package algorithms;

import graph.Edge;
import graph.Graph;
import metrics.Metrics;

import java.util.List;

/**
 * Tarjan's SCC algorithm (bonus A): ONE DFS pass with index[], lowlink[] and a stack of "active"
 * vertices, iterative. A vertex v with lowlink[v] == index[v] is the root of a component; everything
 * above it on the active stack belongs to that component.
 *
 * edgeScans = E (one pass), versus 2E for Kosaraju - the saving is that Tarjan needs no second
 * traversal and no reversed graph.
 */
public final class TarjanSCC {
    private TarjanSCC() { }

    public static SccResult findSCC(Graph graph) {
        int n = graph.getVertexNumber();
        List<Edge>[] adj = graph.getAdjList();
        Metrics metrics = new Metrics();

        int[] index = new int[n];
        int[] lowlink = new int[n];
        int[] pointer = new int[n];
        boolean[] onStack = new boolean[n];
        java.util.Arrays.fill(index, -1);

        int[] callStack = new int[n];   // DFS stack (explicit)
        int[] active = new int[n];      // Tarjan's stack of vertices not yet assigned to a component
        int activeTop = 0;
        int[] componentOf = new int[n];
        int nextIndex = 0;
        int count = 0;

        for (int s = 0; s < n; s++) {
            if (index[s] != -1) continue;
            int sp = 0;
            callStack[sp++] = s;
            index[s] = lowlink[s] = nextIndex++;
            active[activeTop++] = s;
            onStack[s] = true;
            metrics.noteDepth(sp);

            while (sp > 0) {
                int u = callStack[sp - 1];
                List<Edge> out = adj[u];
                if (pointer[u] < out.size()) {
                    int v = out.get(pointer[u]++).getTo();
                    metrics.edgeScans++;
                    if (index[v] == -1) {
                        index[v] = lowlink[v] = nextIndex++;
                        active[activeTop++] = v;
                        onStack[v] = true;
                        callStack[sp++] = v;
                        metrics.noteDepth(sp);
                    } else if (onStack[v]) {
                        lowlink[u] = Math.min(lowlink[u], index[v]);
                    }
                } else {
                    sp--;
                    if (lowlink[u] == index[u]) {
                        int w;
                        do {
                            w = active[--activeTop];
                            onStack[w] = false;
                            componentOf[w] = count;
                        } while (w != u);
                        count++;
                    }
                    if (sp > 0) {
                        int parent = callStack[sp - 1];
                        lowlink[parent] = Math.min(lowlink[parent], lowlink[u]);
                    }
                }
            }
        }
        return new SccResult(componentOf, count, metrics);
    }
}
