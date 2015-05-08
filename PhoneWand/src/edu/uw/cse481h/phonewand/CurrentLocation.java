/**
 * @author Michael Q. Lam (mqtlam@cs.washington.edu)
 * @author Levi Lindsey (levisl@cs.washington.edu)
 * @author Chris Raastad (craastad@cs.washington.edu)
 * 
 * Designed to meet the requirements of the Winter 2011 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * CurrentLocation displays to the user some basic information on her current 
 * location.  This will attempt to obtain address data from the Geocoder, 
 * which may call the PossibleAddresses screen if there are multiple possible 
 * matches for the user's current location.
 */

package edu.uw.cse481h.phonewand;

import java.util.List;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.widget.TextView;

public class CurrentLocation extends PhoneWandActivity {
	// TextView Displays.
	private TextView mNearestAddressDisplay;
	private TextView mLatitudeDisplay;
	private TextView mLongitudeDisplay;
	private TextView mDistanceDisplay;
	
	// Nearest relevant address.
	private String mNearestAddress;
	// Current location.
	private int  mLat;
	private int  mLon;
	
	// How many addresses matches to allow the Geocoding functionality to return.
	private static final int MAX_ADDRESS_MATCHES = 5;
	// Contains possible Address matches for the current location.
	public static List<Address> mPossibleAddresses;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setTitle(R.string.app_name);
		setContentView(R.layout.current_location);
		
		// Ensure that we have given the location manager enough time to come 
		// up with some value for current location.
		if(mCurrentLocation == null) {
			notifyUser(NOTIFY_NO_GPS);
			cancelScreen();
			return;
		}
		
		mLat = mCurrentLocation.getLatitudeE6();
		mLon = mCurrentLocation.getLongitudeE6();
		
		// Current Location.
		mNearestAddressDisplay = (TextView) findViewById(R.id.nearest_address_display);
		mNearestAddressDisplay.setText(getString(R.string.nearest_address_string));
		// Current Location: Latitude.
		mLatitudeDisplay = (TextView) findViewById(R.id.latitude_display);
		mLatitudeDisplay.setText(getString(R.string.latitude_string) + " " + mLat/10.0e6);
		// Current Location: Longitude.
		mLongitudeDisplay = (TextView) findViewById(R.id.longitude_display);
		mLongitudeDisplay.setText(getString(R.string.longitude_string) + " " + mLon/10.0e6);
		// Distance to Destination.
		mDistanceDisplay = (TextView) findViewById(R.id.distance_display);
		
		// Find and set nearest address based on location.
		findNearestAddress();
		
		// Speak the current location info.
		speakCurrentLocation();
	}
	
	/**
	 * Find the nearest address based on users destination.  If there are multiple
	 * 	addresses start a SlideRule chooser to pick an address that seems most right.
	 */
	private void findNearestAddress() {
		new GetAddresses().execute();
	}
	
	/**
	 * Inner (non-static) class which operates on a separate thread in order 
	 * 	to get a list of addresses corresponding to the user's current location.
	 */
	private class GetAddresses extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... voids) {
			Message msg = mHandler.obtainMessage();
			
			// Send a starting message to the main thread.
			msg.what = MESSAGE_START_ADDRESS_RETRIEVAL;
			mHandler.sendMessage(msg);
			msg = mHandler.obtainMessage();
			
			// Get a Geocoder object to process current location.
			Geocoder geocoder = new Geocoder(CurrentLocation.this);
			
			// Try to find a list of matching addresses.
			try {
				// Get a list of potential addresses.
				List<Address> addresses = 
						geocoder.getFromLocation(intToDouble(mLat), intToDouble(mLon), 
								MAX_ADDRESS_MATCHES);
				
				// If at least one address was found, then return the list.
				if(addresses != null && addresses.size() > 0) {
					// Send a message returning the list of addresses found.
					msg.what = MESSAGE_GET_ADDRESSES_SUCCESS;
					msg.obj = addresses;
					
				// If no address was found, then notify the user.
				} else {
					msg.what = MESSAGE_GET_ADDRESSES_FAIL;
					msg.arg1 = NOTIFY_NO_ADDRESSES_FOUND;
				}
				
			// Most likely the getFromLocation method had a problem connecting to the network.
			} catch (Exception e) {
				// Send a message which will post a notification that the user 
				//   needs to establish an Internet connection.
				msg.what = MESSAGE_GET_ADDRESSES_FAIL;
				msg.arg1 = NOTIFY_NO_INTERNET;
				msg.obj = e;
			}
			
			// Send the result to the main thread.
			mHandler.sendMessage(msg);
			
			return null;
		}
	}
	
	/** Sends and receives Messages and Runnables between threads. */
	public final Handler mHandler = new Handler() {
		@SuppressWarnings("unchecked")
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MESSAGE_START_ADDRESS_RETRIEVAL:
				if (D) Log.d(TAG, getString(R.string.address_retrieval_started));
				break;
				
			case MESSAGE_GET_ADDRESSES_FAIL:
				notifyUser(msg.arg1, msg.obj);
				break;
				
			case MESSAGE_GET_ADDRESSES_SUCCESS:
				mPossibleAddresses = (List<Address>) msg.obj;
				
				// If there is only one Address in the list returned, then 
				// 	simply display the address.
				if(mPossibleAddresses.size() == 1 ) {
					Address destination = mPossibleAddresses.get(0);
					mNearestAddress = getAddressString(destination);
					
					checkNearestAddress();
					
					if(D) Log.d(TAG, getString(R.string.one_address_found_log)+" "+mNearestAddress);
					
					// Update the currently displayed destination address.
					mNearestAddressDisplay.setText(getString(R.string.nearest_address_string) + 
							" " + mNearestAddress);
					
					// Speak location information.
					helpDirections();
					
				// There are multiple Addresses in the list, so open the 
				//   PossibleAddresses Activity.
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
	
	/**
	 * Callback method that is called when the PossibleAddresses Activity 
	 * returns.  The PossibleAddresses Activity will return both a Address
	 * String extra containing the most relevant address selected by the user.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		
		// If request was canceled, do nothing.
		if (resultCode == RESULT_CANCELED) {
			cancelScreen();
			return;
		}
		
		switch(requestCode) {
		// Address selected in PossibleAddresses screen.
		case REQUEST_CODE_POSSIBLE_ADDRESSES:
			// Get the latitude and longitude values returned from PossibleAddresses.
			int index = intent.getIntExtra(INDEX_EXTRA, -1);
			// Ensure the extra was passed.
			if(index < 0) {
				if (D) Log.e(TAG, getString(R.string.address_index_extra_fail));
				cancelScreen();
				return;
			}
			
			// Extract the address.
			Address destination = mPossibleAddresses.get(index);

			// Update the currently displayed destination address.
			mNearestAddress = getAddressString(destination);
			mNearestAddressDisplay.setText(getString(R.string.nearest_address_string) + 
					" " + mNearestAddress);
			
			checkNearestAddress();
			
			// Speak location information.
			helpDirections();
			break;
			
		default:
			if (D) Log.e(TAG, getString(R.string.invalid_request_code_fail)+" "+requestCode);
			cancelScreen();
		}
	}
	
	/** Ensure that the current nearest address is valid.  If not, this screen will be closed. */
	private void checkNearestAddress() {
		if(mNearestAddress == null || mNearestAddress.length() < 4) {
			Log.e(TAG, "Error: current mNearestAddress field is invalid: " + mNearestAddress);
			cancelScreen();
		}
	}
	
	private void speakCurrentLocation() {
		ttsSpeak(mNearestAddressDisplay.getText().toString(), TextToSpeech.QUEUE_FLUSH);
		ttsSpeak(mLatitudeDisplay.getText().toString(), TextToSpeech.QUEUE_ADD);
		ttsSpeak(mLongitudeDisplay.getText().toString(), TextToSpeech.QUEUE_ADD);
		ttsSpeak(mDistanceDisplay.getText().toString(), TextToSpeech.QUEUE_ADD);
	}
	
	@Override
	protected boolean doubleTap() {
		checkNearestAddress();
		
		// Save the destination in the database.
		if(mNearestAddress != null){
			ttsSpeak(getString(R.string.tts_add)+ " " + mNearestAddress, TextToSpeech.QUEUE_FLUSH);
		} else {
			ttsSpeak(getString(R.string.tts_fail_add_cur_loc), TextToSpeech.QUEUE_FLUSH);
		}
		mCurrentBookmarkId = addBookmarkRecord(mNearestAddress, mLat, mLon);
		
		// Return result.
		swipeBuzz();
		setResult(RESULT_OK);
		finish();
		
		return true;
	}
	
	@Override
	protected void swipeDown() {
		cancelScreen();
	}
	
	@Override
	protected void swipeLeft() {}
	
	@Override
	protected void swipeRight() {}
	
	@Override
	protected void swipeUp() {}

}
