package edu.washington.cs.hangman;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class EnterLetterAct extends AccessibleActivity implements HangmanView.OnItemSelected {
	private static final String TAG = "enterletteract";

	private static final String ACTIVITY_TITLE = "Enter a letter screen";

	public static String KEY_LETTER_ENTERED = "letter_entered";

	private VBWriteView _vbWriteView = null;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, ACTIVITY_TITLE, Hangman.getTts());

		// Use the full screen (no title bar)
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		_vbWriteView = new VBWriteView(this, Hangman.getTts(), this);
		_vbWriteView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		setContentView(_vbWriteView);

	}


	public void onSelected(int itemId) {
		switch(itemId) {
		case VBWriteView.ENTER_BTN_ID:
			// get dots, get letter, set result.
			Intent intent = new Intent();
			if(!_vbWriteView.isBlank()) {
				boolean[] dots = _vbWriteView.getDots();
				char letter = Hangman.getBrailleTable().decode(dots);
				Bundle bundle = new Bundle();
				bundle.putChar(KEY_LETTER_ENTERED, letter);	
				intent.putExtras(bundle);
			}
			setResult(RESULT_OK, intent);

			finish();
		case VBWriteView.BACK_BTN_ID:
			setResult(RESULT_CANCELED);
			finish();
		}

	}
}
