package cs.washington.edu.buddies;

import java.util.HashMap;
import java.util.List;
import java.util.Random;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/* Updates:
 * 2/14/10, jrh:	added speakStatistics() method and code to handle
 * 						the Back key being pressed so that statistics
 * 						are spoken when leaving the game.
 * 2/14/10, jrh:	added long press processing to speak instructions
 * 						and added initial instructions on entering game.
 * 2/14/10, jrh:	Added onUtteranceCompleted interface to make sure statistics
 * 						information finishes before returning.
 * 2/15/10, JRH: 	Added launchMode setting to manifest for this activity.
 * 2/15/10, jrh:  fixed speakStatistics() method and added mode setting to initial spoken info
 * 2/16/10, jrh:	changed spelling of "one" to "won" in speakStatistics() so it is
 * 					pronounced correctly
 * 2/22/10, jrh:  changed speakStatistics() method so that it doesn't speak statistics
 * 				 	for modes where user exits instead of giving answer.
 * 2/23/10, jrh:	changed speakInstructions() to use synthesized files rather than
 * 					  synthesizing text real-time.
 */

public class WriteBrailleGame extends Activity
	implements TextToSpeech.OnUtteranceCompletedListener {
	
	protected static final int INPUT_ANSWER = 10;
	public static final int RESULT_CORRECT_ANSWER = RESULT_FIRST_USER + 1;
	public static final int ALPHABET = 0;
	public static final int NUMBERS = 1;
	public static final int PUNCTUATION = 2;
	
	private static final String TAG = "WriteBrailLearn";

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
    	"victor","whiskey","ex-ray","yankee","zoo loo"}; 
    private static final String[] modes = {"alphabet", "numbers", "punctuation"};
    private static final String[] regions = {"1", "4", "2", "5", "3", "6"};
   
    private WriteBrailleView wbview;
    private WriteBrailleGame self;
    private TextToSpeech tts;
    HashMap<String, String> utterance = new HashMap<String, String>();
    
//	private String[] wbInstr = {"How to play in write mode:",
//			"The touchscreen is divided into six regions to represent the Braille cell,",
//			"Touch a region and its number will be spoken,",
//			"The game will speak a symbol for you to enter,",
//			"To enter the dots of the symbol, navigate by touch to the region where a dot should be placed and double tap to add a dot,",
//			"Any region that contains a dot will vibrate when touched,",
//			"double tap on a vibrating region to remove the dot,",
//			"to remove all dots at once, shake the phone in a diagonal corner to corner direction,",
//			"Once you are finished entering dots, press the enter key to confirm your answer,",
//			"Depending on your phone, the enter key is either " + 
//				"the center button of the directional pad or the down movement on the trackball,",
//			"Feedback is given to let you know whether your answer is correct;",
//			"If you enter the correct answer, a new symbol to write will be spoken,",
//			"otherwise, you will be prompted to try again if you have not exceeded the maximum number of tries,",
//			"You can leave the game at any time by pressing the back button,",
//			"You can change modes among alphabetic, numeric, and punctuation by pressing the menu key;",
//			"End of instructions."};
	
	private String wbInstr = "How to play in write mode:" +
		" The touchscreen is divided into six regions to represent the Braille cell," +
		" Touch a region and its number will be spoken," +
		" The game will speak a symbol for you to enter," +
		" To enter the dots of the symbol, navigate by touch to the region where a dot should be placed and double tap to add a dot," +
		" Any region that contains a dot will vibrate when touched," +
		" double tap on a vibrating region to remove the dot," +
		" to remove all dots at once, shake the phone in a diagonal corner to corner direction," +
		" Once you are finished entering dots, press the enter key to confirm your answer," +
		" Depending on your phone, the enter key is either " + 
			"the center button of the directional pad or the down movement on the trackball," +
		" Feedback is given to let you know whether your answer is correct," +
		" If you enter the correct answer, a new symbol to write will be spoken," +
		" otherwise, you will be prompted to try again if you have not exceeded the maximum number of tries," +
		" You can leave the game at any time by pressing the back button," +
		" You can change modes among alphabetic, numeric, and punctuation by pressing the menu key;" +
		" End of instructions.";
    
    private int mode;
    private int prevMode;
    private int vibrating = 0;
    private Vibrator vibe;
    private int whichSymbol;
    private Random rand;
    private int[] usedSymbols; 
    private int usedCount;
    private int modeLength;
    private boolean isUserSymbol;
    private boolean showNumSign;
    private String mUserSymbol;
    private String mSymbol; 
    private String lastSymbolDisplayed; 
	private int earned;
    
    // for shake to erase
    private SensorManager sensorManager; 
    private static final int SHAKE_COUNT = 4; 
    private static final int SHAKE_THRESHOLD = 500;
    private static final long SHAKES_INTERVAL= 5000000000L;
    private long[] mShakeTimes;
	private long lastUpdate = -1;
	private float x, y, z;
	private float last_x, last_y, last_z;
	
	// for statistics
	private int[] numWrong = {0,0,0};
	private int[] numCorrect = {0,0,0};
	private int[] numTried = {0,0,0};
	private int[] numGivenAnswer = {0,0,0};
	
	
           	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		// mode: 0 = alphabet, 1 = numbers, 2 = punctuation
		mode = Settings.getMode(this);
		
    	prevMode = mode;
        tts = Game.tts;
        tts.speak("you are in " + modes[mode] + " mode, " +
        		"double tap to enter Braille dots, at any time " +
        		"press on screen for 3 seconds to repeat symbol to enter " +
        		" and to hear full instructions, " +
        		"touch screen again in a different location to interrupt instructions",
        		TextToSpeech.QUEUE_FLUSH, null);
        self = this;
        
        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE); 
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
        if (sensorList.isEmpty()) {
        	tts.speak("No accelerometer available, Cannot shake to undo input", 
        			TextToSpeech.QUEUE_ADD, null);
        }
        else {
        	sensorManager.registerListener(mListener, sensorList.get(0), 
        			SensorManager.SENSOR_DELAY_NORMAL); 
        }
        mShakeTimes = new long[SHAKE_COUNT];
        initShakeArray();
        wbview = new WriteBrailleView(this);
    	mSymbol = "000000";
    	lastSymbolDisplayed = "000000";
    	isUserSymbol = true;
    	showNumSign = false;
    	earned = 0;
    	
//        String filenm;
//		Resources resrcs = getResources();
//		String pkg = getPackageName();
//		int id;
//        for (int i = 0; i < wbInstr.length; i++) {
//        	filenm = pkg + ":raw/wbinstr" + i;
//        	id = resrcs.getIdentifier(filenm, null, null);
//        	if (id != 0)
//        		tts.addSpeech(wbInstr[i], pkg, id);
//        }
        
    	whichSymbol = -1;
    	rand = new Random();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(wbview);
		randomizeSymbol();
    }
    
    private void initShakeArray() {
    	for (int i = 0; i < SHAKE_COUNT; i++) {
    		mShakeTimes[i] = -1;
    	}
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
    	
    	// If in NUMBER mode, mark the number sign as used so it 
    	// won't be selected for display
    	if (mode == NUMBERS) {
    		usedSymbols[0] = 1;
    	}
    	
    	// Reset the user's input to no dots
    	mUserSymbol = "000000";
    	
    	// User will start with input
    	isUserSymbol = true;
    	
    	// Only set the following flag to true if
    	// in Numbers mode.
    	showNumSign = false;
    	
    	// Get next symbol
    	do {
    		whichSymbol = rand.nextInt(modeLength);
    	} while(usedSymbols[whichSymbol] != 0);
    	
    	switch (mode) {
    	case ALPHABET: 
    		mSymbol = alphadots[whichSymbol];
        	Log.v(TAG,"Selected symbol " + alphabet[whichSymbol] + 
        			" which is " + alphadots[whichSymbol]);
    		break;
    	case NUMBERS: 
    		showNumSign = true;
    		mSymbol = numberdots[whichSymbol];
        	Log.v(TAG,"Selected symbol " + numbers[whichSymbol] + 
        			" which is " + numberdots[whichSymbol]);
    		break;
    	case PUNCTUATION:
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
    	speakSymbolToEnter();
    	wbview.invalidate();
    } 
    
    /* Creates and initializes array to mark used symbols
     * each time the mode is changed.
     */
    private void resetUsedArray() {
    	Log.v(TAG, "resetUsedArray");
    	switch (mode) {
    	case ALPHABET:
    		modeLength = alphadots.length;
    		break;
    	case NUMBERS:
    		modeLength = numberdots.length;
    		break;
    	case PUNCTUATION:
    		modeLength = punctdots.length;
    		break;
    	default:
    		Log.e(TAG, "unrecognized mode " + mode);
    		tts.speak("unrecognized mode, switching to alphabet mode", 
    				TextToSpeech.QUEUE_ADD, null);
    		modeLength = alphadots.length;
    		mode = ALPHABET;
    	}
    	usedCount = 0;
    	usedSymbols = new int[modeLength];
    	for (int i = 0; i < modeLength; i++) {
    		usedSymbols[i] = 0;
    	}
    }
    
    private void speakSymbolToEnter() {
    	switch (mode) {
    	case ALPHABET: 
        	tts.speak("The letter to enter is " + alphabet[whichSymbol] + ", as in " + nato[whichSymbol],
        			TextToSpeech.QUEUE_ADD, null);
    		break;
    	case NUMBERS: 
        	tts.speak("The number to enter is " + numbers[whichSymbol] + ", remember to enter two symbols",
        			TextToSpeech.QUEUE_ADD, null);
    		break;
    	case PUNCTUATION:
        	tts.speak("The punctuation symbol to enter is " + punctuation[whichSymbol],
        			TextToSpeech.QUEUE_ADD, null);
    		break;
    	default:
    		Log.e(TAG, "unrecognized mode " + mode);
    		tts.speak("Error: unrecognized mode, ending game", TextToSpeech.QUEUE_ADD, null);
    		finish();
    	}    	
    	tts.speak("Ready", TextToSpeech.QUEUE_ADD, null);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
    	// change to display settings menu  
    	if(keyCode == KeyEvent.KEYCODE_MENU){
    		prevMode = mode;
    		mode = (mode + 1 >= modes.length || mode < 0) ? 0 : mode + 1;
    		Settings.setMode(this, mode); 
    		
    		tts.speak("change mode to " + modes[mode], TextToSpeech.QUEUE_FLUSH, null);
    		randomizeSymbol();
    	}
    	else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
    		// Center key pushed after symbol was replayed
    		if (usedSymbols[whichSymbol] == -1) {
    	    	tts.speak("Selecting next symbol", TextToSpeech.QUEUE_ADD, null);
    			randomizeSymbol();
    		} 
    		// Center key pushed so player can give input on second numeric, alphabetic,
    		// or punctuation symbol
    		else if (!showNumSign){
    			if (!mSymbol.equals(lastSymbolDisplayed)) {
    				lastSymbolDisplayed = mSymbol;
    				numTried[mode]++;
    			}
        		evaluateAnswer();
    		}
    		else { // Center key pushed after player enters number sign (first numeric symbol)
    			evaluateNumberSign();
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
    
    /** 
     * Handles the sensor events for changes to readings and accuracy 
     */ 
	private final SensorEventListener mListener = new SensorEventListener() { 
    	@Override
    	public void onSensorChanged(SensorEvent se) {
    		
    		if (se.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
    			long curTime = System.currentTimeMillis();
    			
    			// only allow one update every 100ms.
    			if ((curTime - lastUpdate) > 200)
    			{
	    			long diffTime = (curTime - lastUpdate);
	    			lastUpdate = curTime;
	
	    			x = se.values[SensorManager.DATA_X];
	    			y = se.values[SensorManager.DATA_Y];
	    			z = se.values[SensorManager.DATA_Z];
	
	    			// Use the velocity of all three dimensions to determine whether a shake has occurred
	    			float speed = 
	    				(Math.abs(x - last_x) + Math.abs(y - last_y) + 
	    						Math.abs(z - last_z)) / diffTime * 10000;
	
	    			if (speed > SHAKE_THRESHOLD)
		    		{
	    				// move previous shake times forward one element in array
	    				for (int i = 1; i < SHAKE_COUNT; i++) {
	        				mShakeTimes[i - 1] = mShakeTimes[i];
	        			}
	    				// store time of this shake in last element of array
	        			mShakeTimes[SHAKE_COUNT - 1] = se.timestamp;
		    		}
	    			last_x = x;
	    			last_y = y;
	    			last_z = z;
	
	    		}

    		}
    		    		
    		// If time of this shake - time of four shakes ago is within the shake interval, erase
    		// (as long as there is data entered)
    		if (mShakeTimes[0] != -1 && mShakeTimes[SHAKE_COUNT - 1] - mShakeTimes[0] < SHAKES_INTERVAL) { 
    			if (!mUserSymbol.equals("000000"))
    				inputCancelled(); 
    			initShakeArray();
    		} 
    	} 

    	@Override
    	public void onAccuracyChanged(Sensor arg0, int arg1) {
    		// TODO Auto-generated method stub

    	}
    }; 
    
	/* Shake phone back and forth to erase input so far */    
    protected void inputCancelled() {
    	Log.v(TAG, "inputCancelled");
    	if (isUserSymbol) {
    		tts.speak("Detected phone being shaken. Erasing previous input", TextToSpeech.QUEUE_ADD, null);
    		mUserSymbol = "000000";
    		wbview.invalidate();
    	}
    } 
 
    // Check players input by comparing to stored symbols.  This method is
    // not called when checking whether the number sign was entered correctly
    // while in numbers mode.  That check is done in the onKeyDown method.
    protected void evaluateAnswer() {
    	Log.v(TAG, "evaluateAnswer, answer = " + mUserSymbol);
    	
    	// Increment number of times tried
  		usedSymbols[whichSymbol] += 1;
  		boolean isCorrect = false;
    	switch (mode) {
    	case ALPHABET: 
    		if (mUserSymbol.equals(alphadots[whichSymbol]))
    			isCorrect = true;
    		break;
    	case NUMBERS: 
    		if (mUserSymbol.equals(numberdots[whichSymbol]))
    			isCorrect = true;
    		break;
    	case PUNCTUATION: 
    		if (mUserSymbol.equals(punctdots[whichSymbol]))
    			isCorrect = true;
    		break;
    	default:
    		Log.e(TAG, "unrecognized mode " + mode);
    	}
    	
    	// If correct input given, say so and then select a new symbol
    	// (No replay needed)
  		if (isCorrect) {
  			tts.speak("That is correct!", TextToSpeech.QUEUE_ADD, null);
  			speakCorrectAnswer();
  			numCorrect[mode]++;
  			BankAccount.addToBalance(this, BankAccount.PER_SYMBOL);
  			earned += BankAccount.PER_SYMBOL;
  			randomizeSymbol();
  		}
  		else {  // Input is incorrect
  			tts.speak("Sorry!", TextToSpeech.QUEUE_ADD, null);
  			numWrong[mode]++;
  			
  			int mMaxTries = Settings.getMaxTries(this); 
  			
  			//  Check if max tries is reached
  			if (mMaxTries <= usedSymbols[whichSymbol]) {
  				speakCorrectAnswer();
  				numGivenAnswer[mode]++;
  				wbview.invalidate();
  				
  				// Flip flag so correct answer vibrates
  				isUserSymbol = false;
  				
  				// Check if you can find a symbol matching what the user input
  				// and let them know what it was
  				boolean found = false;
  				for (int i = 0; i < modeLength; i++) {
  					if (mode == ALPHABET && mUserSymbol.equals(alphadots[i])) {
  						tts.speak("You entered the symbol " + alphabet[i] + ", as in " + nato[i],
  								TextToSpeech.QUEUE_ADD, null);
  			    		found = true;
  			    		break;
  					}
  					else if (mode == NUMBERS && mUserSymbol.equals(numberdots[i])) {
  						tts.speak("You entered the symbol " + numbers[i],
  								TextToSpeech.QUEUE_ADD, null);
  						found = true;
  						break;
  					}
  					else if (mode == PUNCTUATION && mUserSymbol.equals(punctdots[i])) {
  						tts.speak("You entered the symbol " + punctuation[i],
  								TextToSpeech.QUEUE_ADD, null);
  						found = true;
  						break;
  					}
  				}
  				if (!found) 
  					tts.speak("Could not find match for the symbol you entered", 
  							TextToSpeech.QUEUE_ADD, null);
  			}
  			
  			// If max tries not reached, let the user try again from where they left off
  			else {
  				tts.speak("Try again!", TextToSpeech.QUEUE_ADD, null);
  				speakSymbolToEnter();
  			}
  		}
    }
    
    private void evaluateNumberSign() {
		
    	//  User's entry is correct
  		if (mUserSymbol.equals(numberdots[0])) {
  			tts.speak("Number sign entered correctly, please enter number " +
  					numbers[whichSymbol] + "next!", TextToSpeech.QUEUE_ADD, null);
  			
  			// isUserSymbol is still true so user's input will vibrate
  			// Reset user's input symbol to blank screen and flip flag 
  			// so vibrator vibrates second numeric symbol instead of first.
  			mUserSymbol = "000000";
  			wbview.invalidate();
  			showNumSign = false;
  		}
  		else { // User doesn't enter number sign correctly
  			tts.speak("Sorry, that's not the number sign", TextToSpeech.QUEUE_ADD, null);
  			
  			int mMaxTries = Settings.getMaxTries(this); 
  			
  			// Since the number sign (element 0) was initialized to 1, add one to
  			// Max Tries to test if the user has tried to input the number sign the
  			// allowed number of times or more.
  			if (usedSymbols[0] >= mMaxTries + 1) {
  				tts.speak("This is the number sign", TextToSpeech.QUEUE_ADD, null);
  		    	// Mark second number symbol as available since player didn't get to it
  		    	usedSymbols[whichSymbol] = 0;
  		    	wbview.invalidate();
  		    	
  		    	// Flip flag to tell vibrator to vibrate correct symbol
  		    	isUserSymbol = false;
  		    	
  		    	// Set whichSymbol to number sign's index so that when center key is
  		    	// next pressed, a new symbol will be randomly selected.
  				whichSymbol = 0;
  		    	usedSymbols[whichSymbol] = -1;  // gets re-initialized to 1 in randomizeSymbol()
  			}
  			else {
  				tts.speak("Try again!", TextToSpeech.QUEUE_ADD, null);
  			}
  		}
	}
    
    /* Speaks the name of the correct symbol -- called when correct answer is entered 
     * by user or when incorrect answer is entered but max tries is reached.
     * Exception: not called when number sign is being input in Numbers mode
     */
    protected void speakCorrectAnswer() {
    	Log.v(TAG, "speakCorrectAnswer");
    	switch (mode) {
    	case ALPHABET: 
    		tts.speak("This symbol is " + alphabet[whichSymbol] + ", as in " 
    				+ nato[whichSymbol], TextToSpeech.QUEUE_ADD, null);
    		break;
    	case NUMBERS: 
    		tts.speak("This symbol is number " + numbers[whichSymbol], TextToSpeech.QUEUE_ADD, null);
    		break;
    	case PUNCTUATION: 
    		tts.speak("This symbol is " + punctuation[whichSymbol], TextToSpeech.QUEUE_ADD, null);
    		break;
    	default:
    		Log.e(TAG, "unrecognized mode " + mode);
    	}
    	// Mark current symbol as used
    	usedSymbols[whichSymbol] = -1;
		isUserSymbol = false;
    }
    
	/* Called when activity is paused -- when 
	 * phone call arrives.
	 */
	@Override
	public void onPause() {
		if (tts != null) {
			tts.stop();
		}
		// save stats here or in destroy?
		super.onPause();
	}
	
	/* Tells player how (s)he did overall */
	private void speakStatistics() {
		String s = "Game statistics: ";
		for (int i = 0; i < modes.length; i++) {
			if (numTried[i] == 0 || (numCorrect[i] == 0 && numWrong[i] == 0 && numGivenAnswer[i] == 0))
				continue;
			
			if (numTried[i] == 1) {
				String m = (i == ALPHABET) ? "letter" : (i == NUMBERS) ? "number" : "punctuation symbol";
				s = s + "There was won " + m + " attempted, ";
			}
			else {
				String m = (i == ALPHABET) ? "letters" : (i == NUMBERS) ? "numbers" : "punctuation symbols";
				s = s + "There were " + numTried[i] + " " + m + " attempted, ";
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
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "wbstats");
			tts.speak(s, TextToSpeech.QUEUE_ADD, utterance);
		}
		else
			finish();
	}
	
	/* Speak full instructions for the game */
	private void speakInstructions() {
		tts.setOnUtteranceCompletedListener(this);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "instructions");
		tts.speak("the mode is set to " + modes[mode] + 
				" one moment while I retrieve instructions", 
				TextToSpeech.QUEUE_ADD, utterance);		
	}
	
	@Override
	public void onUtteranceCompleted(String utteranceId) {
		if (utteranceId.equalsIgnoreCase("wbstats") || utteranceId == "wbstats") {
			finish();
		}
		else if (utteranceId.equalsIgnoreCase("instructions")) {
			tts.speak(wbInstr, TextToSpeech.QUEUE_ADD, null);
//			for (int i = 0; i < wbInstr.length; i++) {
//		    	tts.speak(wbInstr[i], TextToSpeech.QUEUE_ADD, null);
//		    }
		}
	}
	
	/* View that controls vibrations and drawing dots on screen based on where the user
	 * is touching the screen.
	 */
    protected class WriteBrailleView extends View implements GestureDetector.OnDoubleTapListener,
	GestureDetector.OnGestureListener {

    private final long [] patternCorner = {40,40};
    private final long [] patternMiddle = {0,1000};
    private final Paint mPaintWhite; 
    private final Paint mPaintBlack;
    private int xBound;
    private int y1Bound;
    private int y2Bound;
    private int radius;
    private int lastRegion;
    private GestureDetector detector; 
    private long longPressStartTm;
    private long longPressDuration;
   
    public WriteBrailleView(Context context) {
    	super(context);
    	detector = new GestureDetector(context, this);
    	lastRegion = -1;
    	longPressDuration = 3000;
    	vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
    	mPaintWhite = new Paint();
    	mPaintWhite.setColor(Color.WHITE);
    	mPaintBlack = new Paint();
    	mPaintBlack.setColor(Color.GRAY);
    	mPaintBlack.setStyle(Paint.Style.STROKE);
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

    	String symbol = (isUserSymbol) ? mUserSymbol : 
    		(!showNumSign) ? mSymbol : numberdots[0];
    	for (int i = 0; i < 6; i++) {
    		if (symbol.charAt(i) == '1') {
        		x = (i % 2 == 0) ? xUnit : xUnit + xBound;
    			y = (i < 2) ? yUnit : (i < 4) ? yUnit + y1Bound : yUnit + y2Bound;
    			canvas.drawCircle(x, y, radius, mPaintWhite);
    		}
    		if (symbol.charAt(i) == '0') {
    			x = (i % 2 == 0) ? xUnit : xUnit + xBound;
    			y = (i < 2) ? yUnit : (i < 4) ? yUnit + y1Bound : yUnit + y2Bound;
    			canvas.drawCircle(x, y, radius, mPaintBlack);
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
    	if (detector != null) this.detector.onTouchEvent(me);
    	switch(me.getAction()) {
    	case MotionEvent.ACTION_DOWN:
    		longPressStartTm = me.getDownTime();
    	case MotionEvent.ACTION_MOVE:
    		
    		// Set the symbol to vibrate based on whether the user
    		// is doing input (isUserSymbol == true), whether the
    		// correct answer is being vibrated by the game.  In the
    		// latter case, showNumSign == true only when the number sign
    		// is being vibrated after the user has incorrectly entered the 
    		// first numeric symbol (in Numbers mode only).
    		String symbol = (isUserSymbol) ? mUserSymbol : 
    			(!showNumSign) ? mSymbol : numberdots[0];
    		double x = me.getX();
    		double y = me.getY();
    		x/=getWidth();
    		y/=getHeight();
    		double xx = me.getRawX();
    		double yy = me.getRawY();
    		int loc = (xx < xBound) ? 0 : 1;
    		loc = (yy < y1Bound) ? loc : (yy < y2Bound) ? loc + 2 : loc + 4;
    		// Only say region being touched if the player has just moved into a new
    		// region 
    		
    		//Removed: or if the player lifted his finger then replaced it within
    		// the same region
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
    			speakSymbolToEnter();
    			speakInstructions();
    			longPressStartTm = me.getEventTime();
    		}
    		break;
    	default:
    		setVibration(0);
    	}
    	return true;
    }

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (isUserSymbol && lastRegion >= 0 && lastRegion < 6) {
			char[] str = mUserSymbol.toCharArray();
			for (int i = 0; i < 6; i++) {
				if (i == lastRegion) {
					if (mUserSymbol.charAt(i) == '0') {
						str[i] = '1';
						tts.speak("Added dot at " + regions[i], TextToSpeech.QUEUE_FLUSH, null);
					}
					else {
						str[i] = '0';
						tts.speak("Removed dot at " + regions[i], TextToSpeech.QUEUE_FLUSH, null);
					}						
				}
			}
			mUserSymbol = new String(str);
			invalidate();
		}
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return true;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// no action
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2,
			float distanceX, float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	} 
    
}
}
