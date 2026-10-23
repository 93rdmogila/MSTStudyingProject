package benchmark;

import java.util.HashMap;
import java.util.Map;

/** Reference table from the assignment (section 7). Keys are "n/m". */
public final class ReferenceValues {
    public static final class Row {
        public final long mstWeight;
        public final int sccCount;
        public final long dagDistanceSum;

        Row(long mstWeight, int sccCount, long dagDistanceSum) {
            this.mstWeight = mstWeight;
            this.sccCount = sccCount;
            this.dagDistanceSum = dagDistanceSum;
        }
    }

    private static final Map<String, Row> TABLE = new HashMap<>();

    static {
        put(1000, 4000, 147970L, 27, 396274L);
        put(1000, 20000, 30363L, 1, 252411L);
        put(3162, 12648, 468911L, 137, 1271911L);
        put(3162, 63240, 97589L, 1, 723157L);
        put(10000, 40000, 1486139L, 373, 3941828L);
        put(10000, 200000, 309550L, 1, 2381342L);
        put(31623, 126492, 4715515L, 1188, 12551891L);
        put(31623, 632460, 968517L, 1, 7376612L);
        put(100000, 400000, 15012094L, 3923, 40096539L);
        put(100000, 2000000, 3056681L, 1, 23512087L);
    }

    private ReferenceValues() { }

    private static void put(int n, int m, long mst, int scc, long dag) {
        TABLE.put(n + "/" + m, new Row(mst, scc, dag));
    }

    /** The reference row for (n, m), or null if the assignment gives none. */
    public static Row get(int n, int m) {
        return TABLE.get(n + "/" + m);
    }
}
