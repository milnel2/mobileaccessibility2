package cs.washington.edu.buddies;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

// for gestures
import android.gesture.Gesture;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/* Spend and Earn Tokens menu */
public class Money extends Activity implements OnClickListener, OnGesturePerformedListener, 
	View.OnTouchListener, TextToSpeech.OnUtteranceCompletedListener, ToastDisplay,
	GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener{
	
	// For logging
	private static final String TAG = "Money";
	
	// TextToSpeech
    private TextToSpeech tts;
    HashMap<String, String> utterance = new HashMap<String, String>();
    private int selection;
    
    // For gestures
	public static GestureLibrary mLibrary;
	private boolean gestureMatch;
	
	// Verbal instructions -- these Strings have been synthesized to files that
	// should be in the res/raw directory but if they are removed for any reason,
	// the TextToSpeech instance will use these strings real-time as a backup.
	private String moneyGestureModeInstr = 
		" draw one of the following options to navigate: Circle to shop, Triangle to play" +
		" reed braille, S to play write braille, vertical bar for braille words, or X to exit," +
		" press the Menu kee to change to button touch mode";
	private String touchModeInstr = 
		" touch and drag down screen, use trackball or use directional keys" +
		" to hear menu options; double tap or press the center key or trackball to select " +
		" a menu option; press the Menu kee to change to gestures mode";
	
	// For user muting of speech
	private boolean ttsOn;
	private boolean longSpeech;
	
	private int clearFocusInterval;
    private long lastUpTime = 0;
	
	// Needed for button listeners & touch mode
	private Money self = null;
    private int numBtns;
	private Button[] buttons;
	private Button blankBtn;
	
	// Vars for catching long press events
	private long longPressStartTm;
    private long longPressDuration;
    private int lastLoc;


    // Needed to switch between gesture/touch mode
    private boolean gesturesMode;
    GestureOverlayView gestures;
    private GestureDetector detector;
    private boolean isInitial;
    
    // For starting new activities
    private static final int SHOP = 0;
    private static final int READ = 1;
    private static final int WRITE = 2;
    private static final int WORDS = 3;
    
    // for captions
    private Toast toast;
    private Thread t;
    private String[] toastMsgs = {"Draw gesture symbol to select option",
		"Press Menu key to change to touch mode",
		"Press and hold on screen to repeat instructions"};
    private int index = 0;
    
    // Handle to UI thread so UI can be updated
	// by worker threads
	protected final Handler mHandler = new Handler();
	
	public void displayToast() {
		if (index < toastMsgs.length) {
			toast = Toast.makeText(getApplicationContext(), toastMsgs[index], Toast.LENGTH_SHORT);
			toast.show();
			index++;
		}
	}
    	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.v(TAG, "creating");

        setContentView(R.layout.money);      
        // Keep menu in portrait layout
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Toast display thread
        t = new Thread(new ToastDisplayer(this, mHandler, toastMsgs.length));
		t.start();
        
        //create TextToSpeech
        tts = Game.tts;
        
        //create Gestures
        mLibrary = Game.mLibrary;
        if (!mLibrary.load()) {
        	finish();
        }       
        
        // Save reference of activity for use when assigning
        // listeners
        self = this;
        
        // Check whether user wants to hear speech -- might not
        // want to if using a screen reader
        ttsOn = Settings.getTts(this);
        
        // Set time requirements for long presses and triple taps
        longPressDuration = 3000;
        lastLoc = -1;
        
        // for clearing focus so repeat can occur
        clearFocusInterval = 1500;
        
        // Initialize buttons with listeners
        numBtns = 5;
		buttons =  new Button[numBtns];
		findButtonViews();
        setListeners(); 
        LinearLayout layout = (LinearLayout)findViewById(R.id.mlayout);
        layout.setOnTouchListener(self);
        
        // For gestures mode
        isInitial = true;
        gestures = (GestureOverlayView) findViewById(R.id.gestures);
        gestures.setEventsInterceptionEnabled(false);
        gesturesMode = this.getIntent().getBooleanExtra("gesturesMode", true);
        gestureMatch = false;  // explicitly initialize
        
        tts.speak("Spend and earn tokens menu displayed, one moment please", TextToSpeech.QUEUE_ADD, null);
        longSpeech = true;
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
        
        if (gesturesMode) {
        	turnOnGestures();
        	tts.speak(moneyGestureModeInstr, TextToSpeech.QUEUE_ADD, utterance);
        } else {
        	turnOffGestures();
        	tts.speak(touchModeInstr, TextToSpeech.QUEUE_ADD, utterance);
        }
        tts.speak("touch and hold on screen at any time to repeat instructions", 
        		TextToSpeech.QUEUE_ADD, null);
        isInitial = false;
        detector = new GestureDetector(this, this);
    }
	
	
	/* Turns on gesture mode for navigation and turns off touch buttons */
	private void turnOnGestures() {
		gestures.addOnGesturePerformedListener(this);
        gesturesMode = true;
        // prevent focus on buttons
        blankBtn = (Button)findViewById(R.id.keypad_blank);
        
        blankBtn.requestFocus();
        if (!isInitial)
        	tts.speak("Gesture mode activated", TextToSpeech.QUEUE_ADD, null);
	}
	
	/* Turns on touch button mode for navigation and turns off gestures */
	private void turnOffGestures() {
		gestures.removeAllOnGesturePerformedListeners();
		gesturesMode = false;
		// speak the button.tag value, which has phonetic spelling for 
		// some letters that aren't pronounced correctly if just a single
		// letter is given
		if (!isInitial)
			tts.speak("touch mode activated", TextToSpeech.QUEUE_ADD, null);
	}
	
	/* Store the handle of each button on the menu so that listeners can
	 * quickly be added/removed inside a for loop.
	 */
	private void findButtonViews() {
		buttons[0] = (Button)findViewById(R.id.shop_button);
		buttons[1] = (Button)findViewById(R.id.play_read_button);
		buttons[2] = (Button)findViewById(R.id.play_write_button);
		buttons[3] = (Button)findViewById(R.id.play_word_button);
		buttons[4] = (Button)findViewById(R.id.play_back_button);
	}
	
	
	/* Set touch and click listeners for each button.
	 * Also be sure buttons are touch focusable
	 */
	private void setListeners() {
		for (int i = 0; i < numBtns; i++) {
			buttons[i].setOnTouchListener(this);
			buttons[i].isFocusableInTouchMode();
			buttons[i].setOnClickListener(this);
			final int t = i;
			final String tag = (String)buttons[t].getTag();    
			buttons[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus && ttsOn) {
						tts.speak(tag, TextToSpeech.QUEUE_FLUSH, null);
					}
				}
			});
		}
	}
	
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		
		// We want at least one prediction
		if (predictions.size() > 0 && !gestureMatch) {
			Prediction prediction = predictions.get(0);
			// We want at least some confidence in the result
			if (prediction.score > 1.0) {
				// Show the gesture
				if (t.isAlive())
					t.interrupt();
				toast.cancel();
				toast = Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT);
				toast.show();
				//User drew symbol for Shopping
				if (prediction.name.equals("CIRCLE")) {
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "psubmenu");
					selection = SHOP;
					tts.speak("Selected go shopping", TextToSpeech.QUEUE_FLUSH, utterance);
				} 
				// User drew symbol for Read Braille
				else if (prediction.name.equals("TRIANGLE")) {
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "psubmenu");
					selection = READ;
					tts.speak("Selected reed Braille game", TextToSpeech.QUEUE_FLUSH, utterance);
				}
				// User drew symbol for Write Braille
				else if (prediction.name.equals("S")){
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "psubmenu");
					selection = WRITE;
					tts.speak("Selected write Braille game", TextToSpeech.QUEUE_FLUSH, utterance);
				} 
				// User drew symbol for Braille Words
				else if (prediction.name.equals("VERTICAL")){
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "psubmenu");
					selection = WORDS;
					tts.speak("Selected Braille words game", TextToSpeech.QUEUE_FLUSH, utterance);
				} 
				// User drew symbol to QUIT
				else if (prediction.name.equals("X") || prediction.name.equals("V")){
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "pback");
		    		tts.speak("Selected return to pet status", TextToSpeech.QUEUE_FLUSH, utterance);
				} 
				else {
					tts.speak("Unable to determine shape. Please try again. ", TextToSpeech.QUEUE_FLUSH, null);
				}
			}
			else
				tts.speak("Unable to determine shape. Please try again. ", TextToSpeech.QUEUE_FLUSH, null);
		}
	}

	// Starts a new activity
	private void startNewActivity(int which) {
		Intent intent = new Intent();
		switch (which) {
		case SHOP:
			intent.setClass(this, Store.class);
			break;
		case READ:
			intent.setClass(this, ReadBrailleGame.class);
			break;
		case WRITE:
			intent.setClass(this, WriteBrailleGame.class);
			break;
		case WORDS: 
			intent.setClass(this, BrailleWords.class);
			// TO DO! CHOOSE LEVEL
			intent.putExtra("level", 0);
			break;
		default: //do nothing -- should never get in here
			tts.speak("Error occurred",	TextToSpeech.QUEUE_FLUSH, null);
			gestureMatch = false;
			return;
		}
		intent.putExtra("gesturesMode", gesturesMode);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
	
	// On click event
	@Override
	public void onClick(View v) {
		if (t.isAlive())
			t.interrupt();
		toast.cancel();
    	switch (v.getId()){
    	case R.id.shop_button:
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "psubmenu");
			selection = SHOP;
			tts.speak("Selected go shopping", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
    	case R.id.play_read_button:
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "psubmenu");
			selection = READ;
			tts.speak("Selected reed Braille game", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
    	case R.id.play_write_button:
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "psubmenu");
			selection = WRITE;
			tts.speak("Selected write Braille game", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
    	case R.id.play_word_button:
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "psubmenu");
			selection = WORDS;
			tts.speak("Selected Braille Words game", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
    	case R.id.play_back_button:
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "pback");
    		tts.speak("Selected return to pet status", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
    	}		
	}
    
	// Alert player if hard key is pressed.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {  
    	if(keyCode == KeyEvent.KEYCODE_MENU){
			if (gesturesMode) {
				tts.speak("Menu kee pressed, Turning off gestures", TextToSpeech.QUEUE_FLUSH, null);
				turnOffGestures();
			} else {
				tts.speak("Menu kee pressed, Turning on gestures", TextToSpeech.QUEUE_FLUSH, null);
				turnOnGestures();
			}
    	}
    	else if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if (t.isAlive())
    			t.interrupt();
    		toast.cancel();
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "pback");
    		tts.speak("back kee pressed, returning to pet status", TextToSpeech.QUEUE_FLUSH, utterance);
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
    		return super.onKeyDown(keyCode, keyEvent);
    	}
    	else {
    		return super.onKeyDown(keyCode, keyEvent);
    	}
    	Log.v(TAG, "onKeyDown");
    	return true;
    }
       
	/* Called for touches that are not initial button touches */
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		Log.v(TAG, "onTouchEvent");
		detector.onTouchEvent(me);
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			longPressStartTm = me.getDownTime();
		} 
		// Only process movement events that occur more than a
		// predetermined interval (in ms) apart to improve performance
		else if (me.getAction() == MotionEvent.ACTION_MOVE) {
			
		} else
			super.onTouchEvent(me);
		
		// Capture long presses that occur within a single region and
		// repeat instructions.
		if (me.getEventTime() - longPressStartTm > longPressDuration) {
			longSpeech = true;
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
			tts.speak("Spend and earn money menu displayed, one moment please", TextToSpeech.QUEUE_FLUSH, null);
			if (gesturesMode) {
				tts.speak(moneyGestureModeInstr, TextToSpeech.QUEUE_ADD, utterance);
			} else {
				tts.speak(touchModeInstr, TextToSpeech.QUEUE_ADD, utterance);
			}
			longPressStartTm = me.getEventTime();
		}
		return true;
	}

	/* Called for touches inside the button display */
	@Override
	public boolean onTouch(View v, MotionEvent me) {
		Log.v(TAG, "onTouch");
		detector.onTouchEvent(me);
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			longPressStartTm = me.getDownTime();
			if (!gesturesMode && me.getDownTime() - lastUpTime > clearFocusInterval 
					&& lastLoc >= 0 && lastLoc < buttons.length)
				buttons[lastLoc].clearFocus();
			findKey(me);
		} 
		// Only process movement events that occur more than a
		// predetermined interval (in ms) apart to improve performance
		else if (me.getAction() == MotionEvent.ACTION_MOVE) {
			findKey(me);
		}
		else if (me.getAction() == MotionEvent.ACTION_UP) {
			lastUpTime = me.getEventTime();
		}
		// Capture long presses that occur within a single region and
		// repeat instructions.
		if (me.getEventTime() - longPressStartTm > longPressDuration) {
			longSpeech = true;
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
			tts.speak("Spend and earn money menu displayed, one moment please", TextToSpeech.QUEUE_FLUSH, null);
			if (gesturesMode) {
				tts.speak(moneyGestureModeInstr, TextToSpeech.QUEUE_ADD, utterance);
			} else {
				tts.speak(touchModeInstr, TextToSpeech.QUEUE_ADD, utterance);
			}
			longPressStartTm = me.getEventTime();
		}
		return true;
	}

	
	/* Locates the button on which the motion event occurred 
	 * and gives focus to that button.  Only uses the Y
	 * coordinate because buttons are full screen width and
	 * run vertically down screen.  Button ids must be stored
	 * in array in the same order they are displayed on screen.
	 */
	private boolean findKey(MotionEvent me) {
		double y = me.getRawY();
		int[] loc = new int[2];
		for (int i = 0; i < numBtns; i++) {
			buttons[i].getLocationOnScreen(loc);
			// If the motion event goes over the button, have the button request focus
			if (y <= (loc[1] + buttons[i].getHeight())) {
				if (!gesturesMode)
					buttons[i].requestFocus();
				if (i != lastLoc) {
					longPressStartTm = me.getEventTime();
					lastLoc = i;
				}
				return true;
			}
		}
		return false;
	}
	
	@Override
	public void onRestart() {
		super.onRestart();
		if (gesturesMode) {
			turnOnGestures();
		} else {
			turnOffGestures();
		}
		longSpeech = false;
		tts.speak("spend and earn money menu displayed", TextToSpeech.QUEUE_ADD, null);
	}
	
	// If the TTS instance got wiped out, restart it.
	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		tts.setOnUtteranceCompletedListener(self);
		gestureMatch = false;
	}
	
	/* Return the current gestures mode 
     * This function is called both when a button is clicked and when the
     * BACK button is pressed.
     */
    private void returnModeAsResult() {
    	if (t.isAlive())
			t.interrupt();
		toast.cancel();
    	Intent intent = new Intent();
    	intent.putExtra("gesturesMode", gesturesMode);
    	setResult(RESULT_OK, intent);
    	finish();    	
    }
    
	@Override
	public void onUtteranceCompleted(String utteranceId) {
		if (utteranceId.equalsIgnoreCase("psubmenu")) {
			tts.setOnUtteranceCompletedListener(null);
			startNewActivity(selection);
		}
		else if (utteranceId.equalsIgnoreCase("pback")) {
			returnModeAsResult();
		}
		else if (utteranceId.equalsIgnoreCase("longSpeech")) {
			longSpeech = false;
		}
	}


	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (!gesturesMode && lastLoc >= 0 && lastLoc < buttons.length)
			buttons[lastLoc].performClick();
		return false;
	}


	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (longSpeech) { 
			tts.speak("Speech stopped", TextToSpeech.QUEUE_FLUSH, null);
			longSpeech = false;
			return true;
		}
		return false;
	}


	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
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


    
