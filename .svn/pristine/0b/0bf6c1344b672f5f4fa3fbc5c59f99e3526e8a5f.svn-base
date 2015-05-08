package cs.washington.edu.vbreadwrite;

/* Reads instructions for a given game and lets user
 * scroll back and forth through instructions.
 */

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

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
		
	// ReadBraille instructions
	private String[] rbSpeakInstr = {"How to play VB Reeder:",
			"The touchscreen is divided into six regions to represent the Braille cell,",
			"Touch a region and its number will be spoken,",
			"Any region that contains a dot will vibrate when touched,",
			"use the dots to determine what letter is on the screen",
			"swipe right with two fingers to go to the input screen,",
			"on the input screen you will give your answer to what letter is being displayed",
			"there are several different types of input screens that can be used for entering your answer, ",
			"use the menu button on the phone to select the input screen you want to use, ",
			"fling down with one finger on the input screen for help using that screen, ",
			"pressing the back button on any of the input screens will cancel the input and redisplay the current letter,",
			"All input screens also provide a way to speak the correct answer without entering a letter,",
			"after you enter your answer VB Reeder will tell you whether you were correct,",
			"swipe right with two fingers to move to the next letter,",
			"You can leave VB Reeder at any time by pressing the back button,",
			"You have reached the end of these instructions."};
	
	private String[] rbDisplayInstr = {"How to play VB Reader:",
			"The touchscreen is divided into six regions to represent the Braille cell.",
			"Touch a region and its number will be spoken.",
			"Any region that contains a dot will vibrate when touched.",
			"Use the dots to determine what letter is on the screen.",
			"Swipe right with two fingers to go to the input screen.",
			"On the input screen, you will give your answer to what letter is being displayed.",
			"There are several different types of input screens that can be used for entering your answer.",
			"Use the menu button on the phone to select the input screen you want to use.",
			"Fling down with one finger on the input screen for help using that screen.",
			"Pressing the back button on any of the input screens will cancel the input and redisplay the current letter.",
			"All input screens also provide a way to speak the correct answer without entering a letter.",
			"After you enter your answer, VB Reader will tell you whether you are correct.",
			"Swipe right with two fingers to move to the next letter.",
			"You can leave VB Reader at any time by pressing the back button.",
			"You have reached the end of these instructions."};
	
	// WriteBraille instructions
	private String[] wbSpeakInstr = {"How to play VB Rye Ter:",
			"The touchscreen is divided into six regions to represent the Braille cell,",
			"Touch a region and its number will be spoken,",
			"The game will speak a letter for you to enter,",
			"To enter the dots of the letter, navigate by touch to the region where a dot should be placed and double tap to add a dot,",
			"Any region that contains a dot will vibrate when touched,",
			"double tap on a vibrating region to remove the dot,",
			"Once you are finished entering dots, swipe right with two fingers to confirm your answer,",
			"VB Writer will tell you whether your answer is correct;",
			"If you enter the correct answer, a new letter to write will be spoken,",
			"otherwise, VB Writer will tell you the correct answer,",
			"You can leave VB Writer at any time by pressing the back button,",
			"Press the menu button on the phone to hear again the name of the letter to enter;",
			"You have reached the end of these instructions."};
	
	private String[] wbDisplayInstr = {"How to play VB Writer:",
			"The touchscreen is divided into six regions to represent the Braille cell.",
			"Touch a region and its number will be spoken.",
			"The game will speak a letter for you to enter.",
			"To enter the dots of the letter, navigate by touch to the region where a dot should be placed and double tap to add a dot.",
			"Any region that contains a dot will vibrate when touched.",
			"Double tap on a vibrating region to remove the dot.",
			"Once you are finished entering dots, swipe right with two fingers to confirm your answer.",
			"VB Writer will tell you whether your answer is correct.",
			"If you enter the correct answer, a new letter to write will be spoken.",
			"Otherwise, VB Writer will tell you the correct answer.",
			"You can leave VB Writer at any time by pressing the back button.",
			"Press the menu button on the phone to hear again the name of the letter to enter.",
			"You have reached the end of these instructions."};
	
	private GestureDetector detector; 
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		self = this;
		
		String game = this.getIntent().getAction();
		//Toast.makeText(this, "Game: " + game, Toast.LENGTH_LONG).show();
		game = game.substring(game.lastIndexOf('_') + 1);
		if (game.equals("VBR")) {
			speakInstr = rbSpeakInstr;
			displayInstr = rbDisplayInstr;
		}
		else if (game.equals("VBW")) {
			speakInstr = wbSpeakInstr;
			displayInstr = wbDisplayInstr;
		}
		else {
			speakInstr = null;
			displayInstr = null;
		}

		tts = new TextToSpeech(this, this);
		tts.setOnUtteranceCompletedListener(this);
	
		// Keep menu in portrait layout
		setContentView(R.layout.sentence);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        detector = new GestureDetector(this, this);
        
		tv = (TextView)findViewById(R.id.sentence);
		tv.setOnTouchListener(this);
		index = 0;
		scrollDistance = 0;
		vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		
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
			tts.speak("At the sound of the bell, scroll right to continue, " +
					"tap on screen at any time to repeat current instruction",
					TextToSpeech.QUEUE_FLUSH, null);
			getSentence(TextToSpeech.QUEUE_ADD);
		}
	}

		
	@Override
	public void onPause() {
		super.onPause();
		if (tts != null) {
			tts.stop();
			tts.shutdown();
			tts = null;
		}
	}
	
	@Override
	public void onResume() {
		super.onResume();
		if (tts == null) {
			tts = new TextToSpeech(this, this);
		}
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

	private void scrollRight() {
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
	
	private void scrollLeft() {
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
			
			Toast.makeText(this, "Scroll right for next instruction", Toast.LENGTH_LONG).show();
			
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
