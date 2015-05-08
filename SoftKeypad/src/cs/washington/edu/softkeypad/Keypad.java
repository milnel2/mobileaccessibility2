package cs.washington.edu.softkeypad;

import java.util.List;

import android.app.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

/* Soft touch keypad for entering answers */
public class Keypad extends Activity implements View.OnTouchListener, 
	GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {
	
	private static final String TAG = "Keypad";
	public static final int RESULT_CORRECT_ANSWER = RESULT_FIRST_USER + 1;
	public static final String[] alphabet =
    {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
    	"p","q","r","s","t","u","v","w","x","y","z"};
	// for preventing touch events triggering finger scan functionality until
    // after single and double tap gestures are ruled out
    private static final long tapDetectionInterval = 600;
    // used for repeating option name when user retouches same option after lifting or 
    // touches the same option for a prolonged time.
    private static final long clearFocusInterval = 2500;
    private static final long OPTION_CUE = 50l;
	
    private TextToSpeech tts;
    private int numBtns;
	private Button[] keys;
	private String answer = "";
	private Button blankBtn;
	private int lastKey = -1;
	private boolean multiKey;
	private boolean asWord;
	private Button backspaceKey;
	private Button ansKey;
	private Button replayKey;
	private Button speakKey;
	private Button submitKey;
	private TextView input;
    private GestureDetector detector;
    private long lastSpeakTime = 0;
    private Vibrator vibe;


    // for shake to erase
    private SensorManager sensorManager; 
    private static final int SHAKE_COUNT = 4; 
    private static final int SHAKE_THRESHOLD = 500;
    private static final long SHAKES_INTERVAL= 5000000000L;
    private long[] mShakeTimes;
	private long lastUpdate = -1;
	private float x, y, z;
	private float last_x, last_y, last_z;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
      
        //create TextToSpeech
        GlobalState gs = (GlobalState)getApplication();
        tts = gs.getTTS();
        tts.speak("Please hold phone in landscape orientation", TextToSpeech.QUEUE_ADD, null);
        
        multiKey = false;
        int option = this.getIntent().getIntExtra("Option", MainMenu.QWERTY_LETTER);
        
        if (option < MainMenu.NUMERIC) {
        	numBtns = 31;
        	keys =  new Button[numBtns];
        	if (option <= MainMenu.QWERTY_WORD) {
	        	setContentView(R.layout.qwerty);
	        	findQwertyViews();
        	}
        	else {
        		setContentView(R.layout.alphakeypad);
        		findAlphaViews();
        	}
        	if (option % 2 == 0) {  // letter mode
	        	keys[26].setVisibility(View.INVISIBLE);
	        	keys[29].setVisibility(View.INVISIBLE);
	        	keys[30].setVisibility(View.INVISIBLE);
        	}
        	else {  // word mode
        		multiKey = true;
        		answer = "";  
        		asWord = false;
        		backspaceKey = keys[26];
            	ansKey = keys[27];
            	replayKey = keys[28];
            	speakKey = keys[29];
            	submitKey = keys[30];
        	}
        }
        else if (option == MainMenu.NUMERIC) {
        	numBtns = 12;
        	keys =  new Button[numBtns];
        	setContentView(R.layout.numkeypad);
        	findNumberViews();
        }
        else {  // punctuation
        	numBtns = 12;
        	keys =  new Button[numBtns];
            setContentView(R.layout.punctkeypad);
            findPunctViews();
        }

        setListeners();
        blankBtn = (Button)findViewById(R.id.keypad_blank);
        blankBtn.setOnTouchListener(this);
        blankBtn.requestFocus();
        if (input != null) input.setOnTouchListener(this);
        LinearLayout ll = (LinearLayout)findViewById(R.id.softkeypad);
        ll.setOnTouchListener(this);
        
        // For erasing letters
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
        vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        
        // Keep menu in landscape layout
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Log.v(TAG, "creating");
        detector = new GestureDetector(this, this);
    }
    
    /* Store the handle of each button on the keypad */
    private void findAlphaViews() {
    	keys[0] = (Button)findViewById(R.id.keypad_a);
    	keys[1] = (Button)findViewById(R.id.keypad_b);
    	keys[2] = (Button)findViewById(R.id.keypad_c);
    	keys[3] = (Button)findViewById(R.id.keypad_d);
    	keys[4] = (Button)findViewById(R.id.keypad_e);
    	keys[5] = (Button)findViewById(R.id.keypad_f);
    	keys[6] = (Button)findViewById(R.id.keypad_g);
    	keys[7] = (Button)findViewById(R.id.keypad_h);
    	keys[8] = (Button)findViewById(R.id.keypad_i);
    	keys[9] = (Button)findViewById(R.id.keypad_j);
    	keys[10] = (Button)findViewById(R.id.keypad_k);
    	keys[11] = (Button)findViewById(R.id.keypad_l);
    	keys[12] = (Button)findViewById(R.id.keypad_m);
    	keys[13] = (Button)findViewById(R.id.keypad_n);
    	keys[14] = (Button)findViewById(R.id.keypad_o);
    	keys[15] = (Button)findViewById(R.id.keypad_p);
    	keys[16] = (Button)findViewById(R.id.keypad_q);
    	keys[17] = (Button)findViewById(R.id.keypad_r);
    	keys[18] = (Button)findViewById(R.id.keypad_s);
    	keys[19] = (Button)findViewById(R.id.keypad_t);
    	keys[20] = (Button)findViewById(R.id.keypad_u);
    	keys[21] = (Button)findViewById(R.id.keypad_v);
    	keys[22] = (Button)findViewById(R.id.keypad_w);
    	keys[23] = (Button)findViewById(R.id.keypad_x);
    	keys[24] = (Button)findViewById(R.id.keypad_y);
    	keys[25] = (Button)findViewById(R.id.keypad_z);
    	keys[26] = (Button)findViewById(R.id.keypad_bs);
    	keys[27] = (Button)findViewById(R.id.keypad_ans);
    	keys[28] = (Button)findViewById(R.id.keypad_replay);
    	keys[29] = (Button)findViewById(R.id.keypad_speak);
    	keys[30] = (Button)findViewById(R.id.keypad_submit);
    	input = (TextView)findViewById(R.id.answer);
    }
    
    /* Store the handle of each button on the keypad */
    private void findQwertyViews() {
    	keys[0] = (Button)findViewById(R.id.keypad_q);
    	keys[1] = (Button)findViewById(R.id.keypad_w);
    	keys[2] = (Button)findViewById(R.id.keypad_e);
    	keys[3] = (Button)findViewById(R.id.keypad_r);
    	keys[4] = (Button)findViewById(R.id.keypad_t);
    	keys[5] = (Button)findViewById(R.id.keypad_y);
    	keys[6] = (Button)findViewById(R.id.keypad_u);
    	keys[7] = (Button)findViewById(R.id.keypad_i);
    	keys[8] = (Button)findViewById(R.id.keypad_o);
    	keys[9] = (Button)findViewById(R.id.keypad_p);
    	keys[10] = (Button)findViewById(R.id.keypad_a);
    	keys[11] = (Button)findViewById(R.id.keypad_s);
    	keys[12] = (Button)findViewById(R.id.keypad_d);
    	keys[13] = (Button)findViewById(R.id.keypad_f);
    	keys[14] = (Button)findViewById(R.id.keypad_g);
    	keys[15] = (Button)findViewById(R.id.keypad_h);
    	keys[16] = (Button)findViewById(R.id.keypad_j);
    	keys[17] = (Button)findViewById(R.id.keypad_k);
    	keys[18] = (Button)findViewById(R.id.keypad_l);
    	keys[19] = (Button)findViewById(R.id.keypad_z);
    	keys[20] = (Button)findViewById(R.id.keypad_x);
    	keys[21] = (Button)findViewById(R.id.keypad_c);
    	keys[22] = (Button)findViewById(R.id.keypad_v);
    	keys[23] = (Button)findViewById(R.id.keypad_b);
    	keys[24] = (Button)findViewById(R.id.keypad_n);
    	keys[25] = (Button)findViewById(R.id.keypad_m);
    	keys[26] = (Button)findViewById(R.id.keypad_bs);
    	keys[27] = (Button)findViewById(R.id.keypad_ans);
    	keys[28] = (Button)findViewById(R.id.keypad_replay);
    	keys[29] = (Button)findViewById(R.id.keypad_speak);
    	keys[30] = (Button)findViewById(R.id.keypad_submit);
    	input = (TextView)findViewById(R.id.answer);
    }
    
    /* Store the handle of each button on the keypad */
    private void findNumberViews() {
    	keys[0] = (Button)findViewById(R.id.keypad_1);
    	keys[1] = (Button)findViewById(R.id.keypad_2);
    	keys[2] = (Button)findViewById(R.id.keypad_3);
    	keys[3] = (Button)findViewById(R.id.keypad_4);
    	keys[4] = (Button)findViewById(R.id.keypad_5);
    	keys[5] = (Button)findViewById(R.id.keypad_6);
    	keys[6] = (Button)findViewById(R.id.keypad_7);
    	keys[7] = (Button)findViewById(R.id.keypad_8);
    	keys[8] = (Button)findViewById(R.id.keypad_9);
    	keys[9] = (Button)findViewById(R.id.keypad_0);
    	keys[10] = (Button)findViewById(R.id.keypad_ans);
    	keys[11] = (Button)findViewById(R.id.keypad_replay);
    }
    
    /* Store the handle of each button on the keypad */
    private void findPunctViews() {
    	keys[0] = (Button)findViewById(R.id.keypad_apos);
    	keys[1] = (Button)findViewById(R.id.keypad_bracket);
    	keys[2] = (Button)findViewById(R.id.keypad_cap);
    	keys[3] = (Button)findViewById(R.id.keypad_close_quote);
    	keys[4] = (Button)findViewById(R.id.keypad_comma);
    	keys[5] = (Button)findViewById(R.id.keypad_ex_pt);
    	keys[6] = (Button)findViewById(R.id.keypad_hyphen);
    	keys[7] = (Button)findViewById(R.id.keypad_open_quote);
    	keys[8] = (Button)findViewById(R.id.keypad_period);
    	keys[9] = (Button)findViewById(R.id.keypad_semicolon);
    	keys[10] = (Button)findViewById(R.id.keypad_ans);
    	keys[11] = (Button)findViewById(R.id.keypad_replay);
    }
    
    /* Set touch, focus and click listeners for each button */
    private void setListeners() {
    	for (int i = 0; i < numBtns; i++) {
			final int t = i;
			final String sAns = (String)keys[t].getTag();
    		keys[i].setOnTouchListener(this);
    		keys[i].setFocusableInTouchMode(true);
    		// speak the button.tag value, which has phonetic spelling for 
    		// some letters that aren't pronounced correctly if just a single
    		// letter is given
    		keys[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
    			public void onFocusChange(View v, boolean hasFocus) {
    				if (hasFocus) {
    					tts.speak(sAns, TextToSpeech.QUEUE_FLUSH, null);
    				}
    			}
    		});
    		// return the button.text value, which is what is displayed on the button
    		keys[i].setOnClickListener(new View.OnClickListener() {
    			public void onClick(View v) {
    				tts.speak("Selected " + sAns, TextToSpeech.QUEUE_FLUSH, null);
    				// Take single key answer
    				if (!multiKey) {
	    				answer = (String)keys[t].getText();
	    				returnResult();
    				}
    				// Take multikey answer
    				else {
    					
    					if (keys[t] == ansKey || keys[t] == replayKey) {
    						answer = (String)keys[t].getText();
    						returnResult();
    					}
    					else if (keys[t] == speakKey) {
    						if (answer.length() == 0)
    							tts.speak("You haven't entered anything yet", TextToSpeech.QUEUE_ADD, null);
    						else {
    							String letters = "";
    							for (int j = 0; j < answer.length(); j++) {
    								letters = letters + " " + String.valueOf(answer.charAt(j)) + ",";
    							}
    							letters.replace("A", "eh");
    							tts.speak("You have entered" + letters + ((asWord) ? ", " + answer : ""), TextToSpeech.QUEUE_ADD, null);
    						}
    					}
    					else if (keys[t] == submitKey) {
    						if (answer.length() == 0)
    							tts.speak("Nothing to submit", TextToSpeech.QUEUE_ADD, null);
    						else
    							returnResult();
    					}
    					else if (keys[t] == backspaceKey) {
    						if (answer.length() == 0)
    							tts.speak("No letter to erase", TextToSpeech.QUEUE_ADD, null);
    						else {
    							int last = answer.length() - 1;
    							tts.speak("Removing letter " + answer.charAt(last),
    								TextToSpeech.QUEUE_ADD, null);
    							answer = answer.substring(0, last);
    							input.setText(answer);
    						}
    					}
    					else {
    						answer = answer + (String)keys[t].getText();
    						input.setText(answer);
    					}
    				}
    			}
    		});
    	}
    }
    
    /** 
     * Handles the sensor events for changes to readings and accuracy 
     */ 
	private final SensorEventListener mListener = new SensorEventListener() { 
    	@Override
    	public void onSensorChanged(SensorEvent se) {
    		
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
    			float speed;
    			try {
    				speed =	(Math.abs(x - last_x) + Math.abs(y - last_y) + 
    						Math.abs(z - last_z)) / diffTime * 10000;
    			}
    			catch (Exception e) {
    				speed = 0;
    			}
    			
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
    		    		
    		// If time of this shake - time of four shakes ago is within the shake interval, erase
    		// (as long as there is data entered)
    		if (mShakeTimes[0] != -1 && mShakeTimes[SHAKE_COUNT - 1] - mShakeTimes[0] < SHAKES_INTERVAL) { 
    			if (!answer.equals(""))
    				inputCancelled(); 
    			initShakeArray();
    		} 
    	} 

    	@Override
    	public void onAccuracyChanged(Sensor arg0, int arg1) {}
    }; 
      
    private void initShakeArray() {
    	for (int i = 0; i < SHAKE_COUNT; i++) {
    		mShakeTimes[i] = -1;
    	}
    }
    
	/* Shake phone back and forth to erase input so far */    
    protected void inputCancelled() {
    	Log.v(TAG, "inputCancelled");
    	if (answer != "") {
    		tts.speak("Detected phone being shaken. Erasing previous input", TextToSpeech.QUEUE_ADD, null);
    		answer = "";
    		input.setText(answer);
    	}
    } 
    
    /* Return the result to the BrailLearn activity that called the keypad 
     * This function is called both when a button is clicked and when the
     * BACK button is pressed.
     */
    private void returnResult() {
      Intent intent = new Intent();
      if (answer.equals("") || answer.equalsIgnoreCase("cancel input"))
    	  answer = "Replay symbol";
      intent.putExtra("answer", answer);
      if (answer.equalsIgnoreCase("Replay symbol"))
    	  setResult(RESULT_CANCELED, intent);
      else if (answer.equalsIgnoreCase("Get Correct Answer"))
    	  setResult(RESULT_CORRECT_ANSWER, intent);
      else
    	  setResult(RESULT_OK, intent);
      while (tts.isSpeaking()) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
      finish();    	
    }
    
    private void giveHapticCue(long type)
	{
		vibe.vibrate(type);
	}
       
    /* Called for touches inside the button display */
	@Override
	public boolean onTouch(View v, MotionEvent me) {
    	Log.v(TAG, "onTouch");
    	detector.onTouchEvent(me);
    	if (me.getAction() == MotionEvent.ACTION_MOVE && me.getEventTime() - me.getDownTime() > tapDetectionInterval) {
    		findKey(me);
		} 
		else if (me.getAction() == MotionEvent.ACTION_UP) {
			if (lastKey >= 0 && lastKey < numBtns) blankBtn.requestFocus();
			lastSpeakTime = 0;
		}
		return true;
	}
    
	/* Locates the button on which the motion event occurred 
	 * and gives focus to that button.
	 */
    private boolean findKey(MotionEvent me) {
    	double y = me.getRawY();
    	double x = me.getRawX();
    	int[] loc = new int[2];
    	int[] dim = new int[2];
    	for (int i = 0; i < numBtns; i++) {
    		keys[i].getLocationOnScreen(loc);
    		dim[0] = keys[i].getWidth();
    		dim[1] = keys[i].getHeight();
    		// If the motion event goes over the button, have the button request focus
    		if (y <= (loc[1] + dim[1]) && x <= (loc[0] + dim[0])) {
				// if option changes from previously touched option...
    			Log.i(TAG, "Event: " + me.getEventTime() + ", lastSpeakTime: " + lastSpeakTime);
				if (i != lastKey) {
					giveHapticCue(OPTION_CUE);
					getFocus(i, me.getEventTime());
				}
				else if (me.getEventTime() - lastSpeakTime > clearFocusInterval) {	
					blankBtn.requestFocus();
					getFocus(i, me.getEventTime());
				}			
				lastKey = i;
    			return true;
    		}
    	}
    	return false;
    }
    
    private void getFocus(int i, long time) 
    {
    	Log.i(TAG, keys[i].getId() + " requested focus.");	
		keys[i].requestFocus();
		lastSpeakTime = time;
    }
	
	/* Handles hard key presses */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
    	if(keyCode == KeyEvent.KEYCODE_MENU){
        	
    	}
    	// On back button, resume Braille representation
    	else if(keyCode == KeyEvent.KEYCODE_BACK) {
    		tts.speak("Back kee pressed, replaying last " +
    				((multiKey)? "word" : "symbol"), TextToSpeech.QUEUE_ADD, null);
    		answer = "Replay symbol";
    		returnResult();
    	}
    	// Accept input from hard keyboard
    	else if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
    		tts.speak("Selected " + alphabet[keyCode - KeyEvent.KEYCODE_A] /*(String)keys[keyCode - KeyEvent.KEYCODE_A].getTag()*/, 
    				TextToSpeech.QUEUE_FLUSH, null);
    		if (!multiKey) {
				answer = alphabet[keyCode - KeyEvent.KEYCODE_A] /*(String)keys[keyCode - KeyEvent.KEYCODE_A].getText()*/;
				returnResult();
    		}
    		else {
				answer = answer + alphabet[keyCode - KeyEvent.KEYCODE_A] /*(String)keys[keyCode - KeyEvent.KEYCODE_A].getText()*/;
				input.setText(answer);
    		}
    	}
    	else
    		return super.onKeyDown(keyCode, keyEvent);
    	Log.v(TAG, "onKeyDown");
    	return true;
    }

	/* Speak full instructions for the keypad */
	private void speakInstructions() {
		tts.speak("To use the touchscreen kee pad: You can navigate" +
				" the kee pad by either touching and dragging on the screen or by using the trackball or directional" +
				" keys; the symbols on the keys will be spoken when navigated to,", TextToSpeech.QUEUE_ADD, null);
		if (multiKey)
			tts.speak("four special keys are also displayed," +
				" The Get Correct Answer key will play the correct answer," +
				" the Replay Answer key will return to the display without requiring an answer," +
				" the Speak Answer key will tell you which symbols you have entered as your answer," +
				" and the Submit Answer key will submit your answer," +
				" to select a key, double tap on it or press down again on the trackball or center" + 
				" button of the directional pad",
				TextToSpeech.QUEUE_ADD, null);
		else
			tts.speak("two special keys are also displayed," +
					" one will play the correct answer, the other will return to the display without requiring an" +
					" answer; when you hear the key you want, double tap on it or press down again on the trackball" +
					" or center button of the directional pad; you will return to the vibrating Braille screen and" +
					"the symbol you selected will be your answer", TextToSpeech.QUEUE_ADD, null);
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (lastKey >=0 && lastKey < numBtns)
			keys[lastKey].performClick();
		return true;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		findKey(e);
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}
	
}
