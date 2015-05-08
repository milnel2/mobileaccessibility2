package cs.washington.mobileaccessibility.onebusaway.uistates;

import com.google.marvin.shell.TouchGestureControlOverlay.Gesture;


import android.view.KeyEvent;
import firbi.base.com.BusRoute;
import firbi.base.com.BusStop;

/*
 * The state where everything begins
 */
public class HomeState implements State, EnterNumberState.RouteAcceptor {

	public void longDescribe(CoState cos) {
		// TODO fix this
		cos.outputText("Main menu.  Stroke up for help.");
		cos.displayText("Main menu.");
	}
	
	public void shortDescribe(CoState cos) {
		// TODO consider doing something more meaningful
		cos.outputText("Home menu.");
	}
	
	
	public HomeState() {
		
	}
	
	public String tentativeGesture(Gesture g, CoState cos) {
		switch(g) {
		case UPLEFT:
			return "Search by Stop Number";
		case UP:
			return "Help finding Stop Number";
		case UPRIGHT:
			return "Access Bookmarks";
		case LEFT:
			return "Manage Bookmarks";
		case DOWNRIGHT:
			if(cos.mostRecentStop() == null)
				return ""; // might as well not give them false hopes
			else
				return "Most recent stop";
		case RIGHT:
			return "Search by Route Number";
		case DOWNLEFT:
			return "Search by GPS";
		case DOWN:
			return "Settings";
		default:
			return "";	
		}
	}
	
	public void onTrackballDown(CoState cos) {
		// do nothing
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

	public void onGestureFinish(Gesture g, CoState cos) {
		switch(g) {
		case UPLEFT:
			cos.setState(new EnterNumberState(null));
			break;
		case UP:
			cos.setState(new StopNumberHelpState());
			break;
		case UPRIGHT:
			cos.setState(new BookmarkState(false, cos));
			break;
		case LEFT:
			cos.setState(new BookmarkState(true, cos));
			break;
		case DOWNRIGHT:
			BusStop bs = cos.mostRecentStop();
			if(bs == null)
				cos.outputText("No stop has been visited yet");
			else
				cos.setState(new StopState(bs));
			break;
		case RIGHT:
			cos.setState(new EnterNumberState(this));
			break;
		case DOWNLEFT:
			cos.setState(new GeoState(cos));
			break;
		case DOWN:
			cos.setState(new SettingsState());
			break;
		default:
			break; // do nothing	
		}

	}

	public boolean onKeyDown(int keyCode, CoState cos) {
		switch (keyCode) {
		case KeyEvent.KEYCODE_MENU:
			// TODO read help or whatever
			longDescribe(cos);
			return true;
		default:
			return false;
		}
	}
	
	public void onSidebar(double fraction, CoState cos) {
		// do nothing, this should never be called
	}
	
	// this gets called when we launch an EnterNumberState by
	// sliding to the right.  It will find a route, which it returns
	// to whoever invoked it (which could be a number of different states/nodes
	// in the menu).
	public State acceptRoute(BusRoute route) {
		return new RouteSearchState(route);
	}

	public void onResume(CoState cos) {
		
	}

}
