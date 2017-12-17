/**
 * 
 */
package greedyAlgorithms;

/**
 * @author ccottap
 *
 */
public class Tree<E> {
	protected E root;				// root of the tree
	protected Tree<E> left, right;	// left/right subtrees
	boolean empty;					// whether the tree is empty or not

	/**
	 * Creates an empty tree
	 */
	public Tree() {
		empty = true;
	}
	
	/**
	 * Creates a leaf with element el.
	 * @param el is the element
	 */
	public Tree(E el) {
		this(el, new Tree<E>(), new Tree<E>());
	}

	/**
	 * Creates a tree given its root and left, right subtrees.
	 * @param el is the root
	 * @param L is the left subtree
	 * @param R is the right subtree
	 */
	public Tree(E el, Tree<E> L, Tree<E> R) {
		empty = false; 
		root = el;
		left = L;
		right = R;
	}
	
	/**
	 * Returns whether the tree is empty.
	 */
	public boolean isEmpty()
	{
		return empty;
	}
	
	/**
	 * Returns the root of the tree.
	 */
	public E root() throws RuntimeException
	{
		if (empty) 
			throw new RuntimeException("Cannot get the root of an empty tree.");
		return root;
	}
	
	/**
	 * Returns the left subtree.
	 */
	public Tree<E> left() throws RuntimeException
	{
		if (empty) 
			throw new RuntimeException("Cannot get the left subtree of an empty tree.");
		return left;
	}

	/**
	 * Returns the right subtree.
	 */
	public Tree<E> right() throws RuntimeException
	{
		if (empty) 
			throw new RuntimeException("Cannot get the right subtree of an empty tree.");
		return right;
	}

	/**
	 * Converts the tree to a string for printout purposes
	 */
	public String toString()
	{
		
		return toString(0, null, false);
	}
	
	/**
	 * Auxiliary function for converting to text
	 */
	public String toString(int level, boolean[] branch, boolean next)
	{
		String output;
		
		if (empty)
			output = "";
		else {
			String tabs = "";
			boolean[] newbranch = new boolean[level+1];
			for (int i=0; i<level; i++) {
				tabs = tabs + "  ";
				if (branch[i])
					tabs = tabs + "|";
				else
					tabs = tabs + " ";
				newbranch[i] = branch[i];
			}
			if (level > 0)
				tabs = tabs + "  +";
			newbranch[level] = next;
			output = "\n" + tabs + " " + root.toString() + right.toString(level+1, newbranch, true) + left.toString(level+1, newbranch, false);
		}
		
		return output;
	}
}
