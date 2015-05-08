/**
 * @author Michael Q. Lam (mqtlam@cs.washington.edu)
 * @author Levi Lindsey (levisl@cs.washington.edu)
 * @author Chris Raastad (craastad@cs.washington.edu)
 * 
 * Designed to meet the requirements of the Winter 2011 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * PossibleAddresses displays to the user a vertical list of the possible 
 * address matches returned by the Geocoding functionality of the RouteInput 
 * Activity.  The address selected by the user is then returned back to the 
 * RouteInput Activity.
 */

package edu.uw.cse481h.phonewand;

import java.util.List;

import android.content.Intent;
import android.location.Address;
import android.os.Bundle;
import android.util.Log;

public class PossibleAddresses extends SlideRuleListActivity {
	
	private static List<Address> mPossibleAddresses;
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	super.onCreate(savedInstanceState);
    	
    	mPossibleAddresses = (RouteInput.mFromCurrentLocationScreen || RouteOrienter.mOnCreate) ? 
    			CurrentLocation.mPossibleAddresses : RouteInput.mPossibleAddresses;
    	
    	// add addresses to the list
    	int listSize = mPossibleAddresses.size();
    	String logString = "Displaying "+listSize+
    			" possible matches for the destination entered by the user:";
    	String currentAddressString;
    	
    	mListItems = new String[listSize + 1];
    	for (int i = 0; i < listSize; i++) {
    		currentAddressString = getAddressString(mPossibleAddresses.get(i));
    		logString += "\n\t"+currentAddressString;
    		mListItems[i] = currentAddressString;
    	}
    	mListItems[mPossibleAddresses.size()] = getString(R.string.none_of_these);
		refreshList();
    	
    	if(D) Log.d(TAG, logString);
    }
    
    /** Called when an item is selected. */
    @SuppressWarnings("unchecked")
	@Override
    protected void onItemSelected(int listItemIndex) {
    	Log.d(TAG, "Item selected [" + listItemIndex + "]: " + mListItems[listItemIndex]);
    	
    	// The user selected "None of these".
    	if (listItemIndex == mPossibleAddresses.size()) {
    		cancelScreen();
    	
    	// Otherwise the user selects a valid address.
    	} else {
    		Class callee = (RouteInput.mFromCurrentLocationScreen || RouteOrienter.mOnCreate) ? 
					CurrentLocation.class : RouteInput.class;
	    	Intent intent = new Intent(PossibleAddresses.this, callee);
	    	intent.putExtra(INDEX_EXTRA, listItemIndex);
	    	
	    	// Return result.
	    	swipeBuzz();
	    	setResult(RESULT_OK, intent);
	    	finish();
	    	return;
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
