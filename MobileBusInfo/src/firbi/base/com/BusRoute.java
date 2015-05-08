package firbi.base.com;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.google.android.maps.GeoPoint;


/*
 * This file was originally from firbi.  The original version can be found at
 * http://firbi.googlecode.com/svn/trunk/firbi_public/src/firbi/base/com/BusRoute.java
 * I probably changed parts of it.  However, I haven't gone through and changed the comments,
 * so the javadoc is probably the same.
 */

/**
 * The BusRoute class encapsulates data from One Bus Away pertaining to a King County Metro bus route. This 
 * class should be used to hold information about a specific bus route as well as a way of searching for more
 * BusRoutes as opposed to using {@link FirbiCom}. This class is preferred to using {@link FirbiCom} because
 * this class maintains that only one instance of a specific (based on the id) BusRoute is being used, and 
 * also because this class provides caching of previous searches which can improve lookup times.
 * 
 * @author Danny Swisher
 *
 * As usual, willij6 may have modified this file in some way.
 */
public class BusRoute {
	
	private static HashMap<String, BusRoute> CachedRoutes = new HashMap<String, BusRoute>();
	//polyline and getpolylines need to be added
	
	/**
	 * Because a route number is not unique enough to find a single BusRoute this method will use the location
	 * passed as 'near' to guess which BusRoutes you really want. So for example if you ask for BusRoutes with
	 * routeNumber 71 and near some location in Seattle this method will only return you BusRoutes with agency
	 * ids from King County Metro as opposed to 71 BusRoutes in Pierce county or some other area.
	 * 
	 * @param routeNumber 	the user friendly route number ex. "71" "67E"
	 * @param near 			a location that is near where the route you are searching for operates
	 * @return				an array of BusRoutes who all have the routeNumber specified by the 'routeNumber'
	 *  					parameter and who all operate near the specified 'near' location
	 */
	public static BusRoute[] find(String routeNumber, GeoPoint near) {
		return FirbiCom.getRoutes(routeNumber, near);
	}
	
	/**
	 * Finds the BusRoute with the unique id passed as 'id'.
	 * 
	 * @param id 	the unique id that encapsulates both agency and route ids
	 * @return 		the unique BusRoute for this id and the only one in existence with this id
	 */
	public static BusRoute find(String id) {
		BusRoute route;
		if(CachedRoutes.containsKey(id)){
			route = CachedRoutes.get(id);
		}
		else{
			route = FirbiCom.getRoute(id);
		}
		return route;
	}
	
	/**
	 * This method will create a new BusRoute and store it in the cache if no BusRoute with the passed in
	 * id already exists or it will get the BusRoute with the passed id from the cache and update whatever
	 * other parameters you provide (currently only support for routeNumber).
	 * 
	 * @param id			the unique id that encapsulates both agency and route ids
	 * @param routeNumber	the user friendly route number ex. "71" "67E"
	 * @return				the only BusRoute in existence with the passed in id, with the routeNumber as passed
	 * 						in either by updating it or leaving it as it was
	 */
	public static BusRoute updateOrCreate(String id, String routeNumber) {
		if(id==null)
			return null;
		BusRoute route;
		if(CachedRoutes.containsKey(id)){
			route = CachedRoutes.get(id);
			route.setRouteNumber(routeNumber);
			return route;
		}
		else{
			route = new BusRoute(id, routeNumber);
			CachedRoutes.put(id, route);
		}
		return route;
	}
	
	public static BusRoute updateOrCreate(Map attributes) {
		String id = null;
		String routeNumber = null;
		if(attributes.containsKey("id")){
			Object check_type = attributes.get("id");
			if(check_type instanceof String)
				id = ((String)check_type);
			else{
				return null;
			}
		}
		if(attributes.containsKey("routeNumber")){
			routeNumber = ((String)attributes.get("routeNumber"));
		}
		return updateOrCreate(id, routeNumber);
	}
	
	/**
	 * This method helps to split a BusRoutes id into it's two main parts the agency id and route id. These
	 * are stored in the BusRoutes id as the left-most five digits form the agency id and the right-most five
	 * form the route id.
	 * 
	 * @param id 	the unique id that encapsulates both agency and route ids
	 * @return		an array where array[0] = the agency id and array[1] = the route id
	 */
	public static String[] splitIntoRouteAndAgencyId(String id) {
		String[] split = id.split("_");
		return split;
	}
	
	/**
	 * a unique id that encapsulates both agency and route ids
	 */
	private String mId;
	
	/**
	 * a user friendly route number ex. "71" "67E"
	 */
	private String mRouteNumber;
	
	private Map<String[], BusStop[]> mDestinationStopGroupings = null;
	
	/**
	 * Constructor used to create this object.  Responsible for setting the mId and mRouteNumber. It has
	 * been made protected to ensure that only the static updateOrCreate(int id, String routeNumber) method
	 * is used to ensure that only one BusRoute with a given unique id or mId exists.
	 * 
	 * @param id			a unique id that encapsulates both agency and route ids
	 * @param routeNumber	a user friendly route number ex. "71" "67E"
	 */
	protected BusRoute(String id, String routeNumber) {
		mId = id;
		setRouteNumber(routeNumber);
	}
	
	/**
	 * Returns the id of this BusRoute which is set on creation and never changed.
	 * @return	the unique id that encapsulates both agency and route ids
	 */
	public String getId() {
		return mId;
	}
	
	public String getAgencyId() {
		String[] tmp = splitIntoRouteAndAgencyId(mId);
		return tmp[0];
	}
	
	/**
	 * Returns the route number of this BusRoute which can be changed with setRouteNumber 
	 * @return	the user friendly route number ex. "71" "67E"
	 */
	public String getRouteNumber() {
		return mRouteNumber;
	}
	
	/**
	 * Uses FirbiCom to get all the BusStops which this BusRoute visits.
	 * @return	an array of BusStops that this BusRoute visits
	 */
	public BusStop[] getAllStops() {
		Map<String[], BusStop[]> groups = getDirectionKeyedStopsServedByRoute();
		ArrayList<BusStop> allstops = new ArrayList<BusStop>();
		if(groups!=null){
			Iterator<String[]> i = groups.keySet().iterator();
			while(i.hasNext()){
				String[] names = i.next();
				BusStop[] stops = groups.get(names);
				if(stops!=null){
					for(int j = 0; j < stops.length; j++){
						allstops.add(stops[j]);
					}
				}
			}
		}
		if(allstops.size()>0)
			return allstops.toArray(new BusStop[1]);
		else
			return null;
	}
	
	public BusStop[] getStops(String destination) {
		String[] tmp = {destination};
		return getStops(tmp);
	}
	
	public BusStop[] getStops(String[] destination) {
		Map<String[], BusStop[]> groups = getDirectionKeyedStopsServedByRoute();
		if(groups!=null){
			Iterator<String[]> i = groups.keySet().iterator();
			String[] best_match = null;
			Integer best_match_score = null;
			while(i.hasNext()){
				String[] destination_check = i.next();
				Integer score = null;
				for(int j = 0; j < destination.length; j++){
					String find = destination[j];
					for(int k = 0; k < destination_check.length; k++){
						if(find.equals(destination_check[k])){
							if(score==null)
								score = Integer.valueOf(0);
							score++;
						}
					}
				}
				if(score!=null){
					if(score < destination.length){
						score = score - (destination.length - score);
					}
					if(destination.length < destination_check.length){
						score = score - 1;
					}
					if(best_match_score==null || score > best_match_score){
						best_match_score = score;
						best_match = destination_check;
					}
				}
			}
			BusStop[] ret = groups.get(best_match);
			if(ret!=null)
				return ret.clone();
		}
		return null;
	}
	
	public Map<String[], BusStop[]> getDirectionKeyedStopsServedByRoute() {
		if(mDestinationStopGroupings==null){
			mDestinationStopGroupings = FirbiCom.getStopsServedByRoute(this);
		}
		return mDestinationStopGroupings;
	}
	
	/**
	 * @return an array of Strings containing the destinations of this BusRoute 
	 */
	public String[][] getDestinations() {
		Map<String[], BusStop[]> groups = getDirectionKeyedStopsServedByRoute();
		if(groups!=null)
			return (String[][])groups.keySet().toArray(new String[1][1]);
		else
			return null;
	}
	
	/**
	 * @return a human readable representation of this BusRoute
	 */
	public String toString() {
		return "BusRoute{id: "+getId()+", route number: "+getRouteNumber()+"}";
	}
	
	/**
	 * Sets the route number of this BusRoute
	 * @param routeNumber	the user friendly route number ex. "71" "67E" that you wish to set for this BusRoute 
	 */
	protected void setRouteNumber(String routeNumber) {
		mRouteNumber = routeNumber;
	}
	
	
}
