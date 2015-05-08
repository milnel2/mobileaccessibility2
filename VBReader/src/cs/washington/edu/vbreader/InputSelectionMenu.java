package cs.washington.edu.vbreader;

import android.content.Intent;
import android.os.Bundle;

public class InputSelectionMenu extends AccessibleMenu {
	
	protected static final int QWERTY_LETTER = 0;
	protected static final int ALPHABET_LETTER = 1;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	}
	
	@Override
	protected void makeSelection() {
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
	
}
