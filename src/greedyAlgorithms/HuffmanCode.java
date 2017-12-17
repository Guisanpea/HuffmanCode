/**
 * Generates huffman codes.
 * Provides the methods required to encode and decode a file.
 * It also features a number of statistics (about the symbol encoding and about the size of the encoded file). 
 */
package greedyAlgorithms;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.TreeSet;

/**
 * @author ccottap, guisanpea
 *
 */
public class HuffmanCode {

	Map<Character, String> tableEncoding;	// Map with the encoding of each symbol
	HuffmanTree tree;						// Huffman tree 
	private String buffer;					// buffer for reading/writing to file
	static final int BytesPerInt = 4; 		// number of bytes used to encode an int.
	
	/**
	 * Creates the object
	 */
	public HuffmanCode() {
		tableEncoding = null;
		tree = null;
	}
	
	
	/**
	 * Encodes a string (this method is just for testing: a string of "0"s and "1"s is returned, rather than using actual bits)
	 * @param text is a string to be encoded
	 * @return a string of "0"s and "1"s with the actual encoding
	 */
	
	public String encode (String text)
	{
		String output;
		int n = text.length();
		Map <Character, Integer> freq = new Hashtable<Character, Integer>();	
		
		// First pass: get frequencies
		for (int i=0; i<n; i++) {
			Character c = text.charAt(i);			// gets next symbol
			if (freq.containsKey(c)) 
				freq.put(c, freq.get(c) + 1);		// update count of known symbol
			else
				freq.put(c, 1);						// new symbol added
		}
		
		// generate the code using the frequencies
		generate(freq);
		
		// Second pass: encode the input using the codes generated before
		output = "";
		for (int i=0; i<n; i++) {
			Character c = text.charAt(i);			// gets next symbol and...
			output = output + tableEncoding.get(c);	// ... adds its encoding
		}
		
		return output;
	}
	
	
	/**
	 * Encodes a file
	 * @param input is the name of the input file
	 * @param output is the name of the output file
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	
	public void encode (String input, String output) throws IOException, FileNotFoundException 
	{
		FileInputStream inFile;
		FileOutputStream outStream;
		Map <Character, Integer> freq = new Hashtable<Character, Integer>();	
		int b;
		
		/*
		 *  First pass: get frequencies
		 */
		inFile = new FileInputStream(input);		// opens input file
		b = inFile.read();							// gets a symbol from the file
		while (b != -1) {							// while not end of file
			char c = (char)b;
			if (freq.containsKey(c)) 				// update count of symbol read
				freq.put(c, freq.get(c) + 1);
			else
				freq.put(c, 1);
			b = inFile.read();						// gets new symbol
		}
		
		/*
		 *  generate the code using the frequencies
		 */
		generate(freq);
				
		/*
		 *  Second pass: encode the input using the codes generated before
		 */
		outStream = new FileOutputStream(output);	// opens output file
		writeHeader(outStream);						// write header with huffman code info
		clearBuffer();								// prepares the buffer for starting the encoding
		inFile.close();								// closes the input file...
		inFile = new FileInputStream(input);		// ...and reopens it to re-start reading it.
		b = inFile.read();							// gets a symbol from the file
		while (b != -1) {							// while not end of file
			writeBits(outStream, 					// write encoded symbol as a stream of bits
					  tableEncoding.get((char)b));
			b = inFile.read();						// gets new symbol
		}
		flushBuffer(outStream);						// writes any bits that might be left in the buffer
		inFile.close();								// closes input and output files
		outStream.close();
	}
	
	/**
	 * Decodes a file encoded with huffman codes
	 * @param input is the name of the input file
	 * @param output is the name of the output file
	 * @throws IOException, FileNotFoundException 
	 */
	public void decode(String input, String output) throws IOException, FileNotFoundException {
		FileInputStream inFile;
		FileOutputStream outStream;
		Map <Character, Integer> freq;	
		int c;
		int total;

		inFile = new FileInputStream(input);
		// Reads the header
		freq = readHeader(inFile);
		
		// generate the code using the frequencies
		generate(freq);
		
		// determines the length of the decoded file
		total = getMessageSize();

		// decode the input using the codes generated before
		outStream = new FileOutputStream(output);
		clearBuffer();
		for (int i=0; i<total; i++) {
			c = getSymbol(inFile, tree);
			outStream.write((char)c);
		}
		inFile.close();
		outStream.close();		
	}
	
	
	/**
	 * Prints some statistics about the Huffman code (number of symbols; 
	 * encoding length: max, min, avg; entropy; total encoding length;
	 * space saving),
	 */
	public void printStats()
	{
		int min, max, l, total, n;
		String s;
		DecimalFormatSymbols symb = new DecimalFormatSymbols();
		symb.setDecimalSeparator('.');
		DecimalFormat f = new DecimalFormat("#.00", symb);
		
		min = Integer.MAX_VALUE;
		max = 0;
		for (Entry<Character, String> entry : tableEncoding.entrySet()) {
	        s = entry.getValue();
	        l = s.length();
	        if (l > max)
	        	max = l;
	        if (l < min)
	        	min = l;
		}
		total = getTotalLength(tree, tableEncoding);
		n = tableEncoding.size();
		
		System.out.println("Number of symbols:       " + n);
		System.out.println("Min. encoding lenght:    " + min + " bits");
		System.out.println("Max. encoding lenght:    " + max + " bits");
		System.out.println("Avg. encoding lenght:    " + f.format(((double)total/(double)getMessageSize())) + " bits");
		System.out.println("Message entropy:         " + f.format(getMessageEntropy()) + " bits");
		System.out.println("Message encoding lenght: " + total + " bits");
		System.out.println("Encoded file size:       " + getEncodedFileSize()  + " bytes (" + (getHeaderSize() + " [header] + " + (getTotalLength()+7)/8) + " [message])");
		System.out.println("Gross space saving:      " + f.format((100-100*(double)((getTotalLength()+7)/8)/(double)getMessageSize())) + "%");
		System.out.println("Net space saving:        " + f.format((100-100*(double)getEncodedFileSize()/(double)getMessageSize())) + "%");
	}
	
	
	/**
	 * 
	 * @return the entropy (in bits) of the input message
	 */
	public double getMessageEntropy()
	{
		return getMessageEntropy(tree, getMessageSize())/Math.log(2); 
	}

	/**
	 * Recursively computes the entropy (in natural units) of the input message
	 * @param T is the huffman tree
	 * @param t is the total length of the message
	 * @return
	 */
	private double getMessageEntropy(HuffmanTree T, int t) {
		double e;		
		
		if (T.isLeaf()) {
			double p = (double)T.getFrequency()/(double)t;
			e = -p*Math.log(p);
		}
		else
			e = getMessageEntropy(T.left(), t) + getMessageEntropy(T.right(), t);
		
		return e;

	}


	/**
	 * 
	 * @return the size in bytes of the input message
	 */
	public int getMessageSize()
	{
		return tree.getFrequency(); // the root of the tree has the total count of symbols
	}
	
	
	/**
	 * Determines the size of the header with the information required to reconstruct the code.
	 * @return the size in bytes of the file header
	 */
	public int getHeaderSize() {
		return 2*BytesPerInt + tableEncoding.size()*(1+bytesRequired(tree));
		// 1 int for the number of symbols + 1 int for the number of bytes per frequency value +
		// + number of symbols * (1 byte for the symbol + required bytes per frequency).
	}


	/**
	 * 
	 * @return filesize in bytes including header
	 */
	public int getEncodedFileSize()
	{
		return (getHeaderSize() + (getTotalLength()+7)/8);
		// the size of the header + message encoding length rounded up in bytes
	}
	

	/**
	 * 
	 * @return the number of bits needed to encode the message
	 */
	public int getTotalLength()
	{
		return getTotalLength(tree, tableEncoding);
	}
		
	
	/**
	 * For printing the Huffman code
	 */
	public String toString()
	{
		String output;
		
		if (tree == null)
			output = "";
		else
			output = tree.toString() + "\n\n" + tableEncoding.toString();
		
		return output;
	}
	
	
	//------------------------------------------------------------------------------
	//
	// Private methods below
	//
	//------------------------------------------------------------------------------

	
	/**
	 * Generates the code
	 * @param frequencies is a map with the frequency of each symbol
	 */
	private void generate(Map<Character, Integer> frequencies) {
		PriorityQueue<HuffmanTree> treeList = new PriorityQueue<HuffmanTree>(frequencies.size());
		TreeSet<Character> elements = new TreeSet<Character>(frequencies.keySet());

		for (Character c : elements)
	        treeList.add(new HuffmanTree(c, frequencies.get(c)));

        mergeTrees(treeList);
        this.tree = treeList.element();
        
        this.tableEncoding = traceTable(tree);
	}

	private void mergeTrees(PriorityQueue<HuffmanTree> treeList) {
	    HuffmanTree left;
	    HuffmanTree right;
	    HuffmanTree merged;

        while (treeList.size() != 1){
            left = treeList.poll();
            right = treeList.poll();
            merged = new HuffmanTree(left,right);
            treeList.add(merged);
        }
    }


    private HashMap<Character, String> traceTable(HuffmanTree huffmanTree) {
    	HashMap<Character, String> encodings = new HashMap<>();
    	
    	splitOrMap(huffmanTree, encodings, "");
    	
    	return encodings;
    	
	}

    private void splitOrMap(HuffmanTree huffmanTree, HashMap<Character, String> encodings, String currentEncode) {
    	if(huffmanTree.isLeaf()) {
    		encodings.put(huffmanTree.getSymbol(), currentEncode);
    	}
    	else {
    		splitOrMap(huffmanTree.left(), encodings, currentEncode + '0');
    		splitOrMap(huffmanTree.right(), encodings, currentEncode + '1');
    	}
	}


	/**
	 * Reads bits until a symbol can be determined
	 * @param inFile is the input stream
	 * @param T is the huffman tree
	 * @return the symbol read (-1 if EOF)
	 * @throws IOException 
	 */
	private int getSymbol(FileInputStream inFile, HuffmanTree T) throws IOException {
		char symbol = traverseTree(inFile, T);

		return symbol;
	}



	private char traverseTree(FileInputStream inFile, HuffmanTree tree) throws IOException {
		boolean finishedTraversal = false;
		HuffmanTree currentTree = tree;
				
		while(!finishedTraversal) {
			prepareBuffer(inFile);
			currentTree = nextSubTree(currentTree);
			finishedTraversal = currentTree.isLeaf();
		}
		
		return currentTree.root().getSymbol();
	}


	private void prepareBuffer(FileInputStream inFile) throws IOException {		
		if(buffer.equals("")) {
			buffer = byteToBinary(inFile.read());
		}
	}


	private HuffmanTree nextSubTree(HuffmanTree current) {
		HuffmanTree nextTree;
		
		if(buffer.charAt(0) == '0') {
			nextTree = current.left();
		}else {
			nextTree = current.right();
		}
		buffer = buffer.substring(1);
		
		return nextTree;
	}


	/**
	 * Reads the header of the file and returns the frequencies of each char
	 * @param inFile is the input stream
	 * @return a map with the symbols and their frequencies
	 * @throws IOException 
	 */
	private Map<Character, Integer> readHeader(FileInputStream inFile) throws IOException {
		Map <Character, Integer> freq = new Hashtable<Character, Integer>();	
		int n = readInt(inFile, BytesPerInt);	// number of symbols in the header
		int b = readInt(inFile, BytesPerInt);	// number of bytes per frequency value
		char c;							// a symbol
		int f;							// its frequency
		
		for (int i=0; i<n; i++) {		
			c = (char) inFile.read();	// gets symbol
			f = readInt(inFile, b);		// gets frequency
			freq.put(c, f);				// adds it to the map
		}
		
		return freq;
	}


	/**
	 * Writes a header with information on the encoding (symbols and their frequencies)
	 * @param outStream is the output stream in which the header is written
	 * @throws IOException 
	 */
	private void writeHeader(FileOutputStream outStream) throws IOException {
		writeInt(outStream, tableEncoding.size(), BytesPerInt); 	// writes the number of symbols
		int b = bytesRequired(tree); 
		writeInt(outStream, b, BytesPerInt); 						// writes the number of bytes
																	// required per each frequency
		writeTree(outStream, tree, b); 								// writes the symbols and their frequencies
	}
	
	/**
	 * 
	 * @param T is the Huffman tree
	 * @return the number of bytes required to store frequency values
	 */
	private int bytesRequired(HuffmanTree T) {
		int i, max = maxFrequency(T);
		
		for (i=0; max>0; i++, max/= 256);
	
		return i;
	}
	
	private int maxFrequency(HuffmanTree T) {
		if (T.isLeaf())
			return T.getFrequency();
		else
			return Math.max(maxFrequency(T.left()), maxFrequency(T.right()));
	}

	
	/**
	 * Returns the number of bits needed to encode the message
	 * @param T
	 * @param table
	 * @return
	 */
	private int getTotalLength(HuffmanTree T, Map<Character, String> table) {
		int l;		
		
		if (T.isLeaf())
			l = T.getFrequency()*(table.get(T.getSymbol()).length());
		else
			l = getTotalLength(T.left(), table) + getTotalLength(T.right(), table);
		
		return l;
	}




	/**
	 * Writes recursively the tree (only the leaves)
	 * @param outStream is the output stream in which the info is written
	 * @param T is the huffman tree
	 * @param b is the number of bytes used to store each frequency value
	 * @throws IOException 
	 */

	private void writeTree(FileOutputStream outStream, HuffmanTree T, int b) throws IOException {
		if (T.isLeaf()) {
			outStream.write(T.getSymbol());				// writes symbol
			writeInt(outStream, T.getFrequency(), b);	// writes its frequency
		}
		else {
			writeTree(outStream, T.left(), b);
			writeTree(outStream, T.right(), b);
		}
		
	}

	/**
	 * Writes an int as b bytes (from LSB to MSB)
	 * @param outStream is the output stream
	 * @param v is the int to be written
	 * @param b 
	 * @throws IOException 
	 */
	private void writeInt(FileOutputStream outStream, int v, int b) throws IOException {
		for (int i=0; i<b; i++, v/=256) {
			outStream.write((char)(v%256));
		}
	}
	
	/**
	 * Reads an int as b bytes (from LSB to MSB)
	 * @param inFile is the input stream
	 * @param b is the number of bytes
	 * @return the int read
	 * @throws IOException 
	 */
	private int readInt(FileInputStream inFile, int b) throws IOException {
		int c, v, w;	// c is the byte read, w is its weight, and v is the int
		
		v = 0; w = 1;
		for (int i=0; i<b; i++, w*=256) {
			c = inFile.read();
			v += w*c;
		}
		return v;
	}

	/**
	 * Writes whatever bits may remain in the buffer
	 * @param outStream is the output stream
	 * @throws IOException 
	 */
	private void flushBuffer(FileOutputStream outStream) throws IOException {
		if (buffer.length()>0) {
			char c = binaryToChar (buffer);
			outStream.write(c);
		}
	}

	/**
	 * Clears the buffer
	 */
	private void clearBuffer() {
		buffer = "";
	}

	/**
	 * Writes an array of bits to the file. 
	 * Uses a buffer to write bits in blocks of 8 (i.e., bytes).
	 * @param outStream is the output stream
	 * @param string is the array of bits (represented as a string of "0"s and "1"s)
	 * @throws IOException
	 */
	private void writeBits(FileOutputStream outStream, String string) throws IOException {
		buffer = buffer + string;
		int n = buffer.length();

		while (n>8) {
			char c = binaryToChar (buffer.substring(0, 8));
			outStream.write(c);
			buffer = buffer.substring(8, n);
			n -= 8;
		}
	}

	/**
	 * Converts a string of "0"s and "1"s to its decimal value
	 * @param substring is a string of length <= 8 containing just 0s and 1s
	 * @return a char with the corresponding ascii code
	 */
	private char binaryToChar(String substring) {
		int b = 0;
		int n = substring.length();
		assert n <= 8 : "parameter of binaryToChar cannot be longer than 8 bits";
		
		for (int i=0; i<n; i++)
			if (substring.charAt(i) == '1')
				b = b + (1 << (8-i-1));
		
		return (char)b;
	}
	
	/**
	 * Returns a string of 0s and 1s corresponding to the byte read.
	 * @param c is an int between 0 and 255
	 * @return the binary representation of c
	 */
	@SuppressWarnings("unused")
	private String byteToBinary(int c) {
		assert (c>=0) && (c<=255) : "parameter of intToBinary must be in [0..255]";
		String b = "";
		
		for (int i=0; i<8; i++) {
			if (c%2 == 0)
				b = "0" + b;
			else
				b = "1" + b;
			c = c/2;
		}
		
		return b;

	}


}
