package cs.washington.edu.vbreader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.SystemClock;
import android.os.Vibrator;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Sample code that invokes the speech recognition intent API.  Uses default RecognizerListener, which displays only
 * visual feedback and doesn't return errors.  Not accessible so created custom RecognizerListener
 * in class VoiceInput with audio and vibration feedback.  SpeechInput is not used.
 */
public class SpeechInput extends Activity implements OnInitListener, TextToSpeech.OnUtteranceCompletedListener  {
	
	private static final String TAG = "SpeechInput";
	private TextToSpeech mTts;
	private static final String[] mSpeakTags = {"eh","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
    	"p","q","r","s","t","u","v","w","x","y","z"};
	private static final String[] mAlphabet = {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
    	"p","q","r","s","t","u","v","w","x","y","z"};
	public static final String[] mNato =
    {"alpha","bravo","charlie","delta","echo","fox trot","golf","hotel","india","juliet","kee low",
    	"lima","mike","november","oscar","pahpah", "quebec","romeo","sierra","tango","uniform",
    	"victor","whiskey","ex-ray","yang kee","zoo loo"};
    private static final int VOICE_RECOGNITION_REQUEST_CODE = 1234;
    
    private ListView mList;
	private HashMap<String, String> utterance = new HashMap<String, String>();
	private boolean mRecognizerEnabled;
	private int mIndex;
	private ArrayList<String> mInput;
	private boolean mSRActive;
	private String mCurrentSpeakTag;
	private boolean justCreated;

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        justCreated = true;

        // Check to see if a recognition activity is present
        PackageManager pm = getPackageManager();
        List<ResolveInfo> activities = pm.queryIntentActivities(
                new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH), 0);
        if (activities.size() != 0) {
            mRecognizerEnabled = true;
        } else {
            mRecognizerEnabled = false;
        }

        mSRActive = false;
        
        //set TextToSpeech
        mTts = GlobalState.getTTS();
        if (mTts == null) {
        	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
        	mTts = gs.createTTS(this, this);
        }
        else {
        	mTts.setOnUtteranceCompletedListener(this);
        	if (!mRecognizerEnabled) {
    			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "nospeak");	
    			mTts.speak("Sorry, Speech Recognizer is not available on this phone", TextToSpeech.QUEUE_FLUSH, utterance);
    		}
    		else {
    			startSR();
    		}
        }
        Log.v(TAG, "tts = " + mTts); 
                
        mList = new ListView(this);
        mList.setFocusableInTouchMode(true);    

        View v = new SpeechInputView(this);
        setContentView(v);
    }
    
    @Override
	public void onInit(int status) {
		if (!mRecognizerEnabled) {
			mTts.setOnUtteranceCompletedListener(this);
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "nospeak");	
			mTts.speak("Sorry, Speech Recognizer is not available on this phone", TextToSpeech.QUEUE_FLUSH, utterance);
		}
		else {
			mTts.setOnUtteranceCompletedListener(this);
			startSR();
		}
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
	    	mTts = gs.createTTS(this, null);
	    }
	    mTts.setOnUtteranceCompletedListener(this);
	    Log.v(TAG, "tts = " + mTts);
		justCreated = false;
	}

    protected class SpeechInputView extends ViewGroup implements GestureDetector.OnDoubleTapListener, 
		GestureDetector.OnGestureListener, View.OnTouchListener  {
	
		private GestureDetector mDetector;
		   
	    public SpeechInputView(Context context) {
	    	super(context);
	    	
	    	mDetector = new GestureDetector(context, this);
		    setBackgroundColor(Color.BLACK);
		    mList.setOnTouchListener(this);
		    addView(mList);
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
	    		mTts.speak("Selected " + mSpeakTags[i],	TextToSpeech.QUEUE_FLUSH, null);
				returnResult(mAlphabet[i]);

	    	}
	    	else
	    		return super.onKeyDown(keyCode, keyEvent);
	    	Log.v(TAG, "onKeyDown");
	    	return true;
	    }

	    
		@Override
		public boolean onTouch(View v, MotionEvent event) {
			Log.v(TAG, "onTouch");
			if (mDetector != null) mDetector.onTouchEvent(event);
			final int action = event.getAction();
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
	                break;
	            }
	                
	            case MotionEvent.ACTION_CANCEL: { 
	            	Log.i(TAG, "action = CANCEL");
	                break;
	            }
	            
	            case MotionEvent.ACTION_MOVE: {
					Log.i(TAG, "action = MOVE");	
	                break;
	            }
	            
	            case MotionEvent.ACTION_POINTER_UP: {
	            	Log.i(TAG, "action = POINTER_UP");
	            	startSR();
	                break;
	            }
			
	    	} 
			return false;
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
	                break;
	            }
	                
	            case MotionEvent.ACTION_CANCEL: { 
	            	Log.i(TAG, "action = CANCEL");
	                break;
	            }
	            
	            case MotionEvent.ACTION_MOVE: {
					Log.i(TAG, "action = MOVE");	
	                break;
	            }
	            
	            case MotionEvent.ACTION_POINTER_UP: {
	            	Log.i(TAG, "action = POINTER_UP");
	            	startSR();
	                break;
	            }
			
	    	} 
			return true;
		}	
	
		@Override
		public boolean onDown(MotionEvent e) {
			Log.i(TAG, "onDown()");
			return false;
		}
	
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			Log.i(TAG, "onFling()");
			if (mInput != null) {
				String msg = "Okay";
				if (e2.getRawX() - e1.getRawX() > 0) {
					mIndex++;
					msg += ", trying next";
				}
				else {
					mIndex--;
					msg += ", trying previous";
				}
				mTts.speak(msg, TextToSpeech.QUEUE_ADD, null);
				confirmAnswer();
				return true;
			}
			return false;
		}
	
		@Override
		public void onLongPress(MotionEvent e) {
			Log.i(TAG, "onLongPress()");
		}
	
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2, float dX,
				float dY) {
			Log.i(TAG, "onScroll()");
			return false;
		}
	
		@Override
		public void onShowPress(MotionEvent e) {
			Log.i(TAG, "onShowPress()");
		}
	
		@Override
		public boolean onSingleTapUp(MotionEvent e) {
			Log.i(TAG, "onSingleTapUp()");
			return false;
		}
	
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			Log.i(TAG, "onDoubleTap()");
			if (mInput != null && mIndex >= 0 && mIndex < mInput.size() && mCurrentSpeakTag != null) {
				mTts.speak("Selected " + mCurrentSpeakTag, TextToSpeech.QUEUE_ADD, null);
				returnResult(mInput.get(mIndex));
				return true;
			}
			else if (mInput != null && mIndex >= mInput.size()) {
				mTts.speak("Getting correct answer", TextToSpeech.QUEUE_ADD, null);
				returnResult("");
			}
			else {
				mTts.speak("Cancelling input", TextToSpeech.QUEUE_ADD, null);
				returnResult("cancel");
			}
			return false;
		}
	
		@Override
		public boolean onDoubleTapEvent(MotionEvent e) {
			Log.i(TAG, "onDoubleTapEvent()");
			return false;
		}
	
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			Log.i(TAG, "onSingleTapConfirmed()");			
			return false;
		}

		@Override
		protected void onLayout(boolean arg0, int arg1, int arg2, int arg3,
				int arg4) {}

	}

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        startActivityForResult(intent, VOICE_RECOGNITION_REQUEST_CODE);
    }

    /**
     * Handle the results from the recognition activity.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == VOICE_RECOGNITION_REQUEST_CODE) {
        	mTts.playEarcon("ding", TextToSpeech.QUEUE_FLUSH, null);
        	mSRActive = false;
        	if (resultCode == RESULT_OK) {
	            mInput = data.getStringArrayListExtra(
	                    RecognizerIntent.EXTRA_RESULTS);
	            for (int i = 0; i < mInput.size(); i++) {
	            	String s = mInput.get(i);
	            	if (s.length() > 1) {
		            	if (s.equals("hey"))
		            		mInput.set(i, "a");
		            	else mInput.set(i, s.substring(0,1).toLowerCase());
	            	}
	            	Toast.makeText(this, s + " ==> " + mInput.get(i), Toast.LENGTH_SHORT).show();
	            }
	            //mList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, mInput));
	            mIndex = 0;
	            confirmAnswer();
        	}
        	else {
        		mTts.speak("Speech Recognition failed", TextToSpeech.QUEUE_ADD, null);
        	}
        }

        super.onActivityResult(requestCode, resultCode, data);
    }
	
    private void startSR() {
    	if (!mSRActive) {
    		mSRActive = true;
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speak");	
			mTts.speak("Speak answer", TextToSpeech.QUEUE_FLUSH, utterance);
		}
    }
	
	private void confirmAnswer()
	{
		if (mInput == null) {
			mTts.speak("No results available", TextToSpeech.QUEUE_ADD, null);
			return;
		}
		if (mIndex >= 0 && mIndex < mInput.size()) {
			// find spoken match
			int j;
			for (j = 0; j < mAlphabet.length; j++) {
				if (mAlphabet[j].equals(mInput.get(mIndex))) break;
			}
			String s = mInput.get(mIndex);
			mCurrentSpeakTag = s;
			if (j < mAlphabet.length) {
				s = mSpeakTags[j] + " as in " + mNato[j];
				mCurrentSpeakTag = mSpeakTags[j];
			}
			mTts.speak("Did you say " + s + "?", TextToSpeech.QUEUE_ADD, null);
		}
		else if (mIndex < 0) mTts.speak("No previous matches. Double tap to cancel input and replay symbol", TextToSpeech.QUEUE_ADD, null);
		else {
			mTts.speak("No more matches.  Setting input to blank.", TextToSpeech.QUEUE_ADD, null);
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
		
	@Override
	public void onUtteranceCompleted(String utteranceId) {
		Log.i(TAG, "onUtteranceCompleted(" + utteranceId + ")");
		if (utteranceId.equalsIgnoreCase("speak")) {
			startVoiceRecognitionActivity();
		}
		if (utteranceId.equalsIgnoreCase("nospeak")) {
			finish();
		}
	}
}
