package algorithms;

import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

class DsuTest {

    /** Naive reference: label[] with O(n) relabelling on every union. */
    private static final class Naive {
        final int[] label;
        Naive(int n) { label = new int[n]; for (int i = 0; i < n; i++) label[i] = i; }
        boolean union(int a, int b) {
            if (label[a] == label[b]) return false;
            int from = label[b], to = label[a];
            for (int i = 0; i < label.length; i++) if (label[i] == from) label[i] = to;
            return true;
        }
        int sets() { return (int) java.util.Arrays.stream(label).distinct().count(); }
    }

    @Test
    void agreesWithReferenceAfterRandomUnions() {
        Random rnd = new Random(1);
        for (int trial = 0; trial < 50; trial++) {
            int n = 1 + rnd.nextInt(200);
            DSU dsu = new DSU(n);
            Naive ref = new Naive(n);
            int ops = rnd.nextInt(3 * n);
            for (int i = 0; i < ops; i++) {
                int a = rnd.nextInt(n), b = rnd.nextInt(n);
                assertEquals(ref.union(a, b), dsu.union(a, b));
            }
            for (int a = 0; a < n; a++) {
                for (int b = 0; b < n; b++) {
                    assertEquals(ref.label[a] == ref.label[b], dsu.find(a) == dsu.find(b),
                            "find disagrees with reference for " + a + "," + b);
                }
            }
            assertEquals(ref.sets(), dsu.components());
        }
    }

    @Test
    void countsFindCallsAndSuccessfulUnionsOnly() {
        DSU dsu = new DSU(4);
        assertTrue(dsu.union(0, 1));   // 2 finds + 1 successful union
        assertFalse(dsu.union(1, 0));  // 2 finds, no union
        dsu.find(2);                   // 1 find
        assertEquals(5, dsu.getFindCalls());
        assertEquals(1, dsu.getUnionCalls());
        assertEquals(6, dsu.getOps());
        assertEquals(3, dsu.components());
    }

    @Test
    void makeSetResets() {
        DSU dsu = new DSU(3);
        dsu.union(0, 1);
        dsu.makeSet(5);
        assertEquals(5, dsu.components());
        assertEquals(0, dsu.getOps());
        assertNotEquals(dsu.find(0), dsu.find(1));
    }

    @Test
    void longChainStaysFastAndDoesNotOverflowStack() {
        int n = 200_000;
        DSU dsu = new DSU(n);
        for (int i = 1; i < n; i++) dsu.union(i - 1, i);
        assertEquals(1, dsu.components());
        assertEquals(dsu.find(0), dsu.find(n - 1));
    }

    @Test
    void emptyDsu() {
        assertEquals(0, new DSU(0).components());
    }
}
