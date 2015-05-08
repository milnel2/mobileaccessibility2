/**
 * @author Michael Q. Lam (mqtlam@cs.washington.edu)
 * @author Levi Lindsey (levisl@cs.washington.edu)
 * @author Chris Raastad (craastad@cs.washington.edu)
 * 
 * Designed to meet the requirements of the Winter 2011 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * DirectionsDisplay displays to the user a vertical list of the directions 
 * in the current route.
 */

package edu.uw.cse481h.phonewand;

import android.os.Bundle;
import android.util.Log;

public class DirectionsDisplay extends SlideRuleListActivity {
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	// The route has been setup.
    	if(RouteOrienter.mDirectionsText != null) {
	    	int length = RouteOrienter.mDirectionsText.length;
	    	mListItems = new String[length+1];
			mListItems[0] = getString(R.string.go_back_to_orienter);
			System.arraycopy(RouteOrienter.mDirectionsText, 0, mListItems, 1, length);
			refreshList();
			
		// The route has not been setup yet.
    	} else {
    		cancelScreen();
    	}
    }
    
    /** Called when an item is selected. */
    @Override
    protected void onItemSelected(int listItemIndex) {
    	Log.d(TAG, "Item selected [" + listItemIndex + "]: " + mListItems[listItemIndex]);
    	
    	// if user selected "Back to map view"
    	if (listItemIndex == 0) {
    		cancelScreen();
    	}
    	// Otherwise the user selected a step in the route directions (do nothing).
    }
	
	@Override
	protected void swipeLeft() {
		// Do nothing.
	}
	
	@Override
	protected void swipeDown() {
		// Do nothing.
	}
	
	@Override
	protected void swipeRight() {
		cancelScreen();
	}
	
	@Override
	protected void swipeUp() {
		// Do nothing.
	}
}
