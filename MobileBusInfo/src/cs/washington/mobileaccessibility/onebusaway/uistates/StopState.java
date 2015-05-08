package cs.washington.mobileaccessibility.onebusaway.uistates;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import com.google.marvin.shell.TouchGestureControlOverlay.Gesture;

import cs.washington.mobileaccessibility.onebusaway.BookmarkManager;
// import cs.washington.mobileaccessibility.onebusaway.util.Util;

import android.util.Log;
import android.view.KeyEvent;

import firbi.base.com.Bus;
import firbi.base.com.BusRoute;
import firbi.base.com.BusStop;

/**
 * This is the state/page that displays the information for a given stop.
 * It may or may not be filtering the arrivals to only show a specified route.
 * This is also the page where alarms can be set/removed.
 * @author Will
 *
 */
public class StopState implements State, EnterNumberState.RouteAcceptor {
	
	// The stop being displayed!
	private BusStop theStop;
	// Null -> all arrivals show
	// not-null -> only the arrivals of this route are displayed
	private BusRoute routeFilter;
	private int currentIndex;
	private Bus [] arrivals;
	// true if there ARE NO arrivals
	private boolean none;
	// some text that is prepended to the description of each arrival,
	// to give more context.  It probably says something like the name of the
	// stop, and whether we are filtering results
	// It gets left out when you're just scrolling through the list, in order
	// to make the information more quickly accessible
	private String headerText;
	
	// a cheap trick to allow postRunnable to be called, since we don't get a CoState
	// in the constructor, which makes the Runnable
	private CoState cosForFinderThread = null;
	
	/*
	 * This is the constructor for the case when there is not a route filter
	 */
	public StopState(BusStop stop) {
		theStop = stop;
		routeFilter = null;
		currentIndex = 0;
		// TODO check the sorting stage, since it seems that
		// in some cases the array of Bus[] objects can have nulls
		// in it.  Hard to replicate this error, though.
		Log.i("MobileBusInfo","In constructor of StopState, about to start thread to ask for arrivals");
		arrivals = null;
		none = true;
		// It's going to take a while to load in the arrivals information, so for now
		// start the process, which will get back to us later when it's done
		Runnable r = new Runnable() {
			public void run() {
				Log.i("MobileBusInfo","About to ask for arrivals!");
				arrivals = theStop.getUpcomingArrivals();
				none = (arrivals.length == 0);
				Log.i("MobileBusInfo","Done getting arrivals!");
				if(cosForFinderThread != null) {
					Runnable r2 = new Runnable() {
						public void run() {
							if(cosForFinderThread.amIOnTop(StopState.this))
								longDescribe(cosForFinderThread);
						}
					};
					cosForFinderThread.postRunnable(r2);
				}
			}
			
		};
		new Thread(r).start();
	}
	
	// see if two bus routes are the same, after removing letters
	private boolean routeMatch(BusRoute r1, BusRoute r2) {
		String first = r1.getRouteNumber().replaceAll("[a-zA-Z]", "");
		String second = r2.getRouteNumber().replaceAll("[a-zA-Z]", "");
		return first.equals(second);
	}
	
	/*
	 * This is the constructor for the case where there IS a route filter
	 */
	public StopState(BusStop stop, final BusRoute filter) {
		theStop = stop;
		routeFilter = filter;
		currentIndex = 0;
		
		Log.i("MobileBusInfo","In constructor of StopState, about to start thread to ask for arrivals");
		arrivals = null;
		none = true;
		Runnable r = new Runnable() {
			public void run() {
				Log.i("MobileBusInfo","In constructor of StopState (with route), about to ask for arrivals");
				arrivals = theStop.getUpcomingArrivals();
				Log.i("MobileBusInfo","In constructor of StopState (with route), just got the arrivals");
				ArrayList<Bus> matches = new ArrayList<Bus>();
				for(int i = 0; i < arrivals.length; i++) {
					if(routeMatch(arrivals[i].getRoute(),filter))
						matches.add(arrivals[i]);
				}
				arrivals = matches.toArray(new Bus[0]);
				none = (arrivals.length == 0);
				Log.i("MobileBusInfo","Done getting arrivals!");
				if(cosForFinderThread != null) {
					Runnable r2 = new Runnable() {
						public void run() {
							if(cosForFinderThread.amIOnTop(StopState.this))
								longDescribe(cosForFinderThread);
						}
					};
					cosForFinderThread.postRunnable(r2);
				}
			}
			
		};
		new Thread(r).start();

	}

	// if down is +1, scroll down.  if down is -1, scroll up
	private void scroll(int down, CoState cos) {
		int nextIndex = currentIndex + down;
		if(nextIndex < 0 || nextIndex >= arrivals.length)
			return;
		currentIndex = nextIndex;
		cos.vibrateForScroll();
		updateText(cos,true);
	}
	
	public void onSidebar(double fraction, CoState cos) {
		if(arrivals == null || none)
			return; // though actually we shouldn't be called in this case
		int nextIndex = (int)(fraction*arrivals.length);
		if(nextIndex == arrivals.length)
			nextIndex = arrivals.length - 1; // just in case we get 1.0 exactly
		if(nextIndex >= 0 && nextIndex < arrivals.length) {
			if(nextIndex != currentIndex) {
				currentIndex = nextIndex;
				cos.vibrateForScroll();
				updateText(cos,true);
			}
		}
	}
	
	private void updateText(CoState cos, boolean speak) {
		
		// TODO: fix all the setText calls in this and everywhere else
		// so that they maintain the name of the stop being written at the top
		// Also do this in RouteSearchState and a anywhere else that makes sense.
		String text = "No arrivals found in the next 30 minutes";
		if(!none) {
			Bus b = arrivals[currentIndex];
			String arrivalTimePhrase = getMinutesFromNowAsText(b);
			text = b.getRoute().getRouteNumber() + " to " + b.getDestination() +
				" arriving " + arrivalTimePhrase;
		}
		cos.displayText(headerText + text);
		if(speak)
			cos.outputText(text);
	}
	
	/*
	 * returns -1 if we're unsure(!)
	 */
	public static long getMillisFromNow(Bus b) {
		GregorianCalendar then = b.getPredictedTime();
		if(then == null)
			return -1;
		GregorianCalendar now = (GregorianCalendar) Calendar.getInstance();
		return then.getTimeInMillis() - now.getTimeInMillis();
	}
	
	public static String getMinutesFromNowAsText(Bus b) {
		// If this works, it's about 5 times shorter than what FIRBI did.
		long minutes = getMillisFromNow(b);
		minutes /= 60e3; // why can't I put this on one line with the previous?
		if(minutes == 0)
			return "NOW";
		else if(minutes == 1)
			return "in one minute";
		else if(minutes == -1)
			return "one minute ago";
		else if(minutes < 0)
			return "" + (-minutes) + " minutes ago";
		else
			return "in "+minutes + " minutes";
	}
	
	// this should only be called with left and right gestures
	public String tentativeGesture(Gesture g, CoState cos) {
		if(g == Gesture.LEFT)
			return "Previous Menu";
		if(arrivals == null)
			return "";
		if(g == Gesture.RIGHT) {
			if(routeFilter == null)
				return "Arrivals for one route";
			else
				return "View all arrivals";
		}
		if(none)
			return "";
		if(g == Gesture.UP)
			return "Scroll up";
		if(g == Gesture.DOWN || g == Gesture.DOUBLEDOWN)
			return "Scroll down"; // hm, what TODO about Doubledown?
		if(g == Gesture.UPRIGHT) {
			if(cos.getBookmarks().isBookmarked(theStop))
					return "Remove bookmark for this stop";
				else
					return "Add bookmark for this stop";
		}
		if(g == Gesture.DOWNRIGHT) {
			if(cos.getCurrentAlarmBus() == arrivals[currentIndex])
				return "Clear alarm for this arrival";
			else
				return "Set alarm for this arrival";
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
		return arrivals != null && !none;
	}
	
	public boolean accelerateScroll() {
		return true;
	}
	
	public boolean delayNumberAnnounce() {
		return false;
	}
	
	public State acceptRoute(BusRoute r) {
		return new StopState(theStop, r);
	}

	public void onGestureFinish(Gesture g, CoState cos) {
		if(arrivals == null) {
			// TODO: consider whether the next two lines of code should be deleted
			if (g == Gesture.LEFT) {
				cos.stopPlaying();
				cos.backState();
			}
			return;
		}
		switch(g) {
		case UPRIGHT:
			BookmarkManager mang = cos.getBookmarks();
			mang.toggleBookmark(theStop,cos);
			break;
		case UP:
		case UPLEFT:
			scroll(-1, cos);
			break;
		case DOWNRIGHT:
			toggleAlarm(cos);
			break;
		case DOWN:
		case DOWNLEFT:
			scroll(+1, cos);
			break;
		case LEFT:
			cos.backState();
			break;
		case RIGHT:
			if(routeFilter == null) {
				cos.setState(new EnterNumberState(this));
			} else {
				cos.setState(new StopState(theStop));	
			}
			break;
		}
		// TODO: how to handle doubledown?!
		// I think it might make sense to start reading off the arrivals automatically
		// (without needing manual scrolling), after a certain gesture.
		// That's what the phone system does.  It's hard to do with TTS however.
		
		// note that currently, we actually can't receive doubledown
		// because of how the gesture system works when we are in list mode

	}
	
	public void onTrackballDown(CoState cos) {
		if(arrivals != null) {
			refineToThisRoute(cos);
		}
	}
	
	public void toggleAlarm(final CoState cos) {
		
		final int timeBefore = 5*60*1000; // 5 minutes, in milliseconds
		// TODO consider other values
		if(!none) {
			final Bus b = arrivals[currentIndex];

			String premessage = b.getRoute().getRouteNumber() + " to " + b.getDestination();
			if(cos.getCurrentAlarmBus() == b) {
				cos.setCurrentAlarmRunnable(null);
				cos.setCurrentAlarmBus(null);
				cos.outputText("Cleared alarm for route " + premessage);
				return;
			}
			final Runnable r2 = new Runnable() {
				public void run() {
					cos.setState(new AlarmState(b));
				}
			};
			Runnable r1 = new Runnable() {
				public void run() {
					while(true) {
						long timeRemaining = getMillisFromNow(b);
						if(cos.getCurrentAlarmRunnable() != this)
							return;
					    // otherwise someone else set an alarm...
						if(timeRemaining <= timeBefore) {
							cos.postRunnable(r2);
							return;
						}
						else
							Log.i("MobileBusInfo","There are still " + timeRemaining + " ms left");
						
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
				}
			};
			cos.setCurrentAlarmBus(b);
			cos.setCurrentAlarmRunnable(r1);
			// I doubt we need to worry about someone else trying to set an alarm at the same
			// time...
			new Thread(r1).start();
			
			cos.outputText("Set alarm for route " + premessage);
			
		}
	}
	
	private void refineToThisRoute(CoState cos) {
		if(routeFilter == null && !none) {
			cos.setState(new StopState(theStop,
					arrivals[currentIndex].getRoute()));
		}
	}

	public boolean onKeyDown(int keyCode, CoState cos) {
		if(arrivals == null) {
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
			refineToThisRoute(cos);
			// TODO: consider instead just reading it off again
			// i.e., updateText(cos, true)
			return true;
		case KeyEvent.KEYCODE_CAMERA:
			BookmarkManager mang = cos.getBookmarks();
			mang.toggleBookmark(theStop,cos);
			return true;
		default:
			return false;
		}
	}

	public void longDescribe(CoState cos) {
		cosForFinderThread = cos;
		if(arrivals == null) {
			// let's NOT set cos.setRecentStop, since the user doesn't
			// think we've actually looked at the stop yet
			cos.displayText("LOADING\nPLEASE WAIT");
			cos.outputText("LOADING, PLEASE WAIT");
			cos.startPlaying();
		}
		else {
			cos.stopPlaying(); // just in case

			cos.setRecentStop(theStop);
			// TODO do something better
			String address = theStop.getAddress();
			String text = address;
			String speak = address;
			text += "\n";
			speak += ", stop number ";
			String stopNumber = theStop.getStopNumber();
			text += stopNumber;
			speak += stopNumber;
			if(routeFilter != null) {
				String routeNumber = routeFilter.getRouteNumber();
				text += "\n" + routeNumber;
				speak += ", route number " + routeNumber + " ";
			}
			if(!none) {
				text += "\nScroll for arrivals";
				speak += " Scroll for arrivals ";
			}
			headerText = text + "\n";
			cos.displayText(headerText); // TODO won't this be obliterated when we updateText?
			cos.outputText(speak);
			updateText(cos, true);
		}
	}
	
	public void shortDescribe(CoState cos) {
		if(arrivals == null) {
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
