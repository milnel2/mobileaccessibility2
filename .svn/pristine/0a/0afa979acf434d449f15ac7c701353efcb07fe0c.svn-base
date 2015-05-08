package cs.washington.mobileaccessibility.locationorienter;

import android.app.Activity;
import android.content.*;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;


public class LocationOrienter extends Activity implements TextToSpeech.OnInitListener{
	
	private static final String PREF = "LocationOrienterPref";
	public static TextToSpeech mTTS;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        mTTS = new TextToSpeech(this, this);    
    }
    
    @Override
    public void onRestart(){
    	super.onRestart();
    	finish();
    }
    
    @Override
    public void onDestroy(){
    	mTTS.stop();
    	mTTS.shutdown();
    	super.onDestroy();
    }
    
    /**
     * For TTS initialization, which then also sets up if
     * there were preferences saved or not.
     */
    public void onInit(int status) {
        SharedPreferences s = this.getSharedPreferences(PREF, 0);
        Intent i;
        if(s == null || !s.contains("vision")){
        	i = new Intent(this, SettingsConfiguration.class);

        }else{
        	i = new Intent(this, LocalEyes.class);
        }
        startActivity(i);	
	}
}