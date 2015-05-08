/**
 	 * @author Michael Q. Lam (mqtlam@cs.washington.edu)
 * @author Levi Lindsey (levisl@cs.washington.edu)
 * @author Chris Raastad (craastad@cs.washington.edu)
 * 
 * Designed to meet the requirements of the Winter 2011 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * RouteArchive displays to the user a vertical list of previously entered 
 * directions that have been saved into the internal database.
 */

package edu.uw.cse481h.phonewand;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class RouteArchive extends SlideRuleListActivity {
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);    	
    	
    	// Populate archive with previous routes.
		String addresses[] = getBookmarkAddresses();
		if (addresses != null) {
			mListItems = new String[addresses.length + 1];
			mListItems[0] = getString(R.string.go_back_to_input);
			System.arraycopy(addresses, 0, mListItems, 1, addresses.length);
		} else {
			// Error condition: database query failed.
			mListItems = new String[1];
			mListItems[0] = getString(R.string.go_back_to_input);
		}
		refreshList();
    }
	
    /** Called when an item is selected. */
    @Override
    protected void onItemSelected(int listItemIndex) {
    	if (D) Log.d(TAG, "Item selected [" + listItemIndex + "]: " 
    			+ mListItems[listItemIndex]);
    	
    	// The user selected "None of these".
    	if (listItemIndex == 0) {
    		cancelScreen();
    	} else {
    		// Return address selected to RouteFinder.
    		swipeBuzz();
        	Intent intent = new Intent(RouteArchive.this, RouteInput.class);
        	intent.putExtra(RECORD_ID_EXTRA, 
        			getBookmarkIDByAddress(mListItems[listItemIndex]));
        	setResult(RESULT_OK, intent);
        	finish();
    	}
    }
	
	@Override
	protected void swipeLeft() {
		cancelScreen();
	}
	
	@Override
	protected void swipeDown() {
		// Do nothing.
	}
	
	@Override
	protected void swipeRight() {
		// Do nothing.
	}
	
	@Override
	protected void swipeUp() {
		// Do nothing.
	}
}