package graph;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Graph {
	private final int vertexNumber;
	private int edgeNumber;
	private final boolean isDirected;
	private List<Edge>[] adjList;
	private List<Edge> allEdges;

	public Graph(int vertexNumber, boolean isDirected){
		this.vertexNumber = vertexNumber;
		this.edgeNumber = 0;
		this.isDirected = isDirected;
		this.adjList = new ArrayList[vertexNumber];
		for(int i = 0; i < vertexNumber; i++){
			adjList[i] = new ArrayList<>();
		}
		this.allEdges = new ArrayList<>();
	}

	public void addEdges(int from, int to, int weight){
		Edge edge = new Edge(from, to, weight);
		adjList[from].add(edge);
		allEdges.add(edge);

		if(!isDirected){
			Edge reversedEdge = new Edge(to, from, weight);
			adjList[to].add(reversedEdge);
		}
		edgeNumber++;
	}

	public static Graph fromGraph(String filePath) throws IOException{
		try(BufferedReader reader = new BufferedReader(new FileReader(filePath))){
			String firstLine = reader.readLine();
			if(firstLine == null || firstLine.trim().isEmpty()){
				throw new IllegalArgumentException("Invalid graph file format");
			}

			String[] parts = firstLine.trim().split("\\s+");
			int vertexNumber = Integer.parseInt(parts[0]);
			int edgeNumber = Integer.parseInt(parts[1]);
			if(!(parts[2].equals("0") || parts[2].equals("1"))){
				throw new IllegalArgumentException("Directed flag is not 0 or 1");
			}
			boolean isDirected = (Integer.parseInt(parts[2]) == 1);

			Graph graph = new Graph(vertexNumber, isDirected);
			for(int i = 0; i < edgeNumber; i++){
				String line = reader.readLine();
				if(line == null){
					throw new IllegalArgumentException("File format error: expected "
					+ edgeNumber + " edges, but file finished after " + i + " edges");
				}
				String[] edgeParts = line.trim().split("\\s+");
				if(edgeParts.length < 3){
					throw new IllegalArgumentException("Invalid edge format at line " + (i+2) +
					": expected from, to, weight.");
				}
				int from = Integer.parseInt(edgeParts[0]);
				int to = Integer.parseInt(edgeParts[1]);
				int weight = Integer.parseInt(edgeParts[2]);
				graph.addEdges(from, to, weight);
			}
			return graph;
		}
	}

	public int getVertexNumber(){
		return vertexNumber;
	}
	public int getEdgeNumber(){
		return edgeNumber;
	}
	public boolean isDirected(){
		return isDirected;
	}
	public List<Edge>[] getAdjList(){
		return adjList;
	}
	public List<Edge> getAllEdges(){
		return allEdges;
	}
}
