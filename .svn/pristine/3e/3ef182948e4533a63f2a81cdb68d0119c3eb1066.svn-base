package cs.washington.edu.VBGhost2;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Random;
import junit.framework.Assert;

import LogToFile.FileLog;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.SQLException;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

public class VBWriter extends Activity
	implements TextToSpeech.OnUtteranceCompletedListener, OnInitListener {
	
	private static final String TAG = "VBWriter";
	
    private static final String[] alphadots =
    {"100000","101000","110000","110100","100100","111000","111100","101100",
    	"011000","011100","100010","101010","110010","110110","100110","111010",
    	"111110","101110","011010","011110","100011","101011","011101","110011",
    	"110111","100111"}; 	
    private static int[] alphabetKeyCodes ={KeyEvent.KEYCODE_A, KeyEvent.KEYCODE_B, KeyEvent.KEYCODE_C,
    	KeyEvent.KEYCODE_D, KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_F,
    	KeyEvent.KEYCODE_G, KeyEvent.KEYCODE_H, KeyEvent.KEYCODE_I,
    	KeyEvent.KEYCODE_J, KeyEvent.KEYCODE_K, KeyEvent.KEYCODE_L,
    	KeyEvent.KEYCODE_M, KeyEvent.KEYCODE_N, KeyEvent.KEYCODE_O,
    	KeyEvent.KEYCODE_P, KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_R,
    	KeyEvent.KEYCODE_S, KeyEvent.KEYCODE_T, KeyEvent.KEYCODE_U,
    	KeyEvent.KEYCODE_V, KeyEvent.KEYCODE_W, KeyEvent.KEYCODE_X,
    	KeyEvent.KEYCODE_Y, KeyEvent.KEYCODE_Z};
    
	private static final String ghostInstr1 = "Input from touch screen disabled while reading instructions. "+
			"Hold phone vertically with buttons at bottom, " +
		" The screen displays an empty Braille cell with dots won through three down the left side and " +
		" dots fore through six down the right side, " +
		" a dot's number is spoken when the dot is touched, ";
	private static final String ghostInstr2 = 
		" double tap or press the number keys to add and remove Braille dots. " +
		" the screen will vibrate when you touch a section where a dot has been added, " +
		" fling right or left with two fingers to submit your letter.";
	private static final String[] regions = {"1", "4", "2", "5", "3", "6"};
	 
	private boolean interrupt;
	private boolean ignoreKeypress = false;
	public static String KEY_LETTER_ENTERED = "letter_entered";	
    private int[] pattern = new int[6];
    private WriteBrailleView wbview;
    private TextToSpeech tts;
    private HashMap<String, String> utterance = new HashMap<String, String>();
   // private boolean mLogEnabled;
    private boolean mJustCreated;
   // private String mSessionId;
   // private int mRoundNr;
    private Vibrator vibe;
    private int whichSymbol;  // subscript of alphadots set randomly to choose letter
    private Random rand;

    private boolean playUserInput;  // vibrate user's input (vs. correct answer)
    private String mUserSymbol;  // dots entered by user
    private String mSymbol;  // letter chosen randomly
    private boolean quiet = false;
    private boolean letterGiven = false;
    private VBWriter self;
    private Toast toast;
    private boolean firstTime = true;
    private StringBuilder wordFragment;
    private DictionaryDB _dictionaryDB = null;
    private boolean _dbLoaded = false;
    private boolean _wordSpelled = false;
    private boolean _notWord = false;
    private boolean yourTurn = true;
    private long leftScrollTime = 0;
    private long rightScrollTime = 0;
    private long scrollDetectionTime = 600;
	
	
	private int letterIndex = 0;
	
	// settings
    private boolean sayDotNums = true;  // used for preventing number names from interrupting initial greeting
	
	// for catching numeric key presses
	private int keycodes[] = {KeyEvent.KEYCODE_1, KeyEvent.KEYCODE_4, KeyEvent.KEYCODE_2,
			  				  KeyEvent.KEYCODE_5, KeyEvent.KEYCODE_3, KeyEvent.KEYCODE_6,
			  				  KeyEvent.KEYCODE_Q, KeyEvent.KEYCODE_R, KeyEvent.KEYCODE_W,
							  KeyEvent.KEYCODE_T, KeyEvent.KEYCODE_E, KeyEvent.KEYCODE_Y};
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {	
		super.onCreate(savedInstanceState);
		Log.v(TAG, "onCreate()");
			
		self = this;
		//mLogEnabled = false;
        mJustCreated = true;
        firstTime = true;
        quiet = false;
        letterGiven = false;
        interrupt = true;

    	
    	if (GlobalState.LOG_USER_ACTIVITY) {
    		if (GlobalState.mLog == null) {
    			GlobalState.mLog = new FileLog(this.getApplicationContext(), GlobalState.LOG_FILE, TAG);
    			GlobalState.mLogEnabled = GlobalState.mLog.openLogFile();
	    		if (!GlobalState.mLogEnabled) {
	    			displayToast("Log to file could not be enabled.  \nReason: " + GlobalState.mLog.getReasonWhyDisabled());
	    		}
    		}	
    		GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "as", "VBW Started");
    	}

        wbview = new WriteBrailleView(this);

    	resetScreen();
    	
 		
		//create TextToSpeech    
	    tts = GlobalState.getTTS();    
	    if (tts == null) {
	    	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
	        tts = gs.createTTS(this, this);
	    }
	    Log.v(TAG, "tts = " + tts);


		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);    
		setContentView(wbview);
		speakOnDisplay();
		ignoreKeypress = false;
    }
    
	
    private void speakOnDisplay()
    {
    	Log.v(TAG, "onSpeakDisplay()");
    	tts.setOnUtteranceCompletedListener(self);
		quiet = false;
		if (firstTime) {
			sayDotNums = false;
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "initInstr");
			letterGiven = false;
			tts.speak("Enter your letter and fling left or right with two fingers to submit.", TextToSpeech.QUEUE_FLUSH, utterance);	
		}
		else {
			tts.speak("Enter your letter and fling left or right with two fingers to submit. Fling down with two fingers for instructions", TextToSpeech.QUEUE_FLUSH, null);
		}
    }
    
     
    private void resetScreen(){
    	Log.v(TAG, "resetScreen");
    	
    	// Reset the user's input to no dots
    	mUserSymbol = "000000";
    	
    	// View should vibrate user input, which is stored in mUserSymbol
    	playUserInput = true;
    	
    	if (!firstTime) speakEnterLetter(TextToSpeech.QUEUE_ADD);
    	wbview.invalidate();
    	
    }
    
    private void speakEnterLetter(int mode) {
    	Log.v(TAG, "speakEnterLetter");
    	quiet = true;
    	//if (!letterGiven) {
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "sayLetter");
    		tts.speak("Enter a letter and fling left or right with two fingers to submit",
    				mode, utterance);	
    }
        

    
    /* Capture Enter key press, keycodes for numbers 1 - 6, back key
     * and menu key
     * @see android.app.Activity#onKeyDown(int, android.view.KeyEvent)
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
    	Log.v(TAG, "onKeyDown()");
    	vibe.vibrate(40);
//    	interrupt = true;

    	// Enter key pressed: either check user's input
    	if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {	
    		if (GlobalState.LOG_USER_ACTIVITY) {
    			GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "kp", "EnterKey");
    		}
    		tts.playEarcon("ding", TextToSpeech.QUEUE_FLUSH, null);
    		evaluateEnteredLetter();
    		
    	}
    	
    	// Back key pressed: exit
    	else if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if (GlobalState.LOG_USER_ACTIVITY) {
    			GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "kp", "BackKey");
    		}
    		letterIndex = -1;
    		returnToCurrentGameMenu();
    	}
    	// Menu key pressed: exit
    	else if (keyCode == KeyEvent.KEYCODE_MENU) {
    		if (GlobalState.LOG_USER_ACTIVITY) {
    			GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "kp", "ExitKey");
    		}
    		letterIndex = -1;
    		returnToCurrentGameMenu();
    	}
    	
    	else if(keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z){
    		Log.v(TAG, "checking letter key. keycode = " + keyCode);
    		if (GlobalState.LOG_USER_ACTIVITY) {
    			GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "kp", ""+keyCode);
    		}
    		for(int i = 0; i<alphabetKeyCodes.length; i++){
    			if(keyCode == alphabetKeyCodes[i]){
    				addLetter(i);
    			}
    		}
    	}
    	
    	// Number key 1 - 6 pressed: add/remove dot at that number
    	else {
    		Log.v(TAG, "checking number key. keycode = " + keyCode);
    		if (GlobalState.LOG_USER_ACTIVITY) {
    			GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "kp", ""+keyCode);
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
    
    
    
    protected void evaluateEnteredLetter(){
    	Log.v(TAG, "evaluateEnteredLetter, answer = " + mUserSymbol);
		boolean found = false;
		for (int i = 0; i < alphadots.length; i++) {
			if (mUserSymbol.equals(alphadots[i])) {
	    		if (GlobalState.LOG_USER_ACTIVITY) {
	    			GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "ua", GlobalState.alphabet[i] + ":" + mUserSymbol);
	    		}
				String s = "You entered the symbol " + GlobalState.alphabet[i] + ", as in " + GlobalState.nato[i];
				letterIndex = i;
	    		tts.setOnUtteranceCompletedListener(self);
	    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "returnToCurrentGameMenu");
				tts.speak(s, TextToSpeech.QUEUE_ADD, utterance);
	    		found = true;
	    		break;
			}
		}
		if (!found) {
    		if (GlobalState.LOG_USER_ACTIVITY) {
    			GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "ua", "?:" + mUserSymbol);
    		}
			tts.speak("No match for the symbol you entered", 
					TextToSpeech.QUEUE_ADD, null);
			resetScreen();
	    	wbview.invalidate();
		}

    }
    
    protected void returnLetterToCurrentGameMenu(int i){
    	if(i>=0){
			String sLetter = GlobalState.alphabet[i];
			char[] letter = sLetter.toCharArray();
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putChar(KEY_LETTER_ENTERED, letter[0]);	
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
    	}else{
    		Intent intent = new Intent();
    		setResult(RESULT_OK, intent);
    		finish();
    	}
    }
    
    protected void addLetter(int i){
    	Log.v(TAG, "addLetter");
    	mUserSymbol = alphadots[i];
		if (GlobalState.LOG_USER_ACTIVITY) {
			GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "kl", alphadots[i]+":keypad");
		}
    	//TODO: something with pattern[]
    	wbview.invalidate();
    	
    }
  
    private void returnToCurrentGameMenu(){
    	Log.v(TAG, "returnToCurrentGameMenu");
		String s = "Returning to current game menu.";
		tts.setOnUtteranceCompletedListener(self);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "returnToCurrentGameMenu");
		tts.speak(s, TextToSpeech.QUEUE_ADD, utterance);
    }
    
    protected void addRemoveDot(int i, String loc) {	
    	if (interrupt) {
			Log.v(TAG, "addRemoveDot");
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
					"doubletap");
			sayDotNums = false;
			char[] str = mUserSymbol.toCharArray();
			if (i >= 0 && i < 6) {
				if (mUserSymbol.charAt(i) == '0') {
					str[i] = '1';
					tts.speak("Added " + regions[i], TextToSpeech.QUEUE_FLUSH,
							utterance);
					if (GlobalState.LOG_USER_ACTIVITY) {
		  	    		GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "dc", "+" + regions[i] + loc);
		  	    	}
					pattern[i] = (i < 2) ? 1 : (i < 4) ? 2 : 3;
				} else {
					str[i] = '0';
					tts.speak("Removed " + regions[i],
							TextToSpeech.QUEUE_FLUSH, utterance);
					if (GlobalState.LOG_USER_ACTIVITY) {
		  	    		GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "dc", "-" + regions[i] + loc);
		  	    	}
					pattern[i] = 0;
				}
				mUserSymbol = new String(str);
				wbview.invalidate();
			}
		}
    }
    
	/* Called when activity is paused -- shut down logger
	 */
	@Override
	public void onPause() {
		super.onPause();
		Log.v(TAG, "onPause");
    	quiet = false;
		if (GlobalState.LOG_USER_ACTIVITY) {
			GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "as", "VBW Paused"); // add final log entry	
		}
		if (isFinishing()) {
			if (GlobalState.LOG_USER_ACTIVITY) {
				GlobalState.mLogEnabled = false;
				GlobalState.mLog.stopHttpPostThread();  // stop the worker thread
				GlobalState.mLog.postEntries();  // post any remaining entries on the UI thread
				GlobalState.mLog.closeLogFile();
				GlobalState.mLog = null;
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
    	interrupt = true;
    }
    
    protected void resumeTTSAndLogging() {
    	Log.v(TAG, "resumeTTSAndLogging()");
    	tts = GlobalState.getTTS();
	    if (tts == null) {
	    	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
	        tts = gs.createTTS(this, this);
	    }
	    Log.i(TAG, "tts = " + tts);
    	tts.setOnUtteranceCompletedListener(self);
    	if (GlobalState.LOG_USER_ACTIVITY) {
    		if (GlobalState.mLog == null) {
    			GlobalState.mLog = new FileLog(this.getApplicationContext(), GlobalState.LOG_FILE, TAG);
    			GlobalState.mLogEnabled = GlobalState.mLog.openLogFile();
	    		if (!GlobalState.mLogEnabled) {
	    			displayToast("Log to file could not be enabled.  \nReason: " + GlobalState.mLog.getReasonWhyDisabled());
	    		}
    		}	
    		if (!mJustCreated) {
    			GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "as", "VBW Resumed");
    		}
    	}

    }
    
	
	
	/* Speak full instructions for the game */
	private void speakInstructions() {
		Log.v(TAG, "speakInstructions");
    	quiet = false;
    	interrupt = false;
		tts.setOnUtteranceCompletedListener(self);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "instructions");
		tts.speak(" one moment while I retrieve instructions", 
				TextToSpeech.QUEUE_ADD, null);	
		quiet = false;
		sayDotNums= false;
		tts.speak(ghostInstr1, TextToSpeech.QUEUE_ADD, null);
		tts.speak(ghostInstr2, TextToSpeech.QUEUE_ADD, utterance);
		//interrupt = true;
	}
    private void scrollRight(){
    	interrupt = true;
    	evaluateEnteredLetter();
    }
    
    private void scrollLeft(){
    	interrupt = true;
		evaluateEnteredLetter();
    }
	
	
	/* View that controls vibrations and drawing dots on screen based on where the user
	 * is touching the screen.
	 */
    protected class WriteBrailleView extends View implements GestureDetector.OnDoubleTapListener,
    GestureDetector.OnGestureListener {

    	private static final int INVALID_POINTER_ID = -1;
    	private static final long MAX_FLING_INTERVAL = 2000;
    	private static final long MAX_SWIPE_INTERVAL = 2000;
		
    	// The Ôactive pointerÕ is the one currently moving our object.
    	private int mFirstPointerId = INVALID_POINTER_ID;
    	private int mSecondPointerId = INVALID_POINTER_ID;
    	private int mFirstPointerIndex;
    	private int mSecondPointerIndex;
        private float mUp2TouchX;
        private float mUp2TouchY;
        private float mDown1TouchX;
        private float mDown2TouchX;
        private float mDown1TouchY;
        private float mDown2TouchY;
        private long mMaxFlingDetectionTime = 600;
        private long mMaxSwipeDetectionTime = 500;
        private int mMinFlingDistance;
        private int mMinSwipeDistance;
        private long lastDownFlingTm;
		private long lastRightSwipeTm;
		private long lastLeftSwipeTm;
    	
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
        	//Log.i(TAG, "Width: " + w + ", Swipe distance: " + mMinSwipeDistance);
        	mMinFlingDistance = (int) (h * 0.52);
        	//Log.i(TAG, "Height: " + h + ", Fling distance: " + mMinFlingDistance);
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
	                //mFirstPointerIndex = me.findPointerIndex(mFirstPointerId);
	                
		        	if (GlobalState.LOG_USER_ACTIVITY) {
		            	GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "sc", "FingerDown:" + (int)x + ":" + (int)y);
		            }
		        	registerTouch(me);
	                break;
	            }
	            
	            case MotionEvent.ACTION_POINTER_DOWN: {
	 
	            	int count = me.getPointerCount();
	               	Log.v(TAG, "ACTION POINTER DOWN, count : "+ count);
	            	
	            	// Save the ID of the secondary pointer
	                if (mSecondPointerId < 0) {
	                	mSecondPointerId = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;         
		               // mSecondPointerIndex =me.findPointerIndex(mSecondPointerId);
	                	final float x = me.getX(mSecondPointerIndex); 
		                final float y = me.getY(mSecondPointerIndex); 
		                mDown2TouchX = x;
		                mDown2TouchY = y;
	                }
	                break;
	            }	            
	                
	            case MotionEvent.ACTION_UP: {
	            	final float x = me.getX();
	            	final float y = me.getY();
		        	if (GlobalState.LOG_USER_ACTIVITY) {
		            	GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "sc", "FingerUp:" + (int)x + ":" + (int)y);
		            }
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
		    				if (GlobalState.LOG_USER_ACTIVITY) {
		    	            	GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "df", "Instructions");
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

		    				lastRightSwipeTm = me.getEventTime();
		    				if (GlobalState.LOG_USER_ACTIVITY) {
		    	            	GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "rs", "Enter");
		    	            }
		    				scrollRight();
	
	            	}
	            	// if left swipe was performed
	            	else if (me.getEventTime() - me.getDownTime() < mMaxSwipeDetectionTime && 
	            		me.getEventTime() - lastLeftSwipeTm > MAX_SWIPE_INTERVAL &&
	            		mDown1TouchX - me.getX(mFirstPointerIndex) > mMinSwipeDistance && 
	            		mDown2TouchX - mUp2TouchX > mMinSwipeDistance && 
	            		me.getY(mFirstPointerIndex) - mDown1TouchY < mMinFlingDistance && 
	            		mUp2TouchY - mDown2TouchY < mMinFlingDistance) {
		            		Toast t = Toast.makeText(self, "Left swipe occurred", Toast.LENGTH_SHORT);
		            		t.show();
		            		tts.playEarcon("ding", TextToSpeech.QUEUE_FLUSH, null);
		    				Log.v(TAG, "onTouchEvent(): left swipe");
		    				if (GlobalState.LOG_USER_ACTIVITY) {
		    	            	GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "ls", "Enter");
		    	            }
		    				lastLeftSwipeTm = me.getEventTime();
		    				scrollLeft();
		    				
	            	}
	            	
	            	else {  // debugging
	            		Log.i(TAG, "UP: Horiz1 change: " + (me.getX(mFirstPointerIndex) - mDown1TouchX) + 
	            				   ", Horiz2 change: " + (mUp2TouchX - mDown2TouchX) + 
	            				   "\nVert1 change: " + (me.getY(mFirstPointerIndex) - mDown1TouchY) +
	            				   ", Vert2 change: " + (mUp2TouchY - mDown2TouchY) + 
	            				   "\nSwipe Distance: " + mMinSwipeDistance + ", Fling Distance: " + mMinFlingDistance +
	            				   "\n Time change: " + (me.getEventTime() - me.getDownTime()) +" mMaxSwipeDetection: " + mMaxSwipeDetectionTime + 
	            				   "\n right swipe interval: " + (me.getEventTime() - lastRightSwipeTm) + " Max swipe interval: " + MAX_SWIPE_INTERVAL
	            				);
	            	}
	            	
	                mFirstPointerId = INVALID_POINTER_ID;
	                break;
	            }
	                
	            case MotionEvent.ACTION_CANCEL: {
	                mFirstPointerId = INVALID_POINTER_ID;
//	                Log.i(TAG, "CANCEL: Horiz1 change: " + (me.getX(mFirstPointerId) - mDown1TouchX) + 
//         				   ", Horiz2 change: " + (mUp2TouchX - mDown2TouchX) + 
//         				   "\nVert1 change: " + (me.getY(mFirstPointerId) - mDown1TouchY) +
//         				   ", Vert1 change: " + (mUp2TouchY - mDown2TouchY) + 
//         				   "\nSwipe Distance: " + mMinSwipeDistance + ", Fling Distance: " + mMinFlingDistance);
	                setVibration(0);
	                break;
	            }
	            
	            case MotionEvent.ACTION_MOVE: {
	            	registerTouch(me);
	            	//Log.i(TAG, "x1: " + me.getX(mFirstPointerId) + " y1: " + me.getY(mFirstPointerId));
	            	if (mSecondPointerId >= 0){
	            		//mSecondPointerIndex = me.findPointerIndex(mSecondPointerId);
	            		Log.i(TAG, "x2: " + me.getX(mSecondPointerIndex) + " y2: " + me.getY(mSecondPointerIndex));
	            		}
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
	                	//mFirstPointerIndex = me.findPointerIndex(mFirstPointerIndex);
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
	                	//mSecondPointerIndex = me.findPointerIndex(mSecondPointerIndex);
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
	    		if (!quiet && sayDotNums && interrupt) tts.speak(regions[loc], TextToSpeech.QUEUE_FLUSH, null);
	    		setVibration(pattern[loc]);
	    		if (GlobalState.LOG_USER_ACTIVITY) {
	            	GlobalState.mLog.addEntry(GlobalState.APP_ABBREV, GlobalState.mSessionId, GlobalState.mRoundNr, "rt", regions[loc] + ":" + (int)xx + ":" + (int)yy);
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
				while (tts.isSpeaking() && !quiet && interrupt) 
					tts.stop();
				sayDotNums = true;
				if (!letterGiven) {
					//speakSymbolToEnter(TextToSpeech.QUEUE_FLUSH);
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
		
	}

	public void onUtteranceCompleted(String utteranceId) {
		Log.v(TAG, "onUtteranceCompleted");
		if (utteranceId.equalsIgnoreCase("returnToMainMenu")) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			Intent intent = new Intent(this, MainMenu.class);
			startActivity(intent);
		}
		else if (utteranceId.equalsIgnoreCase("returnToCurrentGameMenu")) {
			Log.v(TAG, "returnToCurrentMenu");
			returnLetterToCurrentGameMenu(letterIndex);
		}
		else if (utteranceId.equalsIgnoreCase("initInstr")) {
			firstTime = false;
			interrupt = true;
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "lastInitInstr");
			tts.speak("Fling down with two fingers for instructions.  ", TextToSpeech.QUEUE_ADD, utterance);
		}
		else if (utteranceId.equalsIgnoreCase("lastInitInstr")) {
			//speakSymbolToEnter(TextToSpeech.QUEUE_FLUSH);
			sayDotNums = true;
			interrupt = true;
		}
		else if (utteranceId.equalsIgnoreCase("Instructions")) {
			sayDotNums = true;
			interrupt = true;
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
			speakOnDisplay();
		}
		else {
			displayToast("TTS not initialized");
		}
	}
	
	public void displayToast(String msg) {
		toast = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
		toast.show();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}


}
