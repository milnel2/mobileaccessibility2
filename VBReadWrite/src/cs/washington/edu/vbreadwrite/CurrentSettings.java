package cs.washington.edu.vbreadwrite;

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
public class CurrentSettings extends Activity implements TextToSpeech.OnUtteranceCompletedListener {
		
		// for speech
		private TextToSpeech tts;
		HashMap<String, String> utterance = new HashMap<String, String>();
		
		//for speaking status
		private TextView version;
		private TextView speechRate;
		private TextView inputType;
		
		private static final String TAG = "CurrentSettings";

		
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.current_settings);
	        
			tts = GlobalState.getTTS();	
			tts.speak("One moment please", TextToSpeech.QUEUE_ADD, null);
			getUIElements();
			setUIValues();
			speakSettings();
		}
		
		/* Get the references of the UI elements */
		private void getUIElements() {
			version = (TextView)findViewById(R.id.version);
			speechRate = (TextView)findViewById(R.id.speech_rate);
			inputType = (TextView)findViewById(R.id.input_type);
		}
		
		/* Set the UI elements to the current settings values */
		private void setUIValues () {
			version.setText(Settings.getVersionText(Settings.getVersionSetting(this)));
			speechRate.setText(Float.toString(Settings.getSpeechRateSetting(this)));
			inputType.setText(Settings.getInputTypeText(Settings.getInputTypeSetting(this)));
		}
		
		/* Speak all current settings */
		private void speakSettings() {
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "settingsRead");
			tts.setOnUtteranceCompletedListener(this);
			String input = Settings.getInputTypeTag(Settings.getInputTypeSetting(this));
			tts.speak("Version is set to " + version.getText() + " version, input screen is set to " + input + 
					", Speech rate is set to " + speechRate.getText(), TextToSpeech.QUEUE_ADD, utterance);
		}
		
		@Override
		public void onUtteranceCompleted(String utteranceId) {
			if (utteranceId.equalsIgnoreCase("settingsRead")) {
				while (tts.isSpeaking()) {
		  			try {
		  				Thread.sleep(1000);
		  			} catch (InterruptedException e) {
		  				e.printStackTrace();
		  			}
		  		}
				finish();
			}
		} 

}

	

