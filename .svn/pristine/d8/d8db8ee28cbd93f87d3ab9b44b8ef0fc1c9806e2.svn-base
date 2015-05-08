package cs.washington.edu.vbreader;

/** 
 * Author: Janet Hollier 5/31/11
 * This view provides for tap-and-hold-based input.
 * Touch the screen to start the alphabet at 'a'.  Holding
 * on the screen or single tapping advances one letter at a 
 * time.  Flinging down advances 5 letters forward and 
 * flinging up moves back 5 letters.  Double tap to select
 * the last spoken letter.
 * Circling in clockwise direction lets you move
 * forward through the letters at your speed while
 * circling in a counterclockwise direction moves 
 * backward through the letters. 
 */

import java.util.HashMap;
import java.util.Locale;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

public class TapHoldInput extends Activity implements OnInitListener, TextToSpeech.OnUtteranceCompletedListener {
	
	private static final String TAG = "TagHoldInput";
	private TextToSpeech mTts;
	private static final String[] mSpeakTags = {"eh","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
    	"p","q","r","s","t","u","v","w","x","y","z"};
	private static final String[] mValueTags = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
    	"p","q","r","s","t","u","v","w","x","y","z"};
	
	private long mLastSingleTapTime;
	private long mLastDoubleTapTime;
	private int mPointer;
	private int mSavePointer;
	private Vibrator mVibrator;
	private boolean mIsTouching;
	private int mTouchId;
	private boolean justCreated;
	private String mGreeting = "Touch screen to start, fling down with two fingers for instructions";
	
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
	

	
	// Master TextToSpeech reference used throughout application
	private HashMap<String, String> utterance = new HashMap<String, String>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        justCreated = true;
        
        mLastSingleTapTime = 0;
        mLastDoubleTapTime = 0;
        
        //set TextToSpeech
        mTts = GlobalState.getTTS();
        if (mTts == null) {
        	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
        	mTts = gs.createTTS(this, this);
        }
        else mTts.speak(mGreeting, TextToSpeech.QUEUE_ADD, null);
        Log.v(TAG, "tts = " + mTts);
        mTts.setOnUtteranceCompletedListener(this);
        
        View v = new TapHoldView(this);
        setContentView(v);
        
        lastDownFlingTm = 0;
        lastRightSwipeTm = 0;    

	}
	
	@Override
	public void onInit(int status) {
		mTts.speak(mGreeting, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume()");
		
		//create TextToSpeech    
		mTts = GlobalState.getTTS();  
	    if (mTts == null) {  // can only happen if killTTS() called on GlobalState when VBReader is destroyed
	    	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
	    	mTts = gs.createTTS(this, this);
	    }
	    else if (!justCreated) mTts.speak(mGreeting, TextToSpeech.QUEUE_ADD, null);
	    mTts.setOnUtteranceCompletedListener(this);
	    Log.v(TAG, "tts = " + mTts);
		justCreated = false;
	}
	
	/* Speak full instructions for the keypad */
	private void speakInstructions() {
		mTts.speak("single tap on the screen to advance one letter at a time through the alphabet," +
				" move finger slightly left when you tap to move backward one letter; " +
				" fling right to jump forward five letters or fling left to jump backward five letters; " +
				" double tap to select the last letter that was spoken." +
				" to hear the correct answer, first fling right with two fingers, " +
				" then immediately double tap. " +
				" press the phone's back key to cancel inn put and redisplay the letter", TextToSpeech.QUEUE_ADD, null);
	}

	protected class TapHoldView extends View implements GestureDetector.OnDoubleTapListener, 
		GestureDetector.OnGestureListener  {
		
		private static final long FLING_TIME = 1000;
		
		private int mJumpInterval = 200;
		private int mStepInterval = 40;
		//private HashMap<String, String> repeatUtterance = new HashMap<String, String>();
		
		
		private int mStartX;
		
		private GestureDetector mDetector;
		private int mGutter;  // may be used to only process touches in the interior of the screen
		   
	    public TapHoldView(Context context) {
	    	super(context);
	    	
	    	mPointer = -1;
	        mSavePointer = mPointer;
	    	mTouchId = -1;
	    	mDetector = new GestureDetector(context, this);
	    	mIsTouching = false;
	    	mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
	    	
	    	setBackgroundColor(Color.BLACK);
	    	setClickable(true);
	    }     
	    
	    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        	Log.i(TAG, "onSizeChanged() -- Width: " + w + ", Height: " + h);
        	mGutter = (int)(Math.min(w, h) * 0.1);
        	mJumpInterval = (int)(w * 0.35);
        	mStepInterval = (int)(w * 0.1);
            
            mMinSwipeDistance = (int) (w * 0.3);
            mMinFlingDistance = (int) (h * 0.52);
        	Log.i(TAG, "Width: " + w + ", Swipe distance: " + mMinSwipeDistance + ", Fling distance: " + mMinFlingDistance);
	    	super.onSizeChanged(w, h, oldw, oldh);
	    }
	
		@Override
		public boolean onTouchEvent(MotionEvent me) {
			Log.v(TAG, "onTouchEvent");
			if (mDetector != null) mDetector.onTouchEvent(me);
			
			final int action = me.getAction();
	    	switch (action & MotionEvent.ACTION_MASK) {
	    	
	            case MotionEvent.ACTION_DOWN: {
	            	Log.i(TAG, "action = DOWN");
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
	            
	            // second finger touched down --> jump forward/backward five letters
	            case MotionEvent.ACTION_POINTER_DOWN: {
	            	Log.i(TAG, "action = POINTER_DOWN");
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
	            	Log.i(TAG, "action = UP");
	            	mIsTouching = false;
	            	// if down fling was performed
		        	if (me.getEventTime() - me.getDownTime() < mMaxFlingDetectionTime && 
		        		me.getEventTime() - lastDownFlingTm > MAX_FLING_INTERVAL &&
		        		me.getY(mFirstPointerId) - mDown1TouchY > mMinFlingDistance && 
		        		mUp2TouchY - mDown2TouchY > mMinFlingDistance && 
		        		me.getX(mFirstPointerId) - mDown1TouchX < mMinSwipeDistance && 
		        		mUp2TouchX - mDown2TouchX < mMinSwipeDistance) {
		            		mTts.playEarcon("flingdown", TextToSpeech.QUEUE_FLUSH, null);
		    				Log.v(TAG, "onTouch(): downward fling");
		    				lastDownFlingTm = me.getEventTime();
		    				utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "instructions");
		    				mTts.speak("Synthesizing instructions, One moment please", TextToSpeech.QUEUE_ADD, utterance);
		        	}
		        	
		        	// if right swipe was performed
		        	else if (me.getEventTime() - me.getDownTime() < mMaxSwipeDetectionTime && 
		        		me.getEventTime() - lastRightSwipeTm > MAX_SWIPE_INTERVAL &&
		        		me.getX(mFirstPointerId) - mDown1TouchX > mMinSwipeDistance && 
		        		mUp2TouchX - mDown2TouchX > mMinSwipeDistance && 
		        		me.getY(mFirstPointerId) - mDown1TouchY < mMinFlingDistance && 
		        		mUp2TouchY - mDown2TouchY < mMinFlingDistance) {
		            		mTts.playEarcon("ding", TextToSpeech.QUEUE_FLUSH, null);
		    				Log.v(TAG, "onTouch(): right swipe");
		    				lastRightSwipeTm = me.getEventTime();
		    				mTts.speak("Double tap now to get correct answer", TextToSpeech.QUEUE_ADD, null);
		    				mSavePointer = mPointer;
		    				mPointer = mSpeakTags.length;
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
	            	mIsTouching = true;
	            	
					Log.i(TAG, "action = MOVE");	
					int deltaX = (int)me.getRawX() - mStartX;
					if (me.getEventTime() - me.getDownTime() > FLING_TIME && Math.abs(deltaX) > mStepInterval)  {
						++mTouchId;
						mStartX = (int)me.getRawX();
						speakNextLetter(mTouchId, true, TextToSpeech.QUEUE_FLUSH);
					}	
					//Log.i(TAG, "x1: " + me.getX(mFirstPointerId) + " y1: " + me.getY(mFirstPointerId));
		        	if (mSecondPointerId >= 0) Log.i(TAG, "x2: " + me.getX(mSecondPointerId) + " y2: " + me.getY(mSecondPointerId));
	                break;
	            }
	            
	            case MotionEvent.ACTION_POINTER_UP: {
	            	Log.i(TAG, "action = POINTER_UP");
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
			
	    	} 
			return true;
		}	
		
		/* Handles hard key presses */
	    @Override
	    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
	    	if(keyCode == KeyEvent.KEYCODE_MENU){
	        	
	    	}
	    	// On back button, resume Braille representation
	    	else if(keyCode == KeyEvent.KEYCODE_BACK) {
	    		mTts.speak("Back kee pressed", TextToSpeech.QUEUE_ADD, null);
	    		returnResult("Replay symbol");
	    	}
	    	// Accept input from hard keyboard
	    	else if (keyCode >= KeyEvent.KEYCODE_A && keyCode <= KeyEvent.KEYCODE_Z) {
	    		int i = keyCode - KeyEvent.KEYCODE_A;
	    		mTts.speak("Selected " + mSpeakTags[i] /*(String)keys[keyCode - KeyEvent.KEYCODE_A].getTag()*/, 
	    				TextToSpeech.QUEUE_FLUSH, null);
				returnResult(mValueTags[i]);

	    	}
	    	else
	    		return super.onKeyDown(keyCode, keyEvent);
	    	Log.v(TAG, "onKeyDown");
	    	return true;
	    }

	
		@Override
		public boolean onDown(MotionEvent e) {
			Log.i(TAG, "onDown()");
			mIsTouching = true;
        	++mTouchId;
        	mStartX = (int)e.getRawX();
			Log.i(TAG, "action = DOWN -- x: " + mStartX);
			return false;
		}
	
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			Log.i(TAG, "onFling()");
			mLastSingleTapTime = e2.getEventTime();
			mIsTouching = false;
			if (mPointer == mSpeakTags.length) mPointer = mSavePointer;
			int deltaX = (int)e2.getRawX() - (int)e1.getRawX();
			if (Math.abs(deltaX) > mJumpInterval)  {
				++mTouchId;
				mPointer = (deltaX >= 0) ? (mPointer + 4) % mSpeakTags.length : (mSpeakTags.length + mPointer - 6) % mSpeakTags.length;
				mTts.playEarcon("ding", TextToSpeech.QUEUE_FLUSH, null);
				speakNextLetter(mTouchId, false, TextToSpeech.QUEUE_ADD);
			}
			else {
				++mTouchId;
				if (deltaX >= 0) speakNextLetter(mTouchId, false, TextToSpeech.QUEUE_ADD);
				else speakPreviousLetter(mTouchId, false, TextToSpeech.QUEUE_ADD);
			}
			return false;
		}
	
		@Override
		public void onLongPress(MotionEvent e) {
			Log.i(TAG, "onLongPress()");
			mIsTouching = true;
		}
	
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float dX,
				float dY) {
			Log.i(TAG, "onScroll()");
			mIsTouching = true;
			return false;
		}
	
		@Override
		public void onShowPress(MotionEvent e) {
			Log.i(TAG, "onShowPress()");
			mIsTouching = true;
			new Thread(new OnDownEvent(e.getDownTime(), mTouchId)).start();
		}
	
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Log.i(TAG, "onSingleTapUp()");
			//mLastSingleTapTime = e.getEventTime();
			mIsTouching = false;
			return false;
		}
	
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Log.i(TAG, "onDoubleTap()");
			mLastDoubleTapTime = e.getEventTime();
			mIsTouching = false;
			return false;
		}
	
		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			Log.i(TAG, "onDoubleTapEvent()");
			if (e.getAction() == MotionEvent.ACTION_UP) {
				mLastDoubleTapTime = e.getEventTime();
				mIsTouching = false;
				if (mPointer >= 0 && mPointer < mSpeakTags.length) {
					mTts.speak("Selected " + mSpeakTags[mPointer], TextToSpeech.QUEUE_FLUSH, null);
					returnResult(mValueTags[mPointer]);
				}
				else if (mPointer >= mSpeakTags.length) {
					mTts.speak("Selected get correct answer", TextToSpeech.QUEUE_FLUSH, null);
					returnResult("");
				}
				else {
					mTts.speak("Replaying letter", TextToSpeech.QUEUE_FLUSH, null);
					returnResult("cancel");
				}
			}
			return false;
		}
	
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.i(TAG, "onSingleTapConfirmed()");
			mLastSingleTapTime = e.getEventTime();
			mIsTouching = false;
			speakNextLetter(mTouchId, false, TextToSpeech.QUEUE_FLUSH);
			return false;
		}
	
	}
	
	public class OnDownEvent implements Runnable {
		
		// Initialize sleep time in milliseconds
		private static final int sleepTime = 700;
		
		private long mDownTime;
		private int mId;
				
		public OnDownEvent(long time, int id) {
			mDownTime = time;
			mId = id;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
					return;
			}
			
			// On waking, check whether single or double tap occurred after down
			if (mDownTime > mLastSingleTapTime && mDownTime > mLastDoubleTapTime) {
				Log.i(TAG, "run(): no double or single tap, id = " + mId);
				speakNextLetter(mId, true, TextToSpeech.QUEUE_ADD);
			}
			return;
		}		
	}

	private void speakNextLetter(int id, boolean repeat, int mode)
	{
		if (mPointer == mSpeakTags.length) mPointer = mSavePointer;
		mPointer = (mPointer + 1) % mSpeakTags.length;
		mTts.setOnUtteranceCompletedListener(this);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(id));
		mVibrator.vibrate(20);
		if (repeat) mTts.speak(mSpeakTags[mPointer], mode, utterance);
		else mTts.speak(mSpeakTags[mPointer], mode, null);
	}
	
	private void speakPreviousLetter(int id, boolean repeat, int mode)
	{
		if (mPointer == mSpeakTags.length) mPointer = mSavePointer;
		mPointer = (mSpeakTags.length + mPointer - 1) % mSpeakTags.length;
		mTts.setOnUtteranceCompletedListener(this);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(id));
		mVibrator.vibrate(20);
		if (repeat) mTts.speak(mSpeakTags[mPointer], mode, utterance);
		else mTts.speak(mSpeakTags[mPointer], mode, null);
	}
	
	@Override
	public void onUtteranceCompleted(String utteranceId) {
		SystemClock.sleep(1000);
		String id = String.valueOf(mTouchId);
		Log.i(TAG, "onUtteranceCompleted(" + utteranceId + "), mTouchId = " + mTouchId);
		if (utteranceId.equalsIgnoreCase(id) && mIsTouching) {
			//repeatUtterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
			Log.i(TAG, "next utterance");
			//mTts.speak(mSpeakTags[mPointer], TextToSpeech.QUEUE_ADD, repeatUtterance);
			speakNextLetter(Integer.parseInt(utteranceId), true, TextToSpeech.QUEUE_ADD);
		}
		else if (utteranceId.equalsIgnoreCase("instructions")) {
    		speakInstructions();   
		}
	}
	
    private void returnResult(String answer) {
        Intent intent = new Intent();
        
        if (answer.equalsIgnoreCase("cancel")) setResult(RESULT_CANCELED, intent);
        else {
        	intent.putExtra("answer", answer);
	        setResult(RESULT_OK, intent);
        }
        while (mTts.isSpeaking()) {
  			try {
  				Thread.sleep(1000);
  			} catch (InterruptedException e) {
  				e.printStackTrace();
  			}
  		}
        finish();    	
     }

}
