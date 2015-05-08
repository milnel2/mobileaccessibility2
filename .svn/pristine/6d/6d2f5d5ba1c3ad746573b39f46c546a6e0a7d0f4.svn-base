package cs.washington.mobileaccessibility.morse;

import android.content.Context;
import android.os.Vibrator;
import android.util.Log;

import java.util.ArrayList;

public class MorseVibrator {
	public Vibrator vibe;
	public static final String [] alpha_patterns =
	    {".-","-...","-.-.","-..",".","..-.","--.","....","..",".---","-.-",".-..","--","-.","---",
		 ".--.","--.-",".-.","...","-","..-","..._",".--","-..-","-.--","--.."};
	public static final String [] symbol_patterns =
	{".-.-.-","--..--","..--..",".----.","-.-.--","-..-.","-.--.","-.--.-",".-...","---...","-.-.-.",
		"-...-",".-.-.","-....-","..--.-",".-..-.","...-..-",".--.-."};
	public static final char [] symbols = {'.',',','?','\'','!','/','(',')','&',':',';','=','+','-',
		'_','\"','$','@'};
	
	
	/*
	 * return empty string if we can't handle it
	 */
	public static String processChar(char c) {
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
	
	public static long [] toPattern(String in, int unit_length) {
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
												
	
	public MorseVibrator(Context context) {
		vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		
	}
	
	public void vibrate(String in, int timing) {
		vibe.vibrate(toPattern(in,timing), -1);
	}

}
