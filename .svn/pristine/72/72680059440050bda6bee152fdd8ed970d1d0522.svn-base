package cs.washington.mobileaccessibility.locationorienter;

import java.util.*;

import gsearch.*;

/**
 * BusinessFinder uses Google's Local Search API to find local Points-of-Interest
 */
public class BusinessFinder {

    /**
     * Finds Points-of-Interest near the given mlat and mlong with the given searchString and 
     * returns a List sorted by in front of you or in back of you, with your current location 
     * inserted in the appropriate place and surrounding entries in increasing order of distance 
     * from your current location
     *
     * The elements of the List are Maps from a type of entry to its value, possible types of entry
     * are: distance, bearing, name, address, phone, lat, and long, most can be null.
     */
	public static List<Map<String,String>> getPOIs(double mlat, double mlong, String heading, String searchString){
		Client client = new Client();

		List<Map<String,String>> results = new ArrayList<Map<String,String>>();
		for(int page = 0; page <= 3; page++){
			List<Result> searchResults = client.searchLocal(mlat, mlong, searchString, page);

			for(int i = 0; i < searchResults.size(); i++){
				Result r = searchResults.get(i);
//				System.out.println(r.getTitleNoFormatting());

				double lat = Double.parseDouble(r.getLat());
				double lng = Double.parseDouble(r.getLng());
				int dist = distance(mlat, mlong, lat, lng);
				String bearing = getHeading((float) bearing(mlat, mlong, lat, lng), true);
				
				Map<String,String> result = new HashMap<String,String>();
				result.put("distance", Integer.toString(dist));
				result.put("bearing", bearing);
				result.put("name", r.getTitleNoFormatting());
				result.put("address", r.getStreetAddress());
				result.put("phone", (r.getPhoneNumbers() != null && r.getPhoneNumbers().get(0) != null) ? 
						r.getPhoneNumbers().get(0).getNumber() : null);
				result.put("lat", Double.toString(lat));
				result.put("long", Double.toString(lng));
				if(isOpposite(heading, bearing)){
					result.put("relative_loc", "Back");
				}else{
					result.put("relative_loc", "Front");
				}
				results.add(result);
			}
		}

		Map<String,String> result = new HashMap<String,String>();
		result.put("distance", "0");
		result.put("bearing", heading);
		result.put("name", "your current location");
		result.put("lat", Double.toString(mlat));
		result.put("long", Double.toString(mlong));
		result.put("relative_loc", "Exact");
		results.add(result);
		
		Collections.sort(results, new ResultCompare());
		removeDuplicates(results, new ResultCompare());

		return results;
	}
	
    /**
     * Removes duplicates from the list, where duplicate is defined as being equal according to
     * the given Comparator
     */
	private static void removeDuplicates(List<Map<String,String>> l, Comparator<Map<String,String>> c){
		if(l.size() < 2){
			return;
		}
		int i = 1;
		while(i < l.size()){
			if(c.compare(l.get(i - 1), l.get(i)) == 0){
				l.remove(i);
			}else{
				i++;
			}
		}		
	}
	
    /**
     * Calculates distance between the given coordinates
     */
	private static int distance(double lat1, double long1, double lat2, double long2){
		Double R = 6371000.0; // m
		double dLat = Math.toRadians(lat2-lat1);
		double dLon = Math.toRadians(long2-long1); 
		double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
		Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * 
		Math.sin(dLon/2) * Math.sin(dLon/2); 
		double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a)); 
		double d = R * c;

		return (int) d;
	}
	
    /**
     * Calculates the bearing from the first coordinate to the second
     */
	private static int bearing(double lat1, double long1, double lat2, double long2){
		double dLon = Math.toRadians(long2-long1);
		
		double y = Math.sin(dLon) * Math.cos(lat2);
		double x = Math.cos(lat1)*Math.sin(lat2) -
		        Math.sin(lat1)*Math.cos(lat2)*Math.cos(dLon);
		double brng = Math.toDegrees(Math.atan2(y, x)) % 360;
				
		return (int) brng;
	}
	
    /**
     * Returns a String representation of the given heading, either abbreviated or not
     */
	private static String getHeading(float heading, boolean abbr) {
		heading = ((heading % 360) + 360) % 360;
		String[] directions;
		if(abbr)
			directions = new String[]{"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
		else
			directions = new String[]{"North", "Northeast", "East", "Southeast", "South", "Southwest", "West", "Northwest"};			
		return directions[((int) (((heading) * 2 + 45) / 90)) % 8];
	}
	
    /**
     * Returns true if bBearing is in the opposite direction from aBearing
     */
	private static boolean isOpposite(String aBearing, String bBearing){
		return oppositeDir(aBearing).contains(bBearing);
	}
	
    /**
     * Returns a Set of all bearings that are opposite the given bearing
     */
	private static Set<String> oppositeDir(String bearing){
		Set<String> s = new HashSet<String>();
		if(bearing.equalsIgnoreCase("N")){
			s.add("S");
			s.add("SE");
			s.add("SW");
		}else if(bearing.equalsIgnoreCase("NW")){
			s.add("S");
			s.add("SE");
			s.add("E");
		}else if(bearing.equalsIgnoreCase("NE")){
			s.add("S");
			s.add("W");
			s.add("SW");
		}else if(bearing.equalsIgnoreCase("S")){
			s.add("N");
			s.add("NE");
			s.add("NW");
		}else if(bearing.equalsIgnoreCase("SE")){
			s.add("N");
			s.add("W");
			s.add("NW");
		}else if(bearing.equalsIgnoreCase("SW")){
			s.add("N");
			s.add("NE");
			s.add("E");
		}else if(bearing.equalsIgnoreCase("E")){
			s.add("W");
			s.add("NW");
			s.add("SW");
		}else if(bearing.equalsIgnoreCase("W")){
			s.add("E");
			s.add("SE");
			s.add("NE");
		}
		return s;
	}

}
