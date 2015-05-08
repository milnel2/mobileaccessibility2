package edu.washington.cs.hangman;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewDebug.HierarchyTraceType;

public class MenuView extends View {
	private final static String TAG = "menuview";

	public static final int ENTER_BTN_ID = 32;
	public static final int BACK_BTN_ID = 33;

	// The Enter and Back buttons are constant size across all MenuViews.
	private static final int ENTER_BTN_HEIGHT = 150;
	private static final int BACK_BTN_HEIGHT = ENTER_BTN_HEIGHT;
	private static final String ENTER_BTN_LABEL = "enter";
	private static final String BACK_BTN_LABEL = "back";	

	private static final int NO_ITEM = -1;

	public interface OnItemSelected {
		void onSelected(int itemId);
	}

	public static class Item {
		private String _label;
		private int _id;
		public Item(String label, int id) {
			_label = label;
			_id = id;
		}
		public String getLabel() { return _label; }
		public int getId() { return _id; }
		
	}
	
	private OnItemSelected _onItemSelected;

	private TextToSpeech _tts;
	private ArrayList<Item> _items = new ArrayList<Item>();
	private int _selectedItem = NO_ITEM;
	private boolean _addBottomBtns = false;

	private Paint _paintUnselected = new Paint();
	private Paint _paintSelected = new Paint();
	private Paint _paintTextUnselected = new Paint();
	private Paint _paintTextSelected = new Paint();

	private static final int DOUBLE_TAP_INTERVAL = 200; 
	private Handler _handler = new Handler();
	private DoubleTapTimeout _doubleTapTimeout = new DoubleTapTimeout();

	private boolean _doubleTapWindow = false;

	private class DoubleTapTimeout implements Runnable {
		float _x = 0;
		float _y = 0;
		int _action = 0;
		long _time = 0;

		public DoubleTapTimeout() {}

		public void start(float x, float y, int action, long time) {
			_doubleTapWindow = true;
			_x = x;
			_y = y;
			_action = action;
			_time = time;

			_handler.removeCallbacks(this);
			_handler.postDelayed(this, DOUBLE_TAP_INTERVAL);
		}

		public void stop() {
			_handler.removeCallbacks(this);
			_doubleTapWindow = false;
		}

		public boolean isDoubleTap() {
			return _doubleTapWindow;
		}

		public boolean waitingForSecondTap() {
			return _doubleTapWindow;
		}

		public void run() {
			_doubleTapWindow = false;
			// we've timed out, so we have a single tap.
			onSingleTap(_x, _y, _action, _time);
		}};

		public MenuView(Context context, TextToSpeech tts, OnItemSelected onItemSelected) {
			super(context);
			_tts = tts;
			_onItemSelected = onItemSelected;

			_paintUnselected.setColor(Hangman.UNSELECTED_BG_COLOR);
			_paintUnselected.setStrokeWidth(Hangman.BTN_STROKE_WIDTH);
			_paintSelected.setColor(Hangman.SELECTED_BG_COLOR);

			_paintTextUnselected.setColor(Hangman.UNSELECTED_FG_COLOR);
			_paintTextUnselected.setTextSize(Hangman.TEXT_SIZE);
			_paintTextUnselected.setTextAlign(Align.CENTER);
			_paintTextSelected.setColor(Hangman.SELECTED_FG_COLOR);
			_paintTextSelected.setTextSize(Hangman.TEXT_SIZE);
			_paintTextSelected.setTextAlign(Align.CENTER);
		}

		public void addItem(String label, int id) {
			_items.add(new Item(label, id));
		}

		public void addEnterBackBtns() {
			_addBottomBtns = true;
		}

		public int getItemHeight() {
			// figure out the width of an item
			int itemsHeight = _addBottomBtns ? getHeight() - ENTER_BTN_HEIGHT :
				getHeight();
			return itemsHeight / _items.size();
		}

		@Override
		protected void onDraw(Canvas canvas) {
			int itemHeight = getItemHeight();
			int itemWidth = getWidth();
			int numItems = _items.size();

			float textOffset = (itemHeight - Hangman.TEXT_SIZE)/2;

			for(int i = 0; i < numItems; ++i) {
				Paint paint = _paintUnselected;
				Paint paintText = _paintTextUnselected;
				if(i == _selectedItem) {
					paint = _paintSelected;
					paintText = _paintTextSelected;
				}
				float top = i * itemHeight;
				float bottom = top + (itemHeight - Hangman.BTN_STROKE_WIDTH - Hangman.BTN_PADDING);
				canvas.drawRect(0, top, itemWidth, bottom, paint);

				float textX = itemWidth/2;
				float textY = bottom - textOffset;
				canvas.drawText(_items.get(i).getLabel(), 
						textX, textY, paintText);
			}

			if(_addBottomBtns) {
				drawBottomBtns(canvas);
			}
		}

		public void drawBottomBtns(Canvas canvas) {
			Paint paint = _paintUnselected;
			Paint paintText = _paintTextUnselected;

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
			paintText = _paintTextUnselected;
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
		public boolean onTouchEvent(MotionEvent event) {
			float x = event.getX();
			float y = event.getY();
			long time = event.getEventTime();
			int action = event.getAction();

			switch (action) {
			case MotionEvent.ACTION_DOWN:

				if(_doubleTapTimeout.isDoubleTap()) {
					_doubleTapTimeout.stop();
					onDoubleTap();
					return true;
				}

				_doubleTapTimeout.start(x, y, action, time);
				break;
			case MotionEvent.ACTION_MOVE:

				if(!_doubleTapTimeout.waitingForSecondTap()) {
					onSingleTap(x, y, action, time);
				}

				break;
			case MotionEvent.ACTION_UP:
				break;		
			}

			return true;
		}

		private int getItem(float x, float y) {
			int item = (int) (y / getItemHeight());
			if(item >= _items.size()) {
				if(x < getWidth()/2) {
					return ENTER_BTN_ID;
				} else {
					return BACK_BTN_ID;
				}
			}

			return item;
		}

		private void onSingleTap(float x, float y, int action, long time) {
			Log.d(TAG, time + " onSingleTap");
			int curItem = getItem(x, y);

			switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN: 	
				// Speak item when finger touches down on it.
				speakItem(curItem);
				if(curItem != _selectedItem) {
					_selectedItem = curItem;
					invalidate();
				}
				break;
			case MotionEvent.ACTION_MOVE:		
				if(curItem != _selectedItem) {
					_selectedItem = curItem;
					invalidate();
					// Only speak item if it's not already selected
					speakItem(_selectedItem);
				}
				break;
			}
		}

		private void speakItem(int itemNum) {
			String speakStr = "";
			if(itemNum == ENTER_BTN_ID) {
				speakStr = ENTER_BTN_LABEL;
			} else if(itemNum == BACK_BTN_ID) {
				speakStr = BACK_BTN_LABEL;
			} else {
				speakStr = _items.get(itemNum).getLabel();
			}
			_tts.speak(speakStr, TextToSpeech.QUEUE_FLUSH, null);
		}

		private void onDoubleTap() {
			Log.d(TAG, "onDoubleTap");
			_tts.speak(_items.get(_selectedItem).getLabel() + " selected", TextToSpeech.QUEUE_FLUSH, null);
			int itemId = _items.get(_selectedItem).getId();
			_onItemSelected.onSelected(itemId);
		}

}
