package graph;

/** Immutable weighted edge. For undirected graphs the adjacency lists hold one Edge per direction. */
public final class Edge {
    private final int from;
    private final int to;
    private final int weight;

    public Edge(int from, int to, int weight) {
        this.from = from;
        this.to = to;
        this.weight = weight;
    }

    public int getFrom() { return from; }
    public int getTo() { return to; }
    public int getWeight() { return weight; }

    @Override
    public String toString() {
        return from + " -> " + to + " (" + weight + ")";
    }
}
