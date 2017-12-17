package greedyAlgorithms;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class TestHuffman {

    /**
     * Metodo principal para realizar pruebas
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    public static void main(String args[]) throws FileNotFoundException, IOException {
        int test = (args.length == 0) ? 1 : Integer.parseInt(args[0]);    // by default Test1() is performed

        switch (test) {
            case 1:
                Test1();
                break;
            case 2:
                Test2();
                break;
            default:
                System.out.println("ERROR: wrong test (" + args[0] + "). Valid test values={1,2}.");
        }
    }


    /**
     * compares two files
     *
     * @param file1
     * @param file2
     * @throws IOException
     */
    private static boolean compareFiles(String file1, String file2) throws IOException {
        FileInputStream inFile1 = new FileInputStream(file1);
        FileInputStream inFile2 = new FileInputStream(file2);
        int c1, c2;

        c1 = inFile1.read();
        c2 = inFile2.read();
        while ((c1 == c2) && (c1 >= 0)) {
            c1 = inFile1.read();
            c2 = inFile2.read();
        }
        inFile1.close();
        inFile2.close();

        return (c1 == c2);
    }

    /**
     * Runs a simple test, encoding and decoding a string
     */
    private static void Test1() {
        HuffmanCode h = new HuffmanCode();
        //String rawText = "mi mama me mima y yo mimo a mi mama.";
        String rawText = "ABCDEFGHABBBBB";
        //String rawText = "she's got a smile that it seems to me reminds me of childhood memories where everything was as fresh as the bright blue sky";
        String encodedText = h.encode(rawText);

        System.out.println("Original string (length = " + rawText.length() + " bytes): " + rawText);
        System.out.println("Encoded string  (length = " + encodedText.length() + " bits): " + encodedText);
        h.printStats();
        System.out.println(h);
    }

    /**
     * Runs a simple test, encoding and decoding a file
     *
     * @throws IOException
     * @throws FileNotFoundException
     */
    private static void Test2() throws FileNotFoundException, IOException {
        HuffmanCode h = new HuffmanCode();
        h.encode("quijote.txt", "quijote-encoded.dat");
        h.printStats();
        h.decode("quijote-encoded.dat", "quijote-decoded.txt");
        if (compareFiles("quijote.txt", "quijote-decoded.txt"))
            System.out.println("OK: File is identical after decoding.");
        else
            System.out.println("ERROR: File is not identical after decoding.");

    }


}
