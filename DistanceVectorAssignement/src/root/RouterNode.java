package root;

import java.util.HashMap;
import java.util.Map.Entry;

public class RouterNode {
	private int myID;
	private GuiTextArea myGUI;
	private RouterSimulator sim;
	private int[] costs = new int[RouterSimulator.NUM_NODES]; // the cost to
	                                                          // each direct
	                                                          // neighbour
	
	// the distance vector of current node and it's neighbours is hold in this
	// map // neighbo
	HashMap<Integer, Integer[]> distanceVectors = new HashMap<>();
	
	// --------------------------------------------------
	/**
	 * @param ID
	 * @param sim
	 * @param costs
	 */
	public RouterNode(int ID, RouterSimulator sim, int[] costs) {
		myID = ID;
		this.sim = sim;
		myGUI = new GuiTextArea("  Output window for Router #" + ID + "  ");
		
		System.arraycopy(costs, 0, this.costs, 0, RouterSimulator.NUM_NODES);
		
		Integer[] initialDistanceArrayForNeighbours = new Integer[RouterSimulator.NUM_NODES];
		for ( int i = 0; i < RouterSimulator.NUM_NODES; i++ ) {
			initialDistanceArrayForNeighbours[i] = RouterSimulator.INFINITY;
		}
		
		// initializing distance vector of neighours and itself
		for ( int i = 0; i < RouterSimulator.NUM_NODES; i++ ) {
			
			if ( i == myID ) {
				Integer[] costsToInteger = convertToInteger(costs);
				distanceVectors.put(i, costsToInteger);
				continue;
			}
			
			distanceVectors.put(i, initialDistanceArrayForNeighbours);
		}
		
		// send distanceVector to neighbours of the current node
		notifyNeighbours();
	}
	
	// --------------------------------------------------
	public void recvUpdate(RouterPacket pkt) {
		Integer[] distanceVectorOfCurrentNode = distanceVectors.get(myID);
		boolean modifiedCurrentDistanceVector = false;
		
		// add the distance vector of neighbour
		distanceVectors.put(pkt.sourceid, convertToInteger(pkt.mincost));
		
		// update the distanceVector of current node if needed
		for ( int i = 0; i < RouterSimulator.NUM_NODES; i++ ) {
			
			// Bellman-Ford Equation
			if ( distanceVectorOfCurrentNode[i] > (costs[pkt.sourceid] + pkt.mincost[i]) ) {
				distanceVectorOfCurrentNode[i] = costs[pkt.sourceid] + pkt.mincost[i];
				modifiedCurrentDistanceVector = true;
			}
			
		}
		
		// send the new distanceVector to neighbours if needed
		if ( modifiedCurrentDistanceVector ) {
			// distanceVectors.put(myID, distanceVectorOfCurrentNode);
			notifyNeighbours();
		}
		
	}
	
	// --------------------------------------------------
	private void sendUpdate(RouterPacket pkt) {
		sim.toLayer2(pkt);
		
	}
	
	// --------------------------------------------------
	public void printDistanceTable() {
		myGUI.println("Current table for " + myID + "  at time " + sim.getClocktime());
		
		for ( Entry<Integer, Integer[]> entry : distanceVectors.entrySet() ) {
			myGUI.print(entry.getKey() + " :");
			
			for ( Integer i : entry.getValue() ) {
				myGUI.print(" " + i);
			}
			
			myGUI.println(" ");
		}
	}
	
	// --------------------------------------------------
	public void updateLinkCost(int dest, int newcost) {
		costs[dest] = newcost;
		
		// update node's distance vector if needed
		Integer[] distanceVectorOfCurrentNode = distanceVectors.get(myID);
		boolean modifiedCurrentDistanceVector = false;
		

		for ( int i = 0; i < RouterSimulator.NUM_NODES; i++ ) {
			
			// node "i" should be a neighbour (otherwise we won't modify
				// distance vector of the current node)
			if ( (i == myID) || (costs[i] != RouterSimulator.INFINITY) ) {
				continue;
			}
			
			Integer[] distanceVectorOfNeighbour = distanceVectors.get(i);
			
			for ( int j = 0; j < RouterSimulator.NUM_NODES; j++ ) {
				
				// for neighbours Bellman-Ford equation may be 0
				if ( j == myID ) {
					continue;
				}
				
				// Bellman-Ford Equation
				if ( distanceVectorOfCurrentNode[i] > (costs[j] + distanceVectorOfNeighbour[i]) ) {
					distanceVectorOfCurrentNode[i] = costs[j] + distanceVectorOfNeighbour[i];
					modifiedCurrentDistanceVector = true;
				}
				
			}
			

		}
		
		// send the new distanceVector to neighbours if needed
		if ( modifiedCurrentDistanceVector ) {
			// distanceVectors.put(myID, distanceVectorOfCurrentNode);
			notifyNeighbours();
		}
		
	}
	
	/**
	 * sends distanceVector to neighbours of the current node
	 */
	public void notifyNeighbours() {
		
		RouterPacket packetToBeSent = null;
		int[] distanceVectorOfCurrentNode = convertToPrimitiveInt(distanceVectors.get(myID));
		
		// send distance vector of current node to it's neighbours
		for ( int i = 0; i < RouterSimulator.NUM_NODES; i++ ) {
			
			// checking if node i represent a neighbour of current node
			if ( (i != myID) && (costs[i] != RouterSimulator.INFINITY) ) {
				
				packetToBeSent = new RouterPacket(myID, i, distanceVectorOfCurrentNode);
				
				sendUpdate(packetToBeSent);
			}
		}
		
	}
	
	/**
	 * @param costs2
	 * @return
	 */
	private Integer[] convertToInteger(int[] primitiveIntArray) {
		Integer[] intObjectsArray = new Integer[primitiveIntArray.length];
		
		for ( int i = 0; i < primitiveIntArray.length; i++ ) {
			intObjectsArray[i] = primitiveIntArray[i];
		}
		
		return intObjectsArray;
	}
	
	private int[] convertToPrimitiveInt(Integer[] nonPrimitiveIntArray) {
		int[] primitiveIntArray = new int[nonPrimitiveIntArray.length];
		int j = 0;
		
		for ( Integer i : nonPrimitiveIntArray ) {
			primitiveIntArray[j++] = i;
		}
		
		return primitiveIntArray;
	}
	
}
