package benchmark;

import algorithms.*;
import graph.Graph;
import metrics.Metrics;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;

/**
 * Self-checking benchmark. For each of the 10 graph configurations (5 sizes x sparse/dense) it generates
 * an undirected graph (Kruskal, Prim), a directed graph (Kosaraju) and a DAG (Kahn, DAG shortest paths),
 * runs every algorithm 5 times (median time goes to the CSV; the counters are deterministic) and CHECKS:
 *
 *   - Kruskal and Prim give the same total weight and the same number of components;
 *   - the topological order is valid (pos[u] < pos[v] for every edge);
 *   - Kosaraju scans exactly 2m edges, Kahn and the DAG relaxation exactly m;
 *   - results equal the reference table of the assignment;
 *   - (bonus) Tarjan finds the same components as Kosaraju and scans exactly m edges.
 *
 * Any failed check aborts the run with exit code 1 BEFORE the CSV is written.
 *
 * Usage: BenchmarkRunner [--out results/results.csv] [--runs 5] [--max-n 100000] [--no-bonus]
 */
public class BenchmarkRunner {
    static final int[] SIZES = {1000, 3162, 10000, 31623, 100000};
    static final int[] DENSITIES = {4, 20};
    static final String[] DENSITY_NAMES = {"sparse", "dense"};

    private static final class Row {
        final String algorithm, graph;
        final int n, m;
        final double timeMs;
        final Metrics metrics;
        final long result;

        Row(String algorithm, String graph, int n, int m, double timeMs, Metrics metrics, long result) {
            this.algorithm = algorithm; this.graph = graph; this.n = n; this.m = m;
            this.timeMs = timeMs; this.metrics = metrics; this.result = result;
        }
    }

    /** Thrown when a self-check fails. */
    static final class CheckFailed extends RuntimeException {
        private static final long serialVersionUID = 1L;
        CheckFailed(String message) { super(message); }
    }

    private final int runs;
    private final boolean bonus;
    private final List<Row> rows = new ArrayList<>();
    private final List<Row> bonusRows = new ArrayList<>();

    BenchmarkRunner(int runs, boolean bonus) {
        this.runs = runs;
        this.bonus = bonus;
    }

    public static void main(String[] args) throws IOException {
        String out = "results/results.csv";
        int runs = 5;
        int maxN = Integer.MAX_VALUE;
        boolean bonus = true;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--out": out = args[++i]; break;
                case "--runs": runs = Integer.parseInt(args[++i]); break;
                case "--max-n": maxN = Integer.parseInt(args[++i]); break;
                case "--no-bonus": bonus = false; break;
                default: throw new IllegalArgumentException("Unknown option: " + args[i]);
            }
        }
        BenchmarkRunner runner = new BenchmarkRunner(runs, bonus);
        try {
            runner.runAll(maxN);
        } catch (CheckFailed e) {
            System.err.println();
            System.err.println("BENCHMARK FAILED: " + e.getMessage());
            System.err.println("results.csv was NOT written.");
            System.exit(1);
        }
        Path csv = Paths.get(out);
        runner.writeCsv(csv, runner.rows);
        System.out.println("\nAll checks passed. " + runner.rows.size() + " rows written to " + csv);
        if (bonus) {
            Path bonusCsv = csv.resolveSibling("results_bonus.csv");
            runner.writeCsv(bonusCsv, runner.bonusRows);
            System.out.println(runner.bonusRows.size() + " bonus rows written to " + bonusCsv);
        }
    }

    void runAll(int maxN) {
        System.out.printf("%-14s %-12s %-12s %-12s %-12s %s%n",
                "graph", "n/m", "MST weight", "SCC count", "DAG dist sum", "reference");
        for (int n : SIZES) {
            if (n > maxN) continue;
            for (int d = 0; d < DENSITIES.length; d++) {
                runConfig(n, DENSITIES[d] * n, "n" + n + "_" + DENSITY_NAMES[d]);
            }
        }
    }

    private void runConfig(int n, int m, String name) {
        ReferenceValues.Row ref = ReferenceValues.get(n, m);

        // ---- undirected: Kruskal and Prim
        Graph undirected = GraphGenerator.undirected(n, m);
        MSTResult kruskal = timed(() -> KruskalMST.findMST(undirected), name, "Kruskal", n, m, r -> r.getTotalWeight(), r -> r.getMetrics());
        MSTResult prim = timed(() -> PrimMST.findMST(undirected), name, "Prim", n, m, r -> r.getTotalWeight(), r -> r.getMetrics());
        check(kruskal.getTotalWeight() == prim.getTotalWeight(),
                name + ": Kruskal weight " + kruskal.getTotalWeight() + " != Prim weight " + prim.getTotalWeight());
        check(kruskal.getComponents() == prim.getComponents(), name + ": Kruskal and Prim disagree on component count");
        check(kruskal.getEdges().size() == n - kruskal.getComponents() && prim.getEdges().size() == n - prim.getComponents(),
                name + ": MST does not have n - components edges");

        // ---- directed: Kosaraju
        Graph directed = GraphGenerator.directed(n, m);
        SccResult kosaraju = timed(() -> KosarajuSCC.findSCC(directed), name, "Kosaraju", n, m, r -> r.getCount(), r -> r.getMetrics());
        check(kosaraju.getMetrics().edgeScans == 2L * m, name + ": Kosaraju edgeScans " + kosaraju.getMetrics().edgeScans + " != 2m");
        if (bonus) {
            runBonusScc(directed, kosaraju, name, n, m);
        }

        // ---- DAG: Kahn, then shortest paths over Kahn's order
        Graph dag = GraphGenerator.dag(n, m);
        TopoResult kahn = timed(() -> KahnTopologicalSort.sort(dag), name, "Kahn", n, m, r -> r.getOrder().length, r -> r.getMetrics());
        check(!kahn.hasCycle(), name + ": generated DAG has a cycle");
        check(KahnTopologicalSort.isValidOrder(dag, kahn.getOrder()), name + ": Kahn returned an invalid topological order");
        check(kahn.getMetrics().edgeScans == m, name + ": Kahn edgeScans " + kahn.getMetrics().edgeScans + " != m");
        DagShortestPath sp = timed(() -> DagShortestPath.compute(dag, kahn.getOrder()), name, "DagShortestPath", n, m,
                r -> r.sumOfDistances(), r -> r.getMetrics());
        check(sp.getMetrics().edgeScans == m, name + ": DagShortestPath edgeScans " + sp.getMetrics().edgeScans + " != m");
        if (bonus) {
            runBonusCriticalPath(directed, kosaraju, name, n, m);
        }

        // ---- reference table
        String status = "(no reference)";
        if (ref != null) {
            check(kruskal.getTotalWeight() == ref.mstWeight, name + ": MST weight " + kruskal.getTotalWeight() + " != reference " + ref.mstWeight);
            check(kosaraju.getCount() == ref.sccCount, name + ": SCC count " + kosaraju.getCount() + " != reference " + ref.sccCount);
            check(sp.sumOfDistances() == ref.dagDistanceSum, name + ": DAG distance sum " + sp.sumOfDistances() + " != reference " + ref.dagDistanceSum);
            status = "OK";
        }
        System.out.printf("%-14s %-12s %-12d %-12d %-12d %s%n", name, n + "/" + m,
                kruskal.getTotalWeight(), kosaraju.getCount(), sp.sumOfDistances(), status);
    }

    // ---- bonus: Tarjan (A) and condensation + critical path (B) --------------------------------

    private void runBonusScc(Graph directed, SccResult kosaraju, String name, int n, int m) {
        // lastTime still holds the median time of the Kosaraju measurement just taken
        bonusRows.add(new Row("Kosaraju", name, n, m, lastTime, kosaraju.getMetrics(), kosaraju.getCount()));
        SccResult tarjan = timedBonus(() -> TarjanSCC.findSCC(directed), name, "Tarjan", n, m, r -> r.getCount(), r -> r.getMetrics());
        check(tarjan.samePartitionAs(kosaraju), name + ": Tarjan and Kosaraju found different components");
        check(tarjan.getMetrics().edgeScans == m, name + ": Tarjan edgeScans " + tarjan.getMetrics().edgeScans + " != m");
    }

    private void runBonusCriticalPath(Graph directed, SccResult scc, String name, int n, int m) {
        Graph condensation = Condensation.build(directed, scc);
        check(!KahnTopologicalSort.sort(condensation).hasCycle(), name + ": condensation is not acyclic");
        CriticalPath cp = timedBonus(() -> CriticalPath.compute(condensation), name, "CriticalPath", n, m, r -> r.getLength(), r -> new Metrics());
        check(cp.getLength() >= 0, name + ": negative critical path");
    }

    // ---- timing helper ----------------------------------------------------------------------------

    private double lastTime;

    private <R> R timed(Supplier<R> task, String graph, String algorithm, int n, int m,
                        java.util.function.ToLongFunction<R> result, java.util.function.Function<R, Metrics> metrics) {
        return measure(task, graph, algorithm, n, m, result, metrics, rows);
    }

    private <R> R timedBonus(Supplier<R> task, String graph, String algorithm, int n, int m,
                             java.util.function.ToLongFunction<R> result, java.util.function.Function<R, Metrics> metrics) {
        return measure(task, graph, algorithm, n, m, result, metrics, bonusRows);
    }

    private <R> R measure(Supplier<R> task, String graph, String algorithm, int n, int m,
                          java.util.function.ToLongFunction<R> result, java.util.function.Function<R, Metrics> metrics, List<Row> target) {
        double[] times = new double[runs];
        R last = null;
        for (int i = 0; i < runs; i++) {
            long t0 = System.nanoTime();
            last = task.get();
            times[i] = (System.nanoTime() - t0) / 1e6;
        }
        Arrays.sort(times);
        lastTime = times[runs / 2];
        target.add(new Row(algorithm, graph, n, m, lastTime, metrics.apply(last), result.applyAsLong(last)));
        return last;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new CheckFailed(message);
        }
    }

    // ---- CSV --------------------------------------------------------------------------------------

    private void writeCsv(Path path, List<Row> data) throws IOException {
        if (path.getParent() != null) {
            Files.createDirectories(path.getParent());
        }
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(path))) {
            pw.println("algorithm,graph,n,m,time_ms,dsu_ops,edge_scans,comparisons,max_depth,result");
            for (Row r : data) {
                pw.println(String.format(Locale.ROOT, "%s,%s,%d,%d,%.3f,%d,%d,%d,%d,%d",
                        r.algorithm, r.graph, r.n, r.m, r.timeMs,
                        r.metrics.dsuOps, r.metrics.edgeScans, r.metrics.comparisons, r.metrics.maxDepth, r.result));
            }
        }
    }
}
