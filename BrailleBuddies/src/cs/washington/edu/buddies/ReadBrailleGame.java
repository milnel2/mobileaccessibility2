package cs.washington.edu.buddies;

/* Updates:
 * 2/14/10, jrh:	added speakStatistics() method and code to handle
 * 						the Back key being pressed so that statistics
 * 						are spoken when leaving the game.
 * 2/14/10, jrh:	added long press processing to speak instructions
 * 						and added initial instructions on entering game.
 * 2/14/10, jrh:	stopped tts when keypad is displayed
 * 2/14/10, jrh:	Added onUtteranceCompleted interface to make sure statistics
 * 						information finishes before returning.
 * 2/15/10, JRH: 	Added launchMode setting to manifest for this activity.
 * 2/15/10, jrh:  fixed speakStatistics() method and added mode setting to initial spoken info
 * 2/16/10, jrh:	changed spelling of "one" to "won" in speakStatistics() so it is
 * 					pronounced correctly
 * 2/16/10, jrh:	changed long press duration to 2.2 seconds
 * 2/16/10, jrh:	added tts.speak call when in numbers mode to say when second
 * 					character of symbol is being displayed.
 * 2/22/10, jrh:  changed speakStatistics() method so that it doesn't speak statistics
 * 				 	for modes where user exits instead of giving answer.
 * 2/23/10, jrh:	changed speakInstructions() to use synthesized files rather than
 * 					  synthesizing text real-time.
 */

import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

public class ReadBrailleGame extends Activity 
	implements TextToSpeech.OnUtteranceCompletedListener {
	
	protected static final int INPUT_ANSWER = 10;
	public static final int RESULT_CORRECT_ANSWER = RESULT_FIRST_USER + 1;

	
	private static final String TAG = "ReadBrailLearn";

    private static final String[] alphadots =
    {"100000","101000","110000","110100","100100","111000","111100","101100",
    	"011000","011100","100010","101010","110010","110110","100110","111010",
    	"111110","101110","011010","011110","100011","101011","011101","110011",
    	"110111","100111"};
    public static final String[] numberdots =
    {"010111","011100","100000","101000","110000","110100","100100","111000",
    	"111100","101100","011000"};
    public static final String[] punctdots =
    {"001110","000010","001000","000011","001101","000001","001011",
    	"000111","001010","001111"};
    public static final String[] alphabet =
    {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
    	"p","q","r","s","t","u","v","w","x","y","z"};
    public static final String[] numbers = 
    {"number sign", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"};
    public static final String[] punctuation =
    {"exclamation point", "apostrophe", "comma", "hyphen", "period or full stop", 
    	"capital", "open quote or question mark",
    	"close quote", "semicolon", "bracket or parenthesis"};
    public static final String[] punctSymbols =
    {"!", "' (apostrophe)", ", (comma)", "-", ".","cap","open ' or ?","close '", ";", "[ ] or ( )"};
    public static final String[] nato =
    {"alpha","bravo","charlie","delta","echo","foxtrot","golf","hotel","india","juliet","kilo",
    	"lima","mike","november","oscar","papa","quebec","romeo","sierra","tango","uniform",
    	"victor","whiskey","ex ray","yankee","zoo loo"}; 
    private static final String[] modes = {"alphabet", "numbers", "punctuation"};
    private static final String[] regions = {"1", "4", "2", "5", "3", "6"};
    
    private ReadBrailleView rbview;
    private TextToSpeech tts;
    HashMap<String, String> utterance = new HashMap<String, String>();
    
    private String rbInstr = "How to play in Reed mode:" +
			" The touchscreen is divided into six regions to represent the Braille cell," +
			" Touch a region and its number will be spoken," +
			" Any region that contains a dot will vibrate when touched," +
			" Depending on what mode you have selected, the dots contained" +
				" within the screen will represent a letter, number, or punctuation mark," +
			" when you have determined what symbol is being displayed, " +
				"either press down on the trackball or on the center button of the directional pad," +
			" a touchscreen kee pad for the current mode will be displayed," +
			" You can navigate the kee pad by either touching and dragging on the screen"  +
				" or by using the trackball or directional keys," +
			" the symbols on the keys will be spoken when navigated to," +
			" two special keys are also displayed," +
			" one will play the correct answer," +
			" the other will return to the display without requiring an answer," +
			" when you hear the key you want, press down again on the trackball or center button of the directional pad," +
			" you will return to the vibrating Braille screen and feedback will be given" +
				" to let you know whether your answer is correct," +
			" you can also enter your answer while on the touch kee pad by using the hard keys instead," +
			" for correct answers, the symbol is replayed before a new symbol is selected and played," +
			" if you do not wish to have correct answers replayed, you can turn off the replay setting on the Settings menu," +
			" for incorrect answers, the symbol will be replayed if you have not exceeded " + 
				"the maximum number of tries and you can try again," +
			" Otherwise the correct answer will be spoken," +
			" You can leave the game at any time by pressing the back button," +
			" You can change modes among alphabetic, numeric, and punctuation by pressing the menu key," +
			" End of instructions.";
    
//    private String[] rbInstr = {"How to play in Reed mode:",
//			"The touchscreen is divided into six regions to represent the Braille cell,",
//			"Touch a region and its number will be spoken,",
//			"Any region that contains a dot will vibrate when touched,",
//			"Depending on what mode you have selected, the dots contained" +
//				" within the screen will represent a letter, number, or punctuation mark;",
//			"when you have determined what symbol is being displayed, " +
//				"either press down on the trackball or on the center button of the directional pad,",
//			"a touchscreen kee pad for the current mode will be displayed,",
//			"You can navigate the kee pad by either touching and dragging on the screen" +
//				" or by using the trackball or directional keys;",
//			"the symbols on the keys will be spoken when navigated to;",
//			"two special keys are also displayed,",
//			"one will play the correct answer,",
//			"the other will return to the display without requiring an answer;",
//			"when you hear the key you want, press down again on the trackball or center button of the directional pad;",
//			"you will return to the vibrating Braille screen and feedback will be given" +
//				" to let you know whether your answer is correct;",
//			"you can also enter your answer while on the touch kee pad by using the hard keys instead;",
//			"for correct answers, the symbol is replayed before a new symbol is selected and played;",
//			"if you do not wish to have correct answers replayed, you can turn off the replay setting on the Settings menu;",
//			"for incorrect answers, the symbol will be replayed if you have not exceeded " + 
//				"the maximum number of tries and you can try again,",
//			"Otherwise the correct answer will be spoken,",
//			"You can leave the game at any time by pressing the back button,",
//			"You can change modes among alphabetic, numeric, and punctuation by pressing the menu key;",
//			"End of instructions."};
	
    private int mode;
    private int prevMode;
    private int vibrating = 0;
    private Vibrator vibe;
    private int whichSymbol;
    private Random rand;
    private int[] usedSymbols; 
    private int usedCount;
    private int modeLength;
    private int lastRegion;
    private boolean showNumSign;
    private String mSymbol;
    private String lastSymbolDisplayed; 
	private int[] numWrong = {0,0,0};
	private int[] numCorrect = {0,0,0};
	private int[] numDisplayed = {0,0,0};
	private int[] numGivenAnswer = {0,0,0};
	private int earned;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // mode: 0 = alphabet, 1 = numbers, 2 = punctuation
    	mode = Settings.getMode(this);   
    	prevMode = mode;
        tts = Game.tts;
        tts.speak("you are in " + modes[mode] + " mode, " +
        		"Touch screen to begin game, at any time " +
        		"press on screen for 3 seconds to hear instructions, "  +
        		"touch screen again in a different location to interrupt instructions",
        		TextToSpeech.QUEUE_FLUSH, null);
        
        rbview = new ReadBrailleView(this);
    	mSymbol = "000000";
    	lastSymbolDisplayed = "000000";
    	showNumSign = false;
    	earned = 0;
    	
//    	String filenm;
//    	Resources resrcs = getResources();
//    	String pkg = getPackageName();
//    	int id;
//        for (int i = 0; i < rbInstr.length; i++) {
//        	filenm = pkg + ":raw/rbinstr" + i;
//        	id = resrcs.getIdentifier(filenm, null, null);
//        	if (id != 0)
//        		tts.addSpeech(rbInstr[i], pkg, id);
//        }
    	
    	whichSymbol = -1;
    	rand = new Random();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(rbview);
    	randomizeSymbol();
    }
    
    /* Mark current symbol as used and choose a
     * new symbol.
     */
    private void randomizeSymbol() {
    	
    	// Get current mode setting
    	mode = Settings.getMode(this);
    	
    	// If mode was just reset
    	if (mode != prevMode) {
    		prevMode = mode;
    		Log.v(TAG, "Mode set to " + mode);
    		resetUsedArray();
    	}  
    	// If all symbols have been used, reset
    	else if (++usedCount >= modeLength) {
    			resetUsedArray();
    	}
    	
    	showNumSign = false;
    	// Get next symbol
    	do {
    		whichSymbol = rand.nextInt(modeLength);
    	} while(usedSymbols[whichSymbol] != 0);
    	switch (mode) {
    	case Settings.ALPHABET: 
    		mSymbol = alphadots[whichSymbol];
        	Log.v(TAG,"Selected symbol " + alphabet[whichSymbol] + 
        			" which is " + alphadots[whichSymbol]);
    		break;
    	case Settings.NUMBERS: 
    		showNumSign = true;
    		mSymbol = numberdots[whichSymbol];
        	Log.v(TAG,"Selected symbol " + numbers[whichSymbol] + 
        			" which is " + numberdots[whichSymbol]);
    		break;
    	case Settings.PUNCTUATION:
    		mSymbol = punctdots[whichSymbol];
        	Log.v(TAG,"Selected symbol " + punctuation[whichSymbol] + 
        			" which is " + punctdots[whichSymbol]);
    		break;
    	default:
    		Log.e(TAG, "unrecognized mode " + mode);
        	Log.v(TAG,"Selected symbol " + alphabet[whichSymbol] + 
        			" which is " + alphadots[whichSymbol]);
    		tts.speak("unrecognized mode, switching to alphabet mode", TextToSpeech.QUEUE_FLUSH, null);
    		modeLength = alphadots.length;
    		mode = 0;
    		mSymbol = alphadots[whichSymbol];
    	}
    	rbview.invalidate();
    	tts.speak("Ready", TextToSpeech.QUEUE_ADD, null);
    } 
    
    /* Creates and initializes array to mark used symbols
     * each time the mode is changed.
     */
    private void resetUsedArray() {
    	Log.v(TAG, "resetUsedArray");
    	switch (mode) {
    	case Settings.ALPHABET:
    		modeLength = alphadots.length;
    		break;
    	case Settings.NUMBERS:
    		modeLength = numberdots.length;
    		break;
    	case Settings.PUNCTUATION:
    		modeLength = punctdots.length;
    		break;
    	default:
    		Log.e(TAG, "unrecognized mode " + mode);
    		tts.speak("unrecognized mode, switching to alphabet mode", 
    				TextToSpeech.QUEUE_FLUSH, null);
    		modeLength = alphadots.length;
    		mode = Settings.ALPHABET;
    	}
    	usedCount = 0;
    	usedSymbols = new int[modeLength];
    	for (int i = 0; i < modeLength; i++) {
    		usedSymbols[i] = 0;
    	}
    	// If in NUMBER mode, mark the number sign as used so it 
    	// won't be selected for display
    	if (mode == Settings.NUMBERS) {
    		usedSymbols[0] = 1;
    	}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
    	// change to display settings menu  
    	if(keyCode == KeyEvent.KEYCODE_MENU){
    		prevMode = mode;
    		mode = (mode + 1 >= modes.length || mode < 0) ? 0 : mode + 1;
    		Settings.setMode(this, mode);
    		
    		tts.speak("change mode to " + modes[mode], 0, null);
    		randomizeSymbol();
    	}
    	else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
    		// Center key pushed after symbol was replayed
    		if (usedSymbols[whichSymbol] == -1) {
    	    	tts.speak("Selecting next symbol", TextToSpeech.QUEUE_ADD, null);
    			randomizeSymbol();
    		} 
    		// Center key pushed so player can give input
    		else if (!showNumSign){
        		displayKeypad();
    		}
    		else {
    			rbview.invalidate();
    			tts.speak("Displaying second character of number", TextToSpeech.QUEUE_ADD, null);
    			showNumSign = false;
    		}
    	}
    	else if(keyCode == KeyEvent.KEYCODE_BACK) {
    		tts.speak("Back kee pressed,", TextToSpeech.QUEUE_FLUSH, null);
    		speakStatistics();
    	}
    	else
    		return super.onKeyDown(keyCode, keyEvent);
    	Log.v(TAG, "onKeyDown");
    	return true;
    }
    
    protected void displayKeypad() {
    	tts.stop();
    	if (!mSymbol.equals(lastSymbolDisplayed)) {
			lastSymbolDisplayed = mSymbol;
			numDisplayed[mode]++;
		}
    	Intent input = new Intent(this, Keypad.class);
    	input.putExtra("mode", mode);
       	startActivityForResult(input, INPUT_ANSWER);
    	Log.v(TAG, "displayKeypad - activity started");
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.v(TAG, "onActivityResult; requestCode = " + requestCode + ", resultCode = " + resultCode);
    	if (requestCode == INPUT_ANSWER) {
    		if (resultCode == RESULT_CANCELED){
    			inputCancelled();
    		}
    		else if (resultCode == RESULT_OK){
        		String answer = data.getStringExtra("answer");
    			evaluateAnswer(answer);
    		}
    		else if (resultCode == RESULT_CORRECT_ANSWER) {
    			numGivenAnswer[mode]++;
    			speakCorrectAnswer();
    		}
    	}
    	else {
    		super.onActivityResult(requestCode, resultCode, data);
    	}
    }
    	    
    protected void inputCancelled() {
    	Log.v(TAG, "inputCancelled");
    	tts.speak("Replaying symbol", TextToSpeech.QUEUE_ADD, null);
    } 
 
    protected void evaluateAnswer(String answer) {
    	Log.v(TAG, "evaluateAnswer, answer = " + answer);
  		usedSymbols[whichSymbol] += 1;
  		boolean isCorrect = false;
    	switch (mode) {
    	case Settings.ALPHABET: 
    		if (answer.equalsIgnoreCase(alphabet[whichSymbol]))
    			isCorrect = true;
    		break;
    	case Settings.NUMBERS: 
    		if (answer.equalsIgnoreCase(numbers[whichSymbol]))
    			isCorrect = true;
    		break;
    	case Settings.PUNCTUATION: 
    		if (answer.equalsIgnoreCase(punctSymbols[whichSymbol]))
    			isCorrect = true;
    		break;
    	default:
    		Log.e(TAG, "unrecognized mode " + mode);
    	}
  		if (isCorrect) {
  			tts.speak("That is correct!", TextToSpeech.QUEUE_ADD, null);
  			numCorrect[mode]++;
  			BankAccount.addToBalance(this, BankAccount.PER_SYMBOL);
  			earned += BankAccount.PER_SYMBOL;
  			checkReplay();
  		}
  		else {
  			tts.speak("Sorry!", TextToSpeech.QUEUE_ADD, null);
  			numWrong[mode]++;
  			
  			int mMaxTries = Settings.getMaxTries(this);
  			
  			if (mMaxTries <= usedSymbols[whichSymbol]) {
  				numGivenAnswer[mode]++;
  				speakCorrectAnswer();
  			}
  			else {
  				tts.speak("Try again!", TextToSpeech.QUEUE_ADD, null);
  			}
  		}
    }
    
    protected void speakCorrectAnswer() {
    	Log.v(TAG, "speakCorrectAnswer");
    	switch (mode) {
    	case Settings.ALPHABET: 
    		tts.speak("Answer is " + alphabet[whichSymbol] + ", as in " + nato[whichSymbol], TextToSpeech.QUEUE_ADD, null);
    		break;
    	case Settings.NUMBERS: 
    		tts.speak("Answer is number " + numbers[whichSymbol], TextToSpeech.QUEUE_ADD, null);
    		break;
    	case Settings.PUNCTUATION: 
    		tts.speak("Answer is " + punctuation[whichSymbol], TextToSpeech.QUEUE_ADD, null);
    		break;
    	default:
    		Log.e(TAG, "unrecognized mode " + mode);
    	}
    	checkReplay();
    }
    
    /* Checks to see whether Braille symbol should be replayed
     * after correct answer given
     */
    private void checkReplay() {
    	Log.v(TAG, "checkReplay");
    	usedSymbols[whichSymbol] = -1;
    	if (!Settings.getReplay(this))
    		randomizeSymbol();  	
    	
    	else {
    		tts.speak("Replaying symbol", TextToSpeech.QUEUE_ADD, null);
	    	switch (mode) {
	    	case Settings.ALPHABET:
	    		tts.speak(alphabet[whichSymbol] + ", as in " + nato[whichSymbol], TextToSpeech.QUEUE_ADD, null);
	    		break;
	    	case Settings.NUMBERS: 
	    		tts.speak("Number " + numbers[whichSymbol], TextToSpeech.QUEUE_ADD, null);
	    		break;
	    	case Settings.PUNCTUATION:
	    		tts.speak(punctuation[whichSymbol], TextToSpeech.QUEUE_ADD, null);
	    		break;
	    	default:
	    		Log.e(TAG, "unrecognized mode " + mode);
	    	}
    	}
    }

	/* Called when activity is paused -- when 
	 * phone call arrives.
	 */
	@Override
	public void onPause() {
		if (tts != null) {
			tts.stop();
		}
		// save values here or in destroy?  If here, reinit vars in onResume
		super.onPause();
	}
	
	/* Tells player how (s)he did overall */
	private void speakStatistics() {
		String s = "Game statistics: ";
		for (int i = 0; i < modes.length; i++) {
			if (numDisplayed[i] == 0 || (numCorrect[i] == 0 && numWrong[i] == 0 && numGivenAnswer[i] == 0))
				continue;
			
			if (numDisplayed[i] == 1) {
				String m = (i == Settings.ALPHABET) ? "letter" : (i == Settings.NUMBERS) ? "number" : "punctuation symbol";
				s = s + "There was won " + m + " attempted, and ";
			}
			else {
				String m = (i == Settings.ALPHABET) ? "letters" : (i == Settings.NUMBERS) ? "numbers" : "punctuation symbols";
				s = s + "There were " + numDisplayed[i] + " " + m + " attempted, and ";
			}
	
			if (numCorrect[i] == 1)
				s = s + "You gave won correct answer, ";
			else
				s = s + "You gave " + numCorrect[i] + " correct answers, ";
		}
		if (earned > 0)
			s = s + "You earned " + earned + " tokens!";
		if (!s.equalsIgnoreCase("Game statistics: ")) {
			tts.setOnUtteranceCompletedListener(this);
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "rbstats");
			tts.speak(s, TextToSpeech.QUEUE_ADD, utterance);
		}
		else
			finish();
	}
	
	@Override
	public void onUtteranceCompleted(String utteranceId) {
		if (utteranceId.equalsIgnoreCase("rbstats") || utteranceId == "rbstats") {
			finish();
		}
		else if (utteranceId.equalsIgnoreCase("instructions")) {
			tts.speak(rbInstr, TextToSpeech.QUEUE_ADD, null);
//			for (int i = 0; i < rbInstr.length; i++) {
//		    	tts.speak(rbInstr[i], TextToSpeech.QUEUE_ADD, null);
//		    }
		}
	}
	
	/* Speak full instructions for the game */
	private void speakInstructions() {
		tts.setOnUtteranceCompletedListener(this);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "instructions");
		tts.speak("the mode is set to " + modes[mode] + 
				" one moment while I retrieve instructions",
				TextToSpeech.QUEUE_FLUSH, utterance);
	}
	
    protected class ReadBrailleView extends View {
 
        private final long [] patternCorner = {40,40};
        private final long [] patternMiddle = {0,1000};
        private final Paint mPaint;    
        private int xBound;
        private int y1Bound;
        private int y2Bound;
        private int radius;
        private long longPressStartTm;
        private long longPressDuration;
       
        public ReadBrailleView(Context context) {
        	super(context);
        	longPressDuration = 3000;
        	lastRegion = -1;
        	vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        	mPaint = new Paint();
        	mPaint.setColor(Color.WHITE);
        	setBackgroundColor(Color.BLACK);
        	setClickable(true);
        } 
        
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        	xBound = (int) w / 2;
        	y1Bound = (int) h / 3;
        	y2Bound = y1Bound * 2;
        	radius = Math.min(xBound, y1Bound)/4;
        	super.onSizeChanged(w, h, oldw, oldh);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
        	float x, y;
        	int xUnit = xBound/2;
        	int yUnit = y1Bound/2;

        	String symbol = mSymbol;
    		if (showNumSign)
    			symbol = numberdots[0];
        	for (int i = 0; i < 6; i++) {
        		if (symbol.charAt(i) == '1') {
        			x = (i % 2 == 0) ? xUnit : xUnit + xBound;
        			y = (i < 2) ? yUnit : (i < 4) ? yUnit + y1Bound : yUnit + y2Bound;
        			canvas.drawCircle(x, y, radius, mPaint);
        		}
        	}
        }
        
        private void setVibration(int value) {
        	if(value != vibrating) {
        		switch(value) {
        		case 1:
        			Log.i(TAG,"Doing corner pattern");
        			vibe.vibrate(patternCorner,0);
        			break;
        		case -1:
        			Log.i(TAG,"Doing middle pattern");
        			vibe.vibrate(patternMiddle,0);
        			break;
        		default:
        			vibe.cancel();
        		}
        		vibrating = value;
        	}
        }
        

        @Override
        public boolean onTouchEvent(MotionEvent me) {
        	Log.v(TAG, "onTouchEvent");
        	switch(me.getAction()) {
        	case MotionEvent.ACTION_DOWN:
        		longPressStartTm = me.getDownTime();
        	case MotionEvent.ACTION_MOVE:
        		String symbol = mSymbol;
        		if (showNumSign)
        			symbol = numberdots[0];
        		double x = me.getX();
        		double y = me.getY();
        		x/=getWidth();
        		y/=getHeight();
        		double xx = me.getRawX();
        		double yy = me.getRawY();
        		int loc = (xx < xBound) ? 0 : 1;
        		loc = (yy < y1Bound) ? loc : (yy < y2Bound) ? loc + 2 : loc + 4;
        		if (loc != lastRegion) {
        			tts.speak(regions[loc], TextToSpeech.QUEUE_FLUSH, null);
        			lastRegion = loc;
        			longPressStartTm = me.getEventTime();
        		}
        		int doit = 0;
        		for(int i = 0; i < 2; i++) {
        			for(int j = 0; j < 3; j++) {
        		    	if(symbol.charAt(i+2*j)=='0')
        		    		continue;
        				if(((int) (2*x)) == i && ((int) (3*y)) == j)
        					doit = (j==1)?(-1):(1);
        			}
        		}
        		setVibration(doit);
        		
        		// Capture long presses that occur within a single region and
        		// repeat the symbol that the user should enter.
        		if (me.getEventTime() - longPressStartTm > longPressDuration) {
        			speakInstructions();
        			longPressStartTm = me.getEventTime();
        		}
        		break;
        	default:
        		setVibration(0);
        	}
        	return true;
        } 
    }
}
