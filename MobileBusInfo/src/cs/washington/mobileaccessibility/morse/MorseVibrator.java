package cs.washington.mobileaccessibility.morse;


import android.os.Vibrator;
import android.util.Log;

import java.util.ArrayList;

/**
 * This class uses the vibrator to send simple things in
 * International morse code. It only knows the alphabet, digits,
 * and a few (about 18) symbols
 * 
 * @author Will
 *
 */
public class MorseVibrator {
	private Vibrator vibe;
	
	// the patterns for a, b, c, d, etc.  This should be length 26
	private static final String [] alpha_patterns =
	    {".-","-...","-.-.","-..",".","..-.","--.","....","..",".---","-.-",".-..","--","-.","---",
		 ".--.","--.-",".-.","...","-","..-","..._",".--","-..-","-.--","--.."};
	
	// the patterns for the symbols in the symbols array
	private static final String [] symbol_patterns =
	{".-.-.-","--..--","..--..",".----.","-.-.--","-..-.","-.--.","-.--.-",".-...","---...","-.-.-.",
		"-...-",".-.-.","-....-","..--.-",".-..-.","...-..-",".--.-."};
	
	// this array should have the same length as the previous one
	private static final char [] symbols = {'.',',','?','\'','!','/','(',')','&',':',';','=','+','-',
		'_','\"','$','@'};
	
	
	/*
	 * Return the string representation of a character,
	 * or the empty string if we were unable to process it
	 */
	private static String processChar(char c) {
		if(c >= 'a' && c <= 'z')
			return alpha_patterns[c - 'a'];
		if(c >= 'A' && c <= 'Z')
			return alpha_patterns[c - 'A'];
		if(c >= '0' && c <= '4') {
			char [] out = new char[5];
			for(int i = 0; i < 5; i++) {
				if(i >= c-'0')
					out[i] = '-';
				else
					out[i] = '.';
			}
			return new String(out);
		}
		if(c >= '5' && c <= '9') {
			char [] out = new char[5];
			for(int i = 0; i < 5; i++) {
				if(i >= c-'5')
					out[i] = '.';
				else
					out[i] = '-';
			}
			return new String(out);
		}
		for(int i = 0; i < symbols.length; i++) {
			if(symbols[i] == c)
				return symbol_patterns[i];
		}
		return "";
	}
	
	// convert a string of text to a pattern that the Android vibrator can take
	// @param unit_length the amount of time for the smallest unit of time (a dot, iirc)
	private static long [] toPattern(String in, int unit_length) {
		long current_delay = 0;
		String debug = "";
		ArrayList<Long> times = new ArrayList<Long>();
		for(int i = 0; i < in.length(); i++) {
			char c = in.charAt(i);
			if(c == ' ') {
				debug += ",";
				current_delay += 4*unit_length;
			}
			else {
				String pattern = processChar(c);
				if(pattern.length() > 0) {
					debug += " ";
					current_delay += 2*unit_length;
					for(int j = 0; j < pattern.length(); j++) {
						current_delay += unit_length;
						times.add(current_delay);
						current_delay = 0;
						char dD = pattern.charAt(j);
						debug += dD;
						if(dD == '-')
							current_delay = 3*unit_length;
						else if(dD == '.')
							current_delay = unit_length;
						else {
							Log.e("MorseConverter","Unknown dot-dash symbol: \'" + dD + "\'");
							current_delay = 3*unit_length;
						}
						times.add(current_delay);
						current_delay = 0;
						
					}
				}
			}
		}
		
		Log.v("MorseConverter","The converted string was \"" + debug + "\"");
		int size = times.size();
		long [] retval = new long[size];
		debug = "{";
		for(int i = 0; i < size; i++) {
			retval[i] = times.get(i);
			debug += retval[i] + ",";
		}
		debug += "}";
		Log.v("MorseConverter","The converted array was " + debug);
		return retval;
	}
												
	// Create a MorseVibrator using the given Vibrator
	public MorseVibrator(Vibrator vibe) {
		this.vibe = vibe;
	}
	
	/*
	 *  Given a string, convert it to Morse code and announce it
	 *  @param in the string to announce
	 *  @param timing the amount of time that the smallest unit (a dot)
	 *  	takes up
	 */
	public void vibrate(String in, int timing) {
		vibe.vibrate(toPattern(in,timing), -1);
	}

}
