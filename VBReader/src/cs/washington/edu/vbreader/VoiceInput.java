package cs.washington.edu.vbreader;


import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.os.Vibrator;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

public class VoiceInput extends Activity implements OnInitListener, TextToSpeech.OnUtteranceCompletedListener,
	RecognitionListener {
	
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
    
 // Names of a few intent extras defined in VoiceSearch's RecognitionService.
    // These let us tweak the endpointer parameters.
    private static final String EXTRA_SPEECH_MINIMUM_LENGTH_MILLIS = "android.speech.extras.SPEECH_INPUT_MINIMUM_LENGTH_MILLIS";
    private static final String EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS = "android.speech.extras.SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS";
    private static final String EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS = "android.speech.extras.SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS";

    // The usual endpointer default value for input complete silence length is 0.5 seconds,
    // but that's used for things like voice search. For dictation-like voice input like this,
    // we go with a more liberal value of 1 second. This value will only be used if a value
    // is not provided from Gservices.
    private static final String INPUT_COMPLETE_SILENCE_LENGTH_DEFAULT_VALUE_MILLIS = "1000";
    
    private ListView mList;
	private HashMap<String, String> utterance = new HashMap<String, String>();
	private boolean mRecognizerEnabled;
	private int mIndex;
	private ArrayList<String> mInput;
	private boolean mSRActive;
	private String mCurrentSpeakTag;
	private SpeechRecognizer mSR = null;
	private SpeechInputView mView;
	private Vibrator mVibe;
	private boolean justCreated;
	
	// Handler for UI thread
	private final Handler mHandler = new Handler();
	
	
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
        
        mVibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
        
        mList = new ListView(this);
        mList.setFocusableInTouchMode(true);    

        mView = new SpeechInputView(this);
        setContentView(mView);
    }
    
    @Override
	public void onInit(int status) {
    	Log.i(TAG, "onInit()");
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

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy()");
		super.onDestroy();
		if (mSR != null) mSR.destroy();
	}

    /**
     * Fire an intent to start the speech recognition activity.
     */
    protected void startVoiceRecognitionActivity() {
    	Log.i(TAG, "startVoiceRecognitionActivity()");
    	mView.showInitializing();
    	if (mSR != null) {
    		mSR.destroy();
    		mSR = null;
    	}
    	mSR = SpeechRecognizer.createSpeechRecognizer(this);
    	mSR.setRecognitionListener(this);
    	Toast.makeText(this, "initializing speech recognition, wait for vibration then speak answer", Toast.LENGTH_SHORT).show();
    	if (!SpeechRecognizer.isRecognitionAvailable(this)) {
    		mTts.speak("No recognition service available", TextToSpeech.QUEUE_ADD, null);
    		mView.showNormal();
    	}
    	else {
    		//mTts.speak("Recognition available", TextToSpeech.QUEUE_ADD, null);
    		Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, "VoiceIME");
            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
            intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 3000L);
            mSR.startListening(intent);
    	}
    }

    /**
     * Handle the results from the recognition activity.
     */
    protected void onSpeechResult(Bundle data) {
    	Log.i(TAG, "onSpeechResult()");
    	mTts.playEarcon("ding", TextToSpeech.QUEUE_FLUSH, null);
    	mSRActive = false;
    	
        mInput = data.getStringArrayList(
                SpeechRecognizer.RESULTS_RECOGNITION);
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
    
    private void startSR() {
    	Log.i(TAG, "startSR()");
    	if (!mSRActive) {
    		mSRActive = true;
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "speak");	
			mTts.speak("initializing speech recognition, wait for vibration then speak answer", TextToSpeech.QUEUE_FLUSH, utterance);
		}
    }
	

	private void confirmAnswer()
	{
		Log.i(TAG, "confirmAnswer()");
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
			mTts.speak("Double tap if the letter to enter is " + s, TextToSpeech.QUEUE_ADD, null);
		}
		else if (mIndex < 0) {
			if (mInput.size() > 1) mTts.speak("No previous matches", TextToSpeech.QUEUE_ADD, null);
			mTts.speak("Double tap to cancel input and replay symbol, two finger tap to restart", TextToSpeech.QUEUE_ADD, null);
		}
		else {
			if (mInput.size() > 1) mTts.speak("No more matches", TextToSpeech.QUEUE_ADD, null);
			mTts.speak("Double tap to get correct answer or two finger tap to restart", TextToSpeech.QUEUE_ADD, null);
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
			mHandler.post(new Runnable() {
				public void run() {
					startVoiceRecognitionActivity();
				}
			});
		}
		if (utteranceId.equalsIgnoreCase("nospeak")) {
			finish();
		}
		if (utteranceId.equalsIgnoreCase("error")) {
			mView.showNormal();
		}
	}

	@Override
	public void onBeginningOfSpeech() {
		Log.i(TAG, "onBeginningOfSpeech()");
		mView.showHearing();
	}

	@Override
	public void onBufferReceived(byte[] buffer) {
		Log.i(TAG, "onBufferReceived()");
	}

	@Override
	public void onEndOfSpeech() {
		Log.i(TAG, "onEndOfSpeech()");  
		mSR.stopListening();
		mView.showWorking();
		mTts.speak("Processing", TextToSpeech.QUEUE_ADD, null);
	}

	@Override
	public void onError(int error) {
		Log.i(TAG, "onError()");
		mView.showError();
		String msg = getErrorString(error);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "error");	
		mTts.speak("Error occurred, " + msg + ", two finger tap to re-try", TextToSpeech.QUEUE_ADD, utterance);
		mSRActive = false;
	}

	@Override
	public void onEvent(int eventType, Bundle params) {
		Log.i(TAG, "onEvent()");
	}

	@Override
	public void onPartialResults(Bundle partialResults) {
		Log.i(TAG, "onPartialResults()");
		//mTts.speak("Partial results ready", TextToSpeech.QUEUE_ADD, null);
	}

	@Override
	public void onReadyForSpeech(Bundle params) {
		Log.i(TAG, "onReadyForSpeech()");
		mVibe.vibrate(100);
		mView.showListening();
		//mTts.speak("ready", TextToSpeech.QUEUE_ADD, null);
	}

	@Override
	public void onResults(Bundle results) {
		Log.i(TAG, "onResults()");
		//mTts.speak("Results ready", TextToSpeech.QUEUE_ADD, null);
		mView.showNormal();
		onSpeechResult(results);
	}

	@Override
	public void onRmsChanged(float rmsdB) {
		Log.i(TAG, "onRmsChanged()");
	}
	
	private String getErrorString(int errorType) {
		Log.i(TAG, "getErrorString()");
        switch (errorType) {
        // We use CLIENT_ERROR to signify that voice search is not available on the device.
        case SpeechRecognizer.ERROR_CLIENT:
            return getResources().getString(R.string.voice_not_installed);
        case SpeechRecognizer.ERROR_NETWORK:
            return getResources().getString(R.string.voice_network_error);
        case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
            return getResources().getString(R.string.voice_network_error);
        case SpeechRecognizer.ERROR_AUDIO:
            return getResources().getString(R.string.voice_audio_error);
        case SpeechRecognizer.ERROR_SERVER:
            return getResources().getString(R.string.voice_server_error);
        case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
            return getResources().getString(R.string.voice_speech_timeout);
        case SpeechRecognizer.ERROR_NO_MATCH:
            return getResources().getString(R.string.voice_no_match);
        default:
            return getResources().getString(R.string.voice_error);
        }
    }
	
	protected class SpeechInputView extends ViewGroup implements GestureDetector.OnDoubleTapListener, 
		GestureDetector.OnGestureListener, View.OnTouchListener  {
	
		private GestureDetector mDetector;
		private Handler mUiHandler;
		   
	    public SpeechInputView(Context context) {
	    	super(context);
	    	mUiHandler = new Handler();
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
				String msg = "trying";
				if (e2.getRawX() - e1.getRawX() > 0) {
					if (mIndex < mInput.size()) mIndex++;
					msg += " next";
				}
				else {
					if (mIndex > -1) mIndex--;
					msg += " previous";
				}
				if (mInput.size() > 1) mTts.speak(msg, TextToSpeech.QUEUE_ADD, null);
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
				int arg4) {
			// TODO Auto-generated method stub
			
		}
		
		public void showInitializing() {
			Log.i(TAG, "onShowInitializing()");
			mUiHandler.post(new Runnable() {
                public void run() {
                	changeBackground(Color.YELLOW);
                	invalidate();
                }
			});
		}

		public void showListening() {
			Log.i(TAG, "onShowListening()");
			mUiHandler.post(new Runnable() {
                public void run() {
                	changeBackground(Color.GREEN);
                	invalidate();
                }
			});
		}
		
		public void showHearing() {
			Log.i(TAG, "onShowHearing()");
			mUiHandler.post(new Runnable() {
                public void run() {
                	changeBackground(0xff009900);
                	invalidate();
                }
			});
		}

		public void showWorking() {
			Log.i(TAG, "onShowWorking()");
			mUiHandler.post(new Runnable() {
                public void run() {
                	changeBackground(Color.BLUE);
                	invalidate();
                }
			});
		}
		
		public void showNormal() {
			Log.i(TAG, "onShowNormal()");
			mUiHandler.post(new Runnable() {
                public void run() {
                	changeBackground(Color.BLACK);
                	invalidate();
                }
			});
		}
		
		public void showError() {
			Log.i(TAG, "onShowError()");
			mUiHandler.post(new Runnable() {
                public void run() {
                	changeBackground(Color.RED);
                	invalidate();
                }
			});
		}
		
		protected void changeBackground(int color) {
			Log.i(TAG, "changeBackground()");
			this.setBackgroundColor(color);
		}

	}
}
