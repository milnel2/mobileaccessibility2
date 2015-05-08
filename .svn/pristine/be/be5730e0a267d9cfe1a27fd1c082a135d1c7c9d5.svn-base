package edu.washington.cs.hangman;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

public class HangmanLayout extends RelativeLayout {
	private static final String TAG = "hangmanlayout";
	
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

	
	public HangmanLayout(Context context) {
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
	
	private void onDoubleTap() {
		// call onactivate on selected view
		View selectedChild = getSelectedChild();
		Log.d(TAG, "activate id=" + selectedChild.getId());
	}
	
	private View getSelectedChild() {
		int childCount = getChildCount();
		for(int i = 0; i < childCount; ++i) {
			if(getChildAt(i).isSelected()) {
				return getChildAt(i);
			}
		}
		return null;
	}
	
	private void onSingleTap(float x, float y, int action, long time) {
		View selctedView = getChildAtPoint(x, y);
		int viewId = -1;
		if(selctedView != null) {
			viewId = selctedView.getId();
			if(!selctedView.hasFocus()) {
				Log.d(TAG, "requesting focus id=" + selctedView.getId());
				selctedView.requestFocus();
				//selctedView.setSelected(true);
			//	selctedView.
			}
//			selectedView.onSingleTap();
		}
		switch(action) {
		case MotionEvent.ACTION_DOWN:
			Log.d(TAG, "action down, view=" + viewId);
			break;
		case MotionEvent.ACTION_MOVE:
			Log.d(TAG, "action move, view=" + viewId);		
		}

	//	return true;
	}
	
	private View getChildAtPoint(float x, float y) {
		int numChildren = getChildCount();
		for(int i = 0; i < numChildren; ++i) {
			View child = getChildAt(i);
			int bottom = child.getBottom();
			int top = child.getTop();
			int left = child.getLeft();
			int right = child.getRight();
			if(x > left && x < right && 
					y > top && y < bottom) {
				return child;
			}
		}
		
		return null;
	}

	
}
