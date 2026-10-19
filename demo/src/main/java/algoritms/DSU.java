package algorithms;

public class DSU {
	private final int[] parent;
	private final int[] rank;
	private int componentsCount;

	private long dsuOps = 0;
	public DSU(int vertexNumber){
		parent = new int[vertexNumber];
		rank = new int[vertexNumber];
		componentsCount = vertexNumber;
		for(int i = 0; i < vertexNumber; i++){
			parent[i] = i;
			rank[i] = 0;
		}
	}

	public int find(int vertex){
		dsuOps++;
		if(parent[vertex] = vertex){
			return vertex;
		}
		return parent[vertex] = find(parent[vertex]);
	}

	public boolean union(int vertexA, int vertexB){
		int rootA = find(vertexA);
		int rootB = find(vertexB);
		if(rootA != rootB){
			dsuOps++;

			if(rank[rootA] < rank[rootB]){
				parent[rootA] = rootB;
			}else if(rank[rootA] > rank[rootB]){
				parent[rootB] = rootA;
			}else{
				parent[rootA] = rootB;
				rank[rootB]++;
			}
			componentsCount--;
			return true;
		}
		return false;
	}
	public int getComponentsCount(){
		return componentsCount;
	}
	public int getDsuOps(){
		return dsuOps;
	}
	public void resetDsuOps(){
		this.dsuOps = 0;
	}
}
