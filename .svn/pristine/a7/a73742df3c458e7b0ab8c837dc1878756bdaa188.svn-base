package cs.washington.mobileaccessibility.onebusaway.uistates;

import com.google.marvin.shell.TouchGestureControlOverlay.Gesture;

// import cs.washington.mobileaccessibility.onebusaway.util.Util;

import android.view.KeyEvent;
import firbi.base.com.BusStop;


/*
 * The state for viewing bookmarks or editing them
 * (There are really two different states, therefore, which are distinguished
 *  by means of a boolean flag) 
 */
public class BookmarkState implements State {

	// true if we're supposed to be editing/managing the bookmarks
	// false if we're just choosing one to see the current arrivals schedule for
	private boolean editing;
	// the index of the currently displayed bookmark
	private int currentIndex;
	private BusStop [] favorites;
	// true if there are not favorites (but then, we shouldn't get to this state)
	private boolean none;
	
	public BookmarkState(boolean managing, CoState cos) {
		this.editing = managing;
		currentIndex = 0;
		// TODO also, this should be done in onStart(), in case
		// a bookmark changes
		favorites = cos.getBookmarks().getBookmarks();
		none = favorites.length == 0;
	}
	
	// if down is +1, scroll down.  if down is -1, scroll up
	private void scroll(int down, CoState cos) {
		int nextIndex = currentIndex + down;
		if(nextIndex < 0 || nextIndex >= favorites.length)
			return;
		currentIndex = nextIndex;
		cos.vibrateForScroll();
		updateText(cos,true);
	}
	
	public void onSidebar(double fraction, CoState cos) {
		if(none)
			return; // though actually we shouldn't be called in this case
		int nextIndex = (int)(fraction*favorites.length);
		if(nextIndex == favorites.length)
			nextIndex = favorites.length - 1; // just in case we get 1.0 exactly
		if(nextIndex >= 0 && nextIndex < favorites.length) {
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
		String text = "You have no favorites!";
		if(!none) {
			BusStop bs = favorites[currentIndex];
			text = bs.getAddress() + ", stop #" +  bs.getStopNumber();
		}
		cos.displayText(text);
		if(speak)
			cos.outputText(text);
	}
	
	public String tentativeGesture(Gesture g, CoState cos) {
		if(g == Gesture.LEFT)
			return "Previous Menu";
		if(none)
			return "";
		if(g == Gesture.RIGHT) {
			if (editing)
				return "Delete selected bookmark";
			else
				return "View selected stop";
		}
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
		return !none;  // no point in scrolling if there are none
	}
	
	public boolean accelerateScroll() {
		return true;
	}
	
	public boolean delayNumberAnnounce() {
		return false;
	}
	
	// they pressed the trackball or hit enter or stroked directly right
	private void selected(CoState cos) {
		if(none)
			return; // nothing to do...
		BusStop bs = favorites[currentIndex];
		if(editing) {
			cos.getBookmarks().toggleBookmark(bs, cos);
			// then need to restart this state...
			currentIndex = 0;
			favorites = cos.getBookmarks().getBookmarks();
			none = favorites.length == 0;
			// TODO: this is common code with the constructor.  Restructure it.
			// as mentioned above, that code should almost go in onStart,
			// because you could move forward to a stop, delete it, and
			// then come back to this state, even if you weren't in the
			// managing-mode.
			
			longDescribe(cos);
		}
		else {
			cos.setState(new StopState(bs));
		}
	}

	public void onGestureFinish(Gesture g, CoState cos) {
		switch(g) {
		case UP:
		case UPRIGHT:
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
		// case SELECT:
			selected(cos);
			break;
		}
		// TODO: find a way of handling doubledown in list modes!!

	}
	
	public void onTrackballDown(CoState cos) {
		selected(cos);
	}

	public boolean onKeyDown(int keyCode, CoState cos) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_MENU:
			longDescribe(cos);
			return true;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_CALL:
			selected(cos);
			return true;
			
		// it's inadvisable to edit and browse a menu at the same time...
		// case KeyEvent.KEYCODE_CAMERA:
		// oh wait, that's exactly what we do in managing mode.
		// hmm.
		default:
			return false;
		}
	}

	public void longDescribe(CoState cos) {
		// TODO fix up these instructions
		if(none) {
			// these won't work right until we have text queueing fixed: 
			/*cos.speakText("You have no bookmarks");
			cos.backState();*/
			updateText(cos, true);
		}
		else if(editing) {
			cos.outputText("Select bookmark to delete. Current selection: ");
			updateText(cos, true);
		}
		else {
			cos.outputText("Select desired stop. Current selection: ");
			updateText(cos, true);
		}
	}

	public void shortDescribe(CoState cos) {
		// TODO make sure that this doesn't do anything surprising like
		// change the text being displayed!
		updateText(cos, true);
		
	}
	
	public void onResume(CoState cos) {
		
	}
	

}
