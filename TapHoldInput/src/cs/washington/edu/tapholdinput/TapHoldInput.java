package cs.washington.edu.tapholdinput;

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


import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
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
	private Vibrator mVibrator;
	private boolean mIsTouching;
	private int mTouchId;

	
	// Master TextToSpeech reference used throughout application
	private HashMap<String, String> utterance = new HashMap<String, String>();
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mLastSingleTapTime = 0;
        mLastDoubleTapTime = 0;
        
        mTts = new TextToSpeech(this, this);
        mTts.setOnUtteranceCompletedListener(this);
        View v = new TapHoldView(this);
        setContentView(v);
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
	                break;
	            }
	            
	            // second finger touched down --> jump forward/backward five letters
	            case MotionEvent.ACTION_POINTER_DOWN: {
	            	Log.i(TAG, "action = POINTER_DOWN");
	                break;
	            }	            
	                
	            case MotionEvent.ACTION_UP: {
	            	Log.i(TAG, "action = UP");
	            	mIsTouching = false;
	                break;
	            }
	                
	            case MotionEvent.ACTION_CANCEL: { 
	            	Log.i(TAG, "action = CANCEL");
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
	                break;
	            }
	            
	            case MotionEvent.ACTION_POINTER_UP: {
	            	Log.i(TAG, "action = POINTER_UP");
	                break;
	            }
			
	    	} 
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
				mTts.speak("Selected " + mSpeakTags[mPointer], TextToSpeech.QUEUE_FLUSH, null);
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


	@Override
	public void onInit(int status) {
		mTts.setSpeechRate((float) 1.25);  // use phone setting instead, automatic in 2.1+
		mTts.addEarcon("ding", getPackageName(), R.raw.ding);
		mTts.speak("Touch screen to start", TextToSpeech.QUEUE_FLUSH, null);
	}
	
	@Override
	public void onPause() {
		super.onPause();
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
			mTts = null;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (mTts == null) {
			mTts = new TextToSpeech(this, this);
		}
	}
	
	private void speakNextLetter(int id, boolean repeat, int mode)
	{
		mPointer = (mPointer + 1) % mSpeakTags.length;
		mTts.setOnUtteranceCompletedListener(this);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, String.valueOf(id));
		mVibrator.vibrate(20);
		if (repeat) mTts.speak(mSpeakTags[mPointer], mode, utterance);
		else mTts.speak(mSpeakTags[mPointer], mode, null);
	}
	
	private void speakPreviousLetter(int id, boolean repeat, int mode)
	{
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
	}

}


	

