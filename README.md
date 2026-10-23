# DAA Assignment 3 - Graphs: DSU, MST, SCC and Shortest Paths

City Network & Job Scheduler: one graph class (adjacency list, directed or undirected) and six hand-written
algorithms - **DSU**, **Kruskal**, **Prim**, **Kosaraju** (+ bonus **Tarjan**), **Kahn** and **shortest paths in a DAG**
(+ bonus **condensation / critical path**) - with operation counters measured *inside* the algorithms.

## Requirements

* JDK 17 or newer, Maven 3.8+
* Python 3 with `pandas`, `numpy`, `matplotlib` (only for the plots / analysis)
* About 3 GB of free RAM for the largest benchmark graph (n = 100 000, m = 2 000 000);
  `.mvn/jvm.config` sets `-Xmx3g` for Maven.

## Build and test (one command)

```bash
mvn clean test
```

JUnit 5 tests cover: DSU vs a naive reference, Kruskal = Prim on 100 random connected graphs (n-1 edges),
SCC vs brute force (BFS from every vertex, n <= 200), topological order validity and cycle detection,
DAG shortest paths vs Bellman-Ford with negative weights, stack safety on a 100 000-vertex path,
the reference values of the assignment for n = 1 000 and 3 162, and the bonus algorithms.

## Run the benchmark

```bash
mvn -q compile exec:java                       # all 10 graphs, median of 5 runs -> results/results.csv
mvn -q compile exec:java -Dexec.args="--max-n 10000 --runs 3"   # quick version
python3 scripts/analyze.py                     # plots -> results/plots/, tables -> results/analysis.md
```

The benchmark **checks itself**: Kruskal weight == Prim weight, the topological order satisfies
`pos[u] < pos[v]` for every edge, Kosaraju scans exactly `2m` edges and Kahn / DAG relaxation exactly `m`,
every result equals the reference table of the assignment (n/m, MST weight, SCC count, sum of DAG distances),
and Tarjan finds the same components as Kosaraju. If any check fails the run prints
`BENCHMARK FAILED`, exits with code 1 and does **not** write the CSV.

Options: `--out <file>`, `--runs <k>`, `--max-n <n>`, `--no-bonus`. Same seed (`new Random(42)`), same numbers on every machine.

Run the algorithms on one input file:

```bash
mvn -q compile exec:java -Dexec.mainClass=benchmark.GraphDemo -Dexec.args="data/directed_cyclic.txt"
```

## Input format

```
n m directed|undirected
u v w        (m lines, vertices 0..n-1, integer weight)
```

Blank lines and lines starting with `#` are ignored. Files in `data/`:
`undirected_connected.txt`, `undirected_disconnected.txt` (isolated vertex, self-loop, parallel edge),
`directed_cyclic.txt` (four SCCs), `directed_dag.txt` (negative weights, two sources).

## Layout

```
src/main/java/graph/        Edge, Graph (one class, directed + undirected, file input)
src/main/java/algorithms/   DSU, EdgeSort, KruskalMST, PrimMST, KosarajuSCC, TarjanSCC, KahnTopologicalSort,
                            DagShortestPath, Condensation, CriticalPath, result classes
src/main/java/metrics/      Metrics (dsuOps, edgeScans, comparisons, maxDepth)
src/main/java/benchmark/    GraphGenerator (exact generator), ReferenceValues, BenchmarkRunner, GraphDemo
src/test/java/              JUnit 5 tests
data/                       small input graphs
results/                    results.csv (50 rows), results_bonus.csv, analysis.md, plots/
scripts/analyze.py          plots and counter-vs-formula tables
REPORT.md                   complexity table, counters vs formulas, correctness arguments, which algorithm when
```

## Counter definitions

| counter | meaning |
|---|---|
| `dsu_ops` | `find` calls + successful `union` calls (Kruskal only) |
| `edge_scans` | edges looked at: Kruskal = edge-list entries examined (stops after n-1 tree edges); Prim = adjacency entries; Kosaraju = 2m; Kahn and DAG paths = m |
| `comparisons` | weight comparisons: Kruskal = in the hand-written merge sort; Prim = every comparison inside the `PriorityQueue`; DAG paths = one per relaxation |
| `max_depth` | deepest explicit DFS stack (Kosaraju, Tarjan); 0 for algorithms without a DFS stack |

`time_ms` of `DagShortestPath` is the relaxation pass only (it reuses the order computed by Kahn, which has its own row).

## Git workflow

`main` holds working code only (tag `v1.0`); work was done on `feature/dsu`, `feature/mst`, `feature/scc`,
`feature/paths`, `feature/metrics` and merged into `main`.

## AI use acknowledgement

An AI assistant (Claude, Anthropic) was used on this project: to compare the first version of the code with the
assignment text, to rewrite and extend the implementation (counters, iterative DFS, generator, self-checking benchmark),
to draft the JUnit tests, the analysis script and the first version of this README and of `REPORT.md`.
Every number in the report comes from `results/results.csv`, produced by the benchmark described above. *(Student: adjust this paragraph so that it describes your own contribution accurately - you will be
asked to explain any part of the code at the defense.)*
