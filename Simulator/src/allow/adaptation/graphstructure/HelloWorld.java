package allow.adaptation.graphstructure;

import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.BreadthFirstIterator;
import org.jgrapht.traverse.GraphIterator;

public class HelloWorld {

    public static void main(String[] args) {
	int width = 0;
	DirectedGraph<String, DefaultEdge> graph = new DefaultDirectedGraph<String, DefaultEdge>(DefaultEdge.class);

	graph.addVertex("INIT");
	graph.addVertex("FB_13");
	graph.addVertex("COM_1");
	graph.addVertex("RM_1");

	graph.addEdge("INIT", "FB_13");
	graph.addEdge("FB_13", "COM_1");
	graph.addEdge("COM_1", "RM_1");

	//
	// GraphIterator<Integer, DefaultEdge> iterator = new
	// DepthFirstIterator<Integer, DefaultEdge>(graph);
	// while (iterator.hasNext()) {
	// System.out.println(iterator.next());
	// }

	GraphIterator<String, DefaultEdge> iterator1 = new BreadthFirstIterator<String, DefaultEdge>(graph);
	while (iterator1.hasNext()) {
	    width++;
	    System.out.println(iterator1.next());
	}
	System.out.println("Exit: " + width);
    }

}