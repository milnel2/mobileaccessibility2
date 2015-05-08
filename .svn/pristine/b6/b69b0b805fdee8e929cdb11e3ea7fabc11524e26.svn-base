package edu.washington.cs.hangman;

import android.app.Activity;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

public class AccessibleActivity extends Activity {
	private String _title;
	private TextToSpeech _tts;

	/**
	 * Creates the accessible activity, setting the title and tts engine for later use.
	 * @param savedInstanceState
	 * @param title	The activity title that will be spoken to the user when the activity is started.
	 * @param tts	A reference to the TTS engine the activity will use.
	 */
	public void onCreate(Bundle savedInstanceState, String title, TextToSpeech tts) {
		super.onCreate(savedInstanceState);
		_title = title;
		_tts = tts;
	}

	public void setTTS(TextToSpeech tts) {
		_tts = tts;
	}
	
	@Override
	public void onStart() {
		super.onStart();
		// Speak title
		if(_tts != null) {
			_tts.speak(_title, TextToSpeech.QUEUE_ADD, null);
		}
	}
}
