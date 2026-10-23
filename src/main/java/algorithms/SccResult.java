package algorithms;

import metrics.Metrics;

import java.util.ArrayList;
import java.util.List;

/** Strongly connected components: componentOf[v] is the component id of v (0 .. count-1). */
public class SccResult {
    private final int[] componentOf;
    private final int count;
    private final Metrics metrics;

    public SccResult(int[] componentOf, int count, Metrics metrics) {
        this.componentOf = componentOf;
        this.count = count;
        this.metrics = metrics;
    }

    public int[] getComponentOf() { return componentOf; }
    public int getCount() { return count; }
    public Metrics getMetrics() { return metrics; }

    /** The components as vertex lists (built on demand). */
    public List<List<Integer>> getComponents() {
        List<List<Integer>> result = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            result.add(new ArrayList<>());
        }
        for (int v = 0; v < componentOf.length; v++) {
            result.get(componentOf[v]).add(v);
        }
        return result;
    }

    /** True if both results split the vertices into the same groups (component ids may differ). */
    public boolean samePartitionAs(SccResult other) {
        if (count != other.count || componentOf.length != other.componentOf.length) {
            return false;
        }
        int[] map = new int[count];
        java.util.Arrays.fill(map, -1);
        for (int v = 0; v < componentOf.length; v++) {
            int a = componentOf[v];
            int b = other.componentOf[v];
            if (map[a] == -1) {
                map[a] = b;
            } else if (map[a] != b) {
                return false;
            }
        }
        // map must be injective too (guaranteed by equal counts and a consistent function that is onto)
        boolean[] used = new boolean[count];
        for (int a = 0; a < count; a++) {
            if (map[a] < 0 || used[map[a]]) {
                return false;
            }
            used[map[a]] = true;
        }
        return true;
    }
}
