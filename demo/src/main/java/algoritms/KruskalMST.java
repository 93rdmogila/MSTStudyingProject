package algorithms;

import kz.edu.astanait.daa.graph.Graph;
import kz.edu.astanait.daa.graph.Edge;

import java.util.ArrayList;
import java.util.List;

public class KruskalMST {
	public static class MSTResult{
		private final List<Edge> mstEdges;
		private final long totalWeight;
		private final long comparisons;

		public MSTResult(List<Edge> mstEdges, long totalWeight, long comparisons){
			this.mstEdges = mstEdges;
			this.totalWeight = totalWeight;
			this.comparisons = comparisons;
		}

		public List<Edge> getMstEdges(){
			return mstEdges;
		}
		public long getTotalWeight(){
			return totalWeight;
		}
		public long getComparisons(){
			return comparisons;
		}
	}
	public static MSTResult findMST(Graph graph){
		if(graph.isDirected() == true){
			throw new IllegalArgumentException("MST doesn't exist for this graph");
		}
		int vertexNumber = graph.getVertexNumber();
		List<Edge> allEdges = new ArrayList<>(graph.getAllEdges());

		long[] comparisons = {0};
		allEdges.sort((element1, element2) -> {
			comparisons[0]++;
			return Integer.compare(element1.getWeight(), element2.getWeight());
		});

		DSU dsu = new DSU(vertexNumber);
		List<Edge> mstEdges = new ArrayList<>();
		long totalWeight = 0;
		for(Edge edge : allEdges){
			if(mstEdges.size() == vertexNumber - 1){
				break;
			}
			int from = edge.getFrom();
			int to = edge.getTo();
			if(dsu.union(from, to)){
				mstEdges.add(edge);
				totalWeight += edge.getWeight();
			}
		}
		if(mstEdges.size() != vertexNumber - 1){
			throw new IllegalArgumentException("MST doesn't exist for this graph");
		}
		return new MSTResult(mstEdges, totalWeight, comparisons[0]);
	}
}
