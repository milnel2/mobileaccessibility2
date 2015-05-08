package cs.wa.edu;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

public class SoftKeypadMenu extends AccessibleMenu {
	
	protected static final int QWERTY_LETTER = 0;
	protected static final int QWERTY_WORD = 1;
	protected static final int ALPHABET_LETTER = 2;
	protected static final int ALPHABET_WORD = 3;
	protected static final int NUMERIC = 4;
	protected static final int PUNCTUATION = 5;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	@Override
    public void onFinishing() {
    	// do nothing
	}
	
}