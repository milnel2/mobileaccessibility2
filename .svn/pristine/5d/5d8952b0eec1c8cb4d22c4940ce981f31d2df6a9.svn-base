package cs.washington.edu.vbwriteparent;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import LogToFile.FileLog;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.Toast;

/* Modified 4-28-11: removed most of initial instructions -- now just says to fling down for instructions. 
 * Also added touch processing to ACTION_DOWN for quicker response.
 */
/* Modified 4-27-11: modified vibration to clean and speed it up
/* Modified 4-25-11: removed swipes and flings and longpress
/* Modified 4-13-11: scaled distances for screen density */
/* Modified 5-9-11: removed double flings and added two-finger down fling  */
/* Bug Fix 8-27-12: changed getX() and getY() methods to take PointerIndex instead of PointerId 
 * (led to errors in Android 3+) */

public class VBWriter extends Activity
	implements TextToSpeech.OnUtteranceCompletedListener, OnInitListener {
	
	private static final String TAG = "VBWriter";
	private static final String LOG_FILE = "VBWriter.log";
	private static final boolean LOG_USER_ACTIVITY = false;  // set true or false, depending on release version
	private static final String APP_ABBREV = "VBW";
	
    private static final String[] alphadots =
    {"100000","101000","110000","110100","100100","111000","111100","101100",
    	"011000","011100","100010","101010","110010","110110","100110","111010",
    	"111110","101110","011010","011110","100011","101011","011101","110011",
    	"110111","100111"};
    public static final String[] alphabet =
    {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
    	"p","q","r","s","t","u","v","w","x","y","z"};
    public static final String[] nato =
    {"alpha","bravo","charlie","delta","echo","fox trot","golf","hotel","india","juliet","kee low",
    	"lima","mike","november","oscar","pahpah", "quebec","romeo","sierra","tango","uniform",
    	"victor","whiskey","ex-ray","yang kee","zoo loo"}; 

    private static final String[] regions = {"1", "4", "2", "5", "3", "6"};
    private int[] pattern = new int[6];

    private WriteBrailleView wbview;
    private TextToSpeech tts;
    private HashMap<String, String> utterance = new HashMap<String, String>();
    private FileLog mLog;
    private boolean mLogEnabled;
    private boolean mJustCreated;
    private String mSessionId;
    private int mRoundNr;
	
	private static final String wbInstr1 = "Hold phone vertically with buttons at bottom, " +
		"listen for the letter to enter, " +
		" The screen displays an empty Braille cell with dots won through three down the left side and " +
		" dots four through six down the right side, " +
		" a dot's number is spoken when the dot is touched, ";
	private static final String wbInstr2 = 
		" double tap or press the number keys to add and remove Braille dots. " +
		" press the menu key to hear the letter again, " +
		" press the enter key or two-finger swipe right to submit your answer.";
	
    //private int vibrating = 0;
    private Vibrator vibe;
    private int whichSymbol;  // subscript of alphadots set randomly to choose letter
    private Random rand;
    private boolean[] usedSymbols; // tracks which letters have been chosen
    private int usedCount;  // count of letters that have been chosen
    private boolean playUserInput;  // vibrate user's input (vs. correct answer)
    private String mUserSymbol;  // dots entered by user
    private String mSymbol;  // letter chosen randomly
    private boolean quiet = false;
    private boolean letterGiven = false;
    private VBWriter self;
    private Toast toast;
    private boolean firstTime = true;
	
	// for statistics
	private int numWrong = 0;
	private int numCorrect = 0;
	private int numTried = 0;
	
	// settings
    private boolean sayDotNums = true;  // used for preventing number names from interrupting initial greeting
	
	// for catching numeric key presses
	private int keycodes[] = {KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_2,
			  				  KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_6,
			  				  KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_W,
							  KeyEvent.KEYCODE_T, KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_Y};
	
	
           
	private void createEarcon() {
		tts.speak("Hold phone vertically with buttons at bottom", 
				TextToSpeech.QUEUE_ADD, null);
		tts.speak("listen for letter to enter", TextToSpeech.QUEUE_ADD, null);
		tts.speak("double tap or press the number keys to add and remove Braille dots, ", 
				TextToSpeech.QUEUE_ADD, null);
		tts.speak("press menu key to hear letter again, ", TextToSpeech.QUEUE_ADD, null);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "lastInitInstr");
		tts.speak("press enter key or two-finger swipe right to submit answer.  ", TextToSpeech.QUEUE_ADD, utterance);
	}


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		
		mSessionId = getSessionAbbrev();
		mRoundNr = 0;	
        tts = new TextToSpeech(this, this);
		self = this;
        
       // tts.setOnUtteranceCompletedListener(this);
        rand = new Random();
        wbview = new WriteBrailleView(this);
    	usedSymbols = new boolean[alphadots.length];
    	resetUsedArray();
    	
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        mLogEnabled = false;
        mJustCreated = true;
    	if (LOG_USER_ACTIVITY) {
    		mLog = new FileLog(this, LOG_FILE, TAG);
    		mLogEnabled = mLog.openLogFile();
            mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "as", "Started");
    	}

		randomizeSymbol();
		setContentView(wbview);
    }
    
    private String getSessionAbbrev()
    {
    	Date now = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
		return sdf.format(now);
    }
    
    /* Increment used count to see if array needs to be re-initialized,
     * then choose a new symbol.
     */
    private void randomizeSymbol() {
    	Log.v(TAG, "randomizeSymbol");
    	
    	if (++usedCount >= alphadots.length) {
    			resetUsedArray();
    	}
    	
    	// Reset the user's input to no dots
    	mUserSymbol = "000000";
    	
    	// View should vibrate user input, which is stored in mUserSymbol
    	playUserInput = true;
    	
    	// Get next symbol
    	do {
    		whichSymbol = rand.nextInt(alphadots.length);
    	} while (usedSymbols[whichSymbol]);
    	
		mSymbol = alphadots[whichSymbol];
		usedSymbols[whichSymbol] = true;
    	Log.v(TAG,"Selected symbol " + alphabet[whichSymbol] + 
    			" which is " + alphadots[whichSymbol]);
 
    	mRoundNr++;
    	if (LOG_USER_ACTIVITY) {
    		mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "ss", alphabet[whichSymbol] + ":" + mSymbol);
    	}
    	speakSymbolToEnter(TextToSpeech.QUEUE_ADD);
    	wbview.invalidate();
    } 
    
    /* Re-initializes the array that is used to mark used symbols
     * after all letters of the alphabet have been chosen once.
     */
    private void resetUsedArray() {
    	Log.v(TAG, "resetUsedArray");
 
    	usedCount = 0;
    	for (int i = 0; i < usedSymbols.length; i++) {
    		usedSymbols[i] = false;
    	}
    }
    
    /* Speak the name of the randomly-chosen symbol so the user
     * knows what to enter.
     */
    private void speakSymbolToEnter(int mode) {
    	Log.v(TAG, "speakSymbolToEnter");
    	quiet = true;
    	//if (!letterGiven) {
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "sayLetter");
    		tts.speak("The letter to enter is " + alphabet[whichSymbol] + ", as in " + nato[whichSymbol],
    				mode, utterance);	
    	//}
    	/*else {
	        tts.speak("The letter to enter is " + alphabet[whichSymbol] + ", as in " + nato[whichSymbol],
	        		TextToSpeech.QUEUE_FLUSH, null);
	        quiet = false;
    	}
    	*/
    }
    
    /* Capture Enter key press, keycodes for numbers 1 - 6, back key
     * and menu key
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
    	
    	vibe.vibrate(40);
    	 
    	// Menu key pressed: repeat symbol to enter
    	if (keyCode == KeyEvent.KEYCODE_MENU){
    		if (LOG_USER_ACTIVITY) {
    			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "kp", "MenuKey");
    		}
    		speakSymbolToEnter(TextToSpeech.QUEUE_ADD);
    	}
    	
    	// Enter key pressed: either check user's input or move to next symbol
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {	
    		if (LOG_USER_ACTIVITY) {
    			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "kp", "EnterKey");
    		}
    		tts.playEarcon("ding", TextToSpeech.QUEUE_FLUSH, null);
    		
    		// Center key pushed after symbol was replayed
    		if (!playUserInput) {
    			// Turn voice back on so dot number is spoken
    			quiet = false;
    		
    	    	// get new symbol
    			randomizeSymbol();
    		} 
    		// Center key pushed so player can give input on second numeric, alphabetic,
    		// or punctuation symbol
    		else {
    			numTried++;
        		evaluateAnswer();
    		}
    		
    	}
    	
    	// Back key pressed: exit
    	else if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if (LOG_USER_ACTIVITY) {
            	mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "kp", "BackKey");
            }
    		speakStatistics();
    	}
    	
    	// Number key 1 - 6 pressed: add/remove dot at that number
    	else {
    		Log.v(TAG, "checking number key. keycode = " + keyCode);
    		if (LOG_USER_ACTIVITY) {
    			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "kp", "" + keyCode);
    		}
    		boolean match = false;
    		for (int i = 0; i < keycodes.length; i++) {
    			if (keyCode == keycodes[i]) {
    				if (i > 5) i -= 6;
    				addRemoveDot(i, "");
    				match = true;
    				break;
    			}
    		}
    		
        	// Any other key pressed
    		if (!match) return super.onKeyDown(keyCode, keyEvent);
    	}
    	
    	Log.v(TAG, "onKeyDown");
    	return true;
    }
    
    /*   
     * Check players input by comparing to stored symbols.
     */
    protected void evaluateAnswer() {
    	Log.v(TAG, "evaluateAnswer, answer = " + mUserSymbol);
  	
    	if (mUserSymbol.equals(alphadots[whichSymbol])) {
  			tts.speak("That is correct!", TextToSpeech.QUEUE_ADD, null);
  			numCorrect++;
  	    	if (LOG_USER_ACTIVITY) {
  	    		mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "ua", alphabet[whichSymbol] + ":" + mUserSymbol);
  	    	}
  			randomizeSymbol();
  		}
  		else {  // Input is incorrect
  			tts.speak("Sorry!", TextToSpeech.QUEUE_ADD, null);
  			numWrong++;
  			
  			// Check if you can find a symbol matching what the user input
			// and let them know what it was
			boolean found = false;
			for (int i = 0; i < alphadots.length; i++) {
				if (mUserSymbol.equals(alphadots[i])) {
					tts.speak("You entered the symbol " + alphabet[i] + ", as in " + nato[i],
							TextToSpeech.QUEUE_ADD, null);
		    		found = true;
		  	    	if (LOG_USER_ACTIVITY) {
		  	    		mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "ua", alphabet[whichSymbol] + ":" + mUserSymbol);
		  	    	}
		    		break;
				}
			}
			if (!found) {
				tts.speak("No match for the symbol you entered", 
						TextToSpeech.QUEUE_ADD, null);
				if (LOG_USER_ACTIVITY) {
	  	    		mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "ua", "?:" + mUserSymbol);
	  	    	}
			}
  			
			// Flip flag so correct answer vibrates
			playUserInput = false;
			
			// Say the correct dots with no speech
			int n = -1, ct = 0;
			boolean[] isDot = {false, false, false, false, false, false};
			for(int i = 0; i < 2; i++) {
    			for(int j = 0; j < 3; j++) {
    				n++;
    		    	if (alphadots[whichSymbol].charAt(i+2*j) == '0') continue;
					isDot[n] = true;
					ct++;
				}
			}
			String dots = "";
			n = 0;
			for (int i = 0; i < 6; i++) {
				if (isDot[i]) {
					n++;
					if (dots != "" && n == ct)
						dots += " and";
					if (i == 0) dots += "won ";
					else dots += ((i + 1) + " ");
				}
			}
			if (ct == 1) dots = "dot is " + dots;
			else dots = "dots are " + dots;
				
			speakCorrectAnswer("The correct " + dots + ", this is letter ");
			quiet = true;
			wbview.invalidate();
  		}
    }
    
    /* Speaks the name of the correct symbol 
     */
    protected void speakCorrectAnswer(String s) {
    	Log.v(TAG, "speakCorrectAnswer");
    	utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "sayAnswer");
    	tts.speak(s + alphabet[whichSymbol] + ", as in " 
    				+ nato[whichSymbol], TextToSpeech.QUEUE_ADD, utterance);
    }
    
    protected void addRemoveDot(int i, String loc) {
    	Log.v(TAG, "addRemoveDot");
    	utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "doubletap");
		sayDotNums = false;
		char[] str = mUserSymbol.toCharArray();
		if (i >= 0 && i < 6) {
			if (mUserSymbol.charAt(i) == '0') {
				str[i] = '1';
				tts.speak("Added " + regions[i], TextToSpeech.QUEUE_FLUSH, utterance);
				if (LOG_USER_ACTIVITY) {
	  	    		mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "dc", "+" + regions[i] + loc);
	  	    	}
				pattern[i] = (i < 2) ? 1 : (i < 4) ? 2 : 3;
			}
			else {
				str[i] = '0';
				tts.speak("Removed " + regions[i], TextToSpeech.QUEUE_FLUSH, utterance);
				if (LOG_USER_ACTIVITY) {
	  	    		mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "dc", "-" + regions[i] + loc);
	  	    	}
				pattern[i] = 0;
			}						
			mUserSymbol = new String(str);
			wbview.invalidate();
		}
    }
    
	/* Called when activity is paused -- shut down tts and logger
	 */
	@Override
	public void onPause() {
		super.onPause();
		Log.v(TAG, "onPause");
    	quiet = false;
		if (tts != null) {
			tts.stop();
			tts.shutdown();
			tts = null;
		}
		if (LOG_USER_ACTIVITY) {
			mLogEnabled = false;
			mLog.stopHttpPostThread();  // stop the worker thread
			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "as", "Paused");
			mLog.postEntries();
			mLog.closeLogFile();
			mLog = null;
		}
	}
	
	// Restart tts and logger
    @Override
    public void onResume() {
    	super.onResume();
    	Log.v(TAG, "onResume");
    	if (tts == null) {
    		tts = new TextToSpeech(getApplicationContext(), this);
    	}
    	tts.setOnUtteranceCompletedListener(self);
    	String reason = null;
    	if (LOG_USER_ACTIVITY) {
    		if (!mLogEnabled) {
	    		mLog = new FileLog(this, LOG_FILE, TAG);
	    		mLogEnabled = mLog.openLogFile();
	    		reason = mLog.getReasonWhyDisabled();
    		}
    		if (!mJustCreated) {
    			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "as", "Resumed");
    		}
    		if (!mLogEnabled) {
    			displayToast("Log to file could not be enabled.  \nReason: " + reason);
    		}
    	}
    	mJustCreated = false;
    }
	
	/* Tells player how (s)he did overall */
	private void speakStatistics() {
		Log.v(TAG, "speakStatistics");
		quiet = true;
		String s = "";
		
		if (numTried > 0 && (numCorrect > 0 || numWrong > 0)) {
			String c;
			if (numTried == 1) {
				c = " won";
			}
			else {
				c = " " + numTried;
			}
			
			if (numCorrect == 1)
				s = s + "You got won out of " + c + " correct";
			else
				s = s + "You got " + numCorrect + " out of " + c + " correct";	
		}

		Log.v(TAG, "tried = " + numTried + ", correct = " + numCorrect + ", wrong = " + numWrong);
		if (s.equals("")) s = "Ending.";
		tts.setOnUtteranceCompletedListener(self);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "wbstats");
		tts.speak(s, TextToSpeech.QUEUE_ADD, utterance);
	}
	
	/* Speak full instructions for the game */
	private void speakInstructions() {
		Log.v(TAG, "speakInstructions");
    	quiet = false;
		//tts.setOnUtteranceCompletedListener(self);
		//utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "instructions");
		tts.speak(" one moment while I retrieve instructions", 
				TextToSpeech.QUEUE_ADD, null);	
		quiet = false;
		tts.speak(wbInstr1, TextToSpeech.QUEUE_ADD, null);
		tts.speak(wbInstr2, TextToSpeech.QUEUE_ADD, null);
	}
	
	/* View that controls vibrations and drawing dots on screen based on where the user
	 * is touching the screen.
	 */
    protected class WriteBrailleView extends View implements GestureDetector.OnDoubleTapListener,
    GestureDetector.OnGestureListener {

    	private static final int INVALID_POINTER_ID = -1;
    	private static final long MAX_FLING_INTERVAL = 2000;
    	private static final long MAX_SWIPE_INTERVAL = 2000;
		
    	// The ‘active pointer’ is the one currently moving our object.
    	private int mFirstPointerId = INVALID_POINTER_ID;
    	private int mSecondPointerId = INVALID_POINTER_ID;
    	private int mFirstPointerIndex = 0;
    	private int mSecondPointerIndex = 1;
        private float mUp2TouchX;
        private float mUp2TouchY;
        private float mDown1TouchX;
        private float mDown2TouchX;
        private float mDown1TouchY;
        private float mDown2TouchY;
        private long mMaxFlingDetectionTime = 600;
        private long mMaxSwipeDetectionTime = 300;
        private int mMinFlingDistance;
        private int mMinSwipeDistance;
        private long lastDownFlingTm;
		private long lastRightSwipeTm;
    	
	    private final long [] patternTop = {40,40};
	    private final long [] patternMiddle = {0,800};
	    private final long [] patternBottom = {50,100};

	    private final Paint mPaintWhite; 
	    private final Paint mPaintBlack;
	    private int xBound;
	    private int y1Bound;
	    private int y2Bound;
	    private int radius;
	    private int lastRegion;
	    private GestureDetector detector; 
	    private long lastUpTime = 0;
	    private long repeatInterval = 2000;
		private double xx;
		private double yy;
		private int loc;
	   
	    public WriteBrailleView(Context context) {
	    	super(context);
	    	detector = new GestureDetector(context, this);
	    	lastRegion = -1;
	    	vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	    	mPaintWhite = new Paint();
	    	mPaintWhite.setColor(Color.WHITE);
	    	mPaintBlack = new Paint();
	    	mPaintBlack.setColor(Color.GRAY);
	    	mPaintBlack.setStyle(Paint.Style.STROKE);
	    	setBackgroundColor(Color.BLACK);
	    	setClickable(true);
	    	
	        lastDownFlingTm = 0;
	        lastRightSwipeTm = 0;
	    } 
	    
	    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
	    	xBound = (int) w / 2;  // x-coordinate of boundary line between columns
        	y1Bound = (int) h / 3;  // y-coordinate of boundary line between first and second row
        	y2Bound = y1Bound * 2;  // y-coordinate of boundary line between second and third row
        	radius = Math.min(xBound, y1Bound)/3;  // radius of dots
        	mMinSwipeDistance = (int) (w * 0.3);
        	Log.i(TAG, "Width: " + w + ", Swipe distance: " + mMinSwipeDistance);
        	mMinFlingDistance = (int) (h * 0.52);
        	Log.i(TAG, "Height: " + h + ", Fling distance: " + mMinFlingDistance);
	    	super.onSizeChanged(w, h, oldw, oldh);
	    }
	    
	    @Override
	    protected void onDraw(Canvas canvas) {
	    	float x, y;
	    	int xUnit = xBound/2;
        	int yUnit = y1Bound/2;
	
    		// Set the symbol to vibrate based on whether the user
    		// is doing input (isUserSymbol == true), whether the
    		// correct answer is being vibrated by the application. 
        	String symbol = (playUserInput) ? mUserSymbol : mSymbol;
	    	for (int i = 0; i < 6; i++) {
	    		x = (i % 2 == 0) ? xUnit : xUnit + xBound;
    			y = (i < 2) ? yUnit : (i < 4) ? yUnit + y1Bound : yUnit + y2Bound;
	    		if (symbol.charAt(i) == '1') {
	    			canvas.drawCircle(x, y, radius, mPaintWhite);
	    			pattern[i] = (i < 2) ? 1 : (i < 4) ? 2 : 3;
	    		}
	    		else {
	    			canvas.drawCircle(x, y, radius, mPaintBlack);
	    			pattern[i] = 0;
	    		}
	    	}
	    }
	    
	    @Override
	    public boolean onTouchEvent(MotionEvent me) {
	    	Log.v(TAG, "onTouchEvent()");
	    	if (detector != null) this.detector.onTouchEvent(me);
	    	final int action = me.getAction();
	    	switch (action & MotionEvent.ACTION_MASK) {
	            case MotionEvent.ACTION_DOWN: {
	            	final float x = me.getX();
	            	final float y = me.getY();
	                mDown1TouchX = x;
	                mDown1TouchY = y;
	                mDown2TouchX = 0;
	                mDown2TouchY = 0;
	                mUp2TouchX = 0;
	                mUp2TouchY = 0;
	
	                // Save the ID of the primary pointer
	                mFirstPointerId = me.getPointerId(0);
	               // mFirstPointerIndex = me.findPointerIndex(mFirstPointerId);
	                 
		        	if (LOG_USER_ACTIVITY) {
		            	mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "sc", "FingerDown:" + (int)x + ":" + (int)y);
		            }
		        	registerTouch(me);
	                break;
	            }
	            
	            case MotionEvent.ACTION_POINTER_DOWN: {
	            	// Save the ID of the secondary pointer
	                if (mSecondPointerId < 0) {
	                	mSecondPointerId = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;         
		                final float x = me.getX(mSecondPointerId); 
		                final float y = me.getY(mSecondPointerId); 
		                mDown2TouchX = x;
		                mDown2TouchY = y;
		                //mSecondPointerIndex = me.findPointerIndex(mSecondPointerId);
	                }
	                break;
	            }	            
	                
	            case MotionEvent.ACTION_UP: {
	            	final float x = me.getX();
	            	final float y = me.getY();
		        	if (LOG_USER_ACTIVITY) {
		            	mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "sc", "FingerUp:" + (int)x + ":" + (int)y);
		            }
		        	//mFirstPointerIndex = me.findPointerIndex(mFirstPointerId);
		        	//mSecondPointerIndex = me.findPointerIndex(mSecondPointerId);
		        	
		    		lastUpTime = me.getEventTime();
		    		setVibration(0);
	            	
		        	// if down fling was performed
	            	if (me.getEventTime() - me.getDownTime() < mMaxFlingDetectionTime && 
	            		me.getEventTime() - lastDownFlingTm > MAX_FLING_INTERVAL &&
	            		me.getY(mFirstPointerIndex) - mDown1TouchY > mMinFlingDistance && 
	            		mUp2TouchY - mDown2TouchY > mMinFlingDistance && 
	            		me.getX(mFirstPointerIndex) - mDown1TouchX < mMinSwipeDistance && 
	            		mUp2TouchX - mDown2TouchX < mMinSwipeDistance) {
		            		Toast t = Toast.makeText(self, "Fling down occurred", Toast.LENGTH_SHORT);
		            		t.show();
		            		tts.playEarcon("flingdown", TextToSpeech.QUEUE_FLUSH, null);
		    				Log.v(TAG, "onTouchEvent(): downward fling");
		    				if (LOG_USER_ACTIVITY) {
		    	            	mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "df", "Instructions");
		    	            }
		    				lastDownFlingTm = me.getEventTime();
		    	    		speakInstructions();     		
	            	}
	            	
	            	// if right swipe was performed
	            	else if (me.getEventTime() - me.getDownTime() < mMaxSwipeDetectionTime && 
	            		me.getEventTime() - lastRightSwipeTm > MAX_SWIPE_INTERVAL &&
	            		me.getX(mFirstPointerIndex) - mDown1TouchX > mMinSwipeDistance && 
	            		mUp2TouchX - mDown2TouchX > mMinSwipeDistance && 
	            		me.getY(mFirstPointerIndex) - mDown1TouchY < mMinFlingDistance && 
	            		mUp2TouchY - mDown2TouchY < mMinFlingDistance) {
		            		Toast t = Toast.makeText(self, "Right swipe occurred", Toast.LENGTH_SHORT);
		            		t.show();
		            		tts.playEarcon("ding", TextToSpeech.QUEUE_FLUSH, null);
		    				Log.v(TAG, "onTouchEvent(): right swipe");
		    				if (LOG_USER_ACTIVITY) {
		    	            	mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "rs", "Enter");
		    	            }
		    				lastRightSwipeTm = me.getEventTime();
		    				
		    				// Center key pushed after symbol was replayed
		    	    		if (!playUserInput) {
		    	    			// Turn voice back on so dot number is spoken
		    	    			quiet = false;
		    	    		
		    	    	    	// get new symbol
		    	    			randomizeSymbol();
		    	    		} 
		    	    		// Center key pushed so player can give input on second numeric, alphabetic,
		    	    		// or punctuation symbol
		    	    		else {
		    	    			numTried++;
		    	        		evaluateAnswer();
		    	    		}	
	            	}
	            	
	            	else {
	            		Log.i(TAG, "UP: Horiz1 change: " + (me.getX(mFirstPointerIndex) - mDown1TouchX) + 
	            				   ", Horiz2 change: " + (mUp2TouchX - mDown2TouchX) + 
	            				   "\nVert1 change: " + (me.getY(mFirstPointerIndex) - mDown1TouchY) +
	            				   ", Vert1 change: " + (mUp2TouchY - mDown2TouchY) + 
	            				   "\nSwipe Distance: " + mMinSwipeDistance + ", Fling Distance: " + mMinFlingDistance);
	            	}
	            	
	                mFirstPointerId = INVALID_POINTER_ID;
	                break;
	            }
	                
	            case MotionEvent.ACTION_CANCEL: {
	                mFirstPointerId = INVALID_POINTER_ID;
	                Log.i(TAG, "CANCEL: Horiz1 change: " + (me.getX(mFirstPointerIndex) - mDown1TouchX) + 
         				   ", Horiz2 change: " + (mUp2TouchX - mDown2TouchX) + 
         				   "\nVert1 change: " + (me.getY(mFirstPointerIndex) - mDown1TouchY) +
         				   ", Vert1 change: " + (mUp2TouchY - mDown2TouchY) + 
         				   "\nSwipe Distance: " + mMinSwipeDistance + ", Fling Distance: " + mMinFlingDistance);
	                setVibration(0);
	                break;
	            }
	            
	            case MotionEvent.ACTION_MOVE: {
	            	//mFirstPointerIndex = me.findPointerIndex(mFirstPointerId);
		        	//mSecondPointerIndex = me.findPointerIndex(mSecondPointerId);
	            	registerTouch(me);
	            	Log.i(TAG, "x1: " + me.getX(mFirstPointerIndex) + " y1: " + me.getY(mFirstPointerIndex));
	            	if (mSecondPointerId >= 0) Log.i(TAG, "x2: " + me.getX(mSecondPointerIndex) + " y2: " + me.getY(mSecondPointerIndex));
	                break;
	            }
	            
	            case MotionEvent.ACTION_POINTER_UP: {
	                // Extract the index of the pointer that left the touch sensor
	                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) 
	                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
	                final int pointerId = me.getPointerId(pointerIndex);
	                if (pointerId == mFirstPointerId) {
	                    // This was our active pointer going up. Choose a new
	                    // active pointer and adjust accordingly.
	                	float tempX = mDown1TouchX;
	                	float tempY = mDown1TouchY;
	                	mDown1TouchX = mDown2TouchX;
	                	mDown1TouchY = mDown2TouchY;
	                	mDown2TouchX = tempX;
	                	mDown2TouchY = tempY;
	                	mUp2TouchX = me.getX(mFirstPointerIndex);
	                	mUp2TouchY = me.getY(mFirstPointerIndex);
	                    mFirstPointerId = mSecondPointerId;   
	                }
	                else {
	                	mUp2TouchX = me.getX(mSecondPointerIndex);
	                	mUp2TouchY = me.getY(mSecondPointerIndex);
	                }
	                mSecondPointerId = INVALID_POINTER_ID;
	                break;
	            }
	            
	            default: {
	            	setVibration(0);
	            }
	    	}
	    	
	    	return true;
	    }
	    
	    private void registerTouch(MotionEvent me) {
    		xx = me.getRawX();
    		yy = me.getRawY();
    		loc = (xx < xBound) ? 0 : 1;
    		loc = (yy < y1Bound) ? loc : (yy < y2Bound) ? loc + 2 : loc + 4;
    		
    		if (loc != lastRegion || me.getEventTime() - lastUpTime > repeatInterval || me.getAction() == MotionEvent.ACTION_DOWN) {
	    		if (!quiet && sayDotNums) tts.speak(regions[loc], TextToSpeech.QUEUE_FLUSH, null);
	    		setVibration(pattern[loc]);
	    		if (LOG_USER_ACTIVITY) {
	            	mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "rt", regions[loc] + ":" + (int)xx + ":" + (int)yy);
	            }
	    		lastUpTime = me.getEventTime();
	    		lastRegion = loc;
    		}	
	    }
	    
	    private void setVibration(int value) {
	    	Log.i(TAG,"in setVibration(" + value + ")");
	    	//if(value != vibrating) { //commented out in order to restart vibration if media volume adjusted
	    		switch(value) {
	    		case 1:
	    			Log.i(TAG,"Doing top pattern");
	    			vibe.vibrate(patternTop,0);
	    			break;
	    		case 2:
	    			Log.i(TAG,"Doing middle pattern");
	    			vibe.vibrate(patternMiddle,0);
	    			break;
	    		case 3:
	    			Log.i(TAG,"Doing bottom pattern");
	    			vibe.vibrate(patternBottom,0);
	    			break;
	    		default:
	    			Log.i(TAG,"Cancelling vibration");
	    			vibe.cancel();
	    		}
	    		//vibrating = value;
    		//}
	    }
	    

	    // Add/remove dot on double tap
		public boolean onDoubleTap(MotionEvent e) {
			Log.v(TAG, "onDoubleTap");
			if (playUserInput && lastRegion >= 0 && lastRegion < 6) {
				addRemoveDot(lastRegion, ":" + (int)(e.getRawX()) + ":" + (int)(e.getRawY()));
			}
			return false;
		}

		public boolean onDoubleTapEvent(MotionEvent e) {
			Log.v(TAG, "onDoubleTapEvent");
			return false;
		}

		// Stop current utterance on single tap
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.v(TAG, "onSingleTapConfirmed");
			if (!quiet) {
				while (tts.isSpeaking() && !quiet) 
					tts.stop();
				sayDotNums = true;
				if (!letterGiven) {
					speakSymbolToEnter(TextToSpeech.QUEUE_FLUSH);
				}
			}
			lastUpTime = 0;
			return false;
		}

		public boolean onDown(MotionEvent e) {
			return false;
		}

		// Toggle speaking dot numbers on fling
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			return false;
		}

		public void onLongPress(MotionEvent e) {			
		}

		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			return false;
		}

		public void onShowPress(MotionEvent e) {
		}

		public boolean onSingleTapUp(MotionEvent e) {
			return false;
		}
		

		public boolean isDownFling(MotionEvent ev) {
            final int action = ev.getAction();
            boolean isFling = false;
            switch (action & MotionEvent.ACTION_MASK) {
	            case MotionEvent.ACTION_DOWN: {
	                final float y = ev.getY();
	                mDown1TouchY = y;
	
	                // Save the ID of the primary pointer
	                mFirstPointerId = ev.getPointerId(0);
	               // mFirstPointerIndex = ev.findPointerIndex(mFirstPointerId);
	                Log.i(TAG, "Down Pointer: " + mFirstPointerId + ", y: " + y);
	                break;
	            }
	            
	            case MotionEvent.ACTION_POINTER_DOWN: {
	            	// Save the ID of the secondary pointer
	                if (mSecondPointerId < 0) {
	                	mSecondPointerId = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;         
		                mSecondPointerIndex = ev.findPointerIndex(mSecondPointerId);
	                	final float y = ev.getY(mSecondPointerIndex);   
		                mDown2TouchY = y;
		                Log.i(TAG, "Down 2nd Pointer: " + mSecondPointerId + ", y: " + y);
	                }
	                break;
	            }	            
	                
	            case MotionEvent.ACTION_UP: {
	            	// determine whether fling was performed
	            	if (ev.getEventTime() - ev.getDownTime() < mMaxFlingDetectionTime && 
	            			ev.getY(mFirstPointerId) - mDown1TouchY > mMinFlingDistance && 
	            			mUp2TouchY - mDown2TouchY > mMinFlingDistance) {
	            		Toast t = Toast.makeText(self, "Fling down occurred", Toast.LENGTH_SHORT);
	            		t.show();
	            		isFling = true;
	            		Log.i(TAG, "Down fling recorded");
	            		lastDownFlingTm = ev.getEventTime();
	            	}
	            	Log.i(TAG, "Pointer up: " + mFirstPointerId + ", y: " + ev.getY(mFirstPointerId));
	                mFirstPointerId = INVALID_POINTER_ID;        
	                break;
	            }
	                
	            case MotionEvent.ACTION_CANCEL: {
	            	Log.i(TAG, "Cancelled pointer: " + mFirstPointerId);
	                mFirstPointerId = INVALID_POINTER_ID;
	                break;
	            }
	            
	            case MotionEvent.ACTION_POINTER_UP: {
	                // Extract the index of the pointer that left the touch sensor
	                final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) 
	                        >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
	                final int pointerId = ev.getPointerId(pointerIndex);
	                
	                if (pointerId == mFirstPointerId) {
	                    // This was our active pointer going up. Choose a new
	                    // active pointer and adjust accordingly.
	                	float temp = mDown1TouchY;
	                	mDown1TouchY = mDown2TouchY;
	                	mDown2TouchY = temp;
	                	mUp2TouchY = ev.getY(mFirstPointerId);
	                    mFirstPointerId = mSecondPointerId;   
	                }
	                else mUp2TouchY = ev.getY(mSecondPointerId);
	                mSecondPointerId = INVALID_POINTER_ID;  
	                Log.i(TAG, "2nd Pointer up: " + pointerId + ", y: " + mUp2TouchY);
	                break;
	            }
            }
            return isFling;
		}
	}

	public void onUtteranceCompleted(String utteranceId) {
		Log.v(TAG, "onUtteranceCompleted");
		if (utteranceId.equalsIgnoreCase("wbstats") || utteranceId == "wbstats") {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			finish();
		}
		else if (utteranceId.equalsIgnoreCase("instructions")) {
			//quiet = false;
			//tts.speak(wbInstr, TextToSpeech.QUEUE_ADD, null);
		}
		else if (utteranceId.equalsIgnoreCase("initInstr")) {
			firstTime = false;
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "lastInitInstr");
			tts.speak("Fling down with two fingers for instructions.  ", TextToSpeech.QUEUE_ADD, utterance);
		}
		else if (utteranceId.equalsIgnoreCase("lastInitInstr")) {
			speakSymbolToEnter(TextToSpeech.QUEUE_FLUSH);
			sayDotNums = true;
		}
		else if (utteranceId.equalsIgnoreCase("doubletap")) {
			sayDotNums = true;
		}
		else if (utteranceId.equalsIgnoreCase("sayLetter")) {
			letterGiven = true;
			quiet = false;
		}
		else if (utteranceId.equalsIgnoreCase("sayAnswer")) {
			quiet = false;
		}
		
	}

	public void onInit(int status) {
		Log.v(TAG, "onInit");
		
		if (status == TextToSpeech.SUCCESS) {
			tts.addEarcon("ding", getPackageName(), R.raw.ding);
			tts.addEarcon("flingdown", getPackageName(), R.raw.flingdown);
			tts.addEarcon("flingup", getPackageName(), R.raw.flingup);
			tts.setSpeechRate((float) 1.25);  // use phone setting instead, automatic in 2.1+
			tts.setOnUtteranceCompletedListener(self);
			quiet = false;
			if (firstTime) {
				sayDotNums = false;
				utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "initInstr");
				letterGiven = false;
				tts.speak("Welcome to VB Rye ter", TextToSpeech.QUEUE_FLUSH, utterance);	
			}
		}
		else {
			displayToast("TTS not initialized");
		}
	}
	
	public void displayToast(String msg) {
		toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.show();
	}


}
