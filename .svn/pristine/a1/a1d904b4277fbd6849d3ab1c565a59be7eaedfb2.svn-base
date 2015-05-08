package edu.washington.cs.kittens;

import android.app.Activity;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.TextView;

/**
 * This class displays the typeit page, where a user enters text which can be read using
 * TTS.
 */

public class TypeIt extends Activity {
	private TextView inputBox;
	private TextToSpeech mTts;
	private CatApplication application;
	
	/** for the background color of the screen*/
    @Override
    public void onAttachedToWindow() {
    	super.onAttachedToWindow();
    	Window window = getWindow();
    	window.setFormat(PixelFormat.RGBA_8888);
    }
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.typeit);

		// Get database and tts objects from the parent application
		this.application = (CatApplication) this.getApplication();
		mTts = this.application.getTextToSpeech();

		// Make volume slider on phone change media volume instead of ringer volume
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		
		inputBox = (TextView) findViewById(R.id.box_input);
		
		View button = findViewById(R.id.button_speak);
		button.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String text = inputBox.getText().toString();
		    	mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
			}
		});	
	}
	
}