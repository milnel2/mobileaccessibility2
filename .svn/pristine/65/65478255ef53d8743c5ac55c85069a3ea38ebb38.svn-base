package cs.washington.mobileaccessibility.locationorienter;

import java.util.*;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.*;
import android.os.Bundle;
import android.util.Log;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/**
 * This code was taken from Orienter and slightly modified
 * (http://code.google.com/p/mobileaccessibility/source/browse/trunk/Orienter/)
 * The main changes are the methods at the bottom of the file, 
 * mostly related to giving back well formated information
 * 
 * LocationFinder will keep track of your location and orientation 
 * (either from gps heading or compass if not available) and provides
 * pretty printing of that information in getInfo()
 */
public class LocationFinder implements SensorEventListener {
	
	private static final String TAG = "Locator";

	/**
	 * Get location updates as frequently as possible to give user
	 * the best possible location.
	 * 
	 * XXX  Currently these values are 0 for testing purposes
	 * TODO Tune these numbers to balance between best location
	 *              and battery performance.
	 */
	private static final long TIME_BETWEEN_FIXES = 0;
	private static final float DIST_BETWEEN_FIXES = 0;

	private Context mContext;
	private LocationManager mLocationManager;
	private Geocoder mGeocoder;
	private Location mLocation;
	private Address mAddress;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Float mCompassHeading;
    private static Map<String, String> mStreetNames;
    
	private LocationListener mLocationListener = new LocationListener() {
		private boolean mGpsEnabled;

        /**
         * Record the new location
         */
		public void onLocationChanged(Location location) {                      
			if(location.getProvider() == LocationManager.GPS_PROVIDER || !mGpsEnabled) {
				Log.d(TAG, "onLocationChanged: " + location);

				mLocation = location;
				try {
					List<Address> address = mGeocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
					mAddress = address.get(0);
				} catch(Exception e) {
					Log.e(TAG, "getCurrentAddress: ", e);
				}
			}
		}

        /**
         * Record that gps is not enabled
         */
		public void onProviderDisabled(String provider) {
			Log.d(TAG, "onProviderDisabled: " + provider);
			if(provider == LocationManager.GPS_PROVIDER)
				mGpsEnabled = false;            
		}

        /**
         * Record that gps is enabled
         */
		public void onProviderEnabled(String provider) {
			Log.d(TAG, "onProviderEnabled: " + provider);
			if(provider == LocationManager.GPS_PROVIDER)
				mGpsEnabled = true;
		}

        /**
         * Log the status change
         */
		public void onStatusChanged(String provider, int status, Bundle extras) {
			StringBuffer logMessage = new StringBuffer("onStatusChanged: ");
			logMessage.append(provider);

			switch (status) {
			case LocationProvider.AVAILABLE:
				logMessage.append(" is now available.");
				logMessage.append("\tUsing ");
				logMessage.append(extras.getInt("satellites")).append(" satellites.");
				break;
			case LocationProvider.OUT_OF_SERVICE:
				logMessage.append(" is out of service.");
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				logMessage.append(" is temporarily unavailable.");
				break;
			}

			Log.d(TAG, logMessage.toString());
		}
	};


	private GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {

        /**
         * Log the new GPS status
         */
		public void onGpsStatusChanged(int event) {
			StringBuffer message = new StringBuffer("onGpsStatusChanged: ");
			switch(event){
			case GpsStatus.GPS_EVENT_STARTED:
				message.append("gps started");
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				message.append("gps stopped");
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				message.append("first fix");
				break;
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				message.append("satellite update");
				break;
			}

			Log.i(TAG, message.toString());
			GpsStatus status = mLocationManager.getGpsStatus(null);
			for(GpsSatellite satellite: status.getSatellites()) {
				String smessage = satellite.getAzimuth() + " " + satellite.getElevation() + " " + satellite.getPrn() + " " + satellite.getSnr();
				Log.i(TAG, smessage);
			}
		}
	};

    /**
     * Constructs a new LocationFinder object with the given context
     */
	public LocationFinder(Context context) {
		mContext = context;
		mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		mGeocoder = new Geocoder(mContext);

		mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(mLocation == null) {
			mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		}
		mAddress = null;
		
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        
        mStreetNames = new HashMap<String, String>();
        mStreetNames.put("ln", "lane ");
        mStreetNames.put("st", "street ");
        mStreetNames.put("ave", "avenue ");
        mStreetNames.put("ct", "court ");
	}

	/**
	 * This must be called once before the first call the either getCurrentLocation() or getCurrentAddress()
	 */
	public void startLocationUpdates() {
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, TIME_BETWEEN_FIXES, DIST_BETWEEN_FIXES, mLocationListener);
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, TIME_BETWEEN_FIXES, DIST_BETWEEN_FIXES, mLocationListener);
		mLocationManager.addGpsStatusListener(mGpsStatusListener);
	}

	/**
	 * This must be called once after the final call the either getCurrentLocation() or getCurrentAddress()
	 */
	public void stopLocationUpdates() {
		mLocationManager.removeUpdates(mLocationListener);
		mLocationManager.removeGpsStatusListener(mGpsStatusListener);
	}

	/**
	 * Returns the last known location.
	 * 
	 * @return Location
	 */
	public Location getCurrentLocation() {
		return new Location(mLocation);
	}

	/**
	 * Returns the address of the last known location.
	 * 
	 * @return Address
	 */
	public Address getCurrentAddress() {
		if(mAddress == null) {
			try {
				Log.d("LF Address", "Lat: " + mLocation.getLatitude() + " Long: " + mLocation.getLongitude());
				List<Address> address = mGeocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
				mAddress = address.get(0);
			} catch(Exception e) {
				Log.e(TAG, "getCurrentAddress: ", e);
			}
		}
		
		return mAddress;
	}
	
    /**
     * Records the new compass heading
     */
	public void onSensorChanged(SensorEvent event) {
		if(mSensor.equals(event.sensor)){
            float[] values = event.values;
            float heading = values[0];
            Log.d(TAG, "onSensorChanged: (" + heading + ", " + values[1] + ", " + values[2] + ")");
            mCompassHeading = heading;
		}
	}

    /**
     * Logs the new accuracy
     */
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		Log.d("OnAccuracyChanged", "New accuracy is: " + accuracy);
	}
	
	/**
	 * Returns a pretty string with basic address/accuracy/bearing info
	 * or null if we're not ready
     * 
     * @return String
	 */
	public String getInfo(boolean spoken, boolean verbose){
		if(mLocation == null || mAddress == null)
			return null;
		
        Log.d("LO", mLocation.toString());
        Log.d("LO", mAddress.toString());
        
        String info;
        if(!spoken){
	        info = "Address: " + mAddress.getAddressLine(0);
	        if(mLocation.hasAccuracy())
	        	info += " +- " + (int) mLocation.getAccuracy() + " meters.";
	        if(mLocation.hasBearing()){
	        	info += " GPS heading: " + getHeading(mLocation.getBearing(), true);
	        }else if(mCompassHeading != null){
	        	info += " Compass heading: " + getHeading(mCompassHeading, true);
	        }
        }else{
        	info = "Your current location is " + LocationFinder.parse(mAddress.getAddressLine(0));
	        if(mLocation.hasAccuracy() && !verbose)
	        	info += "plus or minus " + (int) mLocation.getAccuracy() + " meters ";
	        if(mLocation.hasBearing()){
	        	info += "with a GPS heading of " + getHeading(mLocation.getBearing(), false);
	        }else if(mCompassHeading != null){
	        	info += "with a compass heading of " + getHeading(mCompassHeading, false);
	        }
        }
        
        return info;
        
	}

    /**
     * Returns a new version of the given string for better TTS pronounciation
     *
     * @return String
     */
	public static String parse(String addr){
		Scanner s = new Scanner(addr);
		String newAddr = "";
		while(s.hasNext()){
			if(s.hasNextInt()){
				int a = s.nextInt(); 
				String alpha = "";
				while(a > 0){
					alpha = (a%10) + " " + alpha;
					a = a/10;
				}
				newAddr += " " + alpha;
			}else{
				String p = s.next();
				if(mStreetNames.containsKey(p.toLowerCase()))
					newAddr += mStreetNames.get(p.toLowerCase());
				else
					newAddr += p + " ";
			}
		}
		
		return newAddr;
	}

	/**
	 * Returns an abbreviated alphabetic String representing the current 
     * heading (ie N, SW, E, etc) or an empty string if heading is not available
     *
     * @return String
	 */
	public String getHeading(){
		String heading = "";
        if(mLocation.hasBearing()){
        	heading = getHeading(mLocation.getBearing(), true);
        }else if(mCompassHeading != null){
        	heading = getHeading(mCompassHeading, true);
        }
        return heading;
	}
	
    /**
     * Returns a String representing the given heading, abbreviated if 
     * abbr is true (ie "N", "SW", "E", etc or unabbreviated if false
     * (ie "North", "SouthWest", "East", etc)
     */
	private String getHeading(float heading, boolean abbr) {
		String[] directions;
		if(abbr)
			directions = new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
		else
			directions = new String[]{"North", "Northeast", "East", "Southeast", "South", "Southwest", "West", "Northwest"};			
		return directions[((int) (((heading + 90) * 2 + 45) / 90)) % 8];
	}
}

