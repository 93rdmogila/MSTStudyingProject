package algorithms;

import graph.Edge;
import graph.Graph;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class PrimMST {

    public static KruskalMST.MSTResult findMST(Graph graph) {
        int vertexNumber = graph.getVertexNumber();
        List<Edge>[] adjList = graph.getAdjList();
        
        boolean[] visited = new boolean[vertexNumber];
        List<Edge> mstEdges = new ArrayList<>();
        long totalWeight = 0;
        long comparisons = 0;
		
        PriorityQueue<Edge> pq = new PriorityQueue<>(Comparator.comparingInt(Edge::getWeight));

        for (int startVertex = 0; startVertex < vertexNumber; startVertex++) {
            if (!visited[startVertex]) {
                visit(startVertex, adjList, visited, pq);

                while (!pq.isEmpty()) {
                    comparisons++;
                    Edge edge = pq.poll();
                    int to = edge.getTo();

                    if (visited[to]) continue;

                    visited[to] = true;
                    mstEdges.add(edge);
                    totalWeight += edge.getWeight();

                    visit(to, adjList, visited, pq);
                }
            }
        }

        return new KruskalMST.MSTResult(mstEdges, totalWeight, comparisons);
    }

    private static void visit(int from, List<Edge>[] adjList, boolean[] visited, PriorityQueue<Edge> pq) {
        visited[from] = true;
        for (Edge edge : adjList[from]) {
            if (!visited[edge.getTo()]) {
                pq.add(edge);
            }
        }
    }
}