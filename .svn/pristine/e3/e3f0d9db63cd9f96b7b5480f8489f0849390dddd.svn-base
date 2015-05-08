package cs.washington.edu.buddies;

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
	
	// Braille Words instructions
	private String[] bwSpeakInstr = {"How to play Braille Words:",
			"The goal of the game is to read the Braille letters vibrated on screen " +
				"and determine the word they spell,",
			"The letters in the word are vibrated one at a time,",
			"The touchscreen is divided into six regions to represent the Braille cell,",
			"Touch a region and its number will be spoken,",
			"If the touched region contains a dot, the phone will vibrate,",
			"Use the vibrations to determine what letter is being displayed,",
			"When you have determined what letter is being displayed, either press down " +
				"on the trackball or on the center button of the directional pad, ",
			"The next letter in the word will be displayed and vibrated,",
			"Use the trackball or directional keys to move backward and forward through " +
				"the letters,",
			"When you know the word, press down on the center button or trackball,",
			"a touchscreen kee pad will be displayed where you input the word you just read in Braille,",
			"You can navigate the kee pad by either touching and dragging on the screen" +
				" or by using the trackball or directional keys;",
			" the letters on the keepad's keys will be spoken when the kee is touched,",
			" when you hear the kee you want, press down again on the trackball or center button " +
				"of the directional pad to add that letter onto your answer",
			" you can also double tap on the letter,",
			" continue selecting letters in this manner until you have spelled the word you read in Braille,",
			" you can also use the hard keeboard to add letters to your answer,",
			" you can use the backspace key to remove the last letter from your answer,",
			" to remove all letters at once, shake the phone in a diagonal corner-to-corner direction several times,",
			" at the bottom of the keepad are four special buttons: ",
			" the 'Play Correct Answer' button speaks the correct answer to you before selecting a new word,",
			" the 'Replay Word' button replays the word for you,",
			" the 'Reed Current Answer' button reeds the letters of your current answer back to you,",
			" and the 'Submit Answer' button is used to enter your answer,",
			" to select a button, navigate to it and press down on the trackball or on the center directional key",
			" after you have submitted your answer, the vibrating Braille screen is displayed and feedback is given" +
				" to let you know whether your answer is correct,",
			" if your answer is correct, the word is spoken and then a new word is selected and played,",
			" For incorrect answers, if you have not exceeded the maximum number of tries," + 
				" the word will be replayed and you can try again.",
			" Otherwise the correct answer will be spoken,",
			" You can leave the game at any time by pressing the back button,",
			" You can change the difficulty level by pressing the menu key,",
	" End of instructions."};
			
	private String[] bwDisplayInstr = {"How to play Braille Words:",
			"The goal of the game is to read the Braille letters vibrated on screen " +
				"and determine the word they spell.",
			"The letters in the word are vibrated one at a time.",
			"The touchscreen is divided into six regions to represent the Braille cell.",
			"Touch a region and its number will be spoken.",
			"If the touched region contains a dot, the phone will vibrate.",
			"Use the vibrations to determine what letter is being displayed.",
			"When you have determined what letter is being displayed, either press down " +
				"on the trackball or on the center button of the directional pad.",
			"The next letter in the word will be displayed and vibrated.",
			"Use the trackball or directional keys to move backward and forward through " +
				"the letters.",
			"When you know the word, press down on the center button or trackball.",
			"A touchscreen keypad will be displayed where you input the word you just read in Braille.",
			"You can navigate the keypad by either touching and dragging on the screen" +
			" or by using the trackball or directional keys.",
			"The letters on the keypad's keys will be spoken when the key is touched.",
			"When you hear the key you want, press down again on the trackball or center button " +
				"of the directional pad to add that letter onto your answer.",
			"You can also double tap on the letter.",
			"Continue selecting letters in this manner until you have spelled the word you read in Braille.",
			"You can also use the hard keyboard to add letters to your answer.",
			"You can use the backspace key to remove the last letter from your answer.",
			"To remove all letters at once, shake the phone in a diagonal corner-to-corner direction several times.",
			"At the bottom of the keypad are four special buttons: ",
			"the 'Play Correct Answer' button speaks the correct answer to you before selecting a new word;",
			"the 'Replay Word' button replays the word for you;",
			"the 'Read Current Answer' button reaeds the letters of your current answer back to you;",
			"and the 'Submit Answer' button is used to enter your answer.",
			"To select a button, navigate to it and press down on the trackball or on the center directional key.",
			"After you have submitted your answer, the vibrating Braille screen is displayed and feedback is given" +
				" to let you know whether your answer is correct.",
			"If your answer is correct, the word is spoken and then a new word is selected and played.",
			"For incorrect answers, if you have not exceeded the maximum number of tries," + 
				" the word will be replayed and you can try again.",
			"Otherwise the correct answer will be spoken.",
			"You can leave the game at any time by pressing the Back button.",
			"You can change the difficulty level by pressing the Menu key.",
			"End of instructions."};
	
	// ReadBraille instructions
	private String[] rbSpeakInstr = {"How to play Reed Braille:",
			"The touchscreen is divided into six regions to represent the Braille cell,",
			"Touch a region and its number will be spoken,",
			"Any region that contains a dot will vibrate when touched,",
			"Depending on what mode you have selected, the dots contained" +
				" within the screen will represent a letter, number, or punctuation mark;",
			"when you have determined what symbol is being displayed, " +
				"either press down on the trackball or on the center button of the directional pad,",
			"a touchscreen kee pad for the current mode will be displayed,",
			"You can navigate the kee pad by either touching and dragging on the screen" +
				" or by using the trackball or directional keys;",
			"the symbols on the keys will be spoken when navigated to;",
			"two special keys are also displayed,",
			"one will play the correct answer,",
			"the other will return to the display without requiring an answer;",
			"when you hear the key you want, press down again on the trackball or center button of the directional pad;",
			"you will return to the vibrating Braille screen and feedback will be given" +
				" to let you know whether your answer is correct;",
			"you can also enter your answer while on the touch kee pad by using the hard keys instead;",
			"for correct answers, the symbol is replayed before a new symbol is selected and played;",
			"if you do not wish to have correct answers replayed, you can turn off the replay setting on the Settings menu;",
			"for incorrect answers, the symbol will be replayed if you have not exceeded " + 
				"the maximum number of tries and you can try again,",
			"Otherwise the correct answer will be spoken,",
			"You can leave the game at any time by pressing the back button,",
			"You can change modes among alphabetic, numeric, and punctuation by pressing the menu key;",
			"End of instructions."};
	
	private String[] rbDisplayInstr = {"How to play Read Braille:",
			"The touchscreen is divided into six regions to represent the Braille cell.",
			"Touch a region and its number will be spoken.",
			"Any region that contains a dot will vibrate when touched.",
			"Depending on what mode you have selected, the dots contained" +
				" within the screen will represent a letter, number, or punctuation mark.",
			"When you have determined what symbol is being displayed, " +
				"either press down on the trackball or on the center button of the directional pad.",
			"A touchscreen keypad for the current mode will be displayed.",
			"You can navigate the keypad by either touching and dragging on the screen" +
				" or by using the trackball or directional keys.",
			"The symbols on the keys will be spoken when navigated to.",
			"Two special keys are also displayed.",
			"One will play the correct answer.",
			"The other will return to the display without requiring an answer.",
			"When you hear the key you want, press down again on the trackball or center button of the directional pad.",
			"You will return to the vibrating Braille screen and feedback will be given" +
				" to let you know whether your answer is correct.",
			"You can also enter your answer while on the touch keypad by using the hard keys instead.",
			"For correct answers, the symbol is replayed before a new symbol is selected and played.",
			"If you do not wish to have correct answers replayed, you can turn off the replay setting on the Settings menu.",
			"For incorrect answers, the symbol will be replayed if you have not exceeded " + 
				"the maximum number of tries and you can try again.",
			"Otherwise the correct answer will be spoken.",
			"You can leave the game at any time by pressing the back button.",
			"You can change modes among alphabetic, numeric, and punctuation by pressing the menu key.",
			"End of instructions."};
	
	// WriteBraille instructions
	private String[] wbSpeakInstr = {"How to play write Braille:",
			"The touchscreen is divided into six regions to represent the Braille cell,",
			"Touch a region and its number will be spoken,",
			"The game will speak a symbol for you to enter,",
			"To enter the dots of the symbol, navigate by touch to the region where a dot should be placed and double tap to add a dot,",
			"Any region that contains a dot will vibrate when touched,",
			"double tap on a vibrating region to remove the dot,",
			"to remove all dots at once, shake the phone in a diagonal corner to corner direction,",
			"Once you are finished entering dots, press the enter key to confirm your answer,",
			"Depending on your phone, the enter key is either " + 
				"the center button of the directional pad or the down movement on the trackball,",
			"Feedback is given to let you know whether your answer is correct;",
			"If you enter the correct answer, a new symbol to write will be spoken,",
			"otherwise, you will be prompted to try again if you have not exceeded the maximum number of tries,",
			"You can leave the game at any time by pressing the back button,",
			"You can change modes among alphabetic, numeric, and punctuation by pressing the menu key;",
			"End of instructions."};
	
	private String[] wbDisplayInstr = {"How to play Write Braille:",
			"The touchscreen is divided into six regions to represent the Braille cell.",
			"Touch a region and its number will be spoken.",
			"The game will speak a symbol for you to enter.",
			"To enter the dots of the symbol, navigate by touch to the region where a dot should be placed and double tap to add a dot.",
			"Any region that contains a dot will vibrate when touched.",
			"Double tap on a vibrating region to remove the dot.",
			"To remove all dots at once, shake the phone in a diagonal, corner-to-corner direction.",
			"Once you are finished entering dots, press the enter key to confirm your answer.",
			"Depending on your phone, the enter key is either " + 
				"the center button of the directional pad or the down movement on the trackball.",
			"Feedback is given to let you know whether your answer is correct.",
			"If you enter the correct answer, a new symbol to write will be spoken.",
			"Otherwise, you will be prompted to try again if you have not exceeded the maximum number of tries.",
			"You can leave the game at any time by pressing the back button.",
			"You can change modes among alphabetic, numeric, and punctuation by pressing the menu key.",
			"End of instructions."};
	
	private GestureDetector detector; 
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		self = this;
		
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
		int game = this.getIntent().getIntExtra("game", Instructions.WORDS);
		switch (game) {
		case Instructions.READ:
			speakInstr = rbSpeakInstr;
			displayInstr = rbDisplayInstr;
			break;
		case Instructions.WRITE:
			speakInstr = wbSpeakInstr;
			displayInstr = wbDisplayInstr;
			break;
		case Instructions.WORDS:
			speakInstr = bwSpeakInstr;
			displayInstr = bwDisplayInstr;
			break;
		}
		vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		
	}
	
	@Override
	public void onInit(int status) {
		index = 0;
		tts.addEarcon("ding", getPackageName(), R.raw.ding);
		tts.setOnUtteranceCompletedListener(self);
        utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "bell");
		tts.speak("At the sound of the bell, scroll right to continue, " +
				"tap on screen at any time to repeat current instruction",
				TextToSpeech.QUEUE_FLUSH, null);
		getSentence(TextToSpeech.QUEUE_ADD);
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
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent start, MotionEvent stop, float distanceX,
			float distanceY) {
		
		boolean result = false;
		if (scrollDistance == 0) {
			scrollDistance = ((TextView)findViewById(R.id.sentence)).getWidth() / 2;
			scrollDistance = (scrollDistance == 0) ? 150 : scrollDistance;
		}
		
		// scroll right
		if (stop.getRawX() - start.getRawX() > scrollDistance 
				&& lastDownEvent != start) {
			lastDownEvent = start;
			scrollRight();
			result = true;
		}
		// scroll left
		else if (stop.getRawX() - start.getRawX() < -scrollDistance 
				&& lastDownEvent != start) {
			lastDownEvent = start;
			scrollLeft();
			result = true;
		}
		return result;
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
