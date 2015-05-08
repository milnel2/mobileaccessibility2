package cs.washington.edu.vbreader;

import android.app.Activity;
import android.os.Bundle;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;

import android.content.pm.ActivityInfo;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.widget.Toast;
import LogToFile.*;

/* Modified 4-27-11: modified vibration to clean and speed it up
/* Modified 4-25-11: removed swipes and flings and longpress
/* Modified 4-13-11: scaled distances for screen density */
/* Modified 5-9-11: removed double flings and added two-finger down fling  */

public class VBReader extends Activity
	implements TextToSpeech.OnUtteranceCompletedListener, OnInitListener {
	
	private static final String TAG = "VBReader";
	private static final String LOG_FILE = "VBReader.log";
	private static final boolean LOG_USER_ACTIVITY = false;  // set true or false, depending on release version
	private static final String APP_ABBREV = "VBR";
	protected static final int INPUT_ANSWER = 1000;
	protected static final int SET_INPUT_TYPE = 2000;
	
	private static final String[] ACTIONS = {"cs.washington.edu.vbreader.SOFT_KEYPAD", "cs.washington.edu.vbreader.SOFT_KEYPAD", "cs.washington.edu.vbreader.SIMPLE_KEYPAD",
											 "cs.washington.edu.vbreader.SPEECH_INPUT", "cs.washington.edu.vbreader.TAP_HOLD_INPUT"};
	private static final String[] INPUT_TYPES = {"qwurty kee pad", "alphabetic kee pad", "simple kee pad",
		 "speech in put", "tap hold in put"};

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
   
    private VBReaderView rview;
    private TextToSpeech tts;
    private HashMap<String, String> utterance = new HashMap<String, String>();
    private FileLog mLog;
    private boolean mLogEnabled;
    private boolean mJustCreated;
    private String mSessionId;
    private int mRoundNr;
	
	private static final String vbrInstr1 = "Hold phone so buttons are at bottom, " +
		" The screen displays a Braille cell with dots one through three down the left side and " +
		" dots four through six down the right side, " +
		" dots that are raised vibrate when touched, " +
		" drag your finger around screen to feel which dots vibrate,";
	private static final String vbrInstr2 = " use the vibrations to identify the letter that is being displayed, " +
		" press the enter key or two-finger swipe right to hear the letter and to move to next letter, " +
		" press the back key to exit.";

    //private int vibrating = 0;
    private Vibrator vibe;
    private int whichSymbol;  // subscript of alphadots set randomly to choose letter
    private Random rand;
    private int[] usedSymbols; // tracks which letters have been chosen: 0 - unused, 1 - current, 2 - used
    private int usedCount;  // count of letters that have been chosen
    private String mSymbol;  // letter chosen randomly
    private boolean replayLetter;  // vibrate letter after user pressed enter button
    private boolean quiet;  // stops TTS from being interrupted
    private boolean sayDotNums = true; // if false, won't speak dot numbers -- set in onInit & used during initial greeting
    private VBReader self;
    private Toast toast;
    private boolean firstTime = true;
    private int inputType;
    private String lastSymbolDisplayed; 
	private int numWrong = 0;
	private int numCorrect = 0;
	private int numDisplayed = 0;
	private int numGivenAnswer = 0;
	
	// for statistics
	private int numUserTouched = 0;
	
           	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		Log.i(TAG, "onCreate()");

		mSessionId = getSessionAbbrev();
		mRoundNr = 0;
		quiet = false;
		lastSymbolDisplayed = "000000";
        
		// Start up the TTS for use throughout application
		GlobalState gs = (GlobalState)getApplication();
    	Log.v(TAG, "Creating new tts for application " + gs);
	    tts = gs.createTTS(this, this);
	    tts.setOnUtteranceCompletedListener(this);
	    Log.i(TAG, "Created new tts = " + tts);

	    inputType = 2;  // default to Simple Keypad if not specified
        
		self = this;
              
        rand = new Random();
        rview = new VBReaderView(this);
    	usedSymbols = new int[alphadots.length];
    	resetUsedArray();
    	
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        mLogEnabled = false;
        mJustCreated = true;
    	if (LOG_USER_ACTIVITY) {
    		mLog = new FileLog(this.getApplicationContext(), LOG_FILE, TAG);
    		mLogEnabled = mLog.openLogFile();
    		// even if log to file is disabled, still post messages
            mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "as", "Started");
            if (!mLogEnabled) {
    			displayToast("Log to file could not be enabled.  \nReason: " + mLog.getReasonWhyDisabled());
    		}
    	}

		randomizeSymbol();
		setContentView(rview);
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
    	
    	// Move to next letter
    	replayLetter = false;
    	quiet = false;
    	
    	if (++usedCount >= alphadots.length) {
    			resetUsedArray();
    	}
    	
    	// Get next symbol
    	do {
    		whichSymbol = rand.nextInt(alphadots.length);
    	} while (usedSymbols[whichSymbol] != 0);
    	
		mSymbol = alphadots[whichSymbol];
		usedSymbols[whichSymbol] = 1;
    	nextSymbolReady(TextToSpeech.QUEUE_ADD);
    	rview.invalidate();
		if (numUserTouched > 0) numUserTouched++;
    } 
    
    /* Re-initializes the array that is used to mark used symbols
     * after all letters of the alphabet have been chosen once.
     */
    private void resetUsedArray() {
    	Log.v(TAG, "resetUsedArray");
 
    	usedCount = 0;
    	for (int i = 0; i < usedSymbols.length; i++) {
    		usedSymbols[i] = 0;
    	}
    }
    
    /* Called when a new symbol is ready to be displayed
     */
    private void nextSymbolReady(int mode) {
    	Log.v(TAG, "nextSymbolReady()");
    	mRoundNr++;
    	if (LOG_USER_ACTIVITY) {
    		mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "ss", alphabet[whichSymbol] + ":" + mSymbol);
    	}
	    tts.speak("New letter Ready", mode, null);
    }
    
    /* Capture Enter key press, keycodes for numbers 1 - 6, back key
     * and menu key
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
    	
    	vibe.vibrate(40);
    	 
    	// Menu key pressed: select input type
    	if (keyCode == KeyEvent.KEYCODE_MENU) {
    		tts.speak("Menu key pressed, one moment please", TextToSpeech.QUEUE_FLUSH, null);
    		if (LOG_USER_ACTIVITY) {
    			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "kp", "MenuKey");
        		mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "si", "Start");
    		}
    		Intent intent = new Intent(this, InputSelectionMenu.class);
        	startActivityForResult(intent, SET_INPUT_TYPE);
        	Log.v(TAG, "menu key pressed - set input type, input menu started");
    		return true;
    	}
    	
    	// Enter key pressed: say current letter then move to next symbol
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {	
    		tts.playEarcon("ding", TextToSpeech.QUEUE_FLUSH, null);
    		if (LOG_USER_ACTIVITY) {
    			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "kp", "EnterKey");
    		}
    		
    		// Center key pushed after symbol was played for first time
    		if (usedSymbols[whichSymbol] == 1) {
    			getInput();
    			//replayLetter();
    		} 
    		// Center key pushed after symbol was replayed
    		else {			
    			randomizeSymbol();
    		}

    	}
    	
    	// Back key pressed: speak stats if more than one letter shown then exit
    	else if (keyCode == KeyEvent.KEYCODE_BACK) {
        	if (LOG_USER_ACTIVITY) {
            	mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "kp", "BackKey");
            }
    		speakStatistics();
    	}
    	
    	// Any other key: call super to handle it
    	else {  		
        	// Any other key pressed
    		if (LOG_USER_ACTIVITY) {
    			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "kp", "" + keyCode);
    		}
    		return super.onKeyDown(keyCode, keyEvent);
    	}
    	
    	Log.v(TAG, "onKeyDown");
    	return true;
    }
    
    protected void getInput() {
    	if (!mSymbol.equals(lastSymbolDisplayed)) {
			lastSymbolDisplayed = mSymbol;
			numDisplayed++;
		}
    	Intent intent = new Intent();
    	intent.setAction(ACTIONS[inputType]);
    	intent.putExtra("Option", inputType);
    	if (LOG_USER_ACTIVITY) {
			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "gi", "Start");
		}
    	startActivityForResult(intent, INPUT_ANSWER);
    	Log.v(TAG, "getInput() - activity started");
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.v(TAG, "onActivityResult; requestCode = " + requestCode + ", resultCode = " + resultCode);
		sayDotNums = true;
		resumeTTSAndLogging();
    	if (requestCode == INPUT_ANSWER) {
    		if (LOG_USER_ACTIVITY) {
    			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "gi", "Stop");
    		}
    		if (resultCode == RESULT_CANCELED){
    			inputCancelled();
    		}
    		else if (resultCode == RESULT_OK){
    			evaluateAnswer(data.getStringExtra("answer"));
    		}
    	}
    	if (requestCode == SET_INPUT_TYPE) {
    		if (LOG_USER_ACTIVITY) {
    			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "si", "Stop");
    		}
    		if (resultCode == RESULT_OK){
    			//Log.v(TAG, "inputType = " + inputType + ", type = " + data.getIntExtra("type", -1));
    			inputType = data.getIntExtra("type", inputType);
    			tts.speak(INPUT_TYPES[inputType] + " set, letter displayed", TextToSpeech.QUEUE_ADD, null);
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
  		usedSymbols[whichSymbol] = 2;
  		if (answer.equals("")) {
  			numGivenAnswer++;
  		}
  		else if (answer.equalsIgnoreCase(alphabet[whichSymbol])) {
  			tts.speak("That is correct!", TextToSpeech.QUEUE_ADD, null);
  			numCorrect++;		
  		}
  		else {
  			tts.speak("Sorry!", TextToSpeech.QUEUE_ADD, null);
  			numWrong++;	
  		} 	
  		if (LOG_USER_ACTIVITY) {
  			String value = (answer.equals("")) ? "none" : answer;
	    	mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "ua", alphabet[whichSymbol] + ":" + value);
	    }
    	replayLetter();
    }   
    
    protected void replayLetter() {
    	replayLetter = true;
    	speakLetterName();
    }

    /* Speaks the name of the symbol 
     */
    protected void speakLetterName() {
    	Log.v(TAG, "speakLetterName()");
    	quiet = true;
    	utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "sayLetter");
    	tts.speak("The letter was " + alphabet[whichSymbol] + ", as in " 
    				+ nato[whichSymbol] + ", replaying letter", TextToSpeech.QUEUE_ADD, utterance);
    }
    
	/* Called when activity is paused -- shut down tts and logger
	 */
	@Override
	public void onPause() {
		super.onPause();
		Log.v(TAG, "onPause");
		quiet = false;
		if (LOG_USER_ACTIVITY) {
			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "as", "Paused"); // add final log entry	
		}
		if (isFinishing()) {
			GlobalState gs = (GlobalState)getApplication();
			gs.killTTS();
			if (LOG_USER_ACTIVITY) {
				mLogEnabled = false;
				mLog.stopHttpPostThread();  // stop the worker thread
				mLog.postEntries();  // post any remaining entries on the UI thread
				mLog.closeLogFile();
				mLog = null;
			}
		}
	}
	
	// Restart tts and logger
    @Override
    public void onResume() {
    	super.onResume();
    	Log.v(TAG, "onResume");
    	resumeTTSAndLogging();
    	mJustCreated = false;
    }
    
    protected void resumeTTSAndLogging() {
    	tts = GlobalState.getTTS();
	    if (tts == null) {
	    	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
	        tts = gs.createTTS(this, this);
	    }
	    Log.i(TAG, "tts = " + tts);
    	tts.setOnUtteranceCompletedListener(self);
    	if (LOG_USER_ACTIVITY) {
    		if (mLog == null) {
	    		mLog = new FileLog(this.getApplicationContext(), LOG_FILE, TAG);
	    		mLogEnabled = mLog.openLogFile();
	    		if (!mLogEnabled) {
	    			displayToast("Log to file could not be enabled.  \nReason: " + mLog.getReasonWhyDisabled());
	    		}
    		}	
    		if (!mJustCreated) {
    			mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "as", "Resumed");
    		}
    	}
    }
	
	/* Tells player how (s)he did overall */
	private void speakStatistics() {		
		Log.v(TAG, "speakStatistics");
		quiet = true;
		String s = "";
		
		if (numDisplayed > 0 && (numCorrect > 0 || numWrong > 0)) {
			String c;
			if (numDisplayed == 1) {
				c = " won";
			}
			else {
				c = " " + numDisplayed;
			}
			
			if (numCorrect == 1)
				s = s + "You got won out of " + c + " correct";
			else
				s = s + "You got " + numCorrect + " out of " + c + " correct";	
		}

		Log.v(TAG, "tried = " + numDisplayed + ", correct = " + numCorrect + ", wrong = " + numWrong);
		if (s.equals("")) s = "Ending.";
		tts.setOnUtteranceCompletedListener(self);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "stats");
		tts.speak(s, TextToSpeech.QUEUE_ADD, utterance);
	}
		
	/* Speak full instructions for the game */
	private void speakInstructions() {
		Log.v(TAG, "speakInstructions()");
    	quiet = false;
		tts.speak(" one moment while I retrieve instructions", 
				TextToSpeech.QUEUE_ADD, null);		
		tts.speak(vbrInstr1, TextToSpeech.QUEUE_ADD, null);
		tts.speak(vbrInstr2, TextToSpeech.QUEUE_ADD, null);
	}
	
	/* View that controls vibrations and drawing dots on screen based on where the user
	 * is touching the screen.
	 */
    protected class VBReaderView extends View implements GestureDetector.OnDoubleTapListener,
    	GestureDetector.OnGestureListener {

    	private static final int INVALID_POINTER_ID = -1;
    	private static final long MAX_FLING_INTERVAL = 2000;
    	private static final long MAX_SWIPE_INTERVAL = 2000;
    	
    	// The ‘active pointer’ is the one currently moving our object.
    	private int mFirstPointerId = INVALID_POINTER_ID;
    	private int mSecondPointerId = INVALID_POINTER_ID;
        private float mUp2TouchX;
        private float mUp2TouchY;
        private float mDown1TouchX;
        private float mDown2TouchX;
        private float mDown1TouchY;
        private float mDown2TouchY;
        private boolean firstTime = true;
        private long mMaxFlingDetectionTime = 600;
        private long mMaxSwipeDetectionTime = 300;
        private int mMinFlingDistance;
        private int mMinSwipeDistance;
		private long lastDownFlingTm;
		private long lastRightSwipeTm;
		
	    private final long [] patternTop = {40,40};
	    private final long [] patternMiddle = {0,800};
	    private final long [] patternBottom = {50,200};
	    private int[] pattern = new int[6];
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
  
	    public VBReaderView(Context context) {
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
	
	    	for (int i = 0; i < 6; i++) {
	    		x = (i % 2 == 0) ? xUnit : xUnit + xBound;
    			y = (i < 2) ? yUnit : (i < 4) ? yUnit + y1Bound : yUnit + y2Bound;
	    		if (mSymbol.charAt(i) == '1') {
	    			canvas.drawCircle(x, y, radius, mPaintWhite);
	    			pattern[i] = (i < 2) ? 1 : (i < 4) ? 2 : 3;
	    		}
	    		else {
	    			canvas.drawCircle(x, y, radius, mPaintBlack);
	    			pattern[i] = 0;
	    		}
	    	}
	    }
	    
	    private void setVibration(int value) {
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
	    			vibe.cancel();
	    		}
	    		//vibrating = value;
	    	//}
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
	                
	                // if the user touches the screen after starting the app, count the
		    		// first letter as being shown ~ so if user backs out of app immediately
		    		// after starting without touching screen, don't increment the numShown
		    		// count, which will prevent the statistics from being unnecessarily spoken.
		    		if (numUserTouched == 0) numUserTouched++;  
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
	                }
	                break;
	            }	            
	                
	            case MotionEvent.ACTION_UP: {
	            	lastUpTime = 0;
		    		setVibration(0);
		    		float x = me.getRawX();
		    		float y = me.getRawY();
		        	if (LOG_USER_ACTIVITY) {
		            	mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "sc", "FingerUp:" + (int)x + ":" + (int)y);
		            }
		        	
	            	
		        	// if down fling was performed
	            	if (me.getEventTime() - me.getDownTime() < mMaxFlingDetectionTime && 
	            		me.getEventTime() - lastDownFlingTm > MAX_FLING_INTERVAL &&
	            		me.getY(mFirstPointerId) - mDown1TouchY > mMinFlingDistance && 
	            		mUp2TouchY - mDown2TouchY > mMinFlingDistance && 
	            		me.getX(mFirstPointerId) - mDown1TouchX < mMinSwipeDistance && 
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
	            		me.getX(mFirstPointerId) - mDown1TouchX > mMinSwipeDistance && 
	            		mUp2TouchX - mDown2TouchX > mMinSwipeDistance && 
	            		me.getY(mFirstPointerId) - mDown1TouchY < mMinFlingDistance && 
	            		mUp2TouchY - mDown2TouchY < mMinFlingDistance) {
		            		Toast t = Toast.makeText(self, "Right swipe occurred", Toast.LENGTH_SHORT);
		            		t.show();
		            		tts.playEarcon("ding", TextToSpeech.QUEUE_FLUSH, null);
		    				Log.v(TAG, "onTouchEvent(): right swipe");
		    				if (LOG_USER_ACTIVITY) {
		    	            	mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "rs", "Enter");
		    	            }
		    				lastRightSwipeTm = me.getEventTime();
		    				
		    				// Center key pushed after symbol was played for first time
		    	    		if (usedSymbols[whichSymbol] == 1) {
		    	    			getInput();
		    	    			//replayLetter();
		    	    		} 
		    	    		// Center key pushed after symbol was replayed
		    	    		else {			
		    	    			randomizeSymbol();
		    	    		}		
	            	}
	            	
	            	else {
	            		Log.i(TAG, "UP: Horiz1 change: " + (me.getX(mFirstPointerId) - mDown1TouchX) + 
	            				   ", Horiz2 change: " + (mUp2TouchX - mDown2TouchX) + 
	            				   "\nVert1 change: " + (me.getY(mFirstPointerId) - mDown1TouchY) +
	            				   ", Vert1 change: " + (mUp2TouchY - mDown2TouchY) + 
	            				   "\nSwipe Distance: " + mMinSwipeDistance + ", Fling Distance: " + mMinFlingDistance);
	            	}
	            	
	                mFirstPointerId = INVALID_POINTER_ID;
	                break;
	            }
	                
	            case MotionEvent.ACTION_CANCEL: {
	                mFirstPointerId = INVALID_POINTER_ID;
	                Log.i(TAG, "CANCEL: Horiz1 change: " + (me.getX(mFirstPointerId) - mDown1TouchX) + 
         				   ", Horiz2 change: " + (mUp2TouchX - mDown2TouchX) + 
         				   "\nVert1 change: " + (me.getY(mFirstPointerId) - mDown1TouchY) +
         				   ", Vert1 change: " + (mUp2TouchY - mDown2TouchY) + 
         				   "\nSwipe Distance: " + mMinSwipeDistance + ", Fling Distance: " + mMinFlingDistance);
	                setVibration(0);
	                break;
	            }
	            
	            case MotionEvent.ACTION_MOVE: {
	            	registerTouch(me);
	            	Log.i(TAG, "x1: " + me.getX(mFirstPointerId) + " y1: " + me.getY(mFirstPointerId));
	            	if (mSecondPointerId >= 0) Log.i(TAG, "x2: " + me.getX(mSecondPointerId) + " y2: " + me.getY(mSecondPointerId));
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
	                	mUp2TouchX = me.getX(mFirstPointerId);
	                	mUp2TouchY = me.getY(mFirstPointerId);
	                    mFirstPointerId = mSecondPointerId;   
	                }
	                else {
	                	mUp2TouchX = me.getX(mSecondPointerId);
	                	mUp2TouchY = me.getY(mSecondPointerId);
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
	    		
		    // if region changed or if the player lifted his finger then replaced it within
		    // the same region
		    if (loc != lastRegion || me.getEventTime() - lastUpTime > repeatInterval) {
			    if (!quiet && sayDotNums) tts.speak(regions[loc], TextToSpeech.QUEUE_FLUSH, null);
		    	setVibration(pattern[loc]);
			    if (LOG_USER_ACTIVITY) {
			    	mLog.addEntry(APP_ABBREV, mSessionId, mRoundNr, "rt", regions[loc] + ":" + (int)xx + ":" + (int)yy);
			    }
			    lastUpTime = me.getEventTime(); 
			    lastRegion = loc;
		    }	  
	    }

		public boolean onDoubleTap(MotionEvent e) {
			Log.v(TAG, "onDoubleTap()");
			return false;
		}

		public boolean onDoubleTapEvent(MotionEvent e) {
			Log.v(TAG, "onDoubleTapEvent()");
			return false;
		}

		// Stop current utterance on single tap
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.v(TAG, "onSingleTapConfirmed()");
			if (!quiet) {
				tts.stop();
				if (firstTime) {
					firstTime = false;
					sayDotNums = true;
					nextSymbolReady(TextToSpeech.QUEUE_FLUSH);
				}
			}
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
		
		/*
		public boolean isDownFling(MotionEvent ev) {
            final int action = ev.getAction();
            boolean isFling = false;
            switch (action & MotionEvent.ACTION_MASK) {
	            case MotionEvent.ACTION_DOWN: {
	            	final float x = ev.getX();
	            	final float y = ev.getY();
	                
	                mDown1TouchY = y;
	
	                // Save the ID of the primary pointer
	                mFirstPointerId = ev.getPointerId(0);
	                break;
	            }
	            
	            case MotionEvent.ACTION_POINTER_DOWN: {
	            	// Save the ID of the secondary pointer
	                if (mSecondPointerId < 0) {
	                	mSecondPointerId = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;         
		                final float y = ev.getY(mSecondPointerId);   
		                mDown2TouchY = y;
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
	                mFirstPointerId = INVALID_POINTER_ID;
	                break;
	            }
	                
	            case MotionEvent.ACTION_CANCEL: {
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
	                break;
	            }
            }
            return isFling;
		}
		*/

	}

	public void onUtteranceCompleted(String utteranceId) {
		Log.v(TAG, "onUtteranceCompleted()");
		if (utteranceId.equalsIgnoreCase("stats") || utteranceId == "stats") {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			finish();
		}
		else if (utteranceId.equalsIgnoreCase("initInstr")) {
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "lastInitInstr");
			tts.speak("Fling down with two fingers for instructions.  ", TextToSpeech.QUEUE_ADD, utterance);
		}
		else if (utteranceId.equalsIgnoreCase("lastInitInstr")) {
			nextSymbolReady(TextToSpeech.QUEUE_FLUSH);
			sayDotNums = true;
			firstTime = false;
		}
		else if (utteranceId.equalsIgnoreCase("sayLetter")) {
			quiet = false;
		}
	}

	public void onInit(int status) {
		Log.v(TAG, "onInit()");
		if (status == TextToSpeech.SUCCESS) {
			tts.setOnUtteranceCompletedListener(self);
			quiet = false;
			if (firstTime) {
				sayDotNums = false;
				utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "initInstr");
				tts.speak("Welcome to VB Reader", TextToSpeech.QUEUE_FLUSH, utterance);	
			}
			else {
				tts.speak("braille cell displayed", TextToSpeech.QUEUE_FLUSH, null);
			}
		}
		else {
			displayToast("TTS not initialized");
		}
		//firstTime = false;
	}
	
	public void displayToast(String msg) {
		toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.show();
	}


}
