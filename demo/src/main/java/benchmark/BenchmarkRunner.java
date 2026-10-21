package benchmark;

import algorithms.*;
import graph.Graph;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;

public class BenchmarkRunner {

    public static void main(String[] args) {
        String csvFile = "results.csv";
        
        // 10 конфигураций графов: [Vertices, Edges, isDirected (0 - неориентированный, 1 - ориентированный)]
        int[][] configs = {
            {100, 500, 0},
            {500, 2500, 0},
            {1000, 5000, 0},
            {2000, 10000, 0},
            {5000, 20000, 0},
            {100, 300, 1},
            {500, 1500, 1},
            {1000, 4000, 1},
            {2000, 8000, 1},
            {5000, 15000, 1}
        };

        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
            // Заголовок CSV файла
            pw.println("ConfigIndex,Vertices,Edges,IsDirected,Algorithm,TimeMs,MetricResult");

            for (int i = 0; i < configs.length; i++) {
                int n = configs[i][0];
                int m = configs[i][1];
                int directedFlag = configs[i][2];
                boolean isDirected = (directedFlag == 1);

                Graph graph = generateRandomGraph(n, m, isDirected, 42L + i);

                if (!isDirected) {
                    // Тестируем Крускала
                    long start = System.nanoTime();
                    KruskalMST.MSTResult kruskalRes = KruskalMST.findMST(graph);
                    long timeKruskal = (System.nanoTime() - start) / 1_000_000;
                    pw.printf("%d,%d,%d,%d,%s,%d,%d\n", i, n, m, directedFlag, "Kruskal", timeKruskal, kruskalRes.getTotalWeight());

                    // Тестируем Прима
                    start = System.nanoTime();
                    KruskalMST.MSTResult primRes = PrimMST.findMST(graph);
                    long timePrim = (System.nanoTime() - start) / 1_000_000;
                    pw.printf("%d,%d,%d,%d,%s,%d,%d\n", i, n, m, directedFlag, "Prim", timePrim, primRes.getTotalWeight());
                } else {
                    // Тестируем Косаражу (SCC)
                    long start = System.nanoTime();
                    KosarajuSCC.SCCResult sccRes = KosarajuSCC.findSCC(graph);
                    long timeKosaraju = (System.nanoTime() - start) / 1_000_000;
                    pw.printf("%d,%d,%d,%d,%s,%d,%d\n", i, n, m, directedFlag, "KosarajuSCC", timeKosaraju, sccRes.getComponents().size());

                    // Тестируем Кана (Топосортировка)
                    start = System.nanoTime();
                    KahnTopologicalSort.KahnResult kahnRes = KahnTopologicalSort.sort(graph);
                    long timeKahn = (System.nanoTime() - start) / 1_000_000;
                    pw.printf("%d,%d,%d,%d,%s,%d,%b\n", i, n, m, directedFlag, "KahnToposort", timeKahn, kahnRes.isHasCycle());

                    // Тестируем DAG Кратчайшие пути (если граф без циклов)
                    if (!kahnRes.isHasCycle()) {
                        start = System.nanoTime();
                        DAGShortestPath.ShortestPathResult dagRes = DAGShortestPath.findShortestPaths(graph, 0);
                        long timeDag = (System.nanoTime() - start) / 1_000_000;
                        pw.printf("%d,%d,%d,%d,%s,%d,%d\n", i, n, m, directedFlag, "DAGShortestPath", timeDag, dagRes.getDistances()[n - 1]);
                    }
                }
            }
            System.out.println("Benchmark completed successfully! Results saved to " + csvFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static Graph generateRandomGraph(int n, int m, boolean isDirected, long seed) {
        Graph graph = new Graph(n, isDirected);
        Random random = new Random(seed);
        int added = 0;
        int safetyCounter = 0;
        
        while (added < m && safetyCounter < m * 10) {
            safetyCounter++;
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            if (u == v) continue;
            
            // Для DAG гарантируем направленность u < v, чтобы не плодить тяжелые циклы
            if (isDirected && u >= v) {
                int temp = u;
                u = v;
                v = temp;
                if (u == v) continue;
            }

            int weight = random.nextInt(50) + 1;
            graph.addEdges(u, v, weight);
            added++;
        }
        return graph;
    }
}