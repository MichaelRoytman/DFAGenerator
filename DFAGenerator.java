import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

/**
 * A class that models a DFA generator: given a white list, the program generates a DFA
 * and minimizes it. It then returns a string representation of the DFA.
 * 
 * @author michaelroytman
 *
 */
public class DFAGenerator {
	
	//a map from a state number to a state; represents all the states of the machine
	private Map<Integer, Node> states = new HashMap<Integer, Node>();
	
	private Node startState; //pointer to start state
	private Node deadState; //a "dead state," useful for minimization
	
	private int stateNumber = 1; //counter of states in the machine; 0 is start stage
	
	//alphabet recognized by this machine
	private String alphabet = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890.";
	
	//represents a table for the table filling algorithm
	private int[][] stateMatrix;

	//a map from each node to a set of nodes that it is indistinguishable to
	private Map<Node, HashSet<Node>> indistinguishablePairs;
	
	
	private Map<HashSet<Node>, Node> mergedState;
	
	/**
	 * Constructor for DFA generator, which constructs the start state and puts it in the states map.
	 */
	public DFAGenerator() {
		startState = new Node("S");
		states.put(0, startState);
	}
	
	/**
	 * A method that generated a string representation of a DFA given a whitelist of accepted strings.
	 * @param whitelist
	 * @return
	 */
	public String generate(Set<String> whitelist) {
		//this maintains the position in the graph while adding states for a given string
		Node pointer = startState;

		//loops for each string in our whitelist
		for (String acceptedString : whitelist) {
			
			//a case for the empty string - we need to make the start state an accept state
			if (acceptedString.equals("")) {
				startState.makeAcceptState();
			}
							
			//loops for each character in a given string in the whitelist
			for (int i = 0; i < acceptedString.length(); i++) {
				//character as a String
				String symbol = acceptedString.charAt(i) + "";
					
				//if our graph does not have a transition with the given character, we create a new state for it
				if (!pointer.getTransitions().containsKey(symbol)) {
					
					//create a new state represented by state + number of state in the machine
					Node state = new Node("Q", stateNumber);

					states.put(stateNumber, state);
					
					//increments counter because we have parsed a character
					stateNumber++;
					
					//we have reached the accept state, so we make our state the accept state
					if (i == acceptedString.length()-1) {
						state.makeAcceptState();
					}
					
					//add a transition from the pointer to state via a symbol character
					pointer.addTransition(symbol, state);
					
					//reassignment of pointers
					//if character was the last in the string, pointer points to start state
					if (i == acceptedString.length()-1) {
						pointer = startState;
					}
					//else, pointer is our new state
					else {
						pointer = state;
					}	
				 }
				//if our graph has a transition from the pointer to another state via the given character,
				//we simply move to that state upon consuming the character
				 else {		
					 
					 //reassignment of pointer
					 pointer = pointer.getTransitions().get(symbol);
					 
					 //if the character was the last in the string, we make the pointer an accept state
					 //and reassign the pointer to the start state
					 if (i == acceptedString.length()-1) {
							pointer.makeAcceptState();
							pointer = startState;
					}	 
				}
			}	
		}	
		
		//minimizes the DFA
		minimize();
		
		//removes dead state before creating machine string
		states.remove(deadState.getNumber());
		
		//returns string representation of the machine
		return toString();	
	} 
	
	/**
	 * Method that returns the String representation of the DFA.
	 */
	public String toString() {
		//initializes a StringBuilder to build the string representation of the machine
		StringBuilder machineRepresentation = new StringBuilder();
		
		//for all states in the set of states in our machine
		for (Node state : states.values()) {
			//we get the transitions for a given state
			Map<String, Node> transitions = state.getTransitions();
			
			//for all entries in a given state's transitions
			for (Map.Entry<String, Node> entry : transitions.entrySet()) {
				//we add a string for each transition in the following format: "symbol, from-state, to-state"
				
				//state to which state leads on symbol
				Node toState = entry.getValue();
				
				if (toState != null) {
					machineRepresentation.append(entry.getKey() + "," + state.toString() + ", " + entry.getValue() + "\n");
				}	
			}
		}
	
		//return string representation of machine
		return machineRepresentation.toString();
	} 
	
	/**
	 * Method that minimizes the DFA per the table filling algorithm.
	 */
	public void minimize() {
		//creates a dead state
		deadState = new Node("dead-state", stateNumber);
		
		//puts dead state in the map
		states.put(stateNumber, deadState);
		
		//creates new state matrix of size (number of states)x(number of states)
		stateMatrix = new int[states.size()][states.size()];
		
		//adds transitions from dead state to itself on all characters of alphabet
		for (int h = 0; h < alphabet.length(); h++) {
			deadState.addTransition((alphabet.charAt(h)+""), deadState);
		}
		
		//performs the first pass over the matrix
		firstPass();
		
		//represents whether or not change was made to matrix
		boolean changeMade = true;
		
		//loop while changeMade == true; that is, while changes are occuring in the matrix
		while (changeMade) {

			changeMade = false;
			
			//traverse half the matrix (halves are equal)
			for (int i = 0; i < states.size(); i++) {
				for (int j = 0; j <= i; j++) {
					//fromState1 and fromState2 form a pair
					Node fromState1 = states.get(i);
					Node fromState2 = states.get(j);
				
					//for each character in the alphabet
					for(int k = 0; k < alphabet.length(); k++) {
						
						
						//toState1 and toState2 form a pair
						Node toState1 = fromState1.getTransitions().get((alphabet.charAt(k)+""));
						Node toState2 = fromState2.getTransitions().get((alphabet.charAt(k)+""));
									
						//initialize states to deadState if they do not have a transition for a character
						if (toState1 == null) {
							toState1 = deadState;
						}
						
						if (toState2 == null) {
							toState2 = deadState;
						}
						
						//if toState pair is marked as distinguishable, then marked fromState pair as distinguishable
						//and set changeMade to true
						if (stateMatrix[toState1.getNumber()][toState2.getNumber()]==1) {
							if (stateMatrix[fromState1.getNumber()][fromState2.getNumber()] == 0) {
								stateMatrix[fromState1.getNumber()][fromState2.getNumber()]=1;
								stateMatrix[fromState2.getNumber()][fromState1.getNumber()]=1;
								changeMade = true;
							}
						}
					}
				}
			}
		}
		
		//after matrix is complete, merge distinguishable states
		merge();
	}
	
	/**
	 * Method that performs the first pass over the matrix. It checks whether any pair of states,
	 * is distinguishable if only one of them is an accept state.
	 */
	public void firstPass() {
		//traverses matrix
		for (Node state1 : states.values()) {
			for (Node state2 : states.values()) {
				
				//if only one state of the pair is an accept state, the pair is marked indistinguishable
				if ((state1.isAcceptState() && state2.isNonAcceptState()) || (state1.isNonAcceptState() && state2.isAcceptState())) {
					stateMatrix[state1.getNumber()][state2.getNumber()] = 1;
					stateMatrix[state2.getNumber()][state1.getNumber()] = 1;
				}
			}
		}
	}
	
	/**
	 * Method that merges indistinguishable states in the DFA.
	 */
	public void merge() {
		//a map of Nodes to a Set that contain the nodes that they are indistinguishable to, including itself
		indistinguishablePairs = new HashMap<Node, HashSet<Node>>();
		
		//a map of Sets that contain indistinguishable nodes to the Node that will remain in the machine post-
		//minimization
		mergedState = new HashMap<HashSet<Node>, Node>();
		
		//set of nodes to be deleted from the DFA; unnecessary states
		HashSet<Node> toBeDeleted = new HashSet<Node>();
		
		//traverse half the matrix (halves are equal)
		//fills up indistinguishablePairs map
		for (int i = 0; i < states.size(); i++) {
			for (int j = 0; j < i; j++) {
				if (stateMatrix[i][j] == 0) {
					
					Node state1 = states.get(i);
					Node state2 = states.get(j);
					
					HashSet<Node> pair = new HashSet<Node>();
					
					//puts indistinguishable states in set
					pair.add(state1);
					pair.add(state2);
					
					//puts set in map for both states
					indistinguishablePairs.put(state1, pair);
					indistinguishablePairs.put(state2, pair);
					
					//choose state1 to be our merged state
					mergedState.put(pair, state1);
					
					//add state2 to set to be deleted
					toBeDeleted.add(state2);
				}
			}
		}
		
		//values of states; all the states in the machine
		Collection<Node> values = states.values();
		
		//traverses all states in the machine
		for (Node state : values) {
			//the set of characters which state has transition for
			Set<String> keys = state.getTransitions().keySet();
			
			for (String symbol : keys) {
	
				Set<Node> mergedSet = indistinguishablePairs.keySet(); //sets of all states that are indistinguishable
				Node toState = state.getTransitions().get(symbol); //state that state goes to on character
				
				//if toState is indistinguishable to something
				if (mergedSet.contains(toState)) {
					
					//the state that we are going to be merging into
					Node merged = mergedState.get(indistinguishablePairs.get(toState));
					
					//add a transition from state to the state that will remain in the graph on that character
					state.addTransition(symbol, merged); 
				}
				
				//if the state is a state that will be merged into another one and is the one that will remain in the DFA
				//and if it does not have a transition for a given character that one of its indistinguishable partner does,
				//add that transition to the remaining state.
				if (mergedSet.contains(state) && mergedState.get(indistinguishablePairs.get(state)).equals(state)) {
					if (!mergedState.get(indistinguishablePairs.get(state)).getTransitions().containsKey(symbol)) {
						mergedState.get(indistinguishablePairs.get(state)).addTransition(symbol, state.getTransitions().get(symbol));
					}
				}
			}
			
		}
		
		//remove nodes to be deleted
		for (Node state : toBeDeleted) {
			states.remove(state.getNumber());
		}
	}	
}
