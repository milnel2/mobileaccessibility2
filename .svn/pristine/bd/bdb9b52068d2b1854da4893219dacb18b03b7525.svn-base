package edu.washington.cs.hangman;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class VBWriteView extends HangmanView {
	private static final String TAG = "vbwriteview";

	private static final int TOTAL_DOTS = 6;

	// The Enter and Back buttons are constant size across all MenuViews.
	private static final int ENTER_BTN_HEIGHT = 150;
	private static final int BACK_BTN_HEIGHT = ENTER_BTN_HEIGHT;	

	private static final String ENTER_BTN_LABEL = "enter";
	private static final String BACK_BTN_LABEL = "back";

	public static final int ENTER_BTN_ID = 32;
	public static final int BACK_BTN_ID = 33;

	private Paint _paintUnselected = new Paint();
	private Paint _paintSelected = new Paint();
	private Paint _paintFGUnselected = new Paint();
	private Paint _paintTextSelected = new Paint();

	private boolean[] _dots = new boolean[TOTAL_DOTS];

	private final static int NO_DOT = -1;
	private int _selectedItem = NO_DOT;

	private int _dotHeight = 0;

	private HangmanView.OnItemSelected _onItemSelected = null;

	public VBWriteView(Context context, TextToSpeech tts, HangmanView.OnItemSelected onItemSelected) {
		super(context);

		_onItemSelected = onItemSelected;

		setFocusableInTouchMode(true);

		_paintUnselected.setColor(Hangman.UNSELECTED_BG_COLOR);
		//_paintUnselected.setStrokeWidth(BTN_STROKE_WIDTH);
		_paintSelected.setColor(Hangman.SELECTED_BG_COLOR);

		_paintFGUnselected.setColor(Hangman.UNSELECTED_FG_COLOR);
		_paintFGUnselected.setTextSize(Hangman.TEXT_SIZE);
		_paintFGUnselected.setTextAlign(Align.CENTER);
		_paintTextSelected.setColor(Hangman.SELECTED_FG_COLOR);
		_paintTextSelected.setTextSize(Hangman.TEXT_SIZE);
		_paintTextSelected.setTextAlign(Align.CENTER);
	}


	@Override
	public void draw(Canvas canvas) {
		_dotHeight = (getHeight() - ENTER_BTN_HEIGHT) / 3;
		int dotWidth = getWidth() / 2;

		for(int i = 0; i < _dots.length; ++i) {
			Paint paintBG = _paintUnselected;
			Paint paintFG = _paintFGUnselected;

			if(i == _selectedItem) {
				paintBG = _paintSelected;
				paintFG = _paintTextSelected;
			}

			float top = (i % 3) * _dotHeight;
			float bottom = top + _dotHeight - Hangman.BTN_PADDING;

			float left = (i < 3) ? 0 : dotWidth;
			float right = (i < 3) ? dotWidth : (dotWidth*2);
			right -= Hangman.BTN_PADDING;
			canvas.drawRect(left, top, right, bottom, paintBG);

			if(_dots[i]) {
				// Draw the circle
				float cy = top + _dotHeight/2;
				float cx = left + dotWidth/2;
				float radius = Math.min(dotWidth, _dotHeight) / 4;
				canvas.drawCircle(cx, cy, radius, paintFG);
			}
		}
		drawBottomBtns(canvas);
	}

	public void drawBottomBtns(Canvas canvas) {
		Paint paint = _paintUnselected;
		Paint paintText = _paintFGUnselected;

		// Enter button
		if(ENTER_BTN_ID == _selectedItem) {
			paint = _paintSelected;
			paintText = _paintTextSelected;
		}

		float height = getHeight();
		float width = getWidth();
		float textOffset = (ENTER_BTN_HEIGHT - Hangman.TEXT_SIZE)/2;

		float top = height - ENTER_BTN_HEIGHT;
		float bottom = height - Hangman.BTN_STROKE_WIDTH - Hangman.BTN_PADDING;

		float left = 0;
		float right = width/2 - Hangman.BTN_PADDING;
		canvas.drawRect(left, top, right, bottom, paint);

		float textX = width/4;
		float textY = bottom - textOffset;
		canvas.drawText(ENTER_BTN_LABEL, textX, textY, paintText);		

		// Back button
		paint = _paintUnselected;
		paintText = _paintFGUnselected;
		if(BACK_BTN_ID == _selectedItem) {
			paint = _paintSelected;
			paintText = _paintTextSelected;
		}

		left = right + Hangman.BTN_PADDING * 2;
		right = getWidth();
		canvas.drawRect(left, top, right, bottom, paint);	

		textX = left + width/4;
		textY = bottom - textOffset;
		canvas.drawText(BACK_BTN_LABEL,	textX, textY, paintText);
	}



	@Override
	public void onSingleTap(float x, float y, int action, long time) {
		int touchedDot = getItem(x, y);
		switch(action) {
		case (MotionEvent.ACTION_DOWN): {
			_selectedItem = getItem(x, y);
			speakItem(_selectedItem);
			invalidate();
			break;
		}
		case (MotionEvent.ACTION_MOVE): {
			if(_selectedItem != touchedDot) {
				_selectedItem = getItem(x, y);
				speakItem(_selectedItem);
				invalidate();
			}
			break;
		}
		}

	}

	private void speakItem(int item) {
		int dotNum = item + 1;
		String speakStr = "";
		// The dot number is actually the index + 1, since we start indexing at 0.
		switch(item) {
		case ENTER_BTN_ID:
			speakStr = ENTER_BTN_LABEL;
			break;
		case BACK_BTN_ID:
			speakStr = BACK_BTN_LABEL;
			break;
		default:
			speakStr = "dot " + dotNum;
			if(_dots[_selectedItem]) {
				speakStr += " on";
			} else {
				speakStr += " off";
			}
		}
		Log.d(TAG, speakStr);
		Hangman.getTts().speak(speakStr, TextToSpeech.QUEUE_FLUSH, null);
	}

	@Override
	public void onDoubleTap() {
		// toggle selected dot 
		switch(_selectedItem) {
		case ENTER_BTN_ID:
		case BACK_BTN_ID:{
			_onItemSelected.onSelected(_selectedItem);
			break;
		}
		default: {
			// activate a dot
			_dots[_selectedItem] = !_dots[_selectedItem];
			speakItem(_selectedItem);
			invalidate();
		}
		}

	}

	private int getItem(float x, float y) {
		float width = getWidth();
		float height = getHeight();
		if(y > (height - ENTER_BTN_HEIGHT)) {
			if(x < width/2) {
				return ENTER_BTN_ID;
			} else {
				return BACK_BTN_ID;
			}
		}

		if(x < width/2) {
			return (int) y/_dotHeight; 
		} else {
			return (int) y/_dotHeight + 3;  
		}
	}
	
	/**
	 * Returns true if no dots were entered in the letter.
	 * @return
	 */
	public boolean isBlank() {
		for(int i = 0; i < _dots.length; ++i) {
			if(_dots[i]) {
				return false;
			}
		}
		return true;
	}
	
	public boolean[] getDots() {
		return _dots;
	}


}
