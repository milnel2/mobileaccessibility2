package cs.washington.edu.vbreadwrite;

import android.os.Bundle;
import android.speech.tts.TextToSpeech;

public class SpeechRateSelectionMenu extends AccessibleMenu {

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void makeSelection() {
		boolean success = Settings.setSpeechRate(this, lastLoc);
        if (!success) tts.speak("Setting failed", TextToSpeech.QUEUE_ADD, null);
        else tts.setSpeechRate(Settings.getSpeechRateSetting(this));
        while (tts.isSpeaking()) {
  			try {
  				Thread.sleep(1000);
  			} catch (InterruptedException e) {
  				e.printStackTrace();
  			}
  		}
        finish();  
	}
	
	@Override
	public void onFinishing()
	{
		// do nothing (don't kill TTS)
	}
	
}
