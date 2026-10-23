package graph;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * One graph class for both directed and undirected graphs (adjacency list).
 * Vertices are 0 .. n-1, weights are int.
 *
 * <ul>
 *   <li>{@link #getAdjList()} - outgoing edges of every vertex. In an undirected graph an edge
 *       {u,v} appears twice (u->v in adj[u], v->u in adj[v]); a self-loop appears once.</li>
 *   <li>{@link #getAllEdges()} - the edge list: every edge exactly once, as it was added.
 *       Kruskal sorts this list.</li>
 *   <li>{@link #getInDegree(int)} - maintained on insertion for directed graphs (used by Kahn).</li>
 * </ul>
 * Self-loops, parallel edges, isolated vertices and disconnected graphs are all allowed.
 */
public class Graph {
    private final int vertexNumber;
    private final boolean directed;
    private final List<Edge>[] adjList;
    private final List<Edge> allEdges;
    private final int[] inDegree;

    @SuppressWarnings("unchecked")
    public Graph(int vertexNumber, boolean directed) {
        if (vertexNumber < 0) {
            throw new IllegalArgumentException("Vertex count must be non-negative: " + vertexNumber);
        }
        this.vertexNumber = vertexNumber;
        this.directed = directed;
        this.adjList = new List[vertexNumber];
        for (int i = 0; i < vertexNumber; i++) {
            adjList[i] = new ArrayList<>();
        }
        this.allEdges = new ArrayList<>();
        this.inDegree = new int[vertexNumber];
    }

    public void addEdge(int from, int to, int weight) {
        if (from < 0 || from >= vertexNumber || to < 0 || to >= vertexNumber) {
            throw new IllegalArgumentException("Edge (" + from + ", " + to + ") is outside 0.." + (vertexNumber - 1));
        }
        Edge edge = new Edge(from, to, weight);
        adjList[from].add(edge);
        allEdges.add(edge);
        if (directed) {
            inDegree[to]++;
        } else if (from != to) {
            adjList[to].add(new Edge(to, from, weight));
        }
    }

    /**
     * Reads a graph. First line: {@code n m directed|undirected}, then m lines {@code u v w}.
     * Blank lines and lines starting with '#' are ignored. (The old 0/1 flag is still accepted.)
     */
    public static Graph fromFile(String filePath) throws IOException {
        try (BufferedReader reader = Files.newBufferedReader(Paths.get(filePath))) {
            String header = nextLine(reader);
            if (header == null) {
                throw new IllegalArgumentException("Invalid graph file: empty");
            }
            String[] parts = header.split("\\s+");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Header must be: n m directed|undirected, got: " + header);
            }
            int n = Integer.parseInt(parts[0]);
            int m = Integer.parseInt(parts[1]);
            boolean directed;
            switch (parts[2].toLowerCase()) {
                case "directed": case "1": directed = true; break;
                case "undirected": case "0": directed = false; break;
                default: throw new IllegalArgumentException("Third header field must be directed or undirected: " + parts[2]);
            }
            Graph graph = new Graph(n, directed);
            for (int i = 0; i < m; i++) {
                String line = nextLine(reader);
                if (line == null) {
                    throw new IllegalArgumentException("Expected " + m + " edges, but the file ended after " + i);
                }
                String[] p = line.split("\\s+");
                if (p.length < 3) {
                    throw new IllegalArgumentException("Edge " + (i + 1) + ": expected 'u v w', got: " + line);
                }
                graph.addEdge(Integer.parseInt(p[0]), Integer.parseInt(p[1]), Integer.parseInt(p[2]));
            }
            return graph;
        }
    }

    private static String nextLine(BufferedReader reader) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim();
            if (!line.isEmpty() && !line.startsWith("#")) {
                return line;
            }
        }
        return null;
    }

    public int getVertexNumber() { return vertexNumber; }
    public int getEdgeNumber() { return allEdges.size(); }
    public boolean isDirected() { return directed; }
    public List<Edge>[] getAdjList() { return adjList; }
    public List<Edge> getAllEdges() { return allEdges; }

    /** In-degree of v; only maintained for directed graphs (always 0 for undirected ones). */
    public int getInDegree(int v) { return inDegree[v]; }

    /** Copy of the in-degree array (directed graphs). */
    public int[] copyInDegrees() { return inDegree.clone(); }
}
