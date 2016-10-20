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
	Integer[] infinitValuesArray = new Integer[RouterSimulator.NUM_NODES];
	

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
		

		for ( int i = 0; i < RouterSimulator.NUM_NODES; i++ ) {
			infinitValuesArray[i] = RouterSimulator.INFINITY;
		}
		
		// initializing distance vector of neighours and itself
		for ( int i = 0; i < RouterSimulator.NUM_NODES; i++ ) {
			
			// we add the distance vector of current node in distanceVectors
			if ( i == myID ) {
				Integer[] costsToInteger = convertToInteger(costs);
				distanceVectors.put(i, costsToInteger);
				continue;
			}
			
			// we check if node "i" is a neighbour
			if ( costs[i] == sim.INFINITY ) {
				continue;
			}
			
			distanceVectors.put(i, infinitValuesArray);
		}
		
		// send distanceVector to neighbours of the current node
		notifyNeighbours();
	}
	
	// --------------------------------------------------
	public void recvUpdate(RouterPacket pkt) {
		// add the distance vector of neighbour
		distanceVectors.put(pkt.sourceid, convertToInteger(pkt.mincost));
		
		updateNodeSDistanceVector();
		
	}
	
	// --------------------------------------------------
	private void sendUpdate(RouterPacket pkt) {
		sim.toLayer2(pkt);
		
	}
	
	// --------------------------------------------------
	public void printDistanceTable() {
		myGUI.println("\n\nCurrent state for router " + myID + " at time " + sim.getClocktime());
		
		myGUI.println("Distance table:");
		myGUI.print(F.format(" Dst |", 10));
		
		for ( int i = 0; i < sim.NUM_NODES; i++ ) {
			myGUI.print(F.format(i, 10));
		}
		
		myGUI.println();
		
		for ( int i = 0; i <= (sim.NUM_NODES + 1) * 10; i++ ) {
			myGUI.print("-");
		}
		
		myGUI.println();
		
		for ( Entry<Integer, Integer[]> entry : distanceVectors.entrySet() ) {
			
			if ( myID == entry.getKey() ) {
				continue; // we don't want to print the distance vector of the
				          // current node here
			}
			
			myGUI.print(F.format(" nbr " + entry.getKey() + " |", 10));
			
			for ( Integer i : entry.getValue() ) {
				myGUI.print(F.format(i, 10));
			}
			
			myGUI.println(" ");
		}
		
		myGUI.println();
		
		myGUI.println("Our distance vector and routes:");
		myGUI.print(F.format(" Dst |", 10));
		
		for ( int i = 0; i < sim.NUM_NODES; i++ ) {
			myGUI.print(F.format(i, 10));
		}
		
		myGUI.println();
		
		for ( int i = 0; i <= (sim.NUM_NODES + 1) * 10; i++ ) {
			myGUI.print("-");
		}
		
		myGUI.println();
		
		myGUI.print(F.format(" cost |", 10));
		
		Integer[] dvOfCurrentNode = distanceVectors.get(myID);
		
		for ( Integer i : dvOfCurrentNode ) {
			myGUI.print(F.format(i, 10));
		}
		
		myGUI.println();
		
		myGUI.print(F.format(" route |", 10));
		
		for ( int i = 0; i < sim.NUM_NODES; i++ ) {
			
			if ( dvOfCurrentNode[i] == sim.INFINITY ) {
				myGUI.print(F.format("-", 10));
			} else {
				myGUI.print(F.format(i, 10));
			}
		}
	}
	
	// --------------------------------------------------
	public void updateLinkCost(int dest, int newcost) {
		costs[dest] = newcost;
		
		// reinitialize the dv of current Node;
		distanceVectors.put(myID, infinitValuesArray);
		
		printCurrentDistanceVectors();
		// new vers
		// update node's distance vector
		updateNodeSDistanceVector();
		
	}
	
	/**
	 * 
	 */
	private void printCurrentDistanceVectors() {
		Integer[] currentDistanceVector;
		
		System.out.println("Node " + myID + " distance vectors hm:");
		
		for ( int i = 0; i < sim.NUM_NODES; i++ ) {
			System.out.print(i + " :");
			
			currentDistanceVector = distanceVectors.get(i);
			
			for ( Integer j : currentDistanceVector ) {
				System.out.print(j + " ");
			}
			
			System.out.println();
		}
		
		System.out.println();
		
	}
	
	/**
	 * 
	 */
	public void updateNodeSDistanceVector() {
		Integer[] distanceVectorOfCurrentNode = distanceVectors.get(myID);
		distanceVectorOfCurrentNode[myID] = 0;
		
		boolean modifiedCurrentDistanceVector = false;
		
		for ( int i = 0; i < RouterSimulator.NUM_NODES; i++ ) {
			
			// skip this iteration, we already know that
				// distanceVectOfCurrentNode==0
			if ( (i == myID) ) {
				// {
				
				continue;
			}
			
			// if costs[i] == Infinity (then the dv won't be updated anyway)
			Integer[] distanceVectorOfNeighbour = distanceVectors.get(i);
			
			for ( int j = 0; j < RouterSimulator.NUM_NODES; j++ ) {
				
				// distance to get to current node is always 0
				if ( j == myID ) {
					continue;
				}
				
				// Bellman-Ford Equation
				if ( distanceVectorOfCurrentNode[j] > (costs[i] + distanceVectorOfNeighbour[j]) ) {
					distanceVectorOfCurrentNode[j] = costs[i] + distanceVectorOfNeighbour[j];
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
