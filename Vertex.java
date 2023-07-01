public class Vertex {
	
	public int id;
	
	// As more lines are in the input file, there are more verticies generated, which increases its ID automatically.
	public Vertex(int i) {
		this.id = i;
	}
	
	// As an easy way to refer to vertecies, there is a "V" before every id.
	public String toString() {
		return "V" + this.id;
	}
}