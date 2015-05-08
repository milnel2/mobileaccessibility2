package edu.washington.cs.kittens;

import java.util.Locale;

import android.app.Application;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Display;

/*
 * This class stays open for the life cycle of Pic2Speech and holds certain objects,
 * such as out database manager and text to speech object
 */

public class CatApplication extends Application implements TextToSpeech.OnInitListener {

	public static final String TAG = "Pic2Speech";  
	private TextToSpeech tts;
	private DbManager dbm;
	private String curCat;
	private String changedCat = "";
	private static int imagect = 0;

	
	@Override
	public void onCreate() {
		super.onCreate();
		Context c = getApplicationContext();
		
		curCat = "root";
		
		// Create a TextToSpeech object
		tts = new TextToSpeech(c, this);
		
		// Create database
		dbm = new DbManager(c).open();
	}
	
	@Override
	public void onInit(int status) {
		// status can be either TextSpeech.SUCCESS or TextSpeech.ERROR.
		if (status == TextToSpeech.SUCCESS) {
			// Set preferred language to US english.
			// Note that a language may not be available, and the result will indicate this.
			int result = tts.setLanguage(Locale.US);
			if (result == TextToSpeech.LANG_MISSING_DATA ||
				result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Language data is missing or the language is not supported.
				Log.e(TAG, "Language is not available.");
				tts.setLanguage(Locale.US);
			} else {
				// Yay, it worked!
				Log.d(TAG, "TTS has been initialized. Yay!!!");
			}
		} else {
			// Initialization failed.
			Log.e(TAG, "Could not initialize TextSpeech.");
		}
	}
	
	
	public void destroyApplication() {
		// Shutdown TTS
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
	}
	
	public DbManager getDatabaseManager() {
		return dbm;
	}
	
	public TextToSpeech getTextToSpeech() {
		return tts;
	}

	// Controls the folder/category that the navigation is currently in
	public String getCat(){
	    return curCat;
	}
	public void setCat(String cat){
	    curCat = cat;
	}

	public Boolean changed(){
		Log.d(TAG, "Changed?? curCat =" + curCat + ", changedCat =" + changedCat + ", boolean = " + (curCat == changedCat));
	    return curCat.equals(changedCat);
	}
	
	public void setChanged(String s){
	    changedCat = s;
	}

	public int getImagect(){
		return imagect;
	}
	public void incrementImagect(){
		imagect++;
	}
	


}