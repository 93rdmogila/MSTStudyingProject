package algorithms;

import graph.Edge;
import metrics.Metrics;

/**
 * Bottom-up (iterative) stable merge sort of edges by weight, written by hand so that every weight
 * comparison can be counted. O(E log E) comparisons, O(E) extra space.
 */
public final class EdgeSort {
    private EdgeSort() { }

    public static void sort(Edge[] edges, Metrics metrics) {
        int n = edges.length;
        Edge[] src = edges;
        Edge[] dst = new Edge[n];
        for (int width = 1; width < n; width *= 2) {
            for (int lo = 0; lo < n; lo += 2 * width) {
                int mid = Math.min(lo + width, n);
                int hi = Math.min(lo + 2 * width, n);
                int i = lo, j = mid, k = lo;
                while (i < mid && j < hi) {
                    metrics.comparisons++;
                    if (src[j].getWeight() < src[i].getWeight()) {
                        dst[k++] = src[j++];
                    } else {
                        dst[k++] = src[i++];
                    }
                }
                while (i < mid) dst[k++] = src[i++];
                while (j < hi) dst[k++] = src[j++];
            }
            Edge[] tmp = src; src = dst; dst = tmp;
        }
        if (src != edges) {
            System.arraycopy(src, 0, edges, 0, n);
        }
    }
}
