package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.*;

public class KosarajuSCC {

    public static class SCCResult {
        private final List<List<Integer>> components;
        private final long maxDepth;

        public SCCResult(List<List<Integer>> components, long maxDepth) {
            this.components = components;
            this.maxDepth = maxDepth;
        }

        public List<List<Integer>> getComponents() { return components; }
        public long getMaxDepth() { return maxDepth; }
    }

    public static SCCResult findSCC(Graph graph) {
        int n = graph.getVertexNumber();
        List<Edge>[] adj = graph.getAdjList();
        
        boolean[] visited = new boolean[n];
        Deque<Integer> finishOrder = new ArrayDeque<>();
        long maxDepth = 0;

        for (int i = 0; i < n; i++) {
            if (!visited[i]) {
                maxDepth = Math.max(maxDepth, iterativeDfsFill(i, adj, visited, finishOrder));
            }
        }

        List<Edge>[] transposedAdj = getTransposedAdjList(graph);

        Arrays.fill(visited, false);
        List<List<Integer>> sccComponents = new ArrayList<>();

        while (!finishOrder.isEmpty()) {
            int v = finishOrder.pop();
            if (!visited[v]) {
                List<Integer> component = new ArrayList<>();
                iterativeDfsCollect(v, transposedAdj, visited, component);
                sccComponents.add(component);
            }
        }

        return new SCCResult(sccComponents, maxDepth);
    }

    private static long iterativeDfsFill(int start, List<Edge>[] adj, boolean[] visited, Deque<Integer> finishOrder) {
        Deque<int[]> stack = new ArrayDeque<>();
        stack.push(new int[]{start, 0});
        visited[start] = true;
        long currentMaxDepth = 1;

        while (!stack.isEmpty()) {
            int[] current = stack.peek();
            int u = current[0];
            int edgeIndex = current[1];

            currentMaxDepth = Math.max(currentMaxDepth, stack.size());

            if (edgeIndex < adj[u].size()) {
                current[1]++;
                int v = adj[u].get(edgeIndex).getTo();
                if (!visited[v]) {
                    visited[v] = true;
                    stack.push(new int[]{v, 0});
                }
            } else {
                stack.pop();
                finishOrder.push(u);
            }
        }
        return currentMaxDepth;
    }

    private static void iterativeDfsCollect(int start, List<Edge>[] adj, boolean[] visited, List<Integer> component) {
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(start);
        visited[start] = true;

        while (!stack.isEmpty()) {
            int u = stack.pop();
            component.add(u);

            for (Edge edge : adj[u]) {
                int v = edge.getTo();
                if (!visited[v]) {
                    visited[v] = true;
                    stack.push(v);
                }
            }
        }
    }

    private static List<Edge>[] getTransposedAdjList(Graph graph) {
        int n = graph.getVertexNumber();
        List<Edge>[] transposed = new ArrayList[n];
        for (int i = 0; i < n; i++) {
            transposed[i] = new ArrayList<>();
        }
        for (List<Edge> edges : graph.getAdjList()) {
            for (Edge edge : edges) {
                transposed[edge.getTo()].add(new Edge(edge.getTo(), edge.getFrom(), edge.getWeight()));
            }
        }
        return transposed;
    }
}