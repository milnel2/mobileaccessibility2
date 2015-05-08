package cs.washington.mobileaccessibility.onebusaway;

import com.google.android.maps.GeoPoint;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

/**
 * This class is a facade for the GPS API
 * It's used only by MainActivity, and is geared entirely
 * towards MainActivity, but it seems like it should work fine
 * with other programs too.
 * 
 * This class doubles as the LocationListener, too
 * 
 * @author Will
 *
 */
public class GeoFacade implements LocationListener {

	// TODO: switch to Skyhook XPS, which Joe Muhm figured out how to do
	// (see http://code.google.com/p/mobileaccessibility/source/browse/#svn/trunk/SkyhookLocationDemo)
	private LocationManager locManager;
	private Location lastLocation; // the last location we knew about
	private static final String LOG_TAG = "MobileBusInfo";
	
	// The constructor, called during MainActivity.onCreate
	// We need and Activity so that we can call getSystemService
	public GeoFacade(Activity activity) {
        lastLocation = null;
        locManager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
        
        
        onResume();
        // won't this function be called twice?  onResume is always called after
        // onCreate()
        // 
        // I think I based this off of what firbi does, or maybe what the
        // Android documentation says
        

	}
	
	// This called in MainActivity.onResume
	// It just starts the request for location updates
	public void onResume() {
    	
        // TODO check whether this is necessary
        // [It's possible that this isn't necessary, because locManager has a method
        // getLastKnownLocation() that might be what we want, without using a mechanism
        // of listeners...]
		locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 10, this);
		// TODO that's probably going to be wasting the battery
        // online documentation suggests the timeout should be 10 x that much, i.e., 60000
        // i.e., once every minute
        // it's possible that firbi people missed a zero

	}
	
	// Stop receiving updates.
	// (called during MainActivity.onPause())
	public void onPause() {
		locManager.removeUpdates(this);
	}
	
	// Stop receiving updates
	// (called during MainActivity.onDestroy())
	// Again, this seems redundant!
	public void onDestroy() {
		locManager.removeUpdates(this);
	}
	
	// Called by MainActivity when someone wants to know where we are
	public GeoPoint getLocation() {
		// TODO be more sophisticated and check whether the
		// location is reasonable, or "valid" whatever that means
		if(lastLocation == null) // this seems sketchy TODO
			lastLocation = locManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if(lastLocation == null)
			return null;
		GeoPoint point = new GeoPoint((int)(lastLocation.getLatitude()*1e6),(int)(lastLocation.getLongitude()*1e6));
		return point;
		
	}
	
	// The main callback function that we implement to be a locationListener
	public void onLocationChanged(Location location) {
		Log.w(LOG_TAG,"onLocationChanged()");
		lastLocation = location;
		
	}

	// a miscellaneous callback function (part of LocationListener) which we ignore
	public void onProviderDisabled(String provider) {
		Log.w(LOG_TAG,"onProviderDisabled provider=" + provider);
	}

	// a miscellaneous callback function (part of LocationListener) which we ignore
	public void onProviderEnabled(String provider) {
		Log.w(LOG_TAG,"onProviderEnabled provider=" + provider);
	}

	// a miscellaneous callback function (part of LocationListener) which we ignore
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// apparently we don't need to do anything?
		Log.w(LOG_TAG,"onStatusChanged provider=" + provider + " status=" + status);
	}

}
