package firbi.base.util;

import java.util.Comparator;

import android.location.Location;

import com.google.android.maps.GeoPoint;

import firbi.base.com.BusStop;

/** Comparator for bus stops. Compares them bases on
 * distance from the provided location. 
 *
 */
public class BusStopComparator implements Comparator<BusStop>{
	private double mLocLat;
	private double mLocLon;
	public BusStopComparator(GeoPoint location) {
		mLocLat = location.getLatitudeE6()/1E6;
		mLocLon = location.getLongitudeE6()/1E6;
	}
	
	/** 
	 * Compares bus stops based on distance from the location 
	 * provided in the constructor. 
	 */
	public int compare(BusStop stop1, BusStop stop2) {
		GeoPoint p1 = stop1.getLocation();
		GeoPoint p2 = stop2.getLocation();
		double p1Lat = p1.getLatitudeE6()/1E6;
		double p1Lon = p1.getLongitudeE6()/1E6;
		double p2Lat = p2.getLatitudeE6()/1E6;
		double p2Lon = p2.getLongitudeE6()/1E6;
		
		float[] result = new float[1];
		Location.distanceBetween(p1Lat, p1Lon, mLocLat, mLocLon, result);
		double d1 = result[0];
		Location.distanceBetween(p2Lat, p2Lon, mLocLat, mLocLon, result);
		double d2 = result[0];
		
		if (d1 < d2)
			return -1;
		else if (d1 > d2)
			return 1;
		else
			return 0;
	}
}
