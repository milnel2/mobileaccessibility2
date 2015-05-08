package cs.washington.mobileaccessibility;
/*
 * This class is an abstract class implemented by each intent.  This class contains 
 * general definitions of the gestures to be implemented for the UI for Link Up.
 * All gestures can be overwritten in their respective classes to do what it wants. 
 */

import android.app.Activity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;


/** Definition of gestures dependent on what the calling intent is **/

public abstract class GestureUI extends Activity implements OnGestureListener{
	
	/** Constants for 'Fling Detection' **/
	protected static final int SWIPE_MIN_DISTANCE = 50; // lowe the number, the smaller the distance has to be
	protected static final int SWIPE_MAX_OFF_PATH = 300; // greater the distance, the larger the stroke can be
	protected static final int SWIPE_THRESHOLD_VELOCITY = 150; // Lower the number, the more sensitive it is
	protected GestureDetector gestureScanner;
	
	/**
	 * The public finals below cannot be overwritten. Developers must overwrite the corresponding 
	 * 'protected' methods.  This is because I do not test the booleans that are associated with the 
	 * public finals.
	 */
	
	/** Structure for detecting 'Flings' and calling appropriate methods. **/
	@Override
	public final boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		try {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;
			if(e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// Left Fling
				onLeftFling();
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// Right Fling
				onRightFling();				
			} else if(e1.getY() - e2.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				// Up Fling
				onUpFling();
			} else if (e2.getY() - e1.getY() > SWIPE_MIN_DISTANCE && Math.abs(velocityY) > SWIPE_THRESHOLD_VELOCITY) {
				// Swiping down
				onDownFling();
			}
		} catch (Exception e) {
			Log.e("Exception caught." , e.toString());
		}
		return true;
	}//end of onfling

	@Override
	public final boolean onTouchEvent(MotionEvent me) {
		// Toast.makeText(getApplicationContext(), "", Toast.LENGTH_SHORT).show();
		return gestureScanner.onTouchEvent(me);
	}

	@Override
	public final boolean onDown(MotionEvent e) {
		return true;
	}

	@Override
	public final void onLongPress(MotionEvent e) {
		onLongPressDown();
	}

	@Override
	public final boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		return true;
	}

	@Override
	public final void onShowPress(MotionEvent e) {

	}
	
	@Override
	public final boolean onSingleTapUp(MotionEvent e) {
		onTap();
		return true;
	}
	
	
	/** Define these methods as needed in every class that extends the GesturesUI **/
	protected void onUpFling() {
		Log.d("Fling Direction ", "Up");
	}
	
	protected void onDownFling() {
		Log.d("Fling Direction ", "Down");
	}
	
	protected void onRightFling() {
		Log.d("Fling Direction ", "Right");
	}
	
	protected void onLeftFling() {
		Log.d("Fling Direction ", "Left");
	}
	
	protected void onTap() {
		Log.d("Tap ", "Single Tap");
	}
	
	protected void onLongPressDown() {
		Log.d("Long PRess "," Long press");
		
	}
}
