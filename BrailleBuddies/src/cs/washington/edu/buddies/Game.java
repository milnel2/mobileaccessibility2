package cs.washington.edu.buddies;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;

import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout;

import java.io.File;

// for gestures
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;

/* Pet status and player activities menu */
public class Game extends Activity implements OnClickListener, OnGesturePerformedListener, 
	View.OnTouchListener, TextToSpeech.OnUtteranceCompletedListener, TextToSpeech.OnInitListener,
	GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener, ToastDisplay {
	
	// For logging
	private static final String TAG = "Game";
	
	// Text to speech
	protected static TextToSpeech tts;
	HashMap<String, String> utterance = new HashMap<String, String>();
    private int selection;
    
    // For gestures
	public static GestureLibrary mLibrary;
	GestureOverlayView gestures;
	private boolean gestureMatch;
	private GestureDetector detector;
	
	// for scroll right
	private MotionEvent lastDownEvent = null;
	private int scrollDistance;
	private boolean activityStarted;
		
	// Carry gestures/touch mode setting through
	private boolean gesturesMode;
	
	// Stops certain speech first display only
    private boolean isInitial = true;
	
	// Verbal instructions -- these Strings have been synthesized to files that
	// should be in the res/raw directory but if they are removed for any reason,
	// the TextToSpeech instance will use these strings real-time as a backup.
	private String gameGestureModeInstr = 
		" draw one of the following options to navigate: Circle to feed, Triangle to exercise" +
		" pet, S to play with pet, vertical bar to spend or earn money, or X to exit," +
		" scroll right to edit pet, press the menu button to change to touch mode";
	private String gameTouchModeInstr = 
		" touch and drag down screen, use trackball or use directional keys" +
		" to hear menu options; press center down or trackball key to select last" +
		" spoken menu option; scroll right to edit pet, press menu button to change to gestures mode";
	
	// For user muting of speech
	private boolean ttsOn;
	private boolean longSpeech;

	// UI Elements for status, button listeners & touch mode
	private Game self = null;
	private Button[] mButtons;
	private TextView[] mStatus;
	private int numOptions;
	private Button blankBtn;
	private LinearLayout layout;
	private TableLayout status;
	
	// Vars for catching long press events
	private long longPressStartTm;
    private long longPressDuration;
    private int lastLoc;
    
    // for retouches on same button after lifting
    private int clearFocusInterval = 1200;
    private long lastUpTime = 0;

    // For starting new activities
    private static final int FEED = 0;
    private static final int EXERCISE = 1;
    private static final int INTERACT = 2;
    private static final int MONEY = 3;
    private static final int PET_EDIT = 20;
    
    // for captions
    private Toast toast;
    private Thread t;
    private String[] toastMsgs = {"Draw gesture symbol to select option",
		"Press Menu key to change to touch mode",
		"Scroll right to edit pet",
		"Press and hold on screen to repeat instructions"};
    private int index = 0;

	// Thread for StatusMonitor
	private Thread checker;
	protected boolean sentInterrupt = false;
	private boolean statusChanged;
	
	// Handler for UI thread
	private final Handler mHandler = new Handler();
	
	final Runnable mUpdateResults = new Runnable() {
		public void run() {
			onStatusChange();
		}
	};
	private static Animal mAnimal;
	
	// Random
	private Random random;
	
	
	@Override
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
        random = new Random();       
            
        setContentView(R.layout.game);
		
        // set up toast
        t = new Thread(new ToastDisplayer(this, mHandler, toastMsgs.length));
		t.start();
		
		// Save reference of activity for use when assigning
        // listeners
        self = this;
        
        //create TextToSpeech
        tts = new TextToSpeech(this, this);
        
        //create Gestures
        mLibrary = GestureLibraries.fromRawResource(this, R.raw.gesturesbuddies);
        if (!mLibrary.load()) {
        	finish();
        }
        
        // Check whether user wants to hear speech -- might not
        // want to if using a screen reader
        ttsOn = Settings.getTts(this);
        
        // Set time requirements for long presses and triple taps
        longPressDuration = 2000;
        lastLoc = -2;  // different than menus because game's status section has location of -1
        
        // for clearing focus so repeat can occur
        clearFocusInterval = 1200;
        
        // Initialize buttons with listeners
        numOptions = 5;
        mButtons = new Button[numOptions];
        mStatus = new TextView[numOptions];
        getUIElements();
        setListeners(); 
        
        
        // For gestures mode
        gestures = (GestureOverlayView) findViewById(R.id.gestures);
        gestures.setEventsInterceptionEnabled(false);
        gesturesMode = this.getIntent().getBooleanExtra("gesturesMode", true);
        gestureMatch = false;  // explicitly initialize
        detector = new GestureDetector(this, this);
        scrollDistance = 0;
             
        // Keep menu in portrait layout
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        mAnimal = new Animal();
        statusChanged = mAnimal.checkStatus();
        //refreshStatus(); // runs on resume
        
        checker = new Thread(new StatusMonitor(mAnimal, mHandler, this));
        checker.start();
    }
	
	/* Turns on gesture mode for navigation and turns off touch buttons */
	private void turnOnGestures() {
		gestures.addOnGesturePerformedListener(this);
        gesturesMode = true;
        // prevent focus on buttons
        blankBtn = (Button)findViewById(R.id.blank_button);
        
        blankBtn.requestFocus();
        if (!isInitial)
        	tts.speak("Gesture mode activated", TextToSpeech.QUEUE_FLUSH, null);
	}
	
	/* Turns on touch button mode for navigation and turns off gestures */
	private void turnOffGestures() {
		gestures.removeAllOnGesturePerformedListeners();
		gesturesMode = false;
		if (!isInitial)
			tts.speak("touch mode activated", TextToSpeech.QUEUE_FLUSH, null);
	}
	
	/* Store the handle of each button on the menu so that listeners can
	 * quickly be added/removed inside a for loop.
	 */
	private void getUIElements() {
    	mButtons[0] = (Button)findViewById(R.id.feed_button);
    	mStatus[0] = (TextView)findViewById(R.id.feed_status);
    	mButtons[1] = (Button)findViewById(R.id.play_button);
    	mStatus[1] = (TextView)findViewById(R.id.play_status);
    	mButtons[2] = (Button)findViewById(R.id.interact_button);
    	mStatus[2] = (TextView)findViewById(R.id.interact_status);
    	mButtons[3] = (Button)findViewById(R.id.money_button);
    	mStatus[3] = (TextView)findViewById(R.id.bank_status);
    	mButtons[4] = (Button)findViewById(R.id.exit_button);
    	layout = (LinearLayout)findViewById(R.id.glayout);
    	status = (TableLayout)findViewById(R.id.status_info);
	}
	
	/* Set touch and click listeners for each button.
	 * Also be sure buttons are touch focusable
	 */
	private void setListeners() {
		for (int i = 0; i < numOptions; i++) {
			mButtons[i].setOnTouchListener(this);
			mButtons[i].isFocusableInTouchMode();
			mButtons[i].setOnClickListener(this);
			final int t = i;
			final String tag = (String)mButtons[t].getTag();    
			mButtons[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus && ttsOn) {
						tts.speak(tag, TextToSpeech.QUEUE_FLUSH, null);
					}
				}
			});
		}
		layout.setOnTouchListener(self);
        status.setOnTouchListener(self);
        status.isFocusableInTouchMode();
        status.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					tts.speak("getting pet status, one moment", TextToSpeech.QUEUE_FLUSH, null);
					speakStatus();
				}
			}
		});
	}
	
	@Override
	public void onInit(int status) {
		tts.speak("Pet status displayed", TextToSpeech.QUEUE_FLUSH, null);
		longSpeech = true;
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
        if (gesturesMode) {
        	turnOnGestures();
        	tts.speak(gameGestureModeInstr, TextToSpeech.QUEUE_ADD, utterance);
        } else {
        	turnOffGestures();
        	tts.speak(gameTouchModeInstr, TextToSpeech.QUEUE_ADD, utterance);
        }
        tts.speak("touch and hold on screen at any time to repeat instructions", 
        		TextToSpeech.QUEUE_ADD, null);
        isInitial = false;
	}
	
	// return animal for current game -- needed by feed, exercise and play activities
	protected static Animal getAnimal() {
		return mAnimal;
	}
	
    protected void onStatusChange() {
    	if (this.hasWindowFocus()) {
    		String name = PetData.getName(this);
    		tts.speak(name + "'s status has changed", TextToSpeech.QUEUE_ADD, null);
    		refreshStatus();
    	}
    	else
    		statusChanged = true;
    }
    
    protected void refreshStatus() {
    	String s = (mAnimal.getFeedStatus()) ? "Yes" : "No";
        mStatus[0].setText(s);
        s = (mAnimal.getPlayStatus()) ? "Yes" : "No";
        mStatus[1].setText(s);
        // opposite of boolean because label says "Happy?", 
        // so if status is 'true', means pet is NOT happy
        s = (mAnimal.getGroomingStatus()) ? "No" : "Yes";
        mStatus[2].setText(s);
        s = String.valueOf(BankAccount.getBalance(this));
        mStatus[3].setText(s);
        // speak current status
        speakStatus();
		statusChanged = false;
		layout.invalidate();
    }
    
    private void speakStatus() {
    	String name = PetData.getName(this);
    	longSpeech = true;
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
		tts.speak(name + " is " + ((mStatus[0].getText().equals("Yes")) ? "" : "not") +
				" hungry, " + name + ((mStatus[1].getText().equals("Yes")) ? " needs " : " does not need ") +
				"exercise, and " + name + " is " + ((mStatus[2].getText().equals("No")) ? "not " : "")
				+ "happy,  Your bank account balance is " + mStatus[3].getText() + " tokens"
				, TextToSpeech.QUEUE_ADD, utterance);
    }
	
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		
		// We want at least one prediction
		if (predictions.size() > 0 && !gestureMatch)  {
			Prediction prediction = predictions.get(0);
			// We want at least some confidence in the result
			if (prediction.score > 2.0) {
				// Show the gesture
				if (t.isAlive())
					t.interrupt();
				toast.cancel();
				toast = Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT);
				toast.show();
				if (prediction.name.equals("CIRCLE")) {
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gmenu");
					selection = FEED;
					tts.speak("Feeding pet", TextToSpeech.QUEUE_FLUSH, utterance);
				} 
				else if (prediction.name.equals("TRIANGLE")) {
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gmenu");
					tts.speak("Exercising pet", TextToSpeech.QUEUE_FLUSH, utterance);
					selection = EXERCISE;
				}
				else if (prediction.name.equals("S")){
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gmenu");
			    	tts.speak("Interacting with pet", TextToSpeech.QUEUE_FLUSH, utterance);
			    	selection = INTERACT;
				}  
				else if (prediction.name.equals("VERTICAL")){
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gmenu");
					tts.speak("Selected spend and earn money", TextToSpeech.QUEUE_FLUSH, utterance);
					selection = MONEY;
				} 
				else if (prediction.name.equals("X") || prediction.name.equals("V")){
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(this);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gback");
					tts.speak("Back to main menu.", TextToSpeech.QUEUE_FLUSH, utterance);
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
		if (!activityStarted) {
			activityStarted = true;
			Intent intent = new Intent();
			intent.putExtra("gesturesMode", gesturesMode);
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			switch (which) {
			case FEED:
				intent.setClass(this, FeedPet.class);
				startActivity(intent);
//				mHandler.post(mUpdateResults);  
				break;
			case EXERCISE:
				intent.setClass(this, Play.class);
				startActivity(intent);
				mAnimal.play(1 + random.nextInt(10));
//				mHandler.post(mUpdateResults); 
				break;
			case INTERACT:
				intent.setClass(this, Groom.class);
				startActivity(intent);
				mAnimal.groom();
//				mHandler.post(mUpdateResults); 
				break;
			case MONEY: 
				intent.setClass(this, Money.class);
				startActivityForResult(intent, BrailleBuddiesMenu.LAST_MODE);
				break;
			default: //do nothing -- should never get in here
				tts.speak("Error occurred",	TextToSpeech.QUEUE_FLUSH, null);
				gestureMatch = false;
				activityStarted = false;
				break;
			}
		}
	}
	
	// On click event
	@Override
	public void onClick(View v) {
		if (t.isAlive())
			t.interrupt();
		toast.cancel();
    	switch (v.getId()){
    	
    	case R.id.feed_button:
    		tts.setOnUtteranceCompletedListener(this);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gmenu");
			selection = FEED;
			tts.speak("Feeding pet", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
    	case R.id.play_button:
			tts.setOnUtteranceCompletedListener(this);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gmenu");
    		selection = EXERCISE;
			tts.speak("Exercising pet", TextToSpeech.QUEUE_FLUSH, null);
    		break;
    	case R.id.interact_button:
			tts.setOnUtteranceCompletedListener(this);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gmenu");
    		selection = INTERACT;
	    	tts.speak("Interacting with pet", TextToSpeech.QUEUE_FLUSH, utterance);
	    	break;
    	case R.id.money_button:
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gmenu");
			selection = MONEY;
			tts.speak("Selected Spend and earn menu", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
    	case R.id.exit_button:
    		tts.setOnUtteranceCompletedListener(this);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gback");
    		tts.speak("Selected return to main menu", TextToSpeech.QUEUE_FLUSH, utterance);
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
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "gback");
    		tts.speak("back kee pressed, returning to main menu", TextToSpeech.QUEUE_FLUSH, utterance);
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_UP || keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
    		return super.onKeyDown(keyCode, keyEvent);
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
    		toast.cancel();
    		Intent intent = new Intent();
			intent.setClass(this, PetGUI.class);
			intent.putExtra("origin", "game");
			intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent);
			return true;
    	}
    	else {
    		return super.onKeyDown(keyCode, keyEvent);
    	}
    	Log.v(TAG, "onKeyDown");
    	return true;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.v(TAG, "onActivityResult; requestCode = " + requestCode + ", resultCode = " + resultCode);
    	longSpeech = false;
    	// Returning from Money menu
    	if (requestCode == BrailleBuddiesMenu.LAST_MODE) {
    		if (resultCode == RESULT_OK){
    			gesturesMode = data.getBooleanExtra("gesturesMode", true);
    		}
    		if (gesturesMode) {
    			turnOnGestures();
    		} else {
    			turnOffGestures();
    		}
    		tts.speak("Pet status and activity menu displayed", TextToSpeech.QUEUE_ADD, null);
    	}
    	else {
    		super.onActivityResult(requestCode, resultCode, data);
    	}
    }
       
	/* Called for touches that are not initial button touches */
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		Log.v(TAG, "onTouchEvent");
		detector.onTouchEvent(me);
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			longPressStartTm = me.getDownTime();
		} 
		
		// Capture long presses that occur within a single region and
		// repeat instructions.
		if (me.getEventTime() - longPressStartTm > longPressDuration) {
			longSpeech = true;
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
			tts.speak("game menu displayed, one moment please", TextToSpeech.QUEUE_FLUSH, null);
			if (gesturesMode) {
				tts.speak(gameGestureModeInstr, TextToSpeech.QUEUE_ADD, utterance);
			} else {
				tts.speak(gameTouchModeInstr, TextToSpeech.QUEUE_ADD, utterance);
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
			if (!gesturesMode && me.getDownTime() - lastUpTime > clearFocusInterval ) {
				if (lastLoc == -1) 
					status.clearFocus();
				if (lastLoc >= 0 && lastLoc < mButtons.length)
					mButtons[lastLoc].clearFocus();
			}
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
			tts.speak("game menu displayed, one moment please", TextToSpeech.QUEUE_FLUSH, null);
			if (gesturesMode) {
				tts.speak(gameGestureModeInstr, TextToSpeech.QUEUE_ADD, utterance);
			} else {
				tts.speak(gameTouchModeInstr, TextToSpeech.QUEUE_ADD, utterance);
			}
			longPressStartTm = me.getEventTime();
		}
		return true;
	}

	/* Locates the button on which the motion event occurred 
	 * and gives focus to that button.
	 */
	private boolean findKey(MotionEvent me) {
		double y = me.getRawY();
		int[] loc = new int[2];
		// See if status area is touched
		status.getLocationOnScreen(loc);
		if (y <= loc[1] + status.getHeight()) {
			if (!gesturesMode)
				status.requestFocus();
			if (lastLoc != -1) {
				longPressStartTm = me.getEventTime();
				lastLoc = -1;
			}
			return true;
		}
		// See if button is touched
		for (int i = 0; i < numOptions; i++) {
			mButtons[i].getLocationOnScreen(loc);
			// If the motion event goes over the button, have the button request focus
			if (y <= (loc[1] + mButtons[i].getHeight())) {
				if (!gesturesMode)
					mButtons[i].requestFocus();
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
		tts.speak("pet status and activity menu displayed", TextToSpeech.QUEUE_ADD, null);
	}
	
	// If the TTS instance got wiped out, restart it.
	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		tts.setOnUtteranceCompletedListener(self);
		gestureMatch = false;
		activityStarted = false;
		if (statusChanged) {
			String name = PetData.getName(this);
			tts.speak(name + "'s status has changed", TextToSpeech.QUEUE_ADD, null);
		}
		refreshStatus();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		sentInterrupt = true;
		checker.interrupt();
		if (t.isAlive())
			t.interrupt();
		if (tts != null) {
			tts.stop();
			tts.shutdown();
			tts = null;
		}
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
		if (utteranceId.equalsIgnoreCase("gmenu")) {
			tts.setOnUtteranceCompletedListener(null);
			tts.speak("", TextToSpeech.QUEUE_FLUSH, null);
			startNewActivity(selection);
		}
		else if (utteranceId.equalsIgnoreCase("gback")) {
			returnModeAsResult();
		}
		else if (utteranceId.equalsIgnoreCase("longSpeech")) {
			longSpeech = false;
		}
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (!gesturesMode && lastLoc >= 0 && lastLoc < mButtons.length)
			mButtons[lastLoc].performClick();
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
		}
		else if (gesturesMode)
			speakStatus();
		return true;
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
	public boolean onScroll(MotionEvent start, MotionEvent stop, float distanceX,
			float distanceY) {
		
		boolean result = false;
		if (scrollDistance == 0) {
			scrollDistance = (int) (layout.getWidth() / 1.25);
			scrollDistance = (scrollDistance == 0) ? 150 : scrollDistance;
		}
		
		// scroll right
		if (!gestureMatch && stop.getRawX() - start.getRawX() > scrollDistance 
				&& lastDownEvent != start) {
			gestureMatch = true;
			toast.cancel();
			lastDownEvent = start;
			Intent intent = new Intent();
    		intent.setClass(this, PetGUI.class);
    		intent.putExtra("origin", "game");
    		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
    		startActivity(intent);
			result = true;
		}
		return result;
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


    

