/**
 * 
 */
package greedyAlgorithms;

/**
 * @author ccottap
 *
 */
public class HuffmanTree extends Tree<HuffmanTuple> implements Comparable<HuffmanTree> {

	/**
	 * Creates a leaf
	 */
	public HuffmanTree(char symbol, int freq) {
		super(new HuffmanTuple(symbol, freq));
	}
	
	/**
	 * Creates a new tree joining two subtrees
	 */
	public HuffmanTree(HuffmanTree L, HuffmanTree R) {
		super(new HuffmanTuple(L.getFrequency()+R.getFrequency()), L, R);
	}

	/**
	 * Returns the frequency of the symbol (dummy or not) at the root of the tree
	 */
	public int getFrequency() {
		return root.getFrequency();
	}
	
	/**
	 * Returns whether the tree is just a leaf.
	 */
	public boolean isLeaf() {
		return !(root.isInternal());
	}
	
	/**
	 * Returns the symbol at the root of the tree. If it is dummy, an exception will propagate.
	 */
	public char getSymbol() {
		return root.getSymbol();
	}
	
	/**
	 * Returns the left subtree.
	 */
	public HuffmanTree left()
	{
		return (HuffmanTree) left;
	}

	/**
	 * Returns the right subtree.
	 */
	public HuffmanTree right()
	{
		return (HuffmanTree) right;
	}

	/**
	 * To compare and sort trees
	 */
	@Override
	public int compareTo(HuffmanTree o) {
		return root.compareTo(o.root);
	}


}
