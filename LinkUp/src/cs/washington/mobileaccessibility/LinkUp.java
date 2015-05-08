package cs.washington.mobileaccessibility;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.geonames.Address;
import org.geonames.WebService;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.widget.TextView;
import android.widget.Toast;

import com.google.tts.TTS;

@SuppressWarnings("deprecation")
public class LinkUp extends GestureUI {
	
	/** 
	 * Global Variables 
	 **/
	private TTS myTts; 				// Text to Speech
	private TextView screenText; 	// Main Screen's Text
	private Address currentAddress;	// Current Address object
	private boolean locationObtained; // true: current location grabbed, false: still intro
	
	/** 
	 * GPS Variables 
	 **/
	protected static LocationManager locationM = null;
	protected static Location loc = null;
	protected static double longitude;
	protected static double latitude;
	protected static String provider;
	
    /** 
     * Called when the activity is first created. 
     **/
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		// Set up the GPS Location Manager to get coordinates
		locationM = (LocationManager) getSystemService(Context.LOCATION_SERVICE);   // Sets up  a connection to GPS hardware
		loc = locationM.getLastKnownLocation(LocationManager.GPS_PROVIDER);                         // grabs the location information from GPS
		if (loc != null) {
			longitude = loc.getLongitude();
			latitude = loc.getLatitude();
		}
        
		myTts = new TTS(this, ttsInitListener, true);

		screenText = (TextView) findViewById(R.id.screenContent);
		screenText.setText(R.string.tapLocation);
		
		// Must be instantiated to detect motions
		gestureScanner = new GestureDetector(this);
    }
    
    
	/** 
	 * LinkUp (this) intent is paused. 
	 **/
	@Override
	public void onPause() {
		// Specifically shut off the GPS for now.
		locationM.removeUpdates(locationListener);
		super.onPause();
	}

	
	/** 
	 * LinkUp (this) intent resumes. 
	 **/
	@Override
	public void onResume() {
		locationM.requestLocationUpdates(LocationManager.GPS_PROVIDER,100, 1, locationListener);
		updateWithNewLocation(loc);
		//loadMainScreen();
		super.onResume();
		
		myTts.speak("Tap the screen for Current Location.  " +
				"At any time, scrolling up will bring you back to this screen. " +
				"Scrolling left will pull up a list of nearby friends.", 0, null);
	}
    
	
	/** 
	 * LinkUp (this) intent ends. 
	 **/
	public void onDestroy() {
		super.onDestroy();
	}

    
	/** 
	 * Initialize and setup the Text to Speech 
	 **/
	private TTS.InitListener ttsInitListener = new TTS.InitListener() {

		public void onInit(int version) {
			myTts.speak("Welcome to Link Up! Tap the screen for Current Location.  " +
					"At any time, scrolling up will bring you back to this screen. " +
					"Scrolling left will pull up a list of nearby friends.", 0, null);	
		}
	};

	/** 
	 * Listen for changes in location and call appropriate methods to update location 
	 **/
	private final LocationListener locationListener = new LocationListener() {
		public void onLocationChanged(Location location) {
			updateWithNewLocation(location);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			
		}
	};
	
	/**
	 * Update the current variables tracking the longitude and latitude
	 * @param l
	 */
	private void updateWithNewLocation(Location l) {
		latitude = l.getLatitude();
		longitude = l.getLongitude();
		provider = l.getProvider();
	}
	
	/**
	 * Definition of an Up Swipe for LinkUp Main screen. 
	 * Will Start the Retrieve Locations Intent
	 */
	@Override
	protected void onUpFling() {
		Intent i = new Intent(this, RetrieveLocations.class);
		startActivity(i);
	}
	
	/**
	 * Definition of an Down Swipe for LinkUp Main screen. 
	 * Will start the Save Location Intent if there is currently a location that has been reverse Geocoded.
	 */
	@Override
	protected void onDownFling() {
		if (locationObtained) {	
			Intent j = new Intent(this, SaveALocation.class);
			j.putExtra("longitude", longitude);
			j.putExtra("latitude", latitude);
			j.putExtra("address", currentAddress.getStreetNumber() + " " + currentAddress.getStreet());
			startActivity(j);
		} else {
			myTts.speak("Please tap for location first before trying to save.", 0, null);
		}
	}
	
	/**
	 * Definition of a Right Swipe for LinkUp Main screen. 
	 * Will post the current location to a database that friends can pull from.
	 */
	@Override
	protected void onRightFling() {
		if (locationObtained) {
			PhpScriptAccess conn = new PhpScriptAccess("http://students.washington.edu/kwanste/friends.php");
	        // Add your data   
	        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(4);
	        // not sending our fake ID
	        nameValuePairs.add(new BasicNameValuePair("alias", "AndroidUser11 "));
	        nameValuePairs.add(new BasicNameValuePair("latitude", Double.toString(latitude)));
	        nameValuePairs.add(new BasicNameValuePair("longitude", Double.toString(longitude)));
	        nameValuePairs.add(new BasicNameValuePair("phonenumber", "2062637643"));
	        try {
				conn.postQuery(nameValuePairs);
				myTts.speak("Your location has been updated to your friends.", 0, null);
				Toast.makeText(this, "Post was successful.", Toast.LENGTH_LONG);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Definition of a Left Swipe for LinkUp Main screen. 
	 * Will start the GetFriends Intent
	 */
	@Override
	protected void onLeftFling() {
		Intent k = new Intent(this, GetFriends.class);
		k.putExtra("longitude", longitude);
		k.putExtra("latitude", latitude);
		startActivity(k);
	}
	
	/**
	 * Definition of a tap for LinkUp Main screen. 
	 * Get the current location and reverse geocode.
	 */
	@Override
	protected void onTap() {
		Log.d("Tap ", "FROM THE MAIN SCREEN: Single Tap");
		
		/** Reverse Geocode Command through the Geonames API **/
		try {
			// Note: Webservice needs the jdom-1.0.jar library
			// jdom, used to parse the xml web service result. 
			currentAddress = WebService.findNearestAddress(latitude, longitude);
			
			screenText.setText("Current Location: \n\n" + 
					currentAddress.getStreetNumber() + " " + 
					currentAddress.getStreet());
			locationObtained = true;
			
		} catch (Exception ex) {
			// No information was found, handle this.
			screenText.setText("No information was found for: \nLongitude: " + 
					longitude + "\nLatitude: " + 
					latitude);
		}

	}
}