/**
 * @author Michael Q. Lam (mqtlam@cs.washington.edu)
 * @author Levi Lindsey (levisl@cs.washington.edu)
 * @author Chris Raastad (craastad@cs.washington.edu)
 * 
 * Designed to meet the requirements of the Winter 2011 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * RouteInput prompts the user to input a new destination location.  
 * RouteInput also provides the ability for the user to select a previously 
 * entered location.
 */

package edu.uw.cse481h.phonewand;

import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

//public class RouteInput extends TTSActivity {
public class RouteInput extends PhoneWandActivity {
	
	// -----Geographical Address Entry Contants and Fields-----
	// How many addresses matches to allow the Geocoding functionality to return.
	private static final int MAX_ADDRESS_MATCHES = 5;
	// Contains possible Address matches for the current destination String.
	public static List<Address> mPossibleAddresses;
	// The destination entry EditText View.
	private TextView mDestinationDisplay;
	// The currently displayed destination address String.
	public static String mDestinationString;
	// For determining which type of instance of PossibleAddresses is called.
	public static boolean mFromCurrentLocationScreen;
	
	/** Called when the system creates this Activity. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle(R.string.app_name);
		setContentView(R.layout.route_input);
		
		// Destination entry field.
		mDestinationDisplay = (TextView) findViewById(R.id.destinationdisplay);
		
		mDestinationString = getString(R.string.default_destination_string);
		
		if(mCurrentBookmarkId > 0) {
			// Get a String representing the most recently seen destination.
			Cursor addressCursor = getBookmarkRecord(
					mCurrentBookmarkId);
			mDestinationString = addressCursor.getString(0);
			addressCursor.close();
		}
		
		// Set the destination EditText View to reflect the most recently seen destination.
		mDestinationDisplay.setText(mDestinationString);
	}
	
	@Override
	public void onResume() {
		super.onResume();
		mFromCurrentLocationScreen = false;
		String ttsText = (mDestinationString.equals(getString(R.string.default_destination_string))) ? 
				mDestinationString : "Currently entered destination is " + mDestinationString;
		ttsSpeak(ttsText, TextToSpeech.QUEUE_ADD);
	}
	
	/**
	 * Callback method that is called when the PossibleAddresses Activity 
	 * returns.  The PossibleAddresses Activity will return both a latitude 
	 * and longitude extra representing the destination selected by the user.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
		// If request was canceled, do nothing.
		if (resultCode == RESULT_CANCELED) {
			return;
		}
		
		int latitude = -1;
		int longitude = -1;
		
		switch(requestCode) {
		// Destination entered in BlindText screen.
		case REQUEST_CODE_TOUCH_KEYBOARD:
			// Update the currently displayed destination address.
			String textEntered = intent.getStringExtra(TEXT_ENTERED_EXTRA);
			mDestinationString = (textEntered == null || textEntered.length() < 3) ? 
					getString(R.string.default_destination_string) : textEntered;
			mDestinationDisplay.setText(mDestinationString);
			
			findDestination();
			break;
			
		// Address selected in RouteArchive screen.
		case REQUEST_CODE_ROUTE_ARCHIVE:
			// Get address record id returned from RouteArchive.
			mCurrentBookmarkId = intent.getLongExtra(RECORD_ID_EXTRA, -1);
			// Ensure the extra was passed.
			if(mCurrentBookmarkId < 0) {
				if (D) Log.e(TAG, getString(R.string.address_id_extra_fail));
				finish();
			}
			//mDestinationString = getBookmarkAddresses()[(int) mCurrentBookmarkId]; 
			
			// Retrieve destination from database.
			Cursor addressCursor = getBookmarkRecord(mCurrentBookmarkId);
			mDestinationString = addressCursor.getString(0);
			latitude = addressCursor.getInt(1);
			longitude = addressCursor.getInt(2);
			
			// Update the currently displayed destination address.
			mDestinationDisplay.setText(mDestinationString);
			
			addressCursor.close();
			
			// Open the RouteOrienter screen with the given location information.
			openRouteOrienter(latitude, longitude);
			break;
			
		// Address selected in PossibleAddresses screen.
		case REQUEST_CODE_POSSIBLE_ADDRESSES:
			// Get the latitude and longitude values returned from PossibleAddresses.
			int index = intent.getIntExtra(INDEX_EXTRA, -1);
			// Ensure the extra was passed.
			if(index < 0) {
				if (D) Log.e(TAG, getString(R.string.address_index_extra_fail));
				finish();
			}
			
			// Extract the lat and lon.
			Address destination = mPossibleAddresses.get(index);
			latitude = doubleToInt(destination.getLatitude());
			longitude = doubleToInt(destination.getLongitude());
			// Save the destination in the database.
			mCurrentBookmarkId = addBookmarkRecord(
					getAddressString(destination), latitude, longitude);
			
			// Update the currently displayed destination address.
			mDestinationString = getAddressString(destination);
			mDestinationDisplay.setText(mDestinationString);
			
			// Open the RouteOrienter screen with the given location information.
			openRouteOrienter(latitude, longitude);
			break;
			
		// The RouteOrienter screen returned.
		case REQUEST_CODE_ROUTE_ORIENTER:
			// Do nothing.
			break;
			
		// The CurrentLocation screen returned.
		case REQUEST_CODE_CURRENT_LOCATION:
			// Do nothing.
			break;
		default:
			if (D) Log.e(TAG, getString(R.string.invalid_request_code_fail)+" "+requestCode);
			finish();
			return;
		}
	}
	
	/**
	 * Open the RouteOrienter Activity with the given latitude and longitude 
	 * extras.
	 */
	public void openRouteOrienter(int latitude, int longitude) {
		Log.d(TAG, "Opening RouteOrienter with: latitude = "+latitude+"; longitude = "+longitude);
		
		// Open the RouteOrienter screen with the given destination.
		openActivityForResult(REQUEST_CODE_ROUTE_ORIENTER, latitude, longitude);
	}
	
	/**
	 * Find the user's current location, query Google Maps for a route to specified
	 * 	destination, and open the RouteOrienter screen with this information.
	 */
	private void findDestination() {
		// Try to find some locations that the user's destination String could represent.
		new GetAddresses().execute();
	}
	
	// -----Gesture Control-----
	protected boolean doubleTap() {
		String ttsText = (mDestinationString.equals(getString(R.string.default_destination_string))) ? 
				mDestinationString : "Currently entered destination is " + mDestinationString;
		PhoneWandActivity.ttsSpeak(ttsText, TextToSpeech.QUEUE_FLUSH);
		return true;
	}
	
	@Override
	protected void swipeUp() {
		// Open CurrentLocation screen
		swipeBuzz();
		swipeSpeech(SW_UP);
		
		openActivityForResult(REQUEST_CODE_CURRENT_LOCATION);
	}
	
	protected void swipeDown(){
		swipeSpeech(SW_DN);
		openActivityForResult(REQUEST_CODE_TOUCH_KEYBOARD);
	}
	
	protected void swipeLeft(){
		// open directions display screen
		swipeSpeech(SW_LF);
		findDestination();
	}
	
	protected void swipeRight(){
		// open enter new route screen
		swipeSpeech(SW_RT);
		openActivityForResult(REQUEST_CODE_ROUTE_ARCHIVE);
	}
	
	/**
	 * Inner (non-static) class which operates on a separate thread in order 
	 * to get a list of addresses which may match the destination which the 
	 * user has entered in the destinationEntry EditText field, and then 
	 * return the result to the main thread's Handler.
	 */
	private class GetAddresses extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			Message msg = mHandler.obtainMessage();
			
			// Send a starting message to the main thread.
			msg.what = MESSAGE_START_ADDRESS_RETRIEVAL;
			mHandler.sendMessage(msg);
			msg = mHandler.obtainMessage();
			
			// Ensure that the user has entered some sort of destination String.
			if(mDestinationString == null || mDestinationString.length() < 3 || 
					mDestinationString.equals(getString(R.string.default_destination_string))) {
				// Send a message which will post a notification that the user 
				// needs to enter a valid destination String.
				msg.what = MESSAGE_GET_ADDRESSES_FAIL;
				msg.arg1 = NOTIFY_NO_DESTINATION_STRING;
				
				// Send the result to the main thread.
				mHandler.sendMessage(msg);
				
				return null;
			}
			
			// Get a Geocoder object.
			Geocoder geocoder = new Geocoder(RouteInput.this);
			
			// Try to find a list of matching addresses.
			try {
				// Get a list of potential addresses.
				List<Address> addresses = 
						geocoder.getFromLocationName(mDestinationString, MAX_ADDRESS_MATCHES);
				
				// If at least one address was found, then return the list.
				if(addresses != null && addresses.size() > 0) {
					// Send a message returning the list of addresses found.
					msg.what = MESSAGE_GET_ADDRESSES_SUCCESS;
					msg.obj = addresses;
					
				// If no address was found, then notify the user.
				} else {
					// Send a message which will post a notification that the 
					// user needs to enter a better destination string.
					msg.what = MESSAGE_GET_ADDRESSES_FAIL;
					msg.arg1 = NOTIFY_NO_ADDRESSES_FOUND;
				}
				
			// Most likely the getFromLocationName method had a problem connecting to the network.
			} catch (Exception e) {
				// Send a message which will post a notification that the user 
				// needs to establish an Internet connection.
				msg.what = MESSAGE_GET_ADDRESSES_FAIL;
				msg.arg1 = NOTIFY_NO_INTERNET;
				msg.obj = e;
			}
			
			// Send the result to the main thread.
			mHandler.sendMessage(msg);
			
			return null;
		}
	}
	
	/**
	 * Sends and receives Messages and Runnables between threads.
	 */
	public final Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_START_ADDRESS_RETRIEVAL:
				if (D) Log.d(TAG, getString(R.string.address_retrieval_started) + " " + 
						mDestinationString);
				
				break;
			case MESSAGE_GET_ADDRESSES_FAIL:
				notifyUser(msg.arg1, msg.obj);
				
				break;
			case MESSAGE_GET_ADDRESSES_SUCCESS:
				mPossibleAddresses = (List<Address>) msg.obj;
				
				// If there is only one Address in the list returned, then 
				// open the RouteOrienter Activity with this location.
				if(mPossibleAddresses.size() == 1 ) {
					Address destination = mPossibleAddresses.get(0);
					
					mDestinationString = getAddressString(destination);
					
					if(D) Log.d(TAG, getString(R.string.one_address_found_log)+" "+mDestinationString);
					
					// Extract the lat and lon.
					int latitude = doubleToInt(destination.getLatitude());
					int longitude = doubleToInt(destination.getLongitude());
					
					// Save the destination in the database.
					mCurrentBookmarkId = addBookmarkRecord(
							mDestinationString, latitude, longitude);
					
					// Update the currently displayed destination address.
					mDestinationDisplay.setText(mDestinationString);
					
					// Open the RouteOrienter screen with the given location information.
					openRouteOrienter(latitude, longitude);
					
				// There are multiple Addresses in the list, so open the 
				// PossibleAddresses Activity.
				} else {
					if (D) Log.d(TAG, getString(R.string.multiple_addresses_found_log));
					
					openActivityForResult(REQUEST_CODE_POSSIBLE_ADDRESSES);
				}
				
				break;
			default:
				if (D) Log.e(TAG,getString(R.string.invalid_message_fail)+msg.what);
				break;
			}
		}
	};
}