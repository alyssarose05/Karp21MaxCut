public class Edge {
	
	public Vertex vertex1, vertex2;
	public int weight;
	
	// Each edge has two verticies on their sides, Each weight the user enters corresponds to an edge.
	public Edge (Vertex v1, Vertex v2, int w) {
		this.vertex1 = v1;
		this.vertex2 = v2;
		this.weight = w;
	}
}