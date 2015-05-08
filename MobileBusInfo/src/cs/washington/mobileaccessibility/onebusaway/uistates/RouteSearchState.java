package cs.washington.mobileaccessibility.onebusaway.uistates;

import com.google.android.maps.GeoPoint;
import com.google.marvin.shell.TouchGestureControlOverlay.Gesture;

import cs.washington.mobileaccessibility.onebusaway.BookmarkManager;
// import cs.washington.mobileaccessibility.onebusaway.util.Util;

import android.util.Log;
import android.view.KeyEvent;
import firbi.base.com.BusRoute;
import firbi.base.com.BusStop;
import firbi.base.util.BusStopComparator;

/* 
 * This is used for the states in which we search for a stop by route.
 * whoops, I guess this is really two states.  Oh well.
 * (One is for choosing which destination of the route you want, and the
 * second is for choosing which stop).
 * 
 * An eventual goal of this class is to also include the grouping of stops
 * by city/neighborhood, which is available online by parsing the html of
 * onebusaway's text-only page, but is NOT available through the xml API at all,
 * oddly enough.  Brian Ferris was thinking about adding it eventually.
 *  
 */
public class RouteSearchState implements State {

	// the route in question
	private BusRoute theRoute;
	// whichever list we are in, this is the index of the current selection
	private int currentIndex;
	// The destinations of the route, OR null IF we are waiting on the interwebs
	private String [][] destinations;
	// Which destination the user selected.  Initially this is null, until the user
	// selects a destination
	private String[] destination;
	// the stops along the route.  Probably null until the user selects a destination
	private BusStop [] stops;
	
	private CoState cosForFinderThread; // this is cheating
	// (the problem is that the constructor doesn't get a CoState to call postRunnable on)
	
	
	public RouteSearchState(BusRoute route) {
		theRoute = route;
		currentIndex = 0;
		destinations = null;
		cosForFinderThread = null;
		Runnable r = new Runnable() {
			public void run() {
				destinations = theRoute.getDestinations();
				if(cosForFinderThread != null) {
					Runnable r2 = new Runnable() {
						public void run() {
							if(cosForFinderThread.amIOnTop(RouteSearchState.this))
								longDescribe(cosForFinderThread);
						}
					};
					cosForFinderThread.postRunnable(r2);
				}
			}
		};
		new Thread(r).start();
		destination = null;
		stops = null;
	}
	
	// down should be +1 (for down) or -1 (for up)
	private void scroll(int down, CoState cos) {
		int nextIndex = currentIndex + down;
		if(nextIndex < 0)
			return;
		int upperBound;
		if(destination == null)
			upperBound = destinations.length;
		else
			upperBound = stops.length;
		if(nextIndex >= upperBound)
			return;
		currentIndex = nextIndex;
		cos.vibrateForScroll();
		updateText(cos, true);
	}
	
	public void onSidebar(double fraction, CoState cos) {
		int upperBound;
		if(destination == null)
			upperBound = destinations.length;
		else
			upperBound = stops.length;
		int nextIndex = (int)(fraction*upperBound);
		if(nextIndex == upperBound)
			nextIndex = upperBound - 1; // in case fraction was exactly 1.0
		if(nextIndex >= 0 && nextIndex < upperBound) {
			if(nextIndex != currentIndex) {
				currentIndex = nextIndex;
				cos.vibrateForScroll();
				updateText(cos,true);
			}
		}
	}
	
	private void updateText(CoState cos, boolean speak) {
		// TODO fix the case where destinations is null or zero-length
		if(destination == null) {
			String [] currentDestination = destinations[currentIndex];
			String destinationDescription = currentDestination[0];
			for(int i = 1; i < currentDestination.length; i++)
				destinationDescription += ", " + currentDestination[i];
			if(speak)
				cos.outputText(destinationDescription);
			cos.displayText("Destination: " + destinationDescription);
		}
		else {
			String stopDescription = stops[currentIndex].getAddress();
			if(speak)
				cos.outputText(stopDescription); // Util.convert(stopDescription));
			cos.displayText(stopDescription);
		}
	}
	
	// this should only be called with left and right
	public String tentativeGesture(Gesture g, CoState cos) {
		if(destinations == null) {
			// TODO: consider whether the next two lines of code should be deleted
			// they are bound with two lines of code in onGestureFinish()
			if (g == Gesture.LEFT)
				return "Previous Menu";
			return "";
		}
		if(g == Gesture.LEFT)
			return "Previous Menu";
		if(g == Gesture.RIGHT)
			return "Select this " + ((destination == null)?"destination":"stop");
		if(g == Gesture.UP)
			return "Scroll up";
		if(g == Gesture.DOWN || g == Gesture.DOUBLEDOWN)
			return "Scroll down"; // hm, what TODO about Doubledown?
		if(g == Gesture.UPRIGHT) {
			if(g == Gesture.UPRIGHT) {
				if(cos.getBookmarks().isBookmarked(stops[currentIndex]))
					return "Remove bookmark for this stop";
				else
					return "Add bookmark for this stop";
			}
		}
		return "";
	}
	
	public boolean wantsTrackballScrolling() {
		return true;
	}
	
	public boolean wantsZeroGesture() {
		return true;
	}
	
	public boolean wantsSidebar() {
		return (destinations != null); // i.e., return false iff we are in waiting mode
	}
	
	public boolean accelerateScroll() {
		return true;
	}
	
	public boolean delayNumberAnnounce() {
		return false;
	}
	
	// they pressed the trackball or hit enter or stroked directly right
	private void selected(CoState cos) {
		if(destination == null) {
			destination = destinations[currentIndex];
			loadStops(cos.getLocation());
			longDescribe(cos);
		}
		else {
			BusStop bs = stops[currentIndex];
			cos.setState(new StopState(bs,theRoute));
		}
	}

	public void onGestureFinish(Gesture g, CoState cos) {
		if(destinations == null) {
			// TODO: consider whether the next two lines of code should be deleted
			if (g == Gesture.LEFT) {
				cos.stopPlaying();
				cos.backState();
			}
			return;
		}
		switch(g) {
		case UPRIGHT:
			if(destination != null) {
				BookmarkManager manager = cos.getBookmarks();
				manager.toggleBookmark(stops[currentIndex],cos);
				break;
			}
			// else, fall through!
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
			if(destination == null)
				cos.backState();
			else {
				destination = null;
				currentIndex = 0;
				longDescribe(cos);
			}
			break;
		case RIGHT:
			selected(cos);
			break;
		}
		// TODO: find a way of handling doubledown in list modes!!
		// (See onGestureFinish() in StopState and the discussion there)

	}
	
	public void onTrackballDown(CoState cos) {
		if(destinations != null) {
			selected(cos);
		}
	}

	public boolean onKeyDown(int keyCode, CoState cos) {

		if(destinations == null) {
			if(keyCode == KeyEvent.KEYCODE_BACK) {
				cos.stopPlaying();
				// return false; // we still want the state to go away
			}
			return false;
		}
		
		switch(keyCode) {
		case KeyEvent.KEYCODE_BACK:
			if(destination == null)
				return false; // falls through
				// equivalently, we could've said
				// cos.backState();
			
			else {
				destination = null;
				currentIndex = 0;
				longDescribe(cos);
				return  true;
			}
		case KeyEvent.KEYCODE_MENU:
			longDescribe(cos);
			return true;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_CALL:
			selected(cos);
			return true;
		case KeyEvent.KEYCODE_CAMERA:
			if(destination == null)
				return false; // this is an inappropriate time to press that button
			else {
				BookmarkManager mang = cos.getBookmarks();
				mang.toggleBookmark(stops[currentIndex],cos);
				return true;
			}
		default:
			return false;
		}
	}
	
	// to be called after destination is set to non-null
	private void loadStops(GeoPoint here) {
		// TODO: should this be in a separate thread?  It's not clear why
		// this won't go on forever
		stops = theRoute.getStops(destination);
        // let's follow Brian Ferris's suggestion and find the _closest_ stop,
		// and use that to set the initial valuse of currentIndex
		if(here == null)
			currentIndex = 0;
		else {
			BusStopComparator bsc = new BusStopComparator(here);
			int bestIndex = 0;
			for(int i = 1; i < stops.length; i++) {
				if(bsc.compare(stops[bestIndex], stops[i]) > 0)
					bestIndex = i;
			}
			currentIndex = bestIndex;
		}
		
	}

	public void longDescribe(CoState cos) {
		Log.i("MobileBusInfo","RouteSearchState.longDescribe() called");
		cosForFinderThread = cos;
		if(destinations == null) {
			cos.displayText("LOADING\nPLEASE WAIT");
			cos.outputText("LOADING, PLEASE WAIT");
			cos.startPlaying();
		}
		else {
			cos.stopPlaying(); // just in case
			// TODO fix up these instructions
			if(destination == null) {

				cos.outputText("Select destination for route. Current selection: ");
				updateText(cos, true);
			}
			else {
				cos.outputText("Select desired stop. Current selection: ");
				updateText(cos, true);
			}
		}
	}
	
	public void shortDescribe(CoState cos) {
		// TODO make sure that this doesn't do anything surprising like
		// change the text being displayed!
		if(destinations == null) {
			cos.outputText("Loading, please wait");
		}
		else {
			updateText(cos, true);
		}
		
	}

	public void onResume(CoState cos) {
		
	}
}
