package edu.washington.cs.hangman;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Paint.Align;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.Button;

public class HangmanBtn extends Button {
	private static final String TAG = "hangmanbtn";
	
	// Padding around text
	private static int TEXT_PADDING = 30;
	
	private Paint _paintUnselected = new Paint();
	private Paint _paintSelected = new Paint();
	private Paint _paintTextUnselected = new Paint();
	private Paint _paintTextSelected = new Paint();

	Activity _act;
	
	public HangmanBtn(Activity act) {
		super((Context)act);
		_act = act;

		//setFocusable(true);
		setFocusableInTouchMode(true);
		
		_paintUnselected.setColor(Hangman.UNSELECTED_BG_COLOR);
		//_paintUnselected.setStrokeWidth(BTN_STROKE_WIDTH);
		_paintSelected.setColor(Hangman.SELECTED_BG_COLOR);

		_paintTextUnselected.setColor(Hangman.UNSELECTED_FG_COLOR);
		_paintTextUnselected.setTextSize(Hangman.TEXT_SIZE);
		_paintTextUnselected.setTextAlign(Align.CENTER);
		_paintTextSelected.setColor(Hangman.SELECTED_FG_COLOR);
		_paintTextSelected.setTextSize(Hangman.TEXT_SIZE);
		_paintTextSelected.setTextAlign(Align.CENTER);
	}

	@Override
	protected void onFocusChanged (boolean gainFocus, int direction, Rect previouslyFocusedRect) {
		super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
		Log.d(TAG, "id=" + getId() + " focus gained? " + gainFocus);
		setSelected(gainFocus);	
	}
	
	@Override
	public void draw(Canvas canvas) {
		Paint paint = _paintUnselected;
		Paint paintText = _paintTextUnselected;

		if(isSelected()) {
			paint = _paintSelected;
			paintText = _paintTextSelected;
		}

		float height = getHeight();
		float width = getWidth();
		float textOffset = (height - Hangman.TEXT_SIZE)/2;

		float top = Hangman.BTN_PADDING;
		float bottom = height - Hangman.BTN_STROKE_WIDTH - Hangman.BTN_PADDING;

		float left = 0;
		float right = width - Hangman.BTN_PADDING;
		canvas.drawRect(left, top, right, bottom, paint);

		float textX = width/2;
		float textY = bottom - textOffset;
		canvas.drawText((String) getText(), textX, textY, paintText);		

	}

	@Override
	protected void onMeasure (int widthMeasureSpec, int heightMeasureSpec) {
		Display display = _act.getWindowManager().getDefaultDisplay(); 
		int displayWidth = display.getWidth();
		//int displayHeight = display.getHeight();
		
		// Measure the text height with the fg color.
		int height = (int) (-_paintTextUnselected.ascent() + _paintTextUnselected.descent()) + getPaddingTop()
			+ getPaddingBottom();
		height += TEXT_PADDING*2;
		// width, height
		setMeasuredDimension(displayWidth/2, height);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return false;
	}

	
}
