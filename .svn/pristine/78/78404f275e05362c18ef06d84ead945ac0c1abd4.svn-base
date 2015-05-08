package cs.washington.edu.vbreadwrite;

import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

public class InputSelectionMenu extends AccessibleMenu {
	
	protected static final int QWERTY_LETTER = 0;
	protected static final int ALPHABET_LETTER = 1;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void makeSelection() {
		boolean success = Settings.setInputType(this, lastLoc);
        if (!success) tts.speak("Permanent setting failed", TextToSpeech.QUEUE_ADD, null);
		Intent intent = new Intent();
        intent.putExtra("type", lastLoc);
        setResult(RESULT_OK, intent);
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
