/**
 * 
 */
package greedyAlgorithms;

/**
 * @author ccottap
 *
 */
public class HuffmanTuple implements Comparable<HuffmanTuple> {
	int frequency; 		// number of times a symbol appears.
	char symbol; 		// the symbol
	boolean internal;	// whether it is a dummy symbol or not

	/**
	 * Creates an internal node
	 * @param freq is the frequency of the dummy symbol
	 */
	public HuffmanTuple(int freq) {
		frequency = freq;
		internal = true;
	}

	/**
	 * Creates a leaf
	 * @param symb is the symbol
	 * @param freq is its frequency
	 */
	public HuffmanTuple(char symb, int freq) {
		frequency = freq;
		symbol = symb;
		internal = false;
	}
	
	/**
	 * Returns the frequency of a symbol
	 */
	public int getFrequency()
	{
		return frequency;
	}
	
	/**
	 * Returns the symbol
	 */
	public char getSymbol() throws RuntimeException
	{
		if (internal)
			throw new RuntimeException ("Cannot access symbol in internal Huffman tuple");
		return symbol;
	}
	
	/**
	 * Returns whether a node is internal
	 */
	public boolean isInternal()
	{
		return internal;
	}


	/**
	 * To compare and sort tuples
	 */
	@Override
	public int compareTo(HuffmanTuple o) {
		return frequency - o.frequency;
	}
	
	/**
	 * Converts the tuple to a string for printout purposes
	 */
	public String toString()
	{
		String output;
		
		if (isInternal())
			output = "[" + frequency + "]";
		else
			output = "[" + symbol + ", " + frequency + "]";
		
		return output;
	}


}
