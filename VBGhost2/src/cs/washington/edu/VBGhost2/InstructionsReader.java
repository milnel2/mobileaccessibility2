package cs.washington.edu.VBGhost2;

/* Reads instructions for a given game and lets user
 * scroll back and forth through instructions.
 */

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import cs.washington.edu.VBGhost2.R;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class InstructionsReader extends Activity implements View.OnTouchListener,
	TextToSpeech.OnUtteranceCompletedListener, TextToSpeech.OnInitListener,
	GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {
	
	private static final String TAG = "InstructionsReader";
	
	private InstructionsReader self;
	private TextToSpeech tts;
	HashMap<String, String> utterance = new HashMap<String, String>();
	private int index;
	private MotionEvent lastDownEvent = null;
	private int scrollDistance;
	private TextView tv;
	
	private String[] speakInstr;
	private String[] displayInstr;

	// vibration
	private final long [] pattern = {20,40};
	private Vibrator vibe;
		
	//General Instructions
	private String[] mainSpeakInstr = { "To play the game of ghost,",
			"you start a word fragment with a single letter ",
			"players take turns adding on letters to the fragment.",
			"The word fragment you create must be the beginning of a real word.",
			"If you complete a word that is at least three characters long, you lose.",
			"If you create a fragment that is not the beginning of a valid word, you lose.", 
			"You may either play alone or play with a friend. " ,
			"If you believe the other player has entered a complete word or has entered an invalid word, you can challenge her.",
			"To return to the main menu, use back button or menu button." };
	
	private String[] mainDisplayInstr = { "To play the game of ghost,",
			"you start a word fragment with a single letter ",
			"players take turns adding on letters to the fragment.",
			"The word fragment you create must be the beginning of a real word.",
			"If you complete the word that is at least three characters long, you lose.",
			"If you create a fragment that is not the beginning of a valid word, you lose.", 
			"You may either play alone or play with a friend. ",
			"If you believe the other player has entered a complete word or has entered an invalid word, you can challenge her.",
			"To return to the main menu, use back button or menu button."};
	
	private GestureDetector detector; 
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		self = this;

    	Log.v(TAG, "onCreate instructions Reader");
		speakInstr = mainSpeakInstr;
		displayInstr = mainDisplayInstr;
			
		//create TextToSpeech    
	    tts = GlobalState.getTTS();    
	    if (tts == null) {
	    	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
	        tts = gs.createTTS(this, this);
	    }
	    else {
	    	Log.v(TAG, "on speakOnDisplay()");
	    	//speakOnDisplay();
	    }
	    Log.v(TAG, "tts = " + tts);
		//tts.setOnUtteranceCompletedListener(this);	
		tts.setOnUtteranceCompletedListener(self);
	
		// Keep menu in portrait layout
		setContentView(R.layout.sentence);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        detector = new GestureDetector(this, this);
    	Log.v(TAG, "onCreate instructions Reader after gesture");     
		tv = (TextView)findViewById(R.id.sentence);
		tv.setOnTouchListener(this);
		index = 0;
		scrollDistance = 0;
		vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		speakOnDisplay();
		
	}
	
	@Override
	public void onInit(int status) {
		if (speakInstr == null) {
			tts.speak("Error occurred. Returning to menu.", TextToSpeech.QUEUE_ADD, null);
			finish();
		}
		else {
			index = 0;
			tts.addEarcon("ding", getPackageName(), R.raw.ding);
			tts.setOnUtteranceCompletedListener(self);
	        utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "bell");
			tts.speak("At the sound of the bell, scroll left to continue. " +
					"Tap on screen at any time to repeat current instruction."+
					"Click back to return to main menu",
					TextToSpeech.QUEUE_FLUSH, null);
			getSentence(TextToSpeech.QUEUE_ADD);
		}
	}

	public void speakOnDisplay(){
		tts.addEarcon("ding", getPackageName(), R.raw.ding);
		tts.setOnUtteranceCompletedListener(self);
        utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "bell");
		tts.speak("At the sound of the bell, scroll left to continue. " +
				"Tap on screen at any time to repeat current instruction."+
				"Click back to return to main menu",
				TextToSpeech.QUEUE_FLUSH, null);
		getSentence(TextToSpeech.QUEUE_ADD);
		
	}
		
	@Override
	public void onPause() {
		super.onPause();
	/*	if (tts != null) {
			tts.setOnUtteranceCompletedListener(null);
			tts.stop();
			tts.shutdown();
			tts = null;
		}*/
	}
	
	@Override
	public void onResume() {
		super.onResume();
    	tts = GlobalState.getTTS();
	    if (tts == null) {
	    	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
	        tts = gs.createTTS(this, this);
	    }
	    Log.i(TAG, "tts = " + tts);
    	tts.setOnUtteranceCompletedListener(self);
	}


	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// scroll right
		if (e2.getRawX() - e1.getRawX() > 0) {
			scrollRight();
		}
		// scroll left
		else {
			scrollLeft();
		}
		return true;
	}


	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent start, MotionEvent stop, float distanceX,
			float distanceY) {
		
//		boolean result = false;
//		if (scrollDistance == 0) {
//			scrollDistance = ((TextView)findViewById(R.id.sentence)).getWidth() / 3;
//			scrollDistance = (scrollDistance == 0) ? 100 : scrollDistance;
//		}
//		
//		// scroll right
//		if (stop.getRawX() - start.getRawX() > scrollDistance 
//				&& lastDownEvent != start) {
//			lastDownEvent = start;
//			scrollRight();
//			result = true;
//		}
//		// scroll left
//		else if (stop.getRawX() - start.getRawX() < -scrollDistance 
//				&& lastDownEvent != start) {
//			lastDownEvent = start;
//			scrollLeft();
//			result = true;
//		}
//		return result;
		return false; 
	}

	private void scrollLeft() {
		if (index >= 0 && index < speakInstr.length - 1) {
			index++;
			getSentence(TextToSpeech.QUEUE_FLUSH);
		}
		else if (index >= speakInstr.length - 1) {
			tts.speak("No more instructions to play", TextToSpeech.QUEUE_FLUSH, null);
		}
		else {
			index = 0;
			getSentence(TextToSpeech.QUEUE_FLUSH);
		}
	}
	
	private void scrollRight() {
		int queueAction = TextToSpeech.QUEUE_FLUSH;
		if (index > 0 && index < speakInstr.length) {
			index--;
		}
		else if (index == 0) {
				
				tts.speak("At beginning of instructions", TextToSpeech.QUEUE_FLUSH, null);
				queueAction = TextToSpeech.QUEUE_ADD;
		}
		else {
			index = speakInstr.length - 1;
		}
		getSentence(queueAction);
	}
	
	protected void getSentence(int queueAction) {
		if (index >= 0 && index < speakInstr.length) {
			
			Toast.makeText(this, "Scroll left for next instruction", Toast.LENGTH_LONG).show();
			
			// set sentence on screen
			tv.setText(displayInstr[index]);
				
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "bell");
			tts.speak(speakInstr[index], queueAction, utterance);
			
			// vibrate while synthesizing so user knows scroll was read
			vibe.vibrate(pattern, 0);
			try {
				Thread.sleep(500);
			}
			catch (Exception e) {}
			vibe.cancel();
		}
	}


	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {  
    	if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
    		scrollRight();
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
    		scrollLeft();
    	}
    	else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
    		getSentence(TextToSpeech.QUEUE_FLUSH);
    	}
    	else if (keyCode == KeyEvent.KEYCODE_BACK) {
    		tts.speak("Back to menu", TextToSpeech.QUEUE_FLUSH, null);
    		finish();
    	}
    	else if (keyCode == KeyEvent.KEYCODE_MENU) {
    		tts.speak("Back to menu", TextToSpeech.QUEUE_FLUSH, null);
    		finish();
    	}
    	Log.v(TAG, "onKeyDown");
    	return super.onKeyDown(keyCode, keyEvent);
    }

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	@Override
	public void onUtteranceCompleted(String utteranceId) {
		if (utteranceId.equals("bell")) {
			// play bell sound
			if (tts.isSpeaking()) {
				try {
					Thread.sleep(500);
				}
				catch (Exception e) {}
			}
			tts.playEarcon("ding", TextToSpeech.QUEUE_ADD, null);
		}
	}

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		getSentence(TextToSpeech.QUEUE_FLUSH);
		return true;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// call detector's touch event so double tap on button
		// will be caught.
		if (detector != null) this.detector.onTouchEvent(event);
		return true;
	}
}
