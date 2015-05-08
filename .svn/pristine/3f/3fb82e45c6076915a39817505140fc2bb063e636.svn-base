package com.google.marvin.shell;


import android.content.Context;
import android.view.MotionEvent;
import android.view.View;

/*
 * This file was based off of the file by the same name in the eyes-free shell's
 * source code.  I made a few modifications.  The original file is at the
 * URL given just below.
 */

/**
 * A transparent overlay which catches all touch events and uses a call back to
 * return the gesture that the user performed.
 * 
 * @author clchen@google.com (Charles L. Chen)
 * 
 * Except that I (willij6) modified this a bit from the original file which is at
 *     http://eyes-free.googlecode.com/svn/trunk/shell/src/com/google/marvin/shell/TouchGestureControlOverlay.java
 * in order to better suit my purposes... I added some more Gestures, so that
 * this could do some of what the TalkingDialer can do (which uses 3*4 gestures
 * essentially).
 * 
 * I also added the ability to detect an additional sort of gesture involving an imaginary
 * scrollbar on the right side of the screen
 */
public class TouchGestureControlOverlay extends View {




	/**
	 * An enumeration of the possible gestures.
	 */
	public enum Gesture {
		UPLEFT, UP, UPRIGHT, LEFT, CENTER, RIGHT, DOWNLEFT, DOWN, DOWNRIGHT,
		DOUBLEDOWN
	} 
	// the DOUBLEDOWN gesture is the added one... it corresponds to the way you
	// dial a zero in TalkingDialer
	//
	// I didn't add the equivalent gestures for star and pound
	
    public static int getGestureLevel(Gesture g) {
    	switch(g) {
    	case UP:
    	case UPRIGHT:
    	case UPLEFT:
    		return -1;
    	case LEFT:
    	case RIGHT:
    	case CENTER:
    		return 0;
    	case DOWN:
    	case DOWNRIGHT:
    	case DOWNLEFT:
    		return +1;
    	case DOUBLEDOWN:
    		return +2;
    	}
    	return 0; // huh, you would think this would be unreachable
    }

	/**
	 * The callback interface to be used when a gesture is detected.
	 */
	public interface GestureListener {
		public void onGestureStart(Gesture g);

		public void onGestureChange(Gesture g);

		public void onGestureFinish(Gesture g);
		
		// do they want the doubledown gesture?
		// (for MobileBusInfo, this is only true for the
		//   number-entry page)
		public boolean wantsExtras();
		
		// do they want to use the optional scrollbar
		public boolean wantsSidebar();
		
		// if so, then this gets called when the user
		// touches the scrollbar at fraction of the way
		// from top to bottom
		public void onSidebar(double fraction);
	}
	
	// how far to the left (starting from the right edge of
	// the screen) the scrollbar extends.
	// This setting means the scrollbar takes up a tenth of
	// the screen, when it is turned on.
	private final double sidebarFraction = 0.1;

	private final double left = 0;
	private final double upleft = Math.PI * .25;
	private final double up = Math.PI * .5;
	private final double upright = Math.PI * .75;
	private final double downright = -Math.PI * .75;
	private final double down = -Math.PI * .5;
	private final double downleft = -Math.PI * .25;
	private final double right = Math.PI;
	private final double rightWrap = -Math.PI;

	// the callback
	private GestureListener cb = null;
	
	// During a gesture, these contain the location where
	// the user first touched the screen, which serves as the
	// Origin for interpreting the rest of the gesture
	private double downX;
	private double downY;
	
	// which Gesture we are currently in, based off
	// where the last MotionEvent happened
	private Gesture currentGesture;
	
	// true if the user has started to use the scrollbar
	// If so, we don't want to suddenly switch into gesture mode
	// if they drift too far to the left
	//
	// So, this is reset to false when they stop touching the screen
	private boolean usingSidebar = false;
	
	// True between calls to onGestureStart and onGestureFinish
	private boolean gestureInProgress = false;

	// Create a TouchGestureControlOverlay in the given context,
	// with the given callback
	public TouchGestureControlOverlay(Context context, GestureListener callback) {
		super(context);
		cb = callback;
	}

	// Create a TouchGestureControlOverlay without a given callback
	public TouchGestureControlOverlay(Context context) {
		super(context);
	}

	// Set the callback
	public void setGestureListener(GestureListener callback) {
		cb = callback;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		float x = event.getX();
		float y = event.getY();
		Gesture prevGesture = null;
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			if(cb.wantsSidebar() && x > getWidth()*(1.0 - sidebarFraction)) {
				usingSidebar = true;
				cb.onSidebar(((double) y)/((double) getHeight()));
			}
			else {
				usingSidebar = false; // just in case
				downX = x;
				downY = y;
				currentGesture = Gesture.CENTER;
				if (cb != null) {
					gestureInProgress = true;
					cb.onGestureStart(currentGesture);
				}
			}
			break;
		case MotionEvent.ACTION_UP:
			if(usingSidebar) {
				usingSidebar = false;
				break; // nothing to do
			}
			prevGesture = currentGesture;
			currentGesture = evalMotion(x, y);
			// Do some correction if the user lifts up on deadspace
			if (currentGesture == null) {
				currentGesture = prevGesture;
			}
			if (cb != null) {
				gestureInProgress = false;
				cb.onGestureFinish(currentGesture);
			}
			break;
		default:
			if(usingSidebar) {
				cb.onSidebar(((double) y)/((double) getHeight()));
				break; // nothing to do
			}
			prevGesture = currentGesture;
			currentGesture = evalMotion(x, y);
			// Do some correction if the user lifts up on deadspace
			if (currentGesture == null) {
				currentGesture = prevGesture;
				break;
			}
			if (prevGesture != currentGesture) {
				if (cb != null) {
					cb.onGestureChange(currentGesture);
				}
			}
			break;
		}
		return true;
	}



	// given the location of a touch, (x,y), which
	// gesture is it?  (x,y) aren't adjusted relative
	// to (downX,downY)
	private Gesture evalMotion(double x, double y) {
		float rTolerance = 40; //25;
		double thetaTolerance = (Math.PI / 8); //12);

		double r = Math.sqrt(((downX - x) * (downX - x)) + ((downY - y) * (downY - y)));

		if (r < rTolerance) {
			return Gesture.CENTER;
		}

		double theta = Math.atan2(downY - y, downX - x);
		if (Math.abs(theta - left) < thetaTolerance) {
			return Gesture.LEFT;
		} else if (Math.abs(theta - upleft) < thetaTolerance) {
			return Gesture.UPLEFT;
		} else if (Math.abs(theta - up) < thetaTolerance) {
			return Gesture.UP;
		} else if (Math.abs(theta - upright) < thetaTolerance) {
			return Gesture.UPRIGHT;
		} else if (Math.abs(theta - downright) < thetaTolerance) {
			return Gesture.DOWNRIGHT;
		} else if (Math.abs(theta - down) < thetaTolerance) {
			if(cb.wantsExtras() && r > 3*rTolerance)
				return Gesture.DOUBLEDOWN;
			return Gesture.DOWN;
		} else if (Math.abs(theta - downleft) < thetaTolerance) {
			return Gesture.DOWNLEFT;
		} else if ((theta > right - thetaTolerance) || (theta < rightWrap + thetaTolerance)) {
			return Gesture.RIGHT;
		}
		// Off by more than the threshold, so it doesn't count
		return null;
	}
	
	/*
	 * Return true if a gesture (not the sidebar) is currently in progress
	 */
	public boolean gestureInProgress() {
		return gestureInProgress;
	}


}
