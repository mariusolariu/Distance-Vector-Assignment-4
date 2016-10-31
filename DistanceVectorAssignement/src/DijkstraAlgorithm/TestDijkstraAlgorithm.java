/**
 * 
 */
package DijkstraAlgorithm;

/**
 * @author Marius
 *
 */
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

public class TestDijkstraAlgorithm {
	
	private List<Vertex> nodes;
	private List<Edge> edges;
	DijkstraAlgorithm dijkstra;
	Graph graph;
	
	@Test
	public void testExcute() {
		int numberOfNodes = 3;
		nodes = new ArrayList<>();
		edges = new ArrayList<>();
		for ( int i = 0; i < numberOfNodes; i++ ) {
			Vertex location = new Vertex("Node_" + i, "Node_" + i);
			nodes.add(location);
		}
		
		addLane("Edge_0", 0, 1, 60);
		addLane("Edge_1", 1, 0, 60);
		addLane("Edge_2", 0, 2, 1);
		addLane("Edge_3", 2, 0, 1);
		addLane("Edge_4", 1, 2, 50);
		addLane("Edge_5", 2, 1, 50);

		graph = new Graph(nodes, edges);
		
		for ( int i = 0; i < nodes.size(); i++ ) {
			printDistanceVector(i);
		}
		
		// LinkedList<Vertex> path = dijkstra.getPath(nodes.get(0));
		//
		// assertNotNull(path);
		// assertTrue(path.size() > 0);
		//
		// System.out.println("Cost to reach final dest:" +
		// dijkstra.distance.get(nodes.get(0)));
		// for ( Vertex vertex : path ) {
		// System.out.println(vertex);
		// }
		//
	}
	
	private void addLane(String laneId, int sourceLocNo, int destLocNo, int duration) {
		Edge lane = new Edge(laneId, nodes.get(sourceLocNo), nodes.get(destLocNo), duration);
		edges.add(lane);
	}
	
	private void printDistanceVector(int sourceId){
		dijkstra = new DijkstraAlgorithm(graph);
		dijkstra.execute(nodes.get(sourceId));
		
		System.out.print(sourceId + " : ");
		for ( int i = 0; i < nodes.size(); i++ ) {
			System.out.print(dijkstra.distance.get(nodes.get(i)) + " ");
		}
		
		System.out.println();
		
	}
}