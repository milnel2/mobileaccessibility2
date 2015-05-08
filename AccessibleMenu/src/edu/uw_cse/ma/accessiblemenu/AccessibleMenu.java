package edu.uw_cse.ma.accessiblemenu;

import android.app.Activity;
import android.os.Bundle;
import android.os.Vibrator;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.TypedArray;

import java.util.HashMap;
import java.util.Locale;

import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.speech.tts.TextToSpeech;
import java.util.List;

import android.app.ActivityManager;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.LinearLayout;


public class AccessibleMenu extends Activity implements TextToSpeech.OnInitListener,
	View.OnTouchListener, TextToSpeech.OnUtteranceCompletedListener, 
	GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {
	
	// standard pixel lengths/velocities assuming pixel density of 160 pixels per inch
	private static final float OPTION_MINIMUM_HEIGHT = 80;
	private static final float OPTION_MINIMUM_TEXT_SIZE = 30.0f;
	private static final long OPTION_CUE = 50l;
	private static final float SWIPE_MIN_DISTANCE = 200;
	private static final float SWIPE_MAX_OFF_PATH = 200;
	private static final float SWIPE_THRESHOLD_VELOCITY = 500;
	private static final float FLING_MIN_DISTANCE = 300;
	private static final float FLING_MAX_OFF_PATH = 300;
	private static final float FLING_THRESHOLD_VELOCITY = 500;
	private static final float SUBOPTION_MIN_ACTIVATION_INTERVAL = 40.0f;
	
	// pixel values scaled to device
	private float dpsOPTION_MINIMUM_HEIGHT;
	private float spOPTION_MINIMUM_TEXT_SIZE;
	private float dpsSWIPE_MIN_DISTANCE;
	private float dpsSWIPE_MAX_OFF_PATH;
	private float dpsSWIPE_THRESHOLD_VELOCITY;
	private float dpsFLING_MIN_DISTANCE;
	private float dpsFLING_MAX_OFF_PATH;
	private float dpsFLING_THRESHOLD_VELOCITY;
	private float dpsSUBOPTION_MIN_ACTIVATION_INTERVAL;
	private float dpScale;
	
	// For logging
	private String TAG;  // set to class name; used to get all data resources for menu and for logging
	
	// Master TextToSpeech reference used throughout application
	protected static TextToSpeech tts;
	HashMap<String, String> utterance = new HashMap<String, String>();
	String greeting;
	String goodbye;
	String redisplay;
	
	// Verbal instructions -- these Strings have been synthesized to files that
	// should be in the res/raw directory but if they are removed for any reason,
	// the TextToSpeech instance will use these strings real-time as a backup.
	private String mInstr = 
		" to hear menu options, touch and drag down screen, use trackball or press directional keys;" +
		" if menu option has sub options, move your finger to the right to hear them." +
		" to select the last spoken option, double tap on screen or press the center directional key or trackball." +
		" to leave without selecting an option, press the back button on the phone or fling up with one finger.";
	
	// For user muting of speech
	private boolean ttsOn;
	private boolean longSpeech;
	private boolean mJustCreated;
	
	// Needed for touch and focus listeners
	private AccessibleMenu self = null;
	private int numOptions = 0;
	private Button[] options;
	protected String[][] optionSuboptions;
	protected String[][] optionAction;
	protected String[][][] intentExtras;
	
	// index of last touched option/suboption
	protected int lastLoc;
	protected int lastSubLoc;
	private float subOptionStartX;
	private Vibrator vibe;
	
	// for preventing touch events triggering finger scan functionality until
	// after single and double tap gestures are ruled out
	private static final long tapDetectionInterval = 600;
	
	// used for repeating option name when user retouches same option after lifting or 
	// touches the same option for a prolonged time.
	private static final long clearFocusInterval = 2500;
	private long lastClearTime = 0;
	private int focusOption;
	
	// Needed to detect double tap, single tap and flings 
	private GestureDetector detector;
	
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    mJustCreated = true;
	    
	    // get data for the menu using the class name -- allows separation of menu from data and reuse of menu code
	    TAG = this.getClass().getName();
	    TAG = TAG.substring(TAG.lastIndexOf('.') + 1);
	   
	    setContentView(R.layout.accessiblemenu);
	    
	    Log.v(TAG, "creating");
	    
	    // get resources
	    Resources res = getResources();
	    dpScale = res.getDisplayMetrics().density;
	    
	    // adjust thresholds for screen density
	    dpsOPTION_MINIMUM_HEIGHT = OPTION_MINIMUM_HEIGHT * dpScale;
	    dpsSWIPE_MIN_DISTANCE = SWIPE_MIN_DISTANCE * dpScale;
	    dpsSWIPE_MAX_OFF_PATH = SWIPE_MAX_OFF_PATH * dpScale;
	    dpsSWIPE_THRESHOLD_VELOCITY = SWIPE_THRESHOLD_VELOCITY * dpScale;
	    dpsFLING_MIN_DISTANCE = FLING_MIN_DISTANCE * dpScale;
	    dpsFLING_MAX_OFF_PATH = FLING_MAX_OFF_PATH * dpScale;
	    dpsFLING_THRESHOLD_VELOCITY = FLING_THRESHOLD_VELOCITY * dpScale;
	    dpsSUBOPTION_MIN_ACTIVATION_INTERVAL = SUBOPTION_MIN_ACTIVATION_INTERVAL * dpScale;
		spOPTION_MINIMUM_TEXT_SIZE = OPTION_MINIMUM_TEXT_SIZE * res.getDisplayMetrics().scaledDensity;
	    
	    greeting = getPackageName() + ":string/" + TAG.toLowerCase() + "_greeting";
	    int id = res.getIdentifier(greeting, null, null);
		if (id == 0) {
			greeting = "on " + TAG;
		}
		else greeting = res.getString(id);
		
	    goodbye = getPackageName() + ":string/" + TAG.toLowerCase() + "_goodbye";
	    id = res.getIdentifier(goodbye, null, null);
		if (id == 0) {
			goodbye = "Returning.";
		}
		else goodbye = res.getString(id);
		
		redisplay = getPackageName() + ":string/" + TAG.toLowerCase() + "_redisplay";
	    id = res.getIdentifier(redisplay, null, null);
		if (id == 0) {
			redisplay = "on " + TAG;
		}
		else redisplay = res.getString(id);
	    
		//create TextToSpeech    
	    tts = GlobalState.getTTS();    
	    if (tts == null) {
	    	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
	        tts = gs.createTTS(this, this);
	    }
	    else tts.speak(greeting, TextToSpeech.QUEUE_ADD, null);
	    Log.v(TAG, "tts = " + tts);
	    
	    // Save reference of activity for use when assigning
	    // listeners
	    self = this;
	    
	    // Check whether user wants to hear speech -- might not
	    // want to if using a screen reader
	    ttsOn = true;
	    longSpeech = false;
	    
	    // Initialize last location touched
	    lastLoc = -1;
	    lastSubLoc = 0;
	    
	    // for clearing focus so repeat can occur
	    focusOption = -1;  
	    
	    createMenuOptions(res); 
	    
	    // Keep menu in portrait layout
	    this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	    
	    detector = new GestureDetector(this, this);
	    vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
	}
		
	/* Read in the data for this instance of AccessibleMenu and set up the options
	 * and suboptions for the menu.
	 */
	private void createMenuOptions(Resources res) 
	{
	    LinearLayout layout = (LinearLayout)findViewById(R.id.accessiblemenu_options_layout);
	    layout.setOnTouchListener(this);
	    LinearLayout omlayout = (LinearLayout)findViewById(R.id.accessiblemenu_offmenu_layout);
	    omlayout.setOnTouchListener(this);
	        
	    // Get data for this subclass of AccessibleMenu
	    String arrayName = getPackageName() + ":array/" + TAG.toLowerCase() + "_option_text_array";
	    int id = res.getIdentifier(arrayName, null, null);
		if (id == 0) {
			Log.v(TAG, "Cannot find resource: " + arrayName);
			return;
		}
		String[] optionText = res.getStringArray(id);
		
		arrayName = getPackageName() + ":array/" + TAG.toLowerCase() + "_option_tag_array";
	    id = res.getIdentifier(arrayName, null, null);
		if (id == 0) {
			Log.v(TAG, "Cannot find resource: " + arrayName);
			return;
		}
		String[] optionTag = res.getStringArray(id);
	
		arrayName = getPackageName() + ":array/" + TAG.toLowerCase() + "_option_attributes_array";
	    id = res.getIdentifier(arrayName, null, null);
		if (id == 0) {
			Log.v(TAG, "Cannot find resource: " + arrayName);
			return;
		}
	    TypedArray optionAttributes = res.obtainTypedArray(id);
	 
	    // Convert the dps to pixels
		float height = optionAttributes.getInt(4, 1);
		height = height * dpScale + 0.5f;
		if (height < dpsOPTION_MINIMUM_HEIGHT) height = dpsOPTION_MINIMUM_HEIGHT;
		
		float textSize = optionAttributes.getFloat(0, 1);
		textSize = (textSize * res.getDisplayMetrics().scaledDensity) + 0.5f;
		if (textSize < spOPTION_MINIMUM_TEXT_SIZE) textSize = spOPTION_MINIMUM_TEXT_SIZE;
		
	    
	    numOptions = optionText.length + 1;       
	    options =  new Button[numOptions];
		optionSuboptions = new String[numOptions][];
		optionAction = new String[numOptions][];
		intentExtras = new String[numOptions][][];
		
		for (int i = 0; i < numOptions - 1; i++) {
			options[i] = new Button(self);
			options[i].setText(optionText[i]);
			Log.v(TAG, "text: " + options[i].getText());
			options[i].setOnTouchListener(this);
			options[i].setFocusableInTouchMode(true);
			options[i].setFocusable(true);
			// speak the tag value, which has phonetic spelling for 
			// some letters that aren't pronounced correctly if just a single
			// letter is given
			final int t = i;
			final String tag = optionTag[t];   
			final int text = optionAttributes.getColor(1, R.color.text);
			final int hilite = optionAttributes.getColor(3, R.color.text_highlight);
			options[i].setTag(tag);
			options[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						Log.v(TAG, "Option " + t + " has focus.");
						if (ttsOn && focusOption != t) tts.speak(tag, TextToSpeech.QUEUE_FLUSH, null);
						options[t].setTextColor(hilite);
						focusOption = t;
					}
					else {
						options[t].setTextColor(text);
					}
				}
			});
			
			// set suboption flag
			arrayName = getPackageName() + ":array/" + TAG.toLowerCase() + "_option" + (i + 1) + "_suboptions_array";
			id = res.getIdentifier(arrayName, null, null);
			if (id != 0) {
				Log.v(TAG, "Option " + t + " has suboptions.");
				optionSuboptions[i] = res.getStringArray(id);
			}
			else {
				optionSuboptions[i] = null;
			}
			
			// set actions
			arrayName = getPackageName() + ":array/" + TAG.toLowerCase() + "_option" + (i + 1) + "_action_array";
			id = res.getIdentifier(arrayName, null, null);
			if (id != 0) {
				Log.v(TAG, "Option " + t + " has actions.");
				optionAction[i] = res.getStringArray(id);
			}
			else {
				optionAction[i] = new String[1];
				optionAction[i][0] = "UNKNOWN";  // calls 'UnknownActivity' activity provided with AccessibleMenu package
			}
			
			// set attributes
			options[i].setTextSize(textSize);
			options[i].setTextColor(text);
			options[i].setBackgroundColor(optionAttributes.getColor(2, 0));
			options[i].setHeight((int)height);
			options[i].setHighlightColor(hilite);
			layout.addView(options[i]);
		}
				
		// set up end of menu button
		options[numOptions - 1] = (Button)findViewById(R.id.no_more_options);
		options[numOptions - 1].setBackgroundColor(optionAttributes.getColor(2, 0));
		options[numOptions - 1].setOnTouchListener(this);
		options[numOptions - 1].setFocusableInTouchMode(true);
		options[numOptions - 1].setFocusable(true);
		final int t = numOptions - 1;
		options[numOptions - 1].setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && ttsOn) {
					Log.v(TAG, "Option " + t + " has focus.");
					tts.speak((String)options[t].getTag(), TextToSpeech.QUEUE_FLUSH, null);
				}
			}
		});
		optionSuboptions[numOptions - 1] = null;
	}
	
	@Override
	public void onInit(int status) {
		
		// Check whether the user is currently running the TalkBack screen reader service.
		// If so, let the user know (s)he should turn off the speech in the game to prevent
		// redundant instructions.
		ActivityManager actvityManager = (ActivityManager)self.getSystemService(ACTIVITY_SERVICE);
		List<ActivityManager.RunningServiceInfo>  procInfos = actvityManager.getRunningServices(50);
		
		for(int i = 0; i < procInfos.size(); i++)
		{
			if (procInfos.get(i).service.toString().contains(".talkback")) {
				if (ttsOn)
					tts.speak("TalkBack screen reader is running and will cause menu options to be repeated", TextToSpeech.QUEUE_FLUSH, null);
			}
		}
		
		if (mJustCreated) tts.speak(greeting, TextToSpeech.QUEUE_ADD, null);
		else tts.speak(redisplay, TextToSpeech.QUEUE_ADD, null);
	}
	
	private void select() {
		Log.v(TAG, "in select()");
		tts.setOnUtteranceCompletedListener(self);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "selected");
		if (lastSubLoc > 0 && optionSuboptions[lastLoc] != null && lastSubLoc <= optionSuboptions[lastLoc].length) {		  		
			tts.speak("Selected " + optionSuboptions[lastLoc][lastSubLoc - 1], TextToSpeech.QUEUE_FLUSH, utterance);
		}
		else {
			lastSubLoc = 0;
			tts.speak("Selected " + options[lastLoc].getTag(), TextToSpeech.QUEUE_FLUSH, utterance);
		}
	}
	
	private void returnToCaller() 
	{
		tts.setOnUtteranceCompletedListener(self);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "exit");
		tts.speak(goodbye, TextToSpeech.QUEUE_ADD, utterance);
	}
	
	private void giveHapticCue(long type)
	{
		vibe.vibrate(type);
	}
	
	// Alert player if hard key is pressed.
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {  
		Log.v(TAG, "onKeyDown");
		if (keyCode == KeyEvent.KEYCODE_MENU){
			// ignore
		}
		else if (keyCode == KeyEvent.KEYCODE_BACK) {
			tts.speak("Back kee pressed.", TextToSpeech.QUEUE_FLUSH, null);
			returnToCaller();
		}
		else if (keyCode == KeyEvent.KEYCODE_ENTER || keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			Log.v(TAG, "Center key");
			if (lastLoc >= 0 && lastLoc < options.length) {
				select();
			}
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_UP ) {
			return prevOption();
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			return nextOption();
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT ) {
			return nextSuboption();
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			return prevSuboption();
		}
		else {
			Log.v(TAG, "Keycode = " + keyCode);
			return super.onKeyDown(keyCode, keyEvent);
		}
		return true;
	}
	
	// If the TTS instance got wiped out, restart it.
	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		
		//create TextToSpeech    
	    tts = GlobalState.getTTS();    
	    if (tts == null) {
	    	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
	        tts = gs.createTTS(this, this);
	    }
	    else if (!mJustCreated) tts.speak(redisplay, TextToSpeech.QUEUE_FLUSH, null);
	    Log.v(TAG, "tts = " + tts);
		tts.setOnUtteranceCompletedListener(self);
		mJustCreated = false;
	}
	
	@Override
	public void onPause() {
		super.onPause();
		Log.v(TAG, "onPause");
		if (isFinishing()) onFinishing();
	}
	
	// Overridable method - for use when accessible menu is not the main activity that is last to be destroyed
	public void onFinishing()
	{
		GlobalState gs = (GlobalState)getApplication();
		gs.killTTS();
	}	
	   
	/* Called for touches inside the button display */
	@Override
	public boolean onTouch(View v, MotionEvent me) {
		Log.v(TAG, "onTouch");
		detector.onTouchEvent(me);
		
		if (me.getAction() == MotionEvent.ACTION_MOVE && me.getEventTime() - me.getDownTime() > tapDetectionInterval) {
			findOption(me);
		} 
		else if (me.getAction() == MotionEvent.ACTION_UP) {
			focusOption = -1;
		}
		return true;
	}
	
	/* Locates the option on which the motion event occurred 
	 * and gives focus to that option.  Only uses the Y
	 * coordinate because options are full screen width and
	 * run vertically down screen.  Option ids must be stored
	 * in array in the same order they are displayed on screen.
	 */
	private boolean findOption(MotionEvent me) {
		double y = me.getRawY();
		Log.v(TAG, "findOption - y: " + y);
		int[] loc = new int[2];
		for (int i = 0; i < numOptions; i++) {
			options[i].getLocationOnScreen(loc);
			Log.v(TAG, "y1: " + loc[1] + ", y2: " + options[i].getHeight());
			// If the motion event goes over the option, have the option request focus
			if (y <= (loc[1] + options[i].getHeight())) {
				
				// if finger is on option with suboptions and has moved more than
				// the scroll interval to the right of the starting X touch point
				// for this option...
				//Log.v(TAG, "startX: " + subOptionStartX + ", currX: " + me.getRawX());
				// L-Select Gesture
				if (i == lastLoc && focusOption == i && optionSuboptions[i] != null && me.getRawX() - subOptionStartX > dpsSUBOPTION_MIN_ACTIVATION_INTERVAL) {
					Log.v(TAG, "In suboption.");
					if (lastSubLoc == 0) tts.playEarcon("suboptions", TextToSpeech.QUEUE_FLUSH, null);
					float delta = (me.getRawX() - subOptionStartX) / dpsSUBOPTION_MIN_ACTIVATION_INTERVAL;
					int subLoc = (int)delta;
					Log.v(TAG, "subLoc = " + subLoc);
					if (subLoc != lastSubLoc && subLoc > 0 && subLoc <= optionSuboptions[i].length) {
						tts.speak(optionSuboptions[i][subLoc - 1], TextToSpeech.QUEUE_ADD, null);
						lastSubLoc = subLoc;
					}
				}
				else {	
					lastSubLoc = 0;
					
					// if option changes from previously touched option or if finger has just touched down...
					if (i != lastLoc || focusOption ==  -1) {
						giveHapticCue(OPTION_CUE);
						subOptionStartX = me.getRawX();
						clearFocus(me);  // clear now rather than when finger went up so
					}
					
					// if finger has been touching option for a while, clear focus so option
					// name will be repeated.
					else if (i == lastLoc && me.getEventTime() - lastClearTime > clearFocusInterval 
							&& lastLoc >= 0 && lastLoc < numOptions) {
						clearFocus(me);
						focusOption = -1;
					}		
					// have option request focus.  If it doesn't already have focus,
					// it's name will be spoken.
					Log.v(TAG, "Option " + i + " requested focus.");	
					options[i].requestFocus();
				}
				lastLoc = i;
				return true;
			}
		}
		return false;
	}
	
	private void clearFocus(MotionEvent me) {
		lastClearTime = me.getEventTime();
		if (lastLoc >= 0 && lastLoc < numOptions) options[lastLoc].clearFocus();
		Log.v(TAG, "clearFocus():  Option " + lastLoc + " cleared focus.");
	}
	
	/* for trackball/dpad down movement */
	private boolean nextOption() {
		Log.v(TAG, "in nextOption(). lastLoc = " + lastLoc);
		lastSubLoc = 0;
		lastLoc++;
		if (lastLoc >= options.length) {
			tts.speak("At bottom", TextToSpeech.QUEUE_FLUSH, null);
			lastLoc = options.length - 1;
		}
		options[lastLoc].requestFocus();
		return true;
	}
	
	/* for trackball/dpad up movement */
	private boolean prevOption() {
		Log.v(TAG, "in prevOption(). lastLoc = " + lastLoc);
		lastSubLoc = 0;
		lastLoc--;
		if (lastLoc < 0) {
			tts.speak("At top of menu", TextToSpeech.QUEUE_FLUSH, null);
			lastLoc = 0;
		}
		options[lastLoc].requestFocus();
		return true;
	}
	
	
	/* for trackball/dpad right movement */
	private boolean nextSuboption() {
		Log.v(TAG, "in nextSuboption(). lastLoc = " + lastLoc + ", lastSubLoc = " + lastSubLoc);
		if (lastLoc >= 0 && lastLoc < options.length && optionSuboptions[lastLoc] != null
				&& lastSubLoc < optionSuboptions[lastLoc].length) {
			lastSubLoc++;
			tts.speak(optionSuboptions[lastLoc][lastSubLoc - 1], TextToSpeech.QUEUE_FLUSH, null);
		}
		return true;
	}
	
	/* for trackball/left up movement */
	private boolean prevSuboption() {
		Log.v(TAG, "in prevSuboption(). lastLoc = " + lastLoc  + ", lastSubLoc = " + lastSubLoc);
		if (lastLoc >= 0 && lastLoc < options.length && optionSuboptions[lastLoc] != null
				&& lastSubLoc > 0) {
			lastSubLoc--;
			if (lastSubLoc == 0) {
				options[lastLoc].clearFocus();
				options[lastLoc].requestFocus();
			}
			else tts.speak(optionSuboptions[lastLoc][lastSubLoc - 1], TextToSpeech.QUEUE_FLUSH, null);
		}
		return true;
	}
	
	//override this method to do more with intents
	protected void makeSelection() {
		Intent intent = new Intent();
		intent.setAction(optionAction[lastLoc][lastSubLoc]);
		intent.putExtra("Option", lastLoc);
		intent.putExtra("SubOption", lastSubLoc);
		startActivity(intent);
	}
	
	
	@Override
	public void onUtteranceCompleted(String utteranceId) {
		if (utteranceId.equalsIgnoreCase("selected")) {
			tts.setOnUtteranceCompletedListener(null);
			makeSelection();
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
		Log.v(TAG, "onDoubleTap");
		if (lastLoc >= 0 && lastLoc < options.length) {
			select();
		}
		return false;
	}
	
	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}
	
	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		Log.v(TAG, "onSingleTapConfirmed");
		if (longSpeech) { 
			tts.speak("Speech stopped", TextToSpeech.QUEUE_FLUSH, null);
			longSpeech = false;
		}
		findOption(e);
		return true;
	}
	
	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}
	
	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if (Math.abs(e1.getX() - e2.getX()) > dpsSWIPE_MIN_DISTANCE && Math.abs(velocityX) > dpsSWIPE_THRESHOLD_VELOCITY
				&& Math.abs(e1.getY() - e2.getY()) < dpsSWIPE_MAX_OFF_PATH) {
			Log.v(TAG, "onFling(): horizontal fling");	
		}
		else if (Math.abs(velocityY) > dpsFLING_THRESHOLD_VELOCITY	&& Math.abs(e1.getX() - e2.getX()) < dpsFLING_MAX_OFF_PATH) {
			if (e1.getY() + dpsFLING_MIN_DISTANCE < e2.getY()) {
				tts.playEarcon("flingdown", TextToSpeech.QUEUE_FLUSH, null);
				Log.v(TAG, "onFling(): downward fling");
				tts.setOnUtteranceCompletedListener(self);
	    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
	    		longSpeech = true;
	    		tts.speak(mInstr, TextToSpeech.QUEUE_ADD, utterance);
			}
			else if (e1.getY() - e2.getY() > dpsFLING_MIN_DISTANCE) {
				tts.playEarcon("flingup", TextToSpeech.QUEUE_FLUSH, null);
				Log.v(TAG, "onFling(): upward fling");
				returnToCaller();
			}
		}
		return false;
	}
	
	@Override
	public void onLongPress(MotionEvent e) {
		
	}
	
	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// capture right moves for suboptions
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
