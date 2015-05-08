/**
 * 
 */
package cs.washington.mobileaccessibility.locationorienter;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/**
 * @author stillman
 *
 */
public class SettingsConfiguration extends Activity implements OnClickListener {

	private TextToSpeech mTTS;
	private Button mBlind;
	private Button mYB;
	private Button mWB;
	private Button mBW;
		
	private static final String PREF = "LocationOrienterPref";
	
	private static final String WELCOME = "Welcome to Localize.  Please begin by orienting the screen "
											+ "horizontally with the top of the phone in your left hand and menu and "
											+ "back buttons in your right hand. Here we will setup your initial "
											+ "configuration for Localize. If you ever want to return to "
											+ "this screen, tap the menu button.";
	
	private static final String INSTRUCTIONS = "If you are blind or want a textless screen"
												+ " tap the bottom half of the screen, otherwise"
												+ " select the color contrast that suits your vision best.";
	/**
	 * Creates the setting configuration screen.
	 */
	public void onCreate(Bundle sInstState) {
		super.onCreate(sInstState);
        setContentView(R.layout.configpage);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		mTTS = LocationOrienter.mTTS;
    
        mBlind = (Button) findViewById(R.id.ConfigButtonBlind);
        mBW = (Button) findViewById(R.id.ConfigButtonBW);
        mWB = (Button) findViewById(R.id.ConfigButtonWB);
        mYB = (Button) findViewById(R.id.ConfigButtonYB);
        
		mBW.setOnClickListener(this);
		mWB.setOnClickListener(this);
		mYB.setOnClickListener(this);
		mBlind.setOnClickListener(this);
		SharedPreferences s = getSharedPreferences(PREF, 0);
		if(s == null || s.getBoolean("initial", true)){
			mTTS.speak(WELCOME, TextToSpeech.QUEUE_FLUSH, null);
			mTTS.speak(INSTRUCTIONS, TextToSpeech.QUEUE_ADD, null);
			Editor e = s.edit();
			e.putBoolean("initial", false);
			e.commit();
		}
		if(!mTTS.isSpeaking()){
			mTTS.speak(INSTRUCTIONS, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	
	/**
	 * Analyzes what button was pressed to determine what fields
	 * need to be set for the main app to appear with the correct
	 * configurations.
	 */
	public void onClick(View v) {
        SharedPreferences s = getSharedPreferences(PREF, 0);
        SharedPreferences.Editor e = s.edit();
        mTTS.stop();
        Intent i;
		if(v.getId() == R.id.ConfigButtonBW ||v.getId() == R.id.ConfigButtonWB || v.getId() == R.id.ConfigButtonYB){
			//Sighted button pressed, get which button it was and set op configurations
	        e.putBoolean("vision", true);
	        if(!s.contains("text_Size")){
		        e.putFloat("text_Size", 95f);
		        e.putBoolean("mute", false);
		    }
			if(v.getId() == R.id.ConfigButtonBW){
				e.putInt("background_Color", Color.BLACK);
				e.putInt("text_Color", Color.WHITE);
			}else if(v.getId() == R.id.ConfigButtonWB){
			e.putInt("background_Color", Color.WHITE);
				e.putInt("text_Color", Color.BLACK);
			}else{
				e.putInt("background_Color", Color.YELLOW);
				e.putInt("text_Color", Color.BLUE);
			}
		}else{
			//Non sighted button pressed
	        e.putBoolean("vision", false);
		}
		e.commit();
        i = new Intent(this, LocalEyes.class);
		startActivity(i);
		finish();
	}
}
