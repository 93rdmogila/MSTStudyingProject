package algorithms;

/**
 * Disjoint Set Union with BOTH heuristics: union by rank and path compression.
 * Amortised cost per operation is O(alpha(n)) - practically constant.
 *
 * Counters: {@link #getFindCalls()} counts every call of find(); {@link #getUnionCalls()} counts
 * only SUCCESSFUL unions (two different sets merged). {@link #getOps()} = find + union.
 * union() calls find() twice internally; those finds are counted too.
 */
public class DSU {
    private int[] parent;
    private int[] rank;
    private int components;
    private long findCalls;
    private long unionCalls;

    public DSU(int n) {
        makeSet(n);
    }

    /** (Re)initialises the structure with n singleton sets 0..n-1 and resets the counters. */
    public void makeSet(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("n must be non-negative: " + n);
        }
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
        }
        components = n;
        findCalls = 0;
        unionCalls = 0;
    }

    /** Root of x's set. Iterative (no recursion), two passes: find the root, then compress the path. */
    public int find(int x) {
        findCalls++;
        int root = x;
        while (parent[root] != root) {
            root = parent[root];
        }
        while (parent[x] != root) {
            int next = parent[x];
            parent[x] = root;
            x = next;
        }
        return root;
    }

    /** Merges the sets of x and y. Returns true if they were different (a real merge happened). */
    public boolean union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);
        if (rootX == rootY) {
            return false;
        }
        unionCalls++;
        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }
        components--;
        return true;
    }

    /** Current number of disjoint sets. */
    public int components() { return components; }

    public long getFindCalls() { return findCalls; }
    public long getUnionCalls() { return unionCalls; }
    public long getOps() { return findCalls + unionCalls; }
}
