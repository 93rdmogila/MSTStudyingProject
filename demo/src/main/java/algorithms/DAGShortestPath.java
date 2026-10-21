package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.Arrays;
import java.util.List;

public class DAGShortestPath {

    public static class ShortestPathResult {
        private final int[] distances;
        private final int[] parent;

        public ShortestPathResult(int[] distances, int[] parent) {
            this.distances = distances;
            this.parent = parent;
        }

        public int[] getDistances() { return distances; }
        public int[] getParent() { return parent; }
    }

    public static ShortestPathResult findShortestPaths(Graph graph, int source) {
        int vertexNumber = graph.getVertexNumber();
        List<Edge>[] adj = graph.getAdjList();

        KahnTopologicalSort.KahnResult topoResult = KahnTopologicalSort.sort(graph);
        if (topoResult.isHasCycle()) {
            throw new IllegalArgumentException("Graph contains a cycle! Shortest path in DAG requires an acyclic graph.");
        }

        List<Integer> topoOrder = topoResult.getSortedOrder();

        int[] dist = new int[vertexNumber];
        int[] parent = new int[vertexNumber];
        Arrays.fill(dist, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);

        dist[source] = 0;
        for (int u : topoOrder) {
            if (dist[u] == Integer.MAX_VALUE) {
                continue;
            }

            for (Edge edge : adj[u]) {
                int v = edge.getTo();
                int weight = edge.getWeight();

                if (dist[u] + weight < dist[v]) {
                    dist[v] = dist[u] + weight;
                    parent[v] = u;
                }
            }
        }

        return new ShortestPathResult(dist, parent);
    }
}