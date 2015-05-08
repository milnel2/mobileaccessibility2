package cs.washington.mobileaccessibility.location;

import java.util.List;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.util.Log;

public class Locator {

	private static final String TAG = "Locator";
	
	/**
	 * Get location updates as frequently as possible to give user
	 * the best possible location.
	 * 
	 * XXX  Currently these values are 0 for testing purposes
	 * TODO Tune these numbers to balance between best location
	 * 		and battery performance.
	 */
	private static final long TIME_BETWEEN_FIXES = 0;
	private static final float DIST_BETWEEN_FIXES = 0;
	
	private Context mContext;
	private LocationManager mLocationManager;
    private Geocoder mGeocoder;
    private Location mLocation;
    private Address mAddress;
    
    private LocationListener mLocationListener = new LocationListener() {
    	private boolean mGpsEnabled;

    	
		@Override
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

		@Override
		public void onProviderDisabled(String provider) {
			Log.d(TAG, "onProviderDisabled: " + provider);

			
			if(provider == LocationManager.GPS_PROVIDER)
				mGpsEnabled = false;		
		}

		@Override
		public void onProviderEnabled(String provider) {
			Log.d(TAG, "onProviderEnabled: " + provider);

			
			if(provider == LocationManager.GPS_PROVIDER)
				mGpsEnabled = true;
		}

		@Override
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

		@Override
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
	
    
    public Locator(Context context) {
    	mContext = context;
    	mLocationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        mGeocoder = new Geocoder(mContext);
        
        mLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if(mLocation == null) {
        	mLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        }
        mAddress = null;
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
				List<Address> address = mGeocoder.getFromLocation(mLocation.getLatitude(), mLocation.getLongitude(), 1);
				mAddress = address.get(0);
			} catch(Exception e) {
				Log.e(TAG, "getCurrentAddress: ", e);
			}
    	}
		
		return mAddress;
    }
}
