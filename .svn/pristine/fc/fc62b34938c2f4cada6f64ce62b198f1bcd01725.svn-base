package cs.washington.mobileaccessibility.onebusaway.uistates;

import com.google.marvin.shell.TouchGestureControlOverlay.Gesture;

import firbi.base.com.Bus;

/*
 * this is the state you are in when an alarm is going off
 * The only way out is to hit the back key, apparently
 */
public class AlarmState implements State {

	private Bus bus; // the bus that we're timing!
	
	public AlarmState(Bus b) {
		bus = b;
	}

	public boolean wantsTrackballScrolling() {
		return false;
	}
	
	public boolean wantsZeroGesture() {
		return false;
	}
	
	public boolean wantsSidebar() {
		return false;
	}
	
	public boolean accelerateScroll() {
		return false;
	}
	
	public boolean delayNumberAnnounce() {
		return false;
	}

	public void longDescribe(CoState cos) {
		// repeate the message three times and sound the alarm
		String message = getMessage();
		cos.outputText(message + " " + message + " " + message);
		cos.displayText(message);
		cos.vibrateForAlarm();
	}
	
	// the message that gets displayed, and thrice-spoken
	private String getMessage() {
		String arrivalTimePhrase = StopState.getMinutesFromNowAsText(bus);
		return bus.getRoute().getRouteNumber() + " to " + bus.getDestination() +
			" arriving " + arrivalTimePhrase;
	}

	
	// TODO: if an alarm occurs while the user is entering a gesture, and they complete it...
	// what should happen?
	//
	// in my specification, I wanted no gestures to be accepted that were in progress
	// during the interrupt.  This will require changing the main activity's code.
	public void onGestureFinish(Gesture g, CoState cos) {
		// do nothing... the only way out is back
		// hm, this might solve the T O D O above!

	}

	public boolean onKeyDown(int keyCode, CoState cos) {
		return false;
	}

	public void onSidebar(double fraction, CoState cos) {
		// do nothing

	}

	public void onTrackballDown(CoState cos) {
		// do nothing
	}

	public void shortDescribe(CoState cos) {
		cos.outputText(getMessage());

	}

	public String tentativeGesture(Gesture g, CoState cos) {
		return ""; // no gestures do anything!
	}
	
	
	public void onResume(CoState cos) {
		
	}

}
