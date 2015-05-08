package firbi.base.com;

import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.google.android.maps.GeoPoint;


/*
 * This file was originally from firbi.  The original version can be found at
 * http://firbi.googlecode.com/svn/trunk/firbi_public/src/firbi/base/com/BusStop.java
 * I probably changed parts of it.  However, I haven't gone through and changed the comments,
 * so the javadoc is probably the same.
 */

/**
 * The BusStop class encapsulates data from One Bus Away pertaining to a King County Metro bus stop. This 
 * class should be used to hold information about a specific bus stop as well as a way of searching for more
 * BusStops as opposed to using {@link FirbiCom}. This class is preferred to using {@link FirbiCom} because
 * this class maintains that only one instance of a specific (based on the id) BusStop is being used, and 
 * also because this class provides caching of previous searches which can improve lookup times.
 * 
 * @author Danny Swisher
 * 
 * Except that _I_ might have changed it too.
 *
 */
public class BusStop {
	
	public static final int DIRECTION_N = 0;
	public static final String[] NORTH_STRINGS = {"North", "N"};
	public static final int DIRECTION_E = 1;
	public static final String[] EAST_STRINGS = {"East", "E"};
	public static final int DIRECTION_S = 2;
	public static final String[] SOUTH_STRINGS = {"South", "S"};
	public static final int DIRECTION_W = 3;
	public static final String[] WEST_STRINGS = {"West", "W"};
	public static final int DIRECTION_NE = 4;
	public static final String[] NORTHEAST_STRINGS = {"North East", "NE", "East North", "EN"};
	public static final int DIRECTION_NW = 5;
	public static final String[] NORTHWEST_STRINGS = {"North West", "NW", "West North", "WN"};
	public static final int DIRECTION_SE = 6;
	public static final String[] SOUTHEAST_STRINGS = {"South East", "SE", "East South", "ES"};
	public static final int DIRECTION_SW = 7;
	public static final String[] SOUTHWEST_STRINGS = {"South West", "SW", "West South", "WS"};
	
	/**
	 * A simple method that searches the String array passed as 'a' for the passed in string 'check'. Returns true if
	 * 'check' is found in 'a'. Returns false if 'check' is not found in 'a'. If 'a' is null false is always returned.
	 * 
	 * @param a			the Array to search for check in
	 * @param check		the String to search for in a
	 * @return			true if check is in a, false otherwise, false if a is null
	 */
	protected static boolean containsIgnoreCase(String[] a, String check) {
		if(a!=null){
			for(int i = 0; i < a.length; i++){
				if(check == null && a[i] == null)
					return true;
				if(a[i]!=null && check!=null && a[i].equalsIgnoreCase(check))
					return true;
			}
		}
		return false;
	}
	
	/**
	 * This method parses Strings like "N" or "nOrTh" to a direction constant. As implied by the example it is case insensitive. This method
	 * returns null if the input string cannot be parsed.
	 * 
	 * @param direction	the String to be parsed
	 * @return			the direction constant that most closely fits the passed in string or null if no match could be found
	 */
	public static Integer directionValue(String direction) {
		direction = direction.trim();
		if(containsIgnoreCase(NORTH_STRINGS, direction))
			return Integer.valueOf(DIRECTION_N);
		else if(containsIgnoreCase(EAST_STRINGS, direction))
			return Integer.valueOf(DIRECTION_E);
		else if(containsIgnoreCase(SOUTH_STRINGS, direction))
			return Integer.valueOf(DIRECTION_S);
		else if(containsIgnoreCase(WEST_STRINGS, direction))
			return Integer.valueOf(DIRECTION_W);
		else if(containsIgnoreCase(NORTHEAST_STRINGS, direction))
			return Integer.valueOf(DIRECTION_NE);
		else if(containsIgnoreCase(NORTHWEST_STRINGS, direction))
			return Integer.valueOf(DIRECTION_NW);
		else if(containsIgnoreCase(SOUTHEAST_STRINGS, direction))
			return Integer.valueOf(DIRECTION_SE);
		else if(containsIgnoreCase(SOUTHWEST_STRINGS, direction))
			return Integer.valueOf(DIRECTION_SW);
		else
			return null;
	}
	
	/**
	 * This method takes an int direction as can be found via my_bus_route.getDirection() and converts it into a
	 * human readable string. This method is an overload that calls directionDisplay(int direction, int display) with the
	 * default display = 0.
	 * 
	 * @param direction		a valid direction constant from the BuStop class (see constants like DIRECTION_*)
	 * @return				a human readable String with default format that best represents the direction constant passed in
	 */
	public static String directionDisplay(int direction) {
		return directionDisplay(direction, 0);
	}
	
	/**
	 * This method takes an int direction as can be found via my_bus_route.getDirection() and converts it into a
	 * human readable string.
	 * @param direction		a valid direction constant from the BuStop class (see constants like DIRECTION_*)
	 * @param display		a format id the default is 0
	 * @return				a human readable String with specified format, via the display parameter, that best represents the 
	 * 						direction constant passed in
	 */
	public static String directionDisplay(int direction, int display) {
		String ret = "";
		switch(direction) {
			case DIRECTION_N: ret = NORTH_STRINGS[display%NORTH_STRINGS.length]; break;
			case DIRECTION_E: ret = EAST_STRINGS[display%EAST_STRINGS.length]; break;
			case DIRECTION_S: ret = SOUTH_STRINGS[display%SOUTH_STRINGS.length]; break;
			case DIRECTION_W: ret = WEST_STRINGS[display%WEST_STRINGS.length]; break;
			case DIRECTION_NE: ret = NORTHEAST_STRINGS[display%NORTHEAST_STRINGS.length]; break;
			case DIRECTION_NW: ret = NORTHWEST_STRINGS[display%NORTHWEST_STRINGS.length]; break;
			case DIRECTION_SE: ret = SOUTHEAST_STRINGS[display%SOUTHEAST_STRINGS.length]; break;
			case DIRECTION_SW: ret = SOUTHWEST_STRINGS[display%SOUTHWEST_STRINGS.length]; break;
		}
		return ret;
	}
	
	/**
	 * A cache of all the currently active BusStops indexed by their ids. This Hash should always contain
	 * a BusStop for a given id if that BusStop has been searched for or used.
	 */
	private static HashMap<String, BusStop> CachedStops = new HashMap<String, BusStop>();
	
	/**
	 * Finds all the BusStops within a given radius of the center point.
	 * 
	 * @param center	the center of the circle to return stops from
	 * @param radius	the distance from the center to look for stops to return
	 * @return			all stops that fall within the circle created by center and radius
	 */
	public static BusStop[] find(GeoPoint center, int radius) {
		return FirbiCom.getStops(center, radius);
	}
	
	/**
	 * Finds all the BusStops with an address similar or near to the one passed as "address"
	 * 
	 * @param address	the address to search for bus stops at or near
	 * @return			all BusStops that are at or near this address
	 */
	public static BusStop[] findByAddress(String address) {
		return null;//FirbiCom.getStops(address);
	}
	
	/**
	 * Finds all BusStops that have the passed in stop number. It is possible for their to be multiple BusStops
	 * with the same stop number because different agencies bus stop numbers may overlap.
	 * 
	 * @param stopNumber	the bus stop number designating a stop usually found at the physical stop
	 * @return				all busses with this bus stop number
	 */
	public static BusStop[] findByNumber(String stopNumber, GeoPoint near) {
		return FirbiCom.getStops(stopNumber, near);
	}
	
	/**
	 * Finds the specific BusStop with the passed in id, there can be only one because id must encapsulate both
	 * agency and stop id.
	 * 
	 * @param id	the unique id that encapsulates both agency and route ids
	 * @return		the unique BusStop with the passed in id
	 */
	public static BusStop find(String id) {
		BusStop stop;
		if(CachedStops.containsKey(id)){
			stop = CachedStops.get(id);
		}
		else{
			stop = FirbiCom.getStop(id);
		}
		return stop;
	}
	
	/**
	 * This method will create a new BusStop and store it in the cache if no BusStop with the passed in
	 * id already exists or it will get the BusStop with the passed id from the cache and update whatever
	 * other parameters you provide (currently only support for location and address).
	 * 
	 * @param id		the unique id that encapsulates both agency and route ids
	 * @param location	the physical location of the bus stop represented as a GeoPoint
	 * @param address	the address that best describes the location of this bus stop
	 * @return			the only instance of BusStop with the passed id with the passed values 
	 * 					updated to those that were passed in
	 */
	public static BusStop updateOrCreate(String id, String stopNumber, GeoPoint location, String address, Integer direction) {
		if(id==null)
			return null;
		BusStop stop;
		if(CachedStops.containsKey(id)){
			stop = CachedStops.get(id);
			if(location!=null)
				stop.setLocation(location);
			if(address!=null)
				stop.setAddress(address);
			if(direction!=null)
				stop.setDirection(direction.intValue());
			return stop;
		}
		else{
			stop = new BusStop(id, stopNumber, location, address, direction);
			CachedStops.put(id, stop);
		}
		return stop;
	}
	
	/**
	 * This method is meant to work the same as other updateOrCreate methods but instead of asking for parameters explicitly it allows
	 * you to pass in a map of attribute names to attributes as listed below:
	 * key			necessary type				necessary value
	 * id			Integer						the id of the new or to be updated BusStop (cannot be null)
	 * location		GeoPoint					null OR the location of this BusStop
	 * address		String						null OR the address of this BusStop
	 * direction	Integer						null OR an Integer that corresponds to one of the DIRECTION constants in the BusStop class
	 * @param 	attributes a hash with keys and values as described above
	 * @return 	a BusStop either new with the values passed or a BusStop with the same id as the one passed in and with its attributes 
	 * 			updated to the ones passed.
	 */
	public static BusStop updateOrCreate(Map attributes) {
		String id = null;
		String stopNumber = null;
		GeoPoint location = null;
		String address = null;
		Integer direction = null;
		if(attributes.containsKey("id")){
			Object check_type = attributes.get("id");
			if(check_type instanceof String)
				id = ((String)check_type);
			else{
				//Invalid id type error
			}
		}
		else{
			return null;
		}
		if(attributes.containsKey("stopNumber")){
			stopNumber = ((String)attributes.get("stopNumber"));
		}
		if(attributes.containsKey("location")){
			location = ((GeoPoint)attributes.get("location"));
		}
		if(attributes.containsKey("address")){
			address = ((String)attributes.get("address"));
		}
		if(attributes.containsKey("direction")){
			direction = ((Integer)attributes.get("direction"));
		}
		return updateOrCreate(id, stopNumber, location, address, direction);
	}
	
	/**
	 * This method helps to split a BusStops id into it's two main parts the agency id and route id. These
	 * are stored in the BusStops id as the left-most four digits form the agency id and the right-most five
	 * form the route id.
	 * 
	 * @param id	the unique id that encapsulates both agency and route ids
	 * @return		an array where array[0] = the agency id and array[1] = the stop id
	 */
	public static String[] splitIntoStopAndAgencyId(String id) {
		String[] split = id.split("_");
		return split;
	}
	
	/**
	 * a unique id that encapsulates both agency and route ids. The first several digits are the agency id
	 * and the rest are the stop id.
	 */
	private String mId;
	
	private String mStopNumber = null;
	/**
	 * the coordinates of this BusStop's physical location
	 */
	private GeoPoint mLocation = null;
	/**
	 * the address that best describes the location of this BusStop
	 */
	private String mAddress = null;
	/**
	 * the direction that buses leave this bus stop
	 */
	private Integer mDirection = null;
	
	/**
	 * Constructor used to create this object.  Responsible for setting the mId, mLocation and mAddress. It has
	 * been made protected to ensure that only the static updateOrCreate(int id, GeoPoint location, String address)
	 * method is used to ensure that only one BusStop with a given unique id or mId exists.
	 * 
	 * @param id		the unique id that encapsulates both agency and route ids
	 * @param location	the coordinates of this BusStop's physical location
	 * @param address	the address that best describes the location of this BusStop
	 * @param			the direction that buses leave this BusStop
	 */
	protected BusStop(String id, String stopNumber, GeoPoint location, String address, Integer direction) {
		mId = id;
		mStopNumber = stopNumber;
		mLocation = ((location!=null)? new GeoPoint(location.getLatitudeE6(), location.getLongitudeE6()) : null);
		mAddress = address;
		mDirection = direction;
	}
	
	/**
	 * This method returns the id of this BusStop.
	 * @return	the unique id that encapsulates both agency and route ids
	 */
	public String getId() {
		return mId;
	}
	
	/**
	 * This method returns the stop number of this stop which is unique for each stop of a given agency, but is not
	 * unique across agencies. For a truly unique id use getId().
	 * @return	the stop number of this BusStop
	 */
	public String getStopNumber() {
		return mStopNumber;
	}
	
	/**
	 * This method returns the agency id of this stop. Every stop is owned by a particular agency, that is
	 * every stop is controlled and serviced by a different agency (ex King County Metro, Pierrce County Metro, ...)
	 * this id is the One Bus Away id that identifies the agency.
	 * @return	the agency id of this BusStop
	 */
	public String getAgencyId() {
		String[] tmp = splitIntoStopAndAgencyId(mId);
		return tmp[0];
	}
	
	/**
	 * This method returns the physical location in coordinates of this BusStop.
	 * @return	a GeoPoint representing this BusStop's physical location
	 */
	public GeoPoint getLocation() {
		if(mLocation!=null)
			return new GeoPoint(mLocation.getLatitudeE6(), mLocation.getLongitudeE6());
		else
			return null;
	}
	
	/**
	 * This method returns the closest address for this BusStop.
	 * @return	the address that best describes the location of this BusStop
	 */
	public String getAddress() {
		return mAddress;
	}
	
	/**
	 * This method returns the direction for this BusStop.
	 * @return	an Integer representing this BusStop's direction
	 */
	public Integer getDirection() {
		return mDirection;
	}
	
	
	/**
	 * Returns all stops within a passed radius of this BusStop's location.
	 * @param radius	the distance a bus can be from this BusStop and still be returned by this method
	 * @return			all BusStops within the given radius with this BusStop's location at the center
	 */
	public BusStop[] getNearbyStops(int radius) {
		return FirbiCom.getStops(mLocation, radius);
	}
	
	/**
	 * Returns a schedule of busses that will soon arrive at this BusStop. Note: Support for a maximum or minimum 
	 * time to include in results is possible if requested.
	 * @return	all busses that will arrive at this BusStop soon
	 */
	public Bus[] getUpcomingArrivals() {
		// return FirbiCom.getStopSchedule(this);
		// You know, I actually feel like sorting this first
		Bus[] arrivals = FirbiCom.getStopSchedule(this);
		Comparator<Bus> comp = new Comparator<Bus>() {

			public int compare(Bus arg0, Bus arg1) {
				// only works because I changed getPredictedTime()...
				Calendar c0, c1;
				if(arg0 == null) // not sure whether this will help at all
								// but it seems like I keep getting NullPointerExceptions
					// in java.util.TimSort.sort line 169, which might use this for
					// all I know.
					// 
					// The problem is that comparators ought to work the same as
					// compareTo, which always throws an error when
					c0 = null;
				else
					c0 = arg0.getPredictedTime();
				if(arg1 == null)
					c1 = null;
				else
					c1 = arg1.getPredictedTime();
				if(c0 == null) {
					if(c1 == null)
						return 0;
					return 1; // we'll put nulls at the end
				}
				if(c1 == null)
					return -1;
				return c0.compareTo(c1);
			}
			
		};
		Arrays.sort(arrivals, comp);
		return arrivals;
	}
	
	/**
	 * @return a human readable String of this BusStop
	 */
	public String toString() {
		return "BusStop{id: "+getId()+", location: "+( (mLocation==null)? "null" : "{lat: "+mLocation.getLatitudeE6()+", long: "+mLocation.getLongitudeE6()+"}" )+", address: "+((mAddress==null)? "null" : mAddress)+", direction: "+((mDirection==null)? "null" : directionDisplay(mDirection))+"}";
	}
	/**
	 * This method sets the coordinates of this BusStop's physical location and should be used with caution,
	 * use of the constructor or updateOrCreate is preferred but once a BusStop is created there should be little
	 * need to ever change it's location.
	 * @param 	location	the GeoPoint to change this BusStop's location to which stores the coordinates that 
	 * 			represent the physical location of this BusStop
	 */
	protected void setLocation(GeoPoint location) {
		if(location!=null)
			mLocation = new GeoPoint(location.getLatitudeE6(), location.getLongitudeE6());
	}
	
	 /**
	  * This method sets the address of this BusStop
	  * @param 	address	the address to change this BusStop's address to, that should best describe the 
	  * 		location of this BusStop
	  */
	protected void setAddress(String address) {
		if(address!=null)
			mAddress = address;
	}
	
	/**
	 * This method sets the direction that Busses leave this BusStop in.
	 * @param direction the direction busses will leave this BusStop
	 */
	protected void setDirection(Integer direction) {
		if(direction!=null)
			mDirection = Integer.valueOf(direction);
	}
	
}
