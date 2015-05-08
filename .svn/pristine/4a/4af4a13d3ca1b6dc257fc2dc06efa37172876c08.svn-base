package cs.washington.edu.buddies;

import java.util.HashMap;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

/* UI for Settings class */
public class SettingsGUI extends Activity implements RadioGroup.OnCheckedChangeListener, 
	CompoundButton.OnCheckedChangeListener, View.OnTouchListener, 
	TextToSpeech.OnUtteranceCompletedListener {
	
		//for GUI
		private RadioGroup modes_radio;
		private RadioGroup tries_radio;
		private RadioGroup rate_radio;
		private RadioGroup level_radio;
		private CompoundButton[] checkBoxes;
		private Button blankBtn;
		
		// for speech
		private TextToSpeech tts;
		HashMap<String, String> utterance = new HashMap<String, String>();
		
		//for speaking status
		private String symbolMode;
		private String maxTries;
		private String ttsOn;
		private String replay;
		private String wordLevel;
		private String speechRate;
		private String keypad;
		
		private static final String TAG = "Settings";
		private View[] views;
		private int lastLoc = -1;
		private boolean init = true;

		
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.settingsgui);
	        
			tts = BrailleBuddiesMenu.tts;
			
			tts.speak("Touch to hear setting, tap it to enable it", TextToSpeech.QUEUE_FLUSH, null);	
			
			getUIElements();
			// prevent focus on buttons
	        blankBtn.requestFocus();
			setUIValues();
			
			Toast.makeText(this, "Touch to hear setting, tap to enable it", Toast.LENGTH_LONG).show();
			speakSettings();
			loadViews();
			init = false;
		}
		
		/* Get the references of the UI elements */
		private void getUIElements() {
			modes_radio =(RadioGroup)findViewById(R.id.radio_symbol);
			modes_radio.setOnCheckedChangeListener(this);
			tries_radio = (RadioGroup)findViewById(R.id.radio_tries);
			tries_radio.setOnCheckedChangeListener(this);
			rate_radio = (RadioGroup)findViewById(R.id.radio_speech);
			rate_radio.setOnCheckedChangeListener(this);
			level_radio = (RadioGroup)findViewById(R.id.radio_level);
			level_radio.setOnCheckedChangeListener(this);
			checkBoxes = new CompoundButton[3];
			checkBoxes[0] = (CompoundButton)findViewById(R.id.replay_symbol);
			checkBoxes[1] = (CompoundButton)findViewById(R.id.read_menus);
			checkBoxes[2] = (CompoundButton)findViewById(R.id.qwerty_keypad);
			checkBoxes[0].setOnCheckedChangeListener(this);
			checkBoxes[1].setOnCheckedChangeListener(this);
			checkBoxes[2].setOnCheckedChangeListener(this);
			blankBtn = (Button)findViewById(R.id.keypad_blank);
		}
		
		/* Set the UI elements to the current settings values */
		private void setUIValues () {
			checkBoxes[0].setChecked(Settings.getReplay(this));
			checkBoxes[1].setChecked(Settings.getTts(this));
			checkBoxes[2].setChecked(Settings.getKeypad(this));
			
			switch (Settings.getMode(this)) {
			case Settings.NUMBERS:
				modes_radio.check(R.id.radio_numbers);
				break;
			case Settings.PUNCTUATION:
				modes_radio.check(R.id.radio_punctuation);
				break;
			case Settings.ALPHABET:
			default:
				modes_radio.check(R.id.radio_alphabet);
				break;
			}
			
			switch (Settings.getLevel(this)) {
			case Settings.INTERMEDIATE:
				level_radio.check(R.id.radio_medium);
				break;
			case Settings.HARD:
				level_radio.check(R.id.radio_hard);
				break;
			case Settings.EASY:
			default:
				level_radio.check(R.id.radio_easy);
				break;
			}
			
			switch (Settings.getMaxTries(this)) {
			case Settings.ONE_MAX_TRY:
				tries_radio.check(R.id.radio_one_try);
				break;
			case Settings.THREE_MAX_TRY:
				tries_radio.check(R.id.radio_three_try);
				break;
			case Settings.TWO_MAX_TRY:
			default:
				tries_radio.check(R.id.radio_two_try);
				break;
			}
			
			switch (Settings.getSpeechRate(this)) {
			case Settings.MIN_SPEECH_RATE:
				rate_radio.check(R.id.radio_half);
				break;
			case Settings.DOUBLE_SPEECH_RATE:
				rate_radio.check(R.id.radio_twice);
				break;
			case Settings.MAX_SPEECH_RATE:
				rate_radio.check(R.id.radio_thrice);
				break;
			case Settings.NORMAL_SPEECH_RATE:
			default:
				rate_radio.check(R.id.radio_normal);
				break;
			}
			
		}
		
		/* Speak all current settings */
		private void speakSettings() {
			symbolMode = (Settings.getMode(this) == Settings.ALPHABET) ? "alphabet" :
							(Settings.getMode(this) == Settings.NUMBERS) ? "numbers" : "punctuation";
			maxTries = (Settings.getMaxTries(this) == Settings.ONE_MAX_TRY) ? "1" :
				(Settings.getMaxTries(this) == Settings.TWO_MAX_TRY) ? "2" : "3";
			ttsOn = (Settings.getTts(this)) ? "enabled" : "disabled";
			replay = (Settings.getReplay(this)) ? "enabled" : "disabled";
			wordLevel = (Settings.getLevel(this) == Settings.EASY) ? "easie" :
				(Settings.getLevel(this) == Settings.INTERMEDIATE) ? "intermediate" : "hard";
			speechRate = (Settings.getSpeechRate(this) == Settings.MIN_SPEECH_RATE) ? "half normal" :
				(Settings.getSpeechRate(this) == Settings.NORMAL_SPEECH_RATE) ? "normal" :
				(Settings.getSpeechRate(this) == Settings.DOUBLE_SPEECH_RATE) ? "twice normal " : "three times normal";
			keypad = (Settings.getKeypad(this)) ? "qwerty" : "alphabetic";
			tts.speak("Your current settings are: " + "read menus " + ttsOn + ", replay symbol " + 
					replay + ", keypad format for letters is " + keypad + ", symbol mode is " + symbolMode + 
					", word level is " + wordLevel + ", maximum number of tries is " + maxTries +
					", and speech rate is set to " + speechRate + ", press back kee to return to menu", 
					TextToSpeech.QUEUE_ADD, null);
		}
		
		/* Loads in Views for UI elements so we can figure out which one 
		 * is being touched at any time.
		 */
	    private void loadViews() {
	    	views = new View[20];
	    	views[0] = (CompoundButton)findViewById(R.id.read_menus);
        	views[1] = (CompoundButton)findViewById(R.id.replay_symbol);
        	views[2] = (CompoundButton)findViewById(R.id.qwerty_keypad);
        	views[3] = (TextView)findViewById(R.id.symbol_mode);
        	views[4] = (RadioButton)findViewById(R.id.radio_alphabet);
        	views[5] = (RadioButton)findViewById(R.id.radio_numbers);
        	views[6] = (RadioButton)findViewById(R.id.radio_punctuation);
        	views[7] = (TextView)findViewById(R.id.word_level);
        	views[8] = (RadioButton)findViewById(R.id.radio_easy);
        	views[9] = (RadioButton)findViewById(R.id.radio_medium);
        	views[10] = (RadioButton)findViewById(R.id.radio_hard);
        	views[11] = (TextView)findViewById(R.id.max_tries);
        	views[12] = (RadioButton)findViewById(R.id.radio_one_try);
        	views[13] = (RadioButton)findViewById(R.id.radio_two_try);
        	views[14] = (RadioButton)findViewById(R.id.radio_three_try);
        	views[15] = (TextView)findViewById(R.id.speech_rate);
        	views[16] = (RadioButton)findViewById(R.id.radio_half);
        	views[17] = (RadioButton)findViewById(R.id.radio_normal);
        	views[18] = (RadioButton)findViewById(R.id.radio_twice);
        	views[19] = (RadioButton)findViewById(R.id.radio_thrice);
	    	for (int i = 0; i < views.length; i++) {
	    		final String tag = (String)views[i].getTag();
	    		views[i].setFocusableInTouchMode(true);
	    		views[i].setOnTouchListener(this);
	    		views[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
	 
	    			@Override
	    			public void onFocusChange(View v, boolean hasFocus) {
	    				if (hasFocus && !init) {
		    				tts.speak(tag, TextToSpeech.QUEUE_FLUSH, null);
	    				}
	    			}
	    		});
	    	}
	    }
	    	
	    
		/* Save the radio group values when changed */
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			String text = "";
			boolean success = false;
			if (group == modes_radio) {
				if (checkedId == R.id.radio_alphabet){
					text = "Selected alphabet mode";
					success = Settings.setMode(this, Settings.ALPHABET);
				} else if (checkedId == R.id.radio_numbers){
					text = "Selected numbers mode";
					success = Settings.setMode(this, Settings.NUMBERS);
				} else  {
					text = "Selected punctuation mode";
					success = Settings.setMode(this, Settings.PUNCTUATION);
				}
			} else if (group == tries_radio){
				if (checkedId == R.id.radio_one_try){
					text = "Selected max of 1 try";
					success = Settings.setMaxTries(this, Settings.ONE_MAX_TRY);
				} else if (checkedId == R.id.radio_two_try){
					text = "Selected max of 2 tries";
					success = Settings.setMaxTries(this, Settings.TWO_MAX_TRY);
				} else  {
					text = "Selected max of 3 tries";
					success = Settings.setMaxTries(this, Settings.THREE_MAX_TRY);
				}			
			} else if (group == level_radio){
				if (checkedId == R.id.radio_easy){
					text = "Selected easie word level";
					success = Settings.setLevel(this, Settings.EASY);
				} else if (checkedId == R.id.radio_medium){
					text = "Selected intermediate word level";
					success = Settings.setLevel(this, Settings.INTERMEDIATE);
				} else  {
					text = "Selected hard word level";
					success = Settings.setLevel(this, Settings.HARD);
				}			
			}
			else {
				if (checkedId == R.id.radio_half){
					text = "Selected half speed";
					success = Settings.setSpeechRate(this, Settings.MIN_SPEECH_RATE);
				} else if (checkedId == R.id.radio_normal){
					text = "Selected normal speech rate";
					success = Settings.setSpeechRate(this, Settings.NORMAL_SPEECH_RATE);
				} else if (checkedId == R.id.radio_twice){
					text = "Selected twice normal speech rate";
					success = Settings.setSpeechRate(this, Settings.DOUBLE_SPEECH_RATE);
				} else  {
					text = "Selected three times normal speech rate";
					success = Settings.setSpeechRate(this, Settings.MAX_SPEECH_RATE);
				}			
			}
			if (!text.equals("") && !init)
				tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}

		/* Save the checkbox values when changed */
		@Override
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			String text = "";
			boolean success = false;
	    	switch (buttonView.getId()){
	    	case R.id.replay_symbol:
				if (isChecked) {
					text = "Replay symbol enabled";
					success = Settings.setReplay(this, true);
				} else {
					text = "Replay symbol disabled";
					success = Settings.setReplay(this, false);
				}	
				break;
	    	case R.id.read_menus:
				if (isChecked) {
					text = "Read menus enabled";		
					success = Settings.setTts(this, true);
				} else {
					text = "Read menus disabled";
					success = Settings.setTts(this, false);
				}	
				break;
			case R.id.qwerty_keypad:
				if (isChecked) {
					text = "QWERTY keypad used";		
					success = Settings.setKeypad(this, true);
				} else {
					text = "Alphabetic keypad used";
					success = Settings.setKeypad(this, false);
				}	
				break;
	    	}
	    	if (!text.equals("") && !init)
				tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}

		@Override
	    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {  
	    	if(keyCode == KeyEvent.KEYCODE_MENU){
	    		tts.setOnUtteranceCompletedListener(this);
	    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "exit");
	    		tts.speak("Menu kee pressed", TextToSpeech.QUEUE_FLUSH, utterance);
	    	}
	    	else if (keyCode == KeyEvent.KEYCODE_BACK) {
	    		tts.setOnUtteranceCompletedListener(this);
	    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "exit");
	    		tts.speak("Back kee pressed, returning to main menu", TextToSpeech.QUEUE_FLUSH, utterance);
	    	}
	    	else if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
	    		// TO-DO!
	    		return super.onKeyDown(keyCode, keyEvent);
	    	}
	    	else {
	    		return super.onKeyDown(keyCode, keyEvent);
	    	}
	    	Log.v(TAG, "onKeyDown");
	    	return true;
	    }
		
		/* Called for touches that are not initial button touches */
		@Override
		public boolean onTouchEvent(MotionEvent me) {
			Log.v(TAG, "onTouchEvent");
			if (me.getAction() == MotionEvent.ACTION_DOWN) {
				findKey(me);
			} 
			// Only process movement events that occur more than a
			// predetermined interval (in ms) apart to improve performance
			else if (me.getAction() == MotionEvent.ACTION_MOVE) {
				findKey(me);
			} 
			return false;
		}

		/* Called for touches inside the button display */
		@Override
		public boolean onTouch(View v, MotionEvent me) {
			//super.onTouchEvent(me);
			Log.v(TAG, "onTouch");
			if (me.getAction() == MotionEvent.ACTION_DOWN) {
				findKey(me);
			} 
			// Only process movement events that occur more than a
			// predetermined interval (in ms) apart to improve performance
			else if (me.getAction() == MotionEvent.ACTION_MOVE) {
				findKey(me);
			} 
			
			return false;
		}

		/* Locates the button on which the motion event occurred 
		 * and gives focus to that button.
		 */
		private boolean findKey(MotionEvent me) {
			double y = me.getRawY();
			double x = me.getRawX();
			int[] loc = new int[2];
			int[] dim = new int[2];
			for (int i = 0; i < views.length; i++) {
				views[i].getLocationOnScreen(loc);
				dim[0] = views[i].getWidth();
				dim[1] = views[i].getHeight();
				// If the motion event goes over the button, have the button request focus
				if (y <= (loc[1] + dim[1]) && x <= (loc[0] + dim[0])) {
					if (i != lastLoc) {
						views[i].requestFocus();
						lastLoc = i;
					}
					return true;
				}
			}
			return false;
		}

		@Override
		public void onUtteranceCompleted(String utteranceId) {
			if (utteranceId.equalsIgnoreCase("exit")) {
				finish();
			}
		} 

}

	

