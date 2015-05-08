package cs.washington.edu.buddies;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Handler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

// for gestures
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
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
import java.util.List;

import android.app.ActivityManager;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


public class BrailleBuddiesMenu extends Activity implements OnClickListener, OnGesturePerformedListener, 
	TextToSpeech.OnInitListener, View.OnTouchListener, TextToSpeech.OnUtteranceCompletedListener,
	GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener, ToastDisplay {
	
	// For logging
	private static final String TAG = "BrailleBuddiesMenu";
	
	// Master TextToSpeech reference used throughout application
    protected static TextToSpeech tts;
    HashMap<String, String> utterance = new HashMap<String, String>();
    private int selection;
    
    // speech synthesis files
    private static final String gestureInstrFilenm = 
    	"/sdcard/brailleBud/gestureinstr.wav";
    private static final String touchInstrFilenm = 
    	"/sdcard/brailleBud/touchinstr.wav";
    private File filedir = new File("/sdcard/brailleBud/");
        
    // For gestures
	public static GestureLibrary mLibrary;
	private boolean gestureMatch;
	
	// Verbal instructions -- these Strings have been synthesized to files that
	// should be in the res/raw directory but if they are removed for any reason,
	// the TextToSpeech instance will use these strings real-time as a backup.
	private String gestureModeInstr = 
		" draw won of the following symbols to navigate: Circle to play, " +
		" Triangle for instructions, S for settings, or X to exit," +
		" press the Menu kee to change to touch mode";
	private String touchModeInstr = 
		" touch and drag down screen, use trackball or use directional keys" +
		" to hear menu options; double tap or press the center key or trackball to select " +
		" a menu option; press the Menu kee to change to gestures mode";
	
	// For user muting of speech
	private boolean ttsOn;
	private boolean longSpeech;
	
	// Needed for button listeners & touch mode
	private BrailleBuddiesMenu self = null;
    private int numBtns;
	private Button[] buttons;
	private Button blankBtn;
	
	// Vars for catching long press events
	private long longPressStartTm;
    private long longPressDuration;
    private int lastLoc;
    
    // for retouches on same button after lifting
    private int clearFocusInterval;
    private long lastUpTime = 0;
    
//*** Removed triple tap.  For code example, see old version of this file
//    // Vars for catching triple taps
//    private long[] tapTimes;
//    private int tapCount;
//    private long tripleTapInterval;
    
    // Needed to switch between gesture/touch mode  
    protected static final int LAST_MODE = 11;
    private boolean gesturesMode;
    GestureOverlayView gestures;
    private GestureDetector detector;
	
    // For starting new activities
    private static final int PLAY = 0;
    private static final int INSTRUCTIONS = 1;
    private static final int SETTINGS = 2;
    
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
        setContentView(R.layout.main);
        
        Log.v(TAG, "creating");
        
        //create TextToSpeech
        tts = new TextToSpeech(this, this);
        tts.setLanguage(Locale.US);
        
        //create Gestures
        mLibrary = GestureLibraries.fromRawResource(this, R.raw.gesturesupdated);
        if (!mLibrary.load()) {
        	finish();
        }
        
        // Save reference of activity for use when assigning
        // listeners
        self = this;
        
        // Check whether user wants to hear speech -- might not
        // want to if using a screen reader
        ttsOn = Settings.getTts(this);
        longSpeech = false;
        
        // Set time requirements for long presses and triple taps
        longPressDuration = 3000;
        lastLoc = -1;
        
        // for clearing focus so repeat can occur
        clearFocusInterval = 1500;
        
        // Toast display thread
        t = new Thread(new ToastDisplayer(this, mHandler, toastMsgs.length));
		t.start();

        // Initialize buttons with listeners
        numBtns = 4;
		buttons =  new Button[numBtns];
		findButtonViews();
        setListeners(); 
        LinearLayout layout = (LinearLayout)findViewById(R.id.llayout);
        layout.setOnTouchListener(self);
        
        // Start in gestures mode
        gestures = (GestureOverlayView) findViewById(R.id.gestures);
        gestures.setEventsInterceptionEnabled(false);
        turnOnGestures();
        
        // Keep menu in portrait layout
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        /*************************************** 
         * MONEY FOR DEMO
         */
        //BankAccount.addToBalance(this, 100);
        
        detector = new GestureDetector(this, this);
        
    }
	
	/* Turns on gesture mode for navigation and turns off touch buttons */
	private void turnOnGestures() {
		gestures.addOnGesturePerformedListener(this);
        gesturesMode = true;
        // prevent focus on buttons
        blankBtn = (Button)findViewById(R.id.keypad_blank);
        
        blankBtn.requestFocus();
        tts.speak("Gesture mode activated", TextToSpeech.QUEUE_ADD, null);
	}
	
	/* Turns on touch button mode for navigation and turns off gestures */
	private void turnOffGestures() {
		gestures.removeAllOnGesturePerformedListeners();
		gesturesMode = false;
        tts.speak("touch mode activated,", TextToSpeech.QUEUE_ADD, null);
	}
	
	/* Store the handle of each button on the menu so that listeners can
	 * quickly be added/removed inside a for loop.
	 */
	private void findButtonViews() {
		buttons[0] = (Button)findViewById(R.id.play_button);
		buttons[1] = (Button)findViewById(R.id.instructions_button);
		buttons[2] = (Button)findViewById(R.id.settings_button);
		buttons[3] = (Button)findViewById(R.id.exit_button);
	}
	
	
	/* Set touch and click listeners for each button.
	 * Also be sure buttons are touch focusable
	 */
	private void setListeners() {
		for (int i = 0; i < numBtns; i++) {
			buttons[i].setOnTouchListener(this);
			buttons[i].isFocusableInTouchMode();
			buttons[i].setOnClickListener(this);
			// speak the button.tag value, which has phonetic spelling for 
			// some letters that aren't pronounced correctly if just a single
			// letter is given
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
	
	@Override
	public void onInit(int status) {
		
		// Check whether the user is currently running the TalkBack screen reader service.
		// If so, let the user know (s)he should turn off the speech in the game to prevent
		// redundant instructions.
		ActivityManager actvityManager = (ActivityManager)self.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo>  procInfos = actvityManager.getRunningServices(50);
		
		ttsOn = Settings.getTts(self); //Settings.getSettings(self).getTts();
		for(int i = 0; i < procInfos.size(); i++)
		{
			if (procInfos.get(i).service.toString().contains(".talkback")) {
				if (ttsOn)
					tts.speak("TalkBack screen reader is running, you should go to the Braille Buddies"
							+ "settings menu and turn off Reed Menus option", TextToSpeech.QUEUE_FLUSH, null);
			}
		}
		
		/*************** COMMENTED OUT -- SYTHESIZED FILES ARE NOW LOCATED IN THE RES/RAW DIRECTORY ***********
		 *     			KEEP THIS CODE IN CASE INSTRUCTIONS CHANGE AND FILES MUST BE RE-SYTHESIZED */
//		filedir.mkdirs();
//        filedir = new File(gestureInstrFilenm);
//        if (!filedir.exists()) {
//	        try {
//				filedir.createNewFile();
//			} catch (IOException e) {
//			}
//        }
//       filedir = new File(touchInstrFilenm);
//        if (!filedir.exists()) {
//	        try {
//				filedir.createNewFile();
//			} catch (IOException e) {
//			}
//        }
//        HashMap<String,String> params = new HashMap<String,String>();
//        params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, gestureModeInstr);
//        tts.synthesizeToFile(gestureModeInstr, params, gestureInstrFilenm);
//        HashMap<String,String> touchparams = new HashMap<String,String>();
//        touchparams.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, touchModeInstr);
//        tts.synthesizeToFile(touchModeInstr, touchparams, touchInstrFilenm);
        
//        tts.addSpeech("touch", touchInstrFilenm);
//        tts.addSpeech("gest", gestureInstrFilenm);
		/********************************************************************************************************/
		
        tts.addSpeech(touchModeInstr, this.getPackageName(), R.raw.touchinstr);
        tts.addSpeech(gestureModeInstr, this.getPackageName(), R.raw.gestureinstr );
              
        tts.speak("Welcome to Braille Buddies,", TextToSpeech.QUEUE_ADD, null);
		longSpeech = true;
		
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
        tts.speak(gestureModeInstr, TextToSpeech.QUEUE_ADD, utterance);
        tts.speak("touch and hold on screen at any time to repeat instructions", 
        		TextToSpeech.QUEUE_ADD, null);
	}

	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		
		// We want at least one prediction
		if (predictions.size() > 0 && !gestureMatch) {
			Prediction prediction = predictions.get(0);
			// We want at least some confidence in the result
			if (prediction.score > 1.5) {
				// Show the gesture
				if (t.isAlive())
					t.interrupt();
				toast.cancel();
				toast = Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT);
				toast.show();
				//User drew symbol for PLAY
				if (prediction.name.equals("CIRCLE")) {
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "submenu");
					selection = PLAY;
					tts.speak("Selected play option", TextToSpeech.QUEUE_FLUSH, utterance);
				} 
				// User drew symbol for INSTRUCTIONS
				else if (prediction.name.equals("TRIANGLE")) {
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "submenu");
					selection = INSTRUCTIONS;
					tts.speak("Selected instructions option", TextToSpeech.QUEUE_FLUSH, utterance);
				}
				// User drew symbol for SETTINGS
				else if (prediction.name.equals("S")){
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "submenu");
					selection = SETTINGS;
					tts.speak("Selected settings option", TextToSpeech.QUEUE_FLUSH, utterance);
				} 
				// User drew symbol to QUIT
				else if (prediction.name.equals("X") || prediction.name.equals("V")){
					gestureMatch = true;
					tts.setOnUtteranceCompletedListener(self);
		    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "exit");
		    		tts.speak("Selected exit, Goodbye!", TextToSpeech.QUEUE_FLUSH, utterance);
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
		case PLAY:
			if (PetData.getFirstTime(this)) {
				intent.setClass(this, PetGUI.class);
				intent.putExtra("origin", "menu");
				PetData.setFirstTime(this, false);
			}
			else
				intent.setClass(this, Game.class);
			break;
		case INSTRUCTIONS:
			intent.setClass(this, Instructions.class);
			break;
		case SETTINGS: 
			intent.setClass(this, SettingsGUI.class);
			break;
		default: //do nothing -- should never get in here
			tts.speak("Error occurred",	TextToSpeech.QUEUE_FLUSH, null);
			gestureMatch = false;
			return;
		}
		intent.putExtra("gesturesMode", gesturesMode);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivityForResult(intent, LAST_MODE);
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.v(TAG, "onActivityResult; requestCode = " + requestCode + ", resultCode = " + resultCode);
    	longSpeech = false;
    	if (requestCode == LAST_MODE) {
    		if (resultCode == RESULT_OK){
    			gesturesMode = data.getBooleanExtra("gesturesMode", true);
    		}
    		if (gesturesMode) {
    			turnOnGestures();
    		} else {
    			turnOffGestures();
    		}
    		tts.speak("Main menu displayed", TextToSpeech.QUEUE_ADD, null);
    	}
    	else {
    		super.onActivityResult(requestCode, resultCode, data);
    	}
    }

	// On click event
	@Override
	public void onClick(View v) {
		if (t.isAlive())
			t.interrupt();
		toast.cancel();
    	switch (v.getId()){
    	case R.id.play_button:
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "submenu");
    		selection = PLAY;
    		tts.speak("Selected play option", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
    	case R.id.instructions_button:
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "submenu");
    		selection = INSTRUCTIONS;
    		tts.speak("Selected instructions option", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
    	case R.id.settings_button:
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "submenu");
    		selection = SETTINGS;
    		tts.speak("Selected settings option, , one moment please while I retrieve settings",
    				TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
    	case R.id.exit_button:
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "exit");
    		tts.speak("Selected exit, Goodbye!", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
    	}		
	}
    
	// Alert player if hard key is pressed.
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {  
    	if(keyCode == KeyEvent.KEYCODE_MENU){
    		tts.speak("", TextToSpeech.QUEUE_FLUSH, null);
    		if (gesturesMode) {
				tts.speak("menu kee pressed, Turning off gestures", TextToSpeech.QUEUE_FLUSH, null);
				turnOffGestures();
			} else {
				tts.speak("menu kee pressed, Turning on gestures", TextToSpeech.QUEUE_FLUSH, null);
				turnOnGestures();
			}
    	}
    	else if (keyCode == KeyEvent.KEYCODE_BACK) {
    		if (t.isAlive())
				t.interrupt();
    		toast.cancel();
    		tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "exit");
    		tts.speak("Back kee pressed, ending game, goodbye!", TextToSpeech.QUEUE_FLUSH, utterance);
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
    
	// If the TTS instance got wiped out, restart it.
	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		if (tts == null) {
			tts = new TextToSpeech(getApplicationContext(), this);
		}
		tts.setOnUtteranceCompletedListener(self);
		gestureMatch = false;

	}
	
	// Shutdown the TTS instance when this activity is destroyed
	@Override
	public void onDestroy() {
		Log.v(TAG, "onDestroy");
		if (tts != null) {
			tts.shutdown();
			tts = null;
		}
		if (t.isAlive())
			t.interrupt();
		super.onDestroy();
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
			tts.speak("Main menu displayed,", TextToSpeech.QUEUE_FLUSH, null);
			if (gesturesMode) {
				tts.speak(gestureModeInstr, TextToSpeech.QUEUE_ADD, utterance);
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
			tts.speak("Main menu displayed,", TextToSpeech.QUEUE_FLUSH, null);
			if (gesturesMode) {	
				tts.speak(gestureModeInstr, TextToSpeech.QUEUE_ADD, utterance);
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
	public void onUtteranceCompleted(String utteranceId) {
		if (utteranceId.equalsIgnoreCase("submenu")) {
			tts.setOnUtteranceCompletedListener(null);
			startNewActivity(selection);
		}
		else if (utteranceId.equalsIgnoreCase("exit")) {
			finish();
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
		}
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
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

