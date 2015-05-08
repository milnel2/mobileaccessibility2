package cs.washington.mobileaccessibility.onebusaway.uistates;

import com.google.android.maps.GeoPoint;

import cs.washington.mobileaccessibility.onebusaway.BookmarkManager;

import firbi.base.com.Bus;
import firbi.base.com.BusStop;

/*
 * This is dual to State, sort of.  It's how the thing that implements the
 * State interface responds back to MainActivity.
 * MainActivity implements this interface but nothing else does.
 */
public interface CoState {
	// display text on the screen
	public void displayText(String text);
	// speak text, or express it in morse code or braille, depending on the mode
	public void outputText(String text);
	// trigger the vibration pattern for a scroll
	public void vibrateForScroll();
	// trigger the vibration pattern for an alarm
	public void vibrateForAlarm();
	
	// These next three are usually the last thing that a State does before
	// returning control to MainActivity
	
	// move to a different state
	public void setState(State s);
	// close me, and if I'm the only state, then quit
	public void backState();
	// go back to the very beginning, clearing the stack of pages before this one
	public void startOverState();
	
	// am I the page/state currently being displayed
	public boolean amIOnTop(State s);
	
	// put a runnable object into the queue for the UI thread,
	// which is necessary because some things MUST be done in the UI
	// thread.
	//
	// this is mainly used for two reasons:
	// * alarms that go off
	// * states that need to stall for a while to do some busy work, like
	//   loading information from the web
	public void postRunnable(Runnable r);
	

	// What is the most recent stop that the user inquired about?
	public BusStop mostRecentStop();
	// Set the most recent stop that the previous method accesses
	public void setRecentStop(BusStop bs);
	
	// Get the manager of the bookmarked stops
	public BookmarkManager getBookmarks();
	
	// Get the user's geographical location,
	// or null if we haven't been able to get a good reading
	public GeoPoint getLocation();
	
	// start playing the hold music
	public void startPlaying();
	// stop playing the hold music
	public void stopPlaying();
	

	// We only want one alarm active at a time, which makes the following
	// things necessary
	
	// specify which bus the current alarm is keeping track of
	public void setCurrentAlarmBus(Bus b);
	// see which bus is currently being monitored (mainly used to see
	// whether a given bus is the one, for the sake of toggling)
	public Bus getCurrentAlarmBus();
	// specify which runnable is counting down the time to the alarm
	public void setCurrentAlarmRunnable(Runnable r);
	// get the currently running alarm thread.  Each thread calls this
	// and sees whether itself is the current runnable, and if not, then
	// it kills itself, because it has been superseded by another alarm
	public Runnable getCurrentAlarmRunnable();
	
}
