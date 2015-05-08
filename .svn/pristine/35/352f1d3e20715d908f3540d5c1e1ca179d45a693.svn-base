package cs.washington.edu.speechinput;

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

/**
 * Sample code that invokes the speech recognition intent API.
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

    /**
     * Called with the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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
        mTts = new TextToSpeech(this, this);
        mTts.setOnUtteranceCompletedListener(this);
        
        mList = new ListView(this);
        mList.setFocusableInTouchMode(true);    

        View v = new SpeechInputView(this);
        setContentView(v);
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
				return true;
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
				int arg4) {
			// TODO Auto-generated method stub
			
		}

	}

    /**
     * Fire an intent to start the speech recognition activity.
     */
    private void startVoiceRecognitionActivity() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speech recognition demo");
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
    
    @Override
	public void onInit(int status) {
		mTts.setSpeechRate((float) 1.25);  // use phone setting instead, automatic in 2.1+
		if (!mRecognizerEnabled) {
			mTts.setOnUtteranceCompletedListener(this);
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "nospeak");	
			mTts.speak("Sorry, Speech Recognizer is not available on this phone", TextToSpeech.QUEUE_FLUSH, utterance);
		}
		else {
			mTts.addEarcon("ding", getPackageName(), R.raw.ding);
			mTts.addEarcon("duobong", getPackageName(), R.raw.duobong);
			mTts.setOnUtteranceCompletedListener(this);
			startSR();
		}
	}
    
    private void startSR() {
    	if (!mSRActive) {
    		mSRActive = true;
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speak");	
			mTts.speak("Speak answer", TextToSpeech.QUEUE_FLUSH, utterance);
		}
    }
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (mTts != null) {
			Toast.makeText(this, "Shutting down TTS", Toast.LENGTH_SHORT).show();
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
		else if (mIndex < 0) mTts.speak("No previous matches", TextToSpeech.QUEUE_ADD, null);
		else mTts.speak("No more matches", TextToSpeech.QUEUE_ADD, null);
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
