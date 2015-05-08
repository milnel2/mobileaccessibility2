package cs.washington.mobileaccessibility.onebusaway.uistates;

import com.google.android.maps.GeoPoint;
import com.google.marvin.shell.TouchGestureControlOverlay.Gesture;

// import cs.washington.mobileaccessibility.onebusaway.util.Util;

import android.util.Log;
import android.view.KeyEvent;
import firbi.base.com.BusRoute;
import firbi.base.com.BusStop;

/*
 * This is used for all the times when you have to enter a number.
 * Either when you're entering a stop number, or a route number.
 * 
 * This state has one notable feature: if you press the back button
 * after entering some numbers, instead of closing this state,
 * it just clears the numbers that have been entered so far.
 * 
 * Pressing the back button again actually exits this state.
 * That may have been modeled after the behavior of star or pound
 * in the dial-in menu
 */
public class EnterNumberState implements State {
	
	// There are multiple times when you enter a route number,
	// and in each case, some other node/state created us in order
	// to obtain a route.  So we need to return it back to them eventually.
	public interface RouteAcceptor {
		// return the state that we go to after accepting the route
		public State acceptRoute(BusRoute r);
	}
	
	// a buffer with the numbers that have been entered so far
	private String theNumbers;
	
	// the object that wanted the number in the first place
	// also, whether or not this is null decides whether we're entering
	// a stop number or a route number
	// That's kind of a stupid hack, but apparently it works
	private RouteAcceptor callback;
	// if callback isn't null, we're entering a bus number, and callback wants it.
	// otherwise, we're entering a stop number, and we'll take the entered stop number,
	// find the BusStop, and launch a new StopState to view it.
	
	// If sa is null, this creates a state for entering a stop number.
	// If sa isn't null, this creates a state for entering a bus route number,
	// which will be returned to sa.
	public EnterNumberState(RouteAcceptor sa) {
		callback = sa;
		theNumbers = "";
	}

	// this shouldn't be called, because we're not using gestures for menu items at all.
	public String tentativeGesture(Gesture g, CoState cos) {
		return " " + gestureToChar(g);
	}
	
	private static char gestureToChar(Gesture g) {
		switch(g) {
		case UPLEFT:
			return '1';
		case UP:
			return '2';
		case UPRIGHT:
			return '3';
		case LEFT:
			return '4';
		case CENTER:
			return '5';
		case RIGHT:
			return '6';
		case DOWNLEFT:
			return '7';
		case DOWN:
			return '8';
		case DOWNRIGHT:
			return '9';
		case DOUBLEDOWN:
			return '0';
		default:
			return 'X'; // uh... right.
		}
	}
	
	public boolean wantsTrackballScrolling() {
		return false;
	}
	
	public boolean wantsZeroGesture() {
		return true;
	}
	
	public boolean wantsSidebar() {
		return false;
	}
	
	public boolean accelerateScroll() {
		return false;
	}
	
	public boolean delayNumberAnnounce() {
		return true;
	}

	public void onGestureFinish(Gesture g, CoState cos) {
		char next = gestureToChar(g);
		theNumbers += next;
		// TODONE: consider replacing "" with " "
		// this changes the voice!!!!
		// the main problem with the special voice for
		// numbers is that it's much quieter and harder
		// to hear.  Also, it may not reliable exist,
		// which is annoying for testing reasons.
		// Is it part of eSpeak?  Is it part of some
		// other application?  Who knows!
		
		// Oh, and while we're here, the current version
		// (after the revision work on 8/15-16) now has
		// the number being spoken twice, once when hovering
		// over the gesture, and once when it's selected
		// TODO see if this is the right thing to do
		cos.outputText(" " + next);
		cos.displayText(theNumbers);

	}
	
	public void onTrackballDown(CoState cos) {
		numberFinished(cos);
	}
	
	public void onSidebar(double fraction, CoState cos) {
		// do nothing, this should never be called
	}

	public boolean onKeyDown(int keyCode, CoState cos) {
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			if(theNumbers.length() > 0) {
				// clear the number
				theNumbers = "";
				cos.displayText(theNumbers);
				// TODO: read instructions
				longDescribe(cos);
				return true;
			}
			else
				// falls through and causes us to go back to the previous state
				return false;
		}
		if(keyCode == KeyEvent.KEYCODE_MENU) {
			// TODO: read instructions
			longDescribe(cos);
		}
		if(keyCode >= KeyEvent.KEYCODE_0 && keyCode <= KeyEvent.KEYCODE_9) {
			char next = (char) ('0' + (keyCode - KeyEvent.KEYCODE_0));
			theNumbers += next;
			cos.outputText("" + next);
			cos.displayText(theNumbers);
			return true;
		}
		if(keyCode == KeyEvent.KEYCODE_CALL || keyCode == KeyEvent.KEYCODE_ENTER) {
			numberFinished(cos);
			return true;
		}
		return false;
	}

	public void longDescribe(CoState cos) {
		String type = (callback==null?"stop":"route");
		String endTag = "";
		if(!theNumbers.equals("")) {
			endTag = " - " + theNumbers;
		}
		cos.displayText("Enter " + type + " number" + endTag);
		// TODO fix the instructions
		if(theNumbers.equals(""))
			cos.outputText("Enter the number of the " + type + " or hit back to go back");
		else
			cos.outputText("Enter the number of the " + type + " or hit back to clear numbers so far " + theNumbers);


	}
	
	public void onResume(CoState cos) {
		theNumbers = "";
	}
	
	public void shortDescribe(CoState cos) {
		if(theNumbers.length() > 0) {
			cos.outputText(theNumbers);
		} else if(callback == null) {
			cos.outputText("Enter stop number");
		} else {
			cos.outputText("Enter route number");
		}
	}
	
	// They finished typing the number
	private void numberFinished(CoState cos) {
		if(theNumbers.length() == 0) {
			cos.outputText("Please enter numbers by stroking the screen, or press back " +
					"to go to the previous menu");
			return;
		}
		if(callback == null) {
			// then this is a stop number
			BusStop stop = verifyStop();
			if(stop == null) {
				// invalid selection
				cos.outputText("Unable to find Bus Stop number " + 
						theNumbers /*Util.spaceDigits(theNumbers)*/ + ".  Try again.");
				theNumbers = "";
				if(callback == null)
					cos.displayText("Enter stop number");
				else
					cos.displayText("Enter route number");
				// TODO this section copied straight out of onStart()
				//cos.speakText("Enter the number of the " + (whoWantsIt==null?"stop":"route") + " or hit back to go back");

			}
			else
				cos.setState(new StopState(stop));
		}
		else {
			BusRoute route = verifyRoute();
			if(route == null) {
				// invalid selection
				cos.outputText("Unable to find Bus Route number " + 
						theNumbers /*Util.spaceDigits(theNumbers)*/ + ".  Try again.");
				theNumbers = "";
				if(callback == null)
					cos.displayText("Enter stop number");
				else
					cos.displayText("Enter route number");
				// TODO this section copied straight out of onStart()
				// cos.speakText("Enter the number of the " + (whoWantsIt==null?"stop":"route") + " or hit back to go back");

			}
			else
				cos.setState(callback.acceptRoute(route));
		}
		
	}
	
	// Because of how I implemented things, this state has to verify whatever number
	// the user entered, which involves way more information than it deserves to know.
	// Oh well.
	private BusStop verifyStop() {
		Log.i("MobileBusInfo","Starting to verify stop number " + theNumbers);
		// TODO put these obscure constants in a separate file
		GeoPoint huskies = new GeoPoint(47659878, -122305968);
		BusStop [] bs = BusStop.findByNumber(theNumbers, huskies);
		Log.i("MobileBusInfo","Finished the query for " + theNumbers);
		if(bs == null || bs.length == 0)
			return null;
		else
			return bs[0];
	}
	
	private BusRoute verifyRoute() {
		Log.i("MobileBusInfo","Starting to verify route number " + theNumbers);
		GeoPoint huskies = new GeoPoint(47659878, -122305968);
		BusRoute [] brs = BusRoute.find(theNumbers, huskies);
		Log.i("MobileBusInfo","Finished querying for " + theNumbers);
		if(brs == null || brs.length == 0) {
			Log.e("MobileBusInfo","Yeah, couldn't find " + theNumbers);
			return null;
		}
		else {
			Log.i("MobileBusInfo","We allegedly just found bus route " + theNumbers);
			return brs[0];
		}
	}


}
