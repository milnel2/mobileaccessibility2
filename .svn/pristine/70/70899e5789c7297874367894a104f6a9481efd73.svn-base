package edu.uw_cse.ma.accessiblemenu;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class UnknownActivity extends Activity {
 
	// For logging
	private String TAG;  // set to class name; used to get all data resources for menu and for logging
	
	private TextToSpeech tts;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TextView tv = (TextView)findViewById(R.id.screen);
        tv.setText(this.getIntent().getAction());
        
        // get data for the menu using the class name -- allows separation of menu from data and reuse of menu code
        TAG = this.getClass().getName();
        TAG = TAG.substring(TAG.lastIndexOf('.') + 1);

        tts = GlobalState.getTTS();
        Log.v(TAG, "tts = " + tts);
        
        tts.speak("Displayed " + tv.getText(), TextToSpeech.QUEUE_ADD, null);
	}
	
}
