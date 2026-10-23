#!/usr/bin/env python3
"""Reads results/results.csv, writes the three plots to results/plots/ and the
counter-vs-formula tables + log-log slopes to results/analysis.md.

Usage (from the project root):  python3 scripts/analyze.py
Needs: pandas, numpy, matplotlib.
"""
import math
import os

import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

CSV = "results/results.csv"
PLOTS = "results/plots"
OUT_MD = "results/analysis.md"
ALGOS = ["Kruskal", "Prim", "Kosaraju", "Kahn", "DagShortestPath"]
COLORS = {"Kruskal": "tab:blue", "Prim": "tab:orange", "Kosaraju": "tab:green",
          "Kahn": "tab:red", "DagShortestPath": "tab:purple"}

df = pd.read_csv(CSV)
df["density"] = np.where(df["m"] == 4 * df["n"], "sparse", "dense")
# the generator rounds n = 3162 / 31623, so compare m with 4n / 20n by ratio instead of equality
df["density"] = np.where(df["m"] / df["n"] < 10, "sparse", "dense")
df["ops"] = df["edge_scans"] + df["dsu_ops"] + df["comparisons"]
df["scans_per_edge"] = df["edge_scans"] / df["m"]

os.makedirs(PLOTS, exist_ok=True)


def series(algo, density):
    return df[(df.algorithm == algo) & (df.density == density)].sort_values("n")


def slope(x, y):
    return float(np.polyfit(np.log10(x), np.log10(y), 1)[0])


# ---- plot 1: operations vs n (log-log) --------------------------------------------------------
fig, ax = plt.subplots(figsize=(8, 5.5))
for algo in ALGOS:
    for density, style in (("sparse", "-"), ("dense", "--")):
        s = series(algo, density)
        # Kosaraju (2m scans) and DagShortestPath (m scans + m comparisons) have the same total, so
        # Kosaraju is drawn as a wide translucent line underneath to keep both visible.
        wide = algo == "Kosaraju"
        ax.plot(s.n, s.ops, style, marker="o", ms=4, color=COLORS[algo], label=f"{algo} ({density})",
                lw=7 if wide else 1.8, alpha=0.35 if wide else 1.0, zorder=1 if wide else 2)
ax.set_xscale("log"); ax.set_yscale("log")
ax.text(0.98, 0.04, "Kosaraju and DagShortestPath coincide (both 2m)", transform=ax.transAxes,
        ha="right", fontsize=8, style="italic")
ax.set_xlabel("n (vertices)"); ax.set_ylabel("operations = edgeScans + dsuOps + comparisons")
ax.set_title("Operations vs n (log-log); solid = sparse m=4n, dashed = dense m=20n")
ax.grid(True, which="both", alpha=0.3); ax.legend(fontsize=7, ncol=2)
fig.tight_layout(); fig.savefig(f"{PLOTS}/1_operations_vs_n.png", dpi=150); plt.close(fig)

# ---- plot 2: scans per edge --------------------------------------------------------------------
# Several algorithms have identical edgeScans / m at every n (Prim = Kosaraju = 2.0, Kahn = DagShortestPath = 1.0),
# so their lines lie exactly on top of each other. Prim and Kahn are therefore drawn as a wide translucent line
# underneath (like plot 1) so that every algorithm stays visible; the legend sits outside the axes.
UNDERLAY = {"Prim", "Kahn"}
MARKERS = {"sparse": "o", "dense": "x"}
fig, ax = plt.subplots(figsize=(9.5, 5.5))
for algo in ALGOS:
    for density, style in (("sparse", "-"), ("dense", "--")):
        s = series(algo, density)
        under = algo in UNDERLAY
        ax.plot(s.n, s.scans_per_edge, style, marker=MARKERS[density], ms=7 if under else 4, color=COLORS[algo],
                label=f"{algo} ({density})", lw=8 if under else 1.8, alpha=0.35 if under else 1.0,
                zorder=1 if under else 2)
ax.set_xscale("log"); ax.set_ylim(0, 2.25)
ax.axhline(2.0, color="gray", lw=0.6, ls=":"); ax.axhline(1.0, color="gray", lw=0.6, ls=":")
ax.text(1.05e3, 2.06, "Prim = Kosaraju = 2.0 (exactly 2m scans)", fontsize=8, style="italic")
ax.text(1.05e3, 1.06, "Kahn = DagShortestPath = 1.0 (exactly m scans)", fontsize=8, style="italic")
ax.set_xlabel("n (vertices)"); ax.set_ylabel("edgeScans / m")
ax.set_title("Scans per edge: a flat line = linear work (solid/o = sparse, dashed/x = dense)")
ax.grid(True, which="both", alpha=0.3)
ax.legend(fontsize=7.5, loc="center left", bbox_to_anchor=(1.01, 0.5))
fig.tight_layout(); fig.savefig(f"{PLOTS}/2_scans_per_edge.png", dpi=150); plt.close(fig)

# ---- plot 3: share of edges Kruskal examines ----------------------------------------------------
fig, ax = plt.subplots(figsize=(8, 5.5))
for density, color in (("sparse", "tab:red"), ("dense", "tab:blue")):
    s = series("Kruskal", density)
    ax.plot(s.n, s.scans_per_edge, marker="o", color=color, label=f"Kruskal, {density} (m = {'4n' if density == 'sparse' else '20n'})")
    for x, y in zip(s.n, s.scans_per_edge):
        ax.annotate(f"{y:.2f}", (x, y), textcoords="offset points", xytext=(0, 7), ha="center", fontsize=8)
ax.set_xscale("log"); ax.set_ylim(0, 1.1)
ax.set_xlabel("n (vertices)"); ax.set_ylabel("edges examined by Kruskal / m")
ax.set_title("Share of edges Kruskal examines before the tree is complete")
ax.grid(True, which="both", alpha=0.3); ax.legend()
fig.tight_layout(); fig.savefig(f"{PLOTS}/3_kruskal_edge_share.png", dpi=150); plt.close(fig)

# ---- tables -------------------------------------------------------------------------------------
lines = []
def table(title, header, rows):
    lines.append(f"### {title}\n")
    lines.append("| " + " | ".join(header) + " |")
    lines.append("|" + "|".join(["---"] * len(header)) + "|")
    for r in rows:
        lines.append("| " + " | ".join(r) + " |")
    lines.append("")

sizes = sorted(df.n.unique())
formulas = [
    ("Kruskal", "dsuOps / E", lambda r: r.dsu_ops / r.m),
    ("Kruskal", "dsuOps / edgeScans", lambda r: r.dsu_ops / r.edge_scans),
    ("Kruskal", "comparisons / (E log2 E)", lambda r: r.comparisons / (r.m * math.log2(r.m))),
    ("Prim", "comparisons / (E log2 V)", lambda r: r.comparisons / (r.m * math.log2(r.n))),
    ("Prim", "edgeScans / (2E)", lambda r: r.edge_scans / (2 * r.m)),
    ("Kosaraju", "edgeScans / (V + E)", lambda r: r.edge_scans / (r.n + r.m)),
    ("Kosaraju", "edgeScans / (2E)", lambda r: r.edge_scans / (2 * r.m)),
    ("Kahn", "edgeScans / (V + E)", lambda r: r.edge_scans / (r.n + r.m)),
    ("DagShortestPath", "edgeScans / (V + E)", lambda r: r.edge_scans / (r.n + r.m)),
]
for density in ("sparse", "dense"):
    rows = []
    for algo, label, f in formulas:
        s = series(algo, density)
        vals = [f(r) for r in s.itertuples()]
        rows.append([algo, label] + [f"{v:.3f}" for v in vals] + [f"{max(vals) / min(vals):.2f}"])
    table(f"Counter / formula, {density} graphs", ["algorithm", "ratio"] + [f"n={n}" for n in sizes] + ["max/min"], rows)

rows = []
for algo in ALGOS:
    cells = [algo]
    for density in ("sparse", "dense"):
        s = series(algo, density)
        cells.append(f"{slope(s.n, s.edge_scans):.3f}")
    for density in ("sparse", "dense"):
        s = series(algo, density)
        cells.append(f"{slope(s.n, s.ops):.3f}")
    rows.append(cells)
table("Log-log slope (fit of log10 counter vs log10 n)",
      ["algorithm", "edgeScans sparse", "edgeScans dense", "total ops sparse", "total ops dense"], rows)

rows = []
for density in ("sparse", "dense"):
    s = series("Kruskal", density)
    rows.append([density] + [f"{v:.3f}" for v in s.scans_per_edge])
table("Share of edges Kruskal examines (edgeScans / m)", ["density"] + [f"n={n}" for n in sizes], rows)

rows = []
for density in ("sparse", "dense"):
    k = series("Kruskal", density); p = series("Prim", density)
    rows.append([density, "Kruskal total ops"] + [f"{v:,}" for v in k.ops])
    rows.append([density, "Prim total ops"] + [f"{v:,}" for v in p.ops])
    rows.append([density, "Prim / Kruskal"] + [f"{a / b:.2f}" for a, b in zip(p.ops, k.ops)])
table("Kruskal vs Prim total operations", ["density", "row"] + [f"n={n}" for n in sizes], rows)

if os.path.exists("results/results_bonus.csv"):
    b = pd.read_csv("results/results_bonus.csv")
    rows = []
    for g in b.graph.unique():
        ko = b[(b.graph == g) & (b.algorithm == "Kosaraju")].iloc[0]
        ta = b[(b.graph == g) & (b.algorithm == "Tarjan")].iloc[0]
        cp = b[(b.graph == g) & (b.algorithm == "CriticalPath")].iloc[0]
        rows.append([g, str(ko.edge_scans), str(ta.edge_scans), f"{ta.edge_scans / ko.edge_scans:.2f}",
                     str(ko.result), str(cp.result)])
    table("Bonus: Kosaraju vs Tarjan edge scans, critical path of the condensation",
          ["graph", "Kosaraju scans", "Tarjan scans", "ratio", "SCC count", "critical path length"], rows)

with open(OUT_MD, "w") as f:
    f.write("# Analysis (generated by scripts/analyze.py from results/results.csv)\n\n")
    f.write("\n".join(lines))
print("\n".join(lines))
print(f"\nPlots written to {PLOTS}/, tables to {OUT_MD}")
