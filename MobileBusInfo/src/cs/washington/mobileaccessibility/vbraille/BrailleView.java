package cs.washington.mobileaccessibility.vbraille;

import android.content.Context;
import android.os.Vibrator;
import android.view.MotionEvent;
import android.view.View;


/**
 * An overlay which triggers the vibrator in response to
 * the user touching the screen, in order to send braille information.
 * 
 * Currently this just sends one character at a time
 * 
 * @author Will
 *
 */
public class BrailleView extends View {
	

	
	public BrailleView(Context context, Vibrator vibe) {
		super(context);
		this.vibrator = vibe;
		// vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
		// it's not clear what happens when everyone individually tries to get the vibrator
		
	    setClickable(true);
	    setFocusable(true);
	    setFocusableInTouchMode(true);
	    requestFocus();
	}
	
	// a variable that keeps track of what the vibrator is doing
	// 0 = turned off
	// 1 = the corner pattern (a pulsating pattern)
	// -1 = the edge pattern (a constant vibration)
	private int currentVibration = 0;
	
	// the one we are using
	private Vibrator vibrator;
	
	 // the index of which symbol in the dots array we are using
	private int whichSymbol;
	
	// an array with all the braille representations of the
	// things in the symbols array
	private static final String [] dots = 
	{"001011","000000","001101","001000","001010","001100",
		// letters
		"100000","101000","110000","110100","100100","111000","111100","101100",
		"011000","011100","100010","101010","110010","110110","100110","111010",
		"111110","101110","011010","011110","100011","101011","011101","110011",
		"110111","100111",
		// numbers
		"100000","101000","110000","110100","100100","111000","111100","101100",
		"011000","011100",
		// control characters
		"000001","010111",
		// grade two 'and'
		"111011"
	};
	
	// an array with all the symbols we are able to display.
	// Upper case is not included though
	private static final String [] symbols =
	{"?"," ",".",",",";",":","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
		"p","q","r","s","t","u","v","w","x","y","z","1","2","3","4","5","6","7","8","9","0",
		"+","\\",
		"&"};
	
	// TODO think carefully about what should happen if they scroll through some characters
	// while touching the screen simultaneously
	/*
	 * Set the character to display
	 */
	public void setCharacter(String character) {
		for(int i = 0; i < symbols.length; i++) {
			// TODO a lot of work needs to be done here
			if(symbols[i].equalsIgnoreCase(character)) {
				whichSymbol = i;
				return;
			}
		}
		whichSymbol = 0; // question mark
	}
	
	// the two vibration patterns we use
	private static final long [] patternCorner = {0,40,40};
	private static final long [] patternMiddle = {0,1000};
	
	// turn on the vibrator to the desired value
	// Since we don't want it to restart its pattern, the
	// variable currentVibration is used to keep track of what
	// was previously happening, to see whether anything
	// needs to be done
	private void setVibration(int value) {
		if(value != currentVibration) {
			switch(value) {
			case 1:
				vibrator.vibrate(patternCorner,0);
				break;
			case -1:
				vibrator.vibrate(patternMiddle,0);
				break;
			default:
				vibrator.cancel();
				
			}
			currentVibration = value;
		}
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		switch(me.getAction()) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_MOVE:
			double x = me.getX();
			double y = me.getY();
			x/=getWidth();
			y/=getHeight();
			int doit = 0;
			
			// run through the six cells and see if one
			// contains (x,y) and is enabled
			int i = (int) (2*x);
			if (i > 1)
				i = 1;
			int j = (int) (3*y);
			if (j > 2)
				j = 2;
			if(dots[whichSymbol].charAt(i+2*j)=='1')
				doit = (j == 1)?(-1):(1);
			setVibration(doit);
			break;
		default: // i.e., MotionEvent.ACTION_UP
			setVibration(0);
		}
		return true;
	}

}
