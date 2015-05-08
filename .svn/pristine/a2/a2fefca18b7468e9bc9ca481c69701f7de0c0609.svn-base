package edu.washington.cs.hangman;

import edu.washington.cs.hangman.Hangman.HangmanOnInitListener;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * An activity to appear when the app is launched, that waits for 
 * initialization of the app's components (e.g. the TTS engine). After
 * initialization, the StartGame activity is started.
 * @author shiri
 *
 */
public class LoadingAct extends Activity implements HangmanOnInitListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Hangman.init(/*context*/ this, /*HangmanOnInitListener*/ this);
	}
	
	public void onInit() {
		// Now we can start the StartGame activity
		Intent intent = new Intent(this, StartGameAct.class);
		startActivity(intent);
	}
	
}
