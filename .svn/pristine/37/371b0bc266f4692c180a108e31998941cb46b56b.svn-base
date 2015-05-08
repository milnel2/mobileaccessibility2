package cs.washington.mobileaccessibility.onebusaway.uistates;

import com.google.marvin.shell.TouchGestureControlOverlay.Gesture;

import cs.washington.mobileaccessibility.onebusaway.uistates.EnterNumberState;

import android.view.KeyEvent;
import firbi.base.com.BusRoute;

/*
 * This was loosely based off the second option from the root of the
 * menu on the dial-in service.  Except that that one actually provides help,
 * while this one doesn't currently.  I'm not sure I understand the logic
 * of having two separate help menus from the start (2 and 0, on the dial-in menu),
 * but that's what they currently do.
 * 
 * It's also especially strange that this state is just a limited version of the home
 * one.  Again, that was based directly off the dial-in system.
 */
public class StopNumberHelpState implements State, EnterNumberState.RouteAcceptor {

	
	public StopNumberHelpState() {
		
	}
	
	public String tentativeGesture(Gesture g, CoState cos) {
		switch(g) {
		case UPLEFT:
			return "Search by Stop Number";
		case UP:
			return "Search by Route Number";
		case UPRIGHT:
			return "Search by GPS";
		case LEFT:
			return "Previous Menu";
		default:
			return "";	
		}
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
			cos.setState(new EnterNumberState(this));
			break;
		case UPRIGHT:
			cos.setState(new GeoState(cos));
			break;
		case LEFT:
			cos.backState();
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

	// when the user presses the MENU button, they get the long instructions...
	public void longDescribe(CoState cos) {
		String help = "The stop number is written on the top left side of ";
		help += "the posted schedule at the stop, in the middle of a series ";
		help += "of three numbers separated by dashes.  It tends to be a five";
		help += " digit number, but isn't always. The number may also be ";
		help += "written on the physical shelter, in yellow stenciled letters.";
		help += "  If you cannot find the stop number, but have GPS enabled on ";
		help += "your phone, your best option is to use the GPS search ";
		help += "feature (stroke down and left on the home screen), and" +
				"choose your stop from the list.";
		help += "  It will probably be the first or second one on the list. You can ";
		help += "also search for your stop by route, if you know what bus route";
		help += " you will be catching.  You will be given a list of all the ";
		help +=	"stops along the route, which you can scroll through to find your stop.";
		cos.displayText(help);
		cos.outputText(help);

	}
	
	public void shortDescribe(CoState cos) {
		// TODO FIX THIS!!
		// it's not even clear what we should do here
		// I guess they can always tap the MENU button and get the real instructions
		cos.outputText("Help finding the stop number.");
	}
	
	public void onTrackballDown(CoState cos) {
		// do nothing
	}
	
	public void onSidebar(double fraction, CoState cos) {
		// do nothing, this should never be called
	}
	
	// same as for HomeState
	public State acceptRoute(BusRoute route) {
		return new RouteSearchState(route);
	}

	public void onResume(CoState cos) {
		
	}

}
