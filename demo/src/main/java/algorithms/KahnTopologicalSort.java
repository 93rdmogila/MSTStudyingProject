package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.*;

public class KahnTopologicalSort {

    public static class KahnResult {
        private final List<Integer> sortedOrder;
        private final boolean hasCycle;

        public KahnResult(List<Integer> sortedOrder, boolean hasCycle) {
            this.sortedOrder = sortedOrder;
            this.hasCycle = hasCycle;
        }

        public List<Integer> getSortedOrder() { return sortedOrder; }
        public boolean isHasCycle() { return hasCycle; }
    }

    public static KahnResult sort(Graph graph) {
        int n = graph.getVertexNumber();
        List<Edge>[] adj = graph.getAdjList();
        
        int[] inDegree = new int[n];
        for (int u = 0; u < n; u++) {
            for (Edge edge : adj[u]) {
                inDegree[edge.getTo()]++;
            }
        }

        Queue<Integer> queue = new LinkedList<>();
        for (int i = 0; i < n; i++) {
            if (inDegree[i] == 0) {
                queue.add(i);
            }
        }

        List<Integer> sortedOrder = new ArrayList<>();
        while (!queue.isEmpty()) {
            int u = queue.poll();
            sortedOrder.add(u);

            for (Edge edge : adj[u]) {
                int v = edge.getTo();
                inDegree[v]--;
                if (inDegree[v] == 0) {
                    queue.add(v);
                }
            }
        }

        boolean hasCycle = (sortedOrder.size() != n);
        return new KahnResult(sortedOrder, hasCycle);
    }
}