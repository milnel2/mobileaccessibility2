package edu.washington.cs.kittens;

import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * This class is for displaying a list of options for the application.
 * It is loaded when the editor uses the menu to open it.
 * The content of the page is loaded from res/xml/options
 */

public class Options extends PreferenceActivity {
	private SharedPreferences prefs;
	private SharedPreferences.Editor prefsEditor;
	
	/** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.options);
  
		// Make volume slider on phone change media volume instead of ringer volume
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefsEditor = prefs.edit();
		
	}
	
	/** Called when the activity is resumed. */
	@Override
	protected void onResume() {
		 super.onResume();
	}

	/** Called when the activity is paused. */
	@Override
	protected void onPause() {
		 super.onPause();
		 Log.d("Pic2Speech", prefs.getAll().toString());
	}
	

}

