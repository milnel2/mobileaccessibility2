package edu.washington.cs.hangman;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class HangmanView extends View {
	private static final String TAG = "hangmanview";
	
	private static final int DOUBLE_TAP_INTERVAL = 200; 
	private Handler _handler = new Handler();
	private DoubleTapTimeout _doubleTapTimeout = new DoubleTapTimeout();

	private boolean _doubleTapWindow = false;

	public interface OnItemSelected {
		void onSelected(int itemId);
	}
	
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

	
	public HangmanView(Context context) {
		super(context);
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
	
	protected void onDoubleTap() {
		Log.d(TAG, "doubletap");
	}
	
	protected void onSingleTap(float x, float y, int action, long time) {
		Log.d(TAG, "onsingletap");
	}
	
}
