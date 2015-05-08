package cs.washington.mobileaccessibility.onebusaway.uistates;


import com.google.marvin.shell.TouchGestureControlOverlay.Gesture;

/**
 * Every node in the menu tree is represented by an instance of this interface.
 * I probably named it State, because I was originally thinking of the menu as
 * a finite state machine, before realizing there was sort of an unbounded stack too.
 * 
 * So really, the current state of the application is given by a stack of instances
 * of this interface, and the top one is what's currently in control of the UI,
 * while the next one down is the state that will be reached by hitting the back button.
 * The lowest state is the home menu, from which hitting the back button closes the
 * application.  The stack of these things is stored in MainActivity.
 * 
 * Anyhow, this interface is what MainActivity uses to relay information to whoever
 * is currently managing the user interface.  Some things get handled on the side of
 * MainActivity, in particular those aspects which ought to be uniform throughout the
 * application.  To send information back to MainActivity, there's an interface
 * CoState.  Since MainActivity is in charge, and starts with the control, it passes
 * a CoState object (which happens to be itself) to the State in each call.
 * 
 * @author Will
 *
 */
public interface State {


	// Does this page want to use trackball scrolling, whatever that is?
	// it's probably used when lists are being displayed
	public boolean wantsTrackballScrolling();
	// does this page want the extra doubledown (aka 'zero') gesture, which is
	// like the gesture for 8, but closer to the bottom of the screen?
	public boolean wantsZeroGesture();
	// Does this page want the side scrollbar?
	// Note however that one of the application's settings can disable this
	// So, there will only be a scrollbar if this returns true, and the
	// sidebar setting of the application is enabled
	public boolean wantsSidebar();
	// Do we want to accelerate the scrolling?  Again, this can be disabled
	// by one of the application settings.  It is mainly used for lists,
	// to speed up the process of scrolling through the lists
	//
	// Basically, the list scrolls before you release the gesture, instead of
	// waiting for the upwards/downwards gesture to be completed.
	public boolean accelerateScroll();
	// This is used for the EnterNumberState to prevent the program from pronouncing
	// the names of numbers when the user is tentatively trying the associated guestures.
	// For most menus, that works fine, but with numbers, it sounds like a bunch of random
	// numbers are being entered multiple times.
	//
	// But if the use really is patient, then if they try a gesture but don't release it,
	// and wait long enough, the name of the associated number will eventually be pronounced.
	//
	// So the announcement is just delayed a slight bit.
	public boolean delayNumberAnnounce();
	
	
	// How the menu gets to respond to gestures, keystrokes, and sidebar events
	public void onGestureFinish(Gesture g, CoState cos);
	public boolean onKeyDown(int keyCode, CoState cos);
	public void onSidebar(double fraction, CoState cos);

	// Originally, this was called when the state came into existence
	// But it also morphed into something that a lot of states currently call when
	// the MENU button is pressed, which is supposed to be for help
	// At present, MainActivity calls this whenever it switches to a node, so i.e. both when
	// the node is created, and also when we return to the node from a deeper one,
	// by hitting BACK.
	public void longDescribe(CoState cos);
	
	// OK, now _this_ is what always gets called at the start
	// the only reason to do this is that having MENU delete the numbers entered so far
	// was really weird
	public void onResume(CoState cos);
	// TODO go through everywhere, refactoring by moving stuff in the longDescribe()
	// method to the onResume() method, if appropriate
	
	// new... triggered by the Camera button
	// the closest thing to 'read this screen'
	// note that unlike longDescribe, this probably shouldn't change the screen,
	// or do anything permanent
	// TODO check this everywhere...
	public void shortDescribe(CoState cos);
	
	
	// This is how the controller knows what to display and speak when you are tentatively
	// trying a command, but haven't lifted up your finger to signal selection of the menu item
	//
	// It returns the empty string if the gesture didn't correspond to a menu item
	// (which should in turn cause the background to revert and stop displaying the
	// name of any menu item).
	//
	// Originally this was only used if getMode() was MENU, but I also use it in LIST, for the
	// RIGHT and LEFT gestures, since those can be menu items (whereas all the other gestures
	// are used for navigating up and down the list).
	//
	// Important note: if we return "", the system _thinks_ this was an invalid state,
	// and doesn't buzz, but we _could_ have buzzed and done something else, like change
	// the screen.  So this is how list states handle the tentative gestures
	public String tentativeGesture(Gesture g, CoState cos);

	public void onTrackballDown(CoState cos);
}
