package graph;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class GraphTest {

    @Test
    void readsUndirectedFile() throws IOException {
        Graph g = Graph.fromFile("data/undirected_connected.txt");
        assertEquals(6, g.getVertexNumber());
        assertEquals(9, g.getEdgeNumber());
        assertFalse(g.isDirected());
        assertEquals(9, g.getAllEdges().size());
        int entries = 0;
        for (List<Edge> list : g.getAdjList()) entries += list.size();
        assertEquals(18, entries, "undirected edge appears in both adjacency lists");
    }

    @Test
    void readsDirectedFile() throws IOException {
        Graph g = Graph.fromFile("data/directed_cyclic.txt");
        assertTrue(g.isDirected());
        assertEquals(7, g.getVertexNumber());
        assertEquals(9, g.getEdgeNumber());
        int entries = 0;
        for (List<Edge> list : g.getAdjList()) entries += list.size();
        assertEquals(9, entries);
        assertEquals(2, g.getInDegree(3)); // 2->3 and 4->3
    }

    @Test
    void edgeCasesDoNotCrash() throws IOException {
        Graph g = Graph.fromFile("data/undirected_disconnected.txt");
        assertEquals(6, g.getVertexNumber());
        assertEquals(6, g.getEdgeNumber());
        assertTrue(g.getAdjList()[5].isEmpty(), "vertex 5 is isolated");
        // self-loop 2-2 is stored once, parallel edges 0-1 twice
        long loops = g.getAdjList()[2].stream().filter(e -> e.getTo() == 2).count();
        assertEquals(1, loops);
        assertEquals(2, g.getAdjList()[0].stream().filter(e -> e.getTo() == 1).count(), "parallel edges are kept");
    }

    @Test
    void emptyAndSingleVertexGraphs() {
        assertEquals(0, new Graph(0, false).getVertexNumber());
        Graph one = new Graph(1, true);
        one.addEdge(0, 0, 5);
        assertEquals(1, one.getEdgeNumber());
    }

    @Test
    void rejectsEdgeOutsideRange() {
        Graph g = new Graph(3, false);
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(0, 3, 1));
        assertThrows(IllegalArgumentException.class, () -> g.addEdge(-1, 0, 1));
    }

    @Test
    void rejectsMalformedFiles(@org.junit.jupiter.api.io.TempDir Path dir) throws IOException {
        Path badFlag = dir.resolve("bad_flag.txt");
        Files.writeString(badFlag, "2 1 sideways\n0 1 1\n");
        assertThrows(IllegalArgumentException.class, () -> Graph.fromFile(badFlag.toString()));

        Path tooFew = dir.resolve("too_few.txt");
        Files.writeString(tooFew, "3 2 directed\n0 1 1\n");
        assertThrows(IllegalArgumentException.class, () -> Graph.fromFile(tooFew.toString()));

        Path empty = dir.resolve("empty.txt");
        Files.writeString(empty, "");
        assertThrows(IllegalArgumentException.class, () -> Graph.fromFile(empty.toString()));
    }
}
