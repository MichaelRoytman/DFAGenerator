import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of a state of a DFA.
 * @author michaelroytman
 *
 */
public class Node {
	private String name;
	private int stateNumber; //identification number
	private Map<String, Node> transitions; //map of transition from state to another on a character
	
	/**
	 * Constructor, given a name and an identification number.
	 * @param name
	 * @param stateNumber
	 */
	public Node(String name, int stateNumber) {
		this.name = name + stateNumber;
		this.stateNumber = stateNumber;
		transitions = new HashMap<String, Node>();
	}
	
	/**
	 * Construction for start stat
	 * @param name
	 */
	public Node(String name) {
		this.name = name;
		this.stateNumber = 0;
		transitions = new HashMap<String, Node>();
	}
	
	/**
	 * Getter method to return state name.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Getter method to return state identification number.
	 * @return
	 */
	public int getNumber() {
		return stateNumber;
	}
	
	/**
	 * Getter method to return transition function map.
	 * @return
	 */
	public Map<String, Node> getTransitions() {
		return transitions;
	}
	
	/**
	 * Method that adds a transition to state on symbol
	 * Precondition: symbol is in the alphabet, assumption 
	 * @param symbol
	 * @param state
	 */
	public void addTransition(String symbol, Node state) {
		transitions.put(symbol, state);
	}
	
	/**
	 * Makes the state an accept state by concatenating on a star.
	 */
	public void makeAcceptState() {
		name = name + "*";
	}
	
	/**
	 * Returns whether or not state is an accept state.
	 * @return
	 */
	public boolean isAcceptState() {
		return name.contains("*");
	}
	
	/**
	 * Returns whether or not state is not an accept state.
	 * @return
	 */
	public boolean isNonAcceptState() {
		return !isAcceptState();
	}
	
	/**
	 * Method that checks for equality between states based on name and identification number.
	 */
	public boolean equals(Object o) {
		if (o instanceof Node) {
			Node node = (Node)o;
			return (node.name.equals(name) && stateNumber == node.getNumber());
		}
		else {
			return false;
		}
	}
	
	/**
	 * toString method that returns the name of the state.
	 */
	public String toString() {
		return name;
	}
}
