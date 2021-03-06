package root;

import java.util.Arrays;
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
	int[] nextHopArray = new int[RouterSimulator.NUM_NODES];
	
	public boolean poisonReverse;
	
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
			
			// initializing nextHopArray
			nextHopArray[i] = i;
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
		
		// if ( flag ) {
		// reinitializeDistanceVectorOfCurrentNode();
		// }
		
		updateNodeSDistanceVector();
		
	}
	
	// --------------------------------------------------
	private void sendUpdate(RouterPacket pkt) {
		sim.toLayer2(pkt);
		
	}
	
	// --------------------------------------------------
	public void printDistanceTable() {
		myGUI.println("\n\nCurrent state for router " + myID + " at time " + F.format(sim.getClocktime(), 7));
		
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
				myGUI.print(F.format(nextHopArray[i], 10));
			}
		}
	}
	
	// --------------------------------------------------
	public void updateLinkCost(int dest, int newcost) {
		costs[dest] = newcost;
		
		// reinitialize the dv of current Node;
		// reinitializeDistanceVectorOfCurrentNode();
		
		// distanceVectors.put(myID, convertToInteger(costs));
		
		// printCurrentDistanceVectors("before");
		
		// new vers
		// update node's distance vector
		updateNodeSDistanceVector();
		
		// printCurrentDistanceVectors("after");
		
		// flag = true;
		
	}
	
	/**
	 * 
	 */
	private void reinitializeDistanceVectorOfCurrentNode() {
		Integer[] dvOfCNode = distanceVectors.get(myID);
		
		for ( int i = 0; i < sim.NUM_NODES; i++ ) {
			dvOfCNode[i] = sim.INFINITY;
		}
		
	}
	
	/**
	 * 
	 */
	private void printCurrentDistanceVectors(String time) {
		Integer[] currentDistanceVector;
		
		System.out.println("Node " + myID + " distance vectors " + time + " update:");
		
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
		
		Integer[] initialDistanceVectorOfCurrentNode = distanceVectors.get(myID);
		
		// we want another reference so that we can compare at the end of the
		// method
		// if the distance Vector of the current node was modified
		initialDistanceVectorOfCurrentNode = initialDistanceVectorOfCurrentNode.clone();
		
		int[] potentialNextHopArray = new int[RouterSimulator.NUM_NODES];
		System.arraycopy(nextHopArray, 0, potentialNextHopArray, 0, RouterSimulator.NUM_NODES);
		
		// at each step we recompute the dV based on information that we have
		reinitializeDistanceVectorOfCurrentNode();
		
		Integer[] distanceVectorOfCurrentNode = distanceVectors.get(myID);
		distanceVectorOfCurrentNode[myID] = 0;
		
		for ( int i = 0; i < RouterSimulator.NUM_NODES; i++ ) {
			
			// skip this iteration if i is not a neighbour or we want to are
			// going to set distanceVectorOfCurrentNode[myId], we already know
			// that
			// distanceVectOfCurrentNode[myID]==0
			//
			if ( (i == myID) || (costs[i] == sim.INFINITY) ) {
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
					potentialNextHopArray[j] = i;
					
				}
				
			}
			
		}
		
		// send the new distanceVector to neighbours if needed
		if ( !Arrays.equals(initialDistanceVectorOfCurrentNode, distanceVectorOfCurrentNode) ) {
			
			// we have a new distance vector array for current node, thus a new
			// nextHopArray
			System.arraycopy(potentialNextHopArray, 0, nextHopArray, 0, RouterSimulator.NUM_NODES);
			
			notifyNeighbours();
		}
	}
	
	/**
	 * sends distanceVector to neighbours of the current node
	 */
	public void notifyNeighbours() {
		
		RouterPacket packetToBeSent = null;
		
		// send distance vector of current node to it's neighbours
		for ( int i = 0; i < RouterSimulator.NUM_NODES; i++ ) {
			int[] distanceVectorOfCurrentNode = convertToPrimitiveInt(distanceVectors.get(myID));
			
			// checking if node i represent a neighbour of current node
			if ( (i != myID) && (costs[i] != RouterSimulator.INFINITY) ) {
				
				if ( poisonReverse ) {
					
					distanceVectorOfCurrentNode = getDistanceVectorPoisoned(i, distanceVectorOfCurrentNode);
				}
				
				packetToBeSent = new RouterPacket(myID, i, distanceVectorOfCurrentNode);
				sendUpdate(packetToBeSent);
				
			}
		}
		
	}
	
	/**
	 * @param i
	 * @return
	 */
	private int[] getDistanceVectorPoisoned(int destinationNode, int[] distanceVectorOfCurrentNode) {
		distanceVectorOfCurrentNode = distanceVectorOfCurrentNode.clone();
		
		for ( int i = 0; i < RouterSimulator.NUM_NODES; i++ ) {
			
			// if the current node routes through the destination of the packet
			// to get to another node then we advertise that the distance is
			// Infinity
			if ( (nextHopArray[i] == destinationNode) && (i != destinationNode) && (i != myID) ) {
				
				distanceVectorOfCurrentNode[i] = RouterSimulator.INFINITY;
			}
		}
		
		// if ( (destinationNode == 1) && (myID == 2) ) {
		// System.out.println("Distance vector send to 1");
		// System.out.println(distanceVectorOfCurrentNode[0] + " " +
		// distanceVectorOfCurrentNode[1] + " "
		// + distanceVectorOfCurrentNode[2]);
		//
		// }
		
		return distanceVectorOfCurrentNode;
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
