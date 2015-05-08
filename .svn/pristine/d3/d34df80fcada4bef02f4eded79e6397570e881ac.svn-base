package cs.washington.edu.vbreader;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.Display;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/* Soft touch keypad for entering answers */
public class TCKeypad extends Activity implements OnInitListener, 
	View.OnTouchListener, GestureDetector.OnDoubleTapListener, 
	GestureDetector.OnGestureListener {
	
	private static final String TAG = "Keypad";
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
    private long lastClearTime = 0;
	private int focusOption;
	
    private TextToSpeech tts;
    private int numBtns;
	private Button[] keys;
	private String answer = "";
	private Button blankBtn;
	private int lastKey = -1;
    private GestureDetector detector;
    private long lastSpeakTime = 0;
    private Vibrator vibe;
    private boolean justCreated;
    private String mGreeting = "two column kee pad displayed";
    
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
    private long mMaxFlingDetectionTime = 600;
    private long mMaxSwipeDetectionTime = 300;
    private int mMinFlingDistance;
    private int mMinSwipeDistance;
    private long lastDownFlingTm;
	private long lastRightSwipeTm;
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        justCreated = true;
        
        //set TextToSpeech
        tts = GlobalState.getTTS();
        if (tts == null) {
        	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
            tts = gs.createTTS(this, this);
        }
        else tts.speak(mGreeting, TextToSpeech.QUEUE_ADD, null);
        Log.v(TAG, "tts = " + tts);
        
    	numBtns = 26;
    	keys =  new Button[numBtns];
    	setContentView(R.layout.tckeypad);
    	findAlphaViews();
    	LinearLayout ll = (LinearLayout)findViewById(R.id.tckeypad);
        ll.setOnTouchListener(this);
    	
    	WindowManager w = getWindowManager(); 
        Display d = w.getDefaultDisplay(); 
        int height = d.getHeight(); 
        int width = d.getWidth();
        Resources res = getResources();
        float dpScale = res.getDisplayMetrics().density;
        
        // adjust thresholds for screen density
        //height = (int)((float)height * dpScale);
    	height /= 13;
        setListeners(height);
    	
        blankBtn = (Button)findViewById(R.id.keypad_blank);
        blankBtn.setOnTouchListener(this);
        blankBtn.requestFocus();

        
        // Keep menu in landscape layout
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Log.v(TAG, "creating");
        detector = new GestureDetector(this, this);
        vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        
        // for clearing focus so repeat can occur
	    focusOption = -1;  
	    
	    lastDownFlingTm = 0;
        lastRightSwipeTm = 0;    
      
        mMinSwipeDistance = (int) (width * 0.3);
        mMinFlingDistance = (int) (height * 0.52);
    	Log.i(TAG, "Width: " + w + ", Swipe distance: " + mMinSwipeDistance + ", Fling distance: " + mMinFlingDistance);
    }
    
    /* Called when text to speech service initializes */
	@Override
	public void onInit(int arg0) {
		tts.speak(mGreeting, TextToSpeech.QUEUE_ADD, null);
	} 
		
	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume()");
		
		//create TextToSpeech    
	    tts = GlobalState.getTTS();
	    if (tts == null) {  // can only happen if killTTS() called on GlobalState when VBReader is destroyed
	    	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
	        tts = gs.createTTS(this, this);
	    }
	    else if (!justCreated) tts.speak(mGreeting, TextToSpeech.QUEUE_ADD, null);
	    Log.v(TAG, "tts = " + tts);
		justCreated = false;
	}
    
    /* Store the handle of each button on the keypad */
    private void findAlphaViews() {    	
    	keys[0] = (Button)findViewById(R.id.keypad_a);
    	keys[1] = (Button)findViewById(R.id.keypad_n);
    	keys[2] = (Button)findViewById(R.id.keypad_b);
    	keys[3] = (Button)findViewById(R.id.keypad_o);
    	keys[4] = (Button)findViewById(R.id.keypad_c);
    	keys[5] = (Button)findViewById(R.id.keypad_p);
    	keys[6] = (Button)findViewById(R.id.keypad_d);
    	keys[7] = (Button)findViewById(R.id.keypad_q);
    	keys[8] = (Button)findViewById(R.id.keypad_e);
    	keys[9] = (Button)findViewById(R.id.keypad_r);
    	keys[10] = (Button)findViewById(R.id.keypad_f);
    	keys[11] = (Button)findViewById(R.id.keypad_s);
    	keys[12] = (Button)findViewById(R.id.keypad_g);
    	keys[13] = (Button)findViewById(R.id.keypad_t);
    	keys[14] = (Button)findViewById(R.id.keypad_h);
    	keys[15] = (Button)findViewById(R.id.keypad_u);
    	keys[16] = (Button)findViewById(R.id.keypad_i);
    	keys[17] = (Button)findViewById(R.id.keypad_v);
    	keys[18] = (Button)findViewById(R.id.keypad_j);
    	keys[19] = (Button)findViewById(R.id.keypad_w);
    	keys[20] = (Button)findViewById(R.id.keypad_k);
    	keys[21] = (Button)findViewById(R.id.keypad_x);
    	keys[22] = (Button)findViewById(R.id.keypad_l);
    	keys[23] = (Button)findViewById(R.id.keypad_y);
    	keys[24] = (Button)findViewById(R.id.keypad_m);
    	keys[25] = (Button)findViewById(R.id.keypad_z);
    }
    
    /* Set touch, focus and click listeners for each button */
    private void setListeners(int height) {
    	float textHeight = (float)(height * 0.7);
    	//Toast.makeText(this, "Height: " + height + ", text: " + textHeight, Toast.LENGTH_LONG).show();
    	for (int i = 0; i < numBtns; i++) {
			final int t = i;
			final String sAns = (String)keys[t].getTag();
    		keys[i].setOnTouchListener(this);
    		keys[i].isFocusableInTouchMode();
    		// speak the button.tag value, which has phonetic spelling for 
    		// some letters that aren't pronounced correctly if just a single
    		// letter is given
    		keys[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
    			public void onFocusChange(View v, boolean hasFocus) {
    				if (hasFocus) {
						Log.v(TAG, "Option " + t + " has focus.");
						if (focusOption != t) tts.speak(sAns, TextToSpeech.QUEUE_FLUSH, null);
						focusOption = t;
						lastKey = t;
					}
    			}
    		});
    		// return the button.text value, which is what is displayed on the button
    		keys[i].setOnClickListener(new View.OnClickListener() {
    			public void onClick(View v) {
    				tts.speak("Selected " + sAns, TextToSpeech.QUEUE_FLUSH, null);
    				// Take single key answer
	    			answer = (String)keys[t].getText();
	    			returnResult();
    			}
    		});
    		keys[i].setMaxHeight(height);
    		keys[i].setMinHeight(height - 1);
    		keys[i].setHeight(height);
    		keys[i].setTextSize(TypedValue.COMPLEX_UNIT_PX, textHeight);
    	}
    }
    
    /* Return the result to the BrailLearn activity that called the keypad 
     * This function is called both when a button is clicked and when the
     * BACK button is pressed.
     */
    private void returnResult() {
      Intent intent = new Intent();
      if (answer.equals("") || answer.equalsIgnoreCase("cancel input"))	answer = "Replay symbol";
      else if (answer.equals("Get Correct Answer")) answer = "";
      
      intent.putExtra("answer", answer);
      
      if (answer.equalsIgnoreCase("Replay symbol")) setResult(RESULT_CANCELED, intent);
      else setResult(RESULT_OK, intent);
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
	        	focusOption = -1;
	        	
	        	// if down fling was performed
	        	if (me.getEventTime() - me.getDownTime() < mMaxFlingDetectionTime && 
	        		me.getEventTime() - lastDownFlingTm > MAX_FLING_INTERVAL &&
	        		me.getY(mFirstPointerId) - mDown1TouchY > mMinFlingDistance && 
	        		mUp2TouchY - mDown2TouchY > mMinFlingDistance && 
	        		me.getX(mFirstPointerId) - mDown1TouchX < mMinSwipeDistance && 
	        		mUp2TouchX - mDown2TouchX < mMinSwipeDistance) {
	            		Toast t = Toast.makeText(this, "Fling down occurred", Toast.LENGTH_SHORT);
	            		t.show();
	            		tts.playEarcon("flingdown", TextToSpeech.QUEUE_FLUSH, null);
	    				Log.v(TAG, "onTouch(): downward fling");
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
	            		Toast t = Toast.makeText(this, "Right swipe occurred", Toast.LENGTH_SHORT);
	            		t.show();
	            		tts.playEarcon("ding", TextToSpeech.QUEUE_FLUSH, null);
	    				Log.v(TAG, "onTouch(): right swipe");
	    				lastRightSwipeTm = me.getEventTime();
	    				tts.speak("Double tap now to get correct answer", TextToSpeech.QUEUE_ADD, null);
	    				clearFocus(me);
	    				lastKey = 26;				
	        	}
	            mFirstPointerId = INVALID_POINTER_ID;
	            break;
	        }
	            
	        case MotionEvent.ACTION_CANCEL: {
	            mFirstPointerId = INVALID_POINTER_ID;
	            Log.i(TAG, "ACTION_CANCEL");
	            break;
	        }
	        
	        case MotionEvent.ACTION_MOVE: {
	        	if (me.getAction() == MotionEvent.ACTION_MOVE && me.getEventTime() - me.getDownTime() > tapDetectionInterval) {
	        		findKey(me);
	    		}
	        	//Log.i(TAG, "x1: " + me.getX(mFirstPointerId) + " y1: " + me.getY(mFirstPointerId));
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
    	} // end switch
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
				if (i != lastKey || focusOption ==  -1) {
					giveHapticCue(OPTION_CUE);
					clearFocus(me);
				}
				else if (i == lastKey && me.getEventTime() - lastClearTime > clearFocusInterval) {	
					clearFocus(me);
					focusOption = -1;
				}
				// have option request focus.  If it doesn't already have focus,
				// it's name will be spoken.
				Log.v(TAG, "Key " + i + " requested focus.");	
				keys[i].requestFocus();
				lastKey = i;
    			return true;
    		}
    		//if (y > loc[1] + dim[1]) tts.playEarcon("end", TextToSpeech.QUEUE_FLUSH, null);
    	}
    	return false;
    }
    
    private void clearFocus(MotionEvent me) {
		lastClearTime = me.getEventTime();
		if (lastKey >= 0 && lastKey < numBtns) keys[lastKey].clearFocus();
		Log.v(TAG, "clearFocus():  Key " + lastKey + " cleared focus.");
	}
    	
    /* Handles hard key presses */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
    	if(keyCode == KeyEvent.KEYCODE_MENU){
        	
    	}
    	// On back button, resume Braille representation
    	else if(keyCode == KeyEvent.KEYCODE_BACK) {
    		tts.speak("Back kee pressed, replaying symbol", TextToSpeech.QUEUE_ADD, null);
    		answer = "Replay symbol";
    		returnResult();
    	}
    	// Accept input from hard keyboard
    	else if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
    		int i = keyCode - KeyEvent.KEYCODE_A;
    		tts.speak("Selected " + alphabet[i] , TextToSpeech.QUEUE_FLUSH, null);
			answer = alphabet[i];
			returnResult();
    	}
    	// Enter key pressed: say current letter then move to next symbol
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {	
    		if (lastKey >= 0 && lastKey < numBtns) answer = alphabet[lastKey];
    		else if (lastKey == -1) answer = "Replay symbol";
    		else answer = "Get Correct Answer";
    		tts.speak("Selected " + answer, TextToSpeech.QUEUE_FLUSH, null);
			returnResult();	
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
				" keys; the symbols on the keys will be spoken when you navigate to them, double tap to select the last spoken letter," +
				" press the back key to redisplay the letter", TextToSpeech.QUEUE_ADD, null);
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		if (lastKey >=0 && lastKey < numBtns)
			keys[lastKey].performClick();
		else {
			if (lastKey == -1) answer = "Replay symbol";
    		else answer = "Get Correct Answer";
    		tts.speak("Selected " + answer, TextToSpeech.QUEUE_FLUSH, null);
			returnResult();	
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
