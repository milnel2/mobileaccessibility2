package cs.washington.mobileaccessibility.onebusaway.uistates;

import java.util.Arrays;

import android.util.Log;
import android.view.KeyEvent;

import com.google.android.maps.GeoPoint;
import com.google.marvin.shell.TouchGestureControlOverlay.Gesture;

import cs.washington.mobileaccessibility.onebusaway.BookmarkManager;
import cs.washington.mobileaccessibility.onebusaway.util.Util;
// import cs.washington.mobileaccessibility.onebusaway.util.Util;

import firbi.base.com.BusStop;
import firbi.base.util.BusStopComparator;

/*
 * The part of the menu where you see a list of nearby stops.
 */
public class GeoState implements State {

	private BusStop[] nearbyStops;
	// index of the stop being displayed
	private int currentIndex;
	// flag if the GPS didn't turn up
	private boolean noGPS;
	// flag if there just were no stops around
	private boolean none;
	
	// actually serves as a flag for whether longDescribe has been called yet
	private CoState cosForFinderThread = null; 
	
	public GeoState(CoState cos) {
		final GeoPoint loc = cos.getLocation();
		none = true;
		noGPS = false;
		if(loc == null) {
			noGPS = true;
			none = true;
			nearbyStops = new BusStop[0];
		}
		else {
			Log.i("WHOAH","It thinks we're at " + loc.getLatitudeE6() + " and " + loc.getLongitudeE6());
			cosForFinderThread = null;
			nearbyStops = null;
			Runnable r = new Runnable() {
				public void run() {
					// TODO: conceal the radius (in a separate file)!
					// 230 is taken from sDEFAULT_RADIUS which is used in
					// this exact manner as an argument of BusStop.find() in
					// firbi.base.Search.java
					nearbyStops = BusStop.find(loc, 500);
					if(nearbyStops == null) {
						Log.e("WHOAH","Error of some sort!");
						Log.e("WHOAH","Perhaps because the location was lat=" + loc.getLatitudeE6() + " and lon=" + loc.getLongitudeE6());
						none = true;
						nearbyStops = new BusStop[0];
					}
					else {
						Arrays.sort(nearbyStops, new BusStopComparator(loc));
						if(nearbyStops.length == 0) {
						none = true;
						}
						else
							none = false;
						if(cosForFinderThread != null) {
							Runnable r2 = new Runnable() {
								public void run() {
									if(cosForFinderThread.amIOnTop(GeoState.this)) {
										longDescribe(cosForFinderThread);
									}
								}
							};
							cosForFinderThread.postRunnable(r2);
						}
					}
				}
			};
			new Thread(r).start();
		}
	}
	
	// if down is +1, scroll down.  if down is -1, scroll up
	private void scroll(int down, CoState cos) {
		int nextIndex = currentIndex + down;
		// none == none || noGPS, because noGPS implies none
		if(none || nextIndex < 0 || nextIndex >= nearbyStops.length)
			return;
		currentIndex = nextIndex;
		cos.vibrateForScroll();
		updateText(cos,true);
	}
	
	public void onSidebar(double fraction, CoState cos) {
		if(none)
			return; // though actually we shouldn't be called in this case
		int nextIndex = (int)(fraction*nearbyStops.length);
		if(nextIndex == nearbyStops.length)
			nextIndex = nearbyStops.length - 1; // just in case we get 1.0 exactly
		if(nextIndex >= 0 && nextIndex < nearbyStops.length) {
			if(nextIndex != currentIndex) {
				currentIndex = nextIndex;
				cos.vibrateForScroll();
				updateText(cos,true);
			}
		}
	}
	
	// TODO rename 'speak' parameter to 'verbose', here and
	// everywhere else
	private void updateText(CoState cos, boolean speak) {
		if(noGPS) {
			cos.displayText("GPS unavailable");
			if(speak)
				cos.outputText("GPS unavailable");
		} else if(none) {
			cos.displayText("No nearby stops found!");
			if(speak)
				cos.outputText("No nearby stops found!");
			// TODO: much better would be to increase the radius until we'd found
			// some number of stops!!!
			// so this todo really belongs up above where we ask for the stops
			// in the first place
		}
		else {
			BusStop bs = nearbyStops[currentIndex];
			String text = bs.getAddress();
			text += " " + Util.directionToString(bs.getDirection()) + " bound";
			text += " (" + Util.getDistance(bs.getLocation(), cos.getLocation(), false) + " feet away)";
			cos.displayText(text);
			if(speak)
				cos.outputText(text);
		}
	}
	
	

	
	public String tentativeGesture(Gesture g, CoState cos) {
		if(g == Gesture.LEFT)
			return "Previous Menu";
		if(nearbyStops == null)
			return "";
		if(none)
			return "";
		if(g == Gesture.UPRIGHT) {
			if(cos.getBookmarks().isBookmarked(nearbyStops[currentIndex]))
				return "Remove bookmark for this stop";
			else
				return "Add bookmark for this stop";
		}
		if(g == Gesture.RIGHT)
			return "Select this stop";
		if(g == Gesture.UP)
			return "Scroll up";
		if(g == Gesture.DOWN || g == Gesture.DOUBLEDOWN)
			return "Scroll down"; // hm, what TODO about Doubledown?
		return "";
	}


	
	public boolean wantsTrackballScrolling() {
		return true;
	}
	
	public boolean wantsZeroGesture() {
		return true;
	}
	
	public boolean wantsSidebar() {
		return nearbyStops != null && !none;
	}
	
	public boolean accelerateScroll() {
		return true;
	}
	
	public boolean delayNumberAnnounce() {
		return false;
	}
	
	
	// they pressed the trackball or hit enter or stroked directly right
	private void selected(CoState cos) {
		if(!none) {
			BusStop bs = nearbyStops[currentIndex];
			cos.setState(new StopState(bs));
		}
	}

	public void onGestureFinish(Gesture g, CoState cos) {
		if(nearbyStops == null) {
			// TODO: consider whether the next two lines of code should be deleted
			if (g == Gesture.LEFT) {
				cos.stopPlaying();
				cos.backState();
			}
			return;
		}
		switch(g) {
		case UPRIGHT:
			if(!none) {
				BookmarkManager mang = cos.getBookmarks();
				mang.toggleBookmark(nearbyStops[currentIndex],cos);
				break;
			} // else, fall through!
		case UP:
		case UPLEFT:
			scroll(-1, cos);
			break;
		case DOWN:
		case DOWNRIGHT:
		case DOWNLEFT:
			scroll(+1, cos);
			break;
		case LEFT:
			cos.backState();
			break;
		case RIGHT:
		//case SELECT:
			selected(cos);
			break;
		}

	}
	
	public void onTrackballDown(CoState cos) {
		if(nearbyStops != null) {
			selected(cos);
		}
	}

	public boolean onKeyDown(int keyCode, CoState cos) {

		if(nearbyStops == null) {
			if(keyCode == KeyEvent.KEYCODE_BACK) {
				cos.stopPlaying();
				// return false; // we still want the state to go away
			}
			return false;
		}
		
		switch(keyCode) {
		case KeyEvent.KEYCODE_MENU:
			longDescribe(cos);
			return true;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_CALL:
			selected(cos);
			return true;
		case KeyEvent.KEYCODE_CAMERA:
			if(none)
				return false; // this is an inappropriate time to press that button
			else {
				BookmarkManager mang = cos.getBookmarks();
				mang.toggleBookmark(nearbyStops[currentIndex],cos);
				return true;
			}
		default:
			return false;
		}
	}

	public void longDescribe(CoState cos) {
		cosForFinderThread = cos;
		if(nearbyStops == null) {
			cos.displayText("LOADING\nPLEASE WAIT");
			cos.outputText("LOADING, PLEASE WAIT");
			cos.startPlaying();
		}
		else {
			cos.stopPlaying(); // just in case
			
			// TODO Maybe have it actually update the list, in case we walked somewhere else?
			if(none) {
				// these won't work right until we have text queueing fixed: 
				/*cos.speakText("You have no bookmarks");
				cos.backState();*/
				updateText(cos, true);
			}
			else {
				cos.outputText("Select desired stop. Current selection: ");
				updateText(cos, true);
			}
		}

	}
	
	public void shortDescribe(CoState cos) {
		if(nearbyStops == null) {
			cos.outputText("Loading, please wait");
		}
		else {
			// TODO make sure that this doesn't do anything surprising like
			// change the text being displayed!
			updateText(cos, true);
		}
		
	}
	
	public void onResume(CoState cos) {
		
	}


}
