package edu.washington.cs.hangman;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.util.HashSet;

import android.content.Context;
import android.content.res.Resources.NotFoundException;

import junit.framework.Assert;

public class BrailleTable {	
	public static final char INVALID_CHAR = 0xf8; 	// small sigma with a slash through it for an 
													// invalid character.
	private static final char TOTAL_DOTS = 6;	// Total number of dots in a Braille cell.
	private static final char DELIM = ',';
	private static final char DOT_ON = '1';
	private static final char DOT_OFF = '0';
	
	// Special characters
	public static final String BACKSPACE = "&#008;";
	
	private static interface Node {	}
	private static class InternalNode implements Node {
		public Node _dotOn;
		public Node _dotOff;
	}

	private static class LeafNode implements Node {
		LeafNode(char ch) { _ch = ch; }
		public char _ch;	// Represent char as a string b/c some
		// Braille characters don't correspond to English characters.
	}

	private InternalNode _root;

	// A 2D array to store sets of inputs with the same number of dots in each column. It's important
	// to keep track of these sets because inputs within each set
	// can be mixed up if the user moves his/her hand around.
	// The indeces of the set represent the number of dots in the first column (row) and second column 
	// (col) of the Braille cell. We need a row and a colunn for 0 dots too, so we have 0, 1, 2, or 3 dots
	// in each column.
	private HashSet<String>[][] _inputClasses = new HashSet[TOTAL_DOTS/2 + 1][TOTAL_DOTS/2 + 1];
	
	/**
	 * Create the Braille character tree.
	 * @param context
	 */
	public BrailleTable(Context context, int resId) {
		_root = new InternalNode();	
		for(int i = 0; i < _inputClasses.length; ++i) {
			for(int j = 0; j < _inputClasses[i].length; ++j) {
				_inputClasses[i][j] = new HashSet<String>();
			}
		}
		buildTree(context, resId);
	}

	private boolean buildTree(Context context, int resId) {
		InputStream instream;
		try {
			// open the file for reading
			instream = context.getResources().openRawResource(resId);
		} catch (NotFoundException e) {
			e.printStackTrace();
			return false;
		}
		try {
			// if file the available for reading
			if (instream != null) {
				// prepare the file for reading
				InputStreamReader inputreader = new InputStreamReader(instream);
				BufferedReader buffreader = new BufferedReader(inputreader);

				String line;
				// read every line of the file into the line-variable, on line at the time
				while ((line = buffreader.readLine()) != null) {
					int delimInd = line.lastIndexOf(DELIM);
					Assert.assertTrue(delimInd != -1);

					char chInput = line.charAt(0);
					String strDots = line.substring(delimInd + 1);
					Assert.assertTrue(TOTAL_DOTS == strDots.length());
					
					addToTree(chInput, strDots);
				}
			}

			// close the file again
			instream.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		} 

		return true;
	}
	
	private void addToTree(char chInput, String strDots) {	
		// Create the tree of Braille letters using the letter table. 
		InternalNode curNode = _root;
		for(int j = 0; j < strDots.length() - 1; j++) {
			if(isDotOn(strDots.charAt(j))) {
				if(curNode._dotOn == null) {
					curNode._dotOn = new InternalNode();
				}
				curNode = (InternalNode)curNode._dotOn;
			} else {
				if(curNode._dotOff == null) {
					curNode._dotOff = new InternalNode();
				}
				curNode = (InternalNode)curNode._dotOff;
			}
		}

		// Now we should be at the right leaf. Add the letter itself.                                                                                                                                                                                      
		if(isDotOn(strDots.charAt(strDots.length() - 1))) {
			// NOTE: we can just set dotOff or dotOn to the character itself.                                                                                                                                                                              
			// This is more traditional though.                                                                                                                                                                                                            
			curNode._dotOn = new LeafNode(chInput);
		} else {
			curNode._dotOff = new LeafNode(chInput);
		}
	}

	private static boolean isDotOn(char c) {
		return (c == DOT_ON);
	}

	public char decode(boolean[] pnts) {
		Assert.assertEquals(TOTAL_DOTS, pnts.length);
		
		Node curNode = _root;
		int i = 0;	
		for(; i < TOTAL_DOTS; i++) { 
			boolean isDotOn = pnts[i];
			if(isDotOn) {
				if(curNode == null) {
					// log.error("unkown character: " + dots);
					return INVALID_CHAR;
				} else if(curNode.getClass() == InternalNode.class) {
					curNode = ((InternalNode)curNode)._dotOn;
				} else {
					// error!
					Assert.fail("node in braille tree is a leaf when it should be internal!");
				}
			} else {
				if(curNode == null) {
					// log.error("unknow character: " + dots);
					return INVALID_CHAR;
				} else if(curNode.getClass() == InternalNode.class) {
					curNode = ((InternalNode)curNode)._dotOff;
				} else {
					// error!
					Assert.fail("node in braille tree is a leaf when it should be internal!");
				}
			}
		}
		Assert.assertEquals(TOTAL_DOTS, i);
		if(curNode == null) {
			// log.error("unknown character: " + dots);
			return INVALID_CHAR;
		}
		if(curNode.getClass() == LeafNode.class) {
			return ((LeafNode)curNode)._ch;
		}

		// Return an invalid character
		return INVALID_CHAR;
	}

}
