package firbi.base.com;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.zip.GZIPInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;


import android.util.Log;

import com.google.android.maps.GeoPoint;


/*
 * This file was originally from firbi.  The original version can be found at
 * http://firbi.googlecode.com/svn/trunk/firbi_public/src/firbi/base/com/FirbiCom.java
 * I probably changed parts of it.  However, I haven't gone through and changed the comments,
 * so the javadoc is probably the same.
 */

/**
 * This class is the main communication class with One Bus Away. This class uses the One Bus Away API which can be found here: 
 * http://code.google.com/p/onebusaway/wiki/OneBusAwayApiReference. This class should only be used by models that can wrap and fail safe
 * the many nuances of this class. Currently BusStop, BusRoute, and Bus are the only classes that communicate with this class. All other comunication
 * for example betwen views and One Bus Away should be done through these models.
 * @author Danny
 * 
 * And me too?
 *
 */
public class FirbiCom {
	
	/**
	 * This String is the identifier that the android log cat will identify prints with
	 */
	private static final String LOGTAG = "FirbiCom";
	/**
	 * This method is a convenience method to help with printing to the android console log cat
	 * @param to_print		the string to get printed to log cat
	 */
	public static void print(String to_print) {
		Log.v(LOGTAG, to_print);
	}
	
	/**
	 * This is the key used when communicating with One Bus Away
	 * Do Not Change This Without Permission from One Bus Away or queries will fail
	 */
	public static final String ONEBUSAWAY_API_KEY = "v1_eV9Gcr4bKoDm%2Bx%2BLmmyEahKasZg%3Dd2lsbGlqNkBnbWFpbC5jb20%3D";//"edu.washington.cs.cse403b";
	
	public static final String ONEBUSAWAY_API_BASEURL = "http://api.onebusaway.org/api/";
	
	/**
	 * This code signifies that One Bus Away was successful in finding your query but it does not guarantee there will be data
	 */
	public static final int RESPONSE_CODE_SUCCESS = 200;
	/**
	 * This code means that One Bus Away believes your query was malformed
	 */
	public static final int RESPONSE_CODE_INVALID_REQUEST = 400;
	/**
	 * This code means that the application key was either not sent or sent incorrectly be sure to specify key=ONEBUSAWAY_API_KEY in every query
	 */
	public static final int RESPONSE_CODE_INVALID_APPLICATION_KEY = 401;
	/**
	 * This means that an error occurred that does not have a normal response code
	 */
	public static final int RESPONSE_CODE_NOT_FOUND = 404;
	/**
	 * This could mean any number of things but most likely you failed to connect to One Bus Away
	 */
	public static final int RESPONSE_CODE_GENERAL_ERROR = 500;
	
	/**
	 * This method converts a double whih is used by One Bus Away into a micro int for use with GeoPoints
	 * @param x		the double to be converted
	 * @return		a micro int made from x
	 */
	public static int doubleToGeoInt(double x) {
		return ((int)(x*Math.pow(10, 6)));
	}
	
	/**
	 * This method converts micro ints that are used by GeoPoints into doubles which are used by One Bus Away
	 * @param x		the micro int to convert
	 * @return		a double that is created from the pased in micro int
	 */
	public static double geoIntToDouble(int x) {
		return ((x*1.0)/Math.pow(10, 6));
	}
	
	/**
	 * This method is a convienince method that converts a GeoPoint into a double array of size 2. If the name of the double array this method returns
	 * was result result[0] would be the double conversion of the passed in GeoPoints latitude, and result[1] would be the double conversion of the 
	 * GeoPoints longitude.
	 * @param point		the GeoPoint to convert
	 * @return			a double array of size 2 where result[0] = the GeoPoints converted latitude, and result[1] = the GeoPoints converted longitude, if null
	 * 					is passed in null is returned.
	 */
	public static double[] geoPointToDoubles(GeoPoint point) {
		if(point!=null){
			double[] ret = {geoIntToDouble(point.getLatitudeE6()), geoIntToDouble(point.getLongitudeE6())};
			return ret;
		}
		else{
			return null;
		}
	}
	
	/**
	 * This method returns all the BusRoutes with the passed in routeNumber that operate near the passed in location near. In order to be considered 
	 * operating near the location a BusRoute need only operate as part of the same agency who runs buses at the specified location 'near'. If two 
	 * or more agencies operate BusRoutes in the same area and they all have the passed in routeNumber then all of these routes are returned.
	 * @param routeNumber	the routeNumber to find BusRoutes with
	 * @param near			the location to search for BusRoutes near
	 * @return				all BusRoutes that have the passed in routeNumber and operate within the an agency that operates buses at the location passed.
	 */
	public static BusRoute[] getRoutes(String routeNumber, GeoPoint near) {
		double[] latlon = geoPointToDoubles(near);
		return generalBusRouteQuery(ONEBUSAWAY_API_BASEURL+"where/routes-for-location.xml?key="+ONEBUSAWAY_API_KEY+"&lat="+latlon[0]+"&lon="+latlon[1]+"&query="+routeNumber+"&radius=100000");
	}
	
	/**
	 * This method returns all BusRoutes with the passed in id.
	 * @param id	the unique One Bus Away id that encapsulates both the agency id and agency unique id.
	 * @return		the BusRoute that has he passed in id or null if no BusRoute can be found with the passed id or if the id is null
	 */
	public static BusRoute getRoute(String id) {
		BusRoute[] ret = generalBusRouteQuery(ONEBUSAWAY_API_BASEURL+"where/route/"+id+".xml?key="+ONEBUSAWAY_API_KEY);
		if(ret!=null && ret.length>0)
			return ret[0];
		return null;
	}
	
	/**
	 * This method returns all BusStops with the passed in stopNumber and who operate within the same agency as the agency that operates buses in the same
	 * area as the passed in location 'near'.
	 * @param stopNumber	the stopNumber to search for BusStops with
	 * @param near			the location to help specify an agency to search for BusStops within
	 * @return				all BusStops who have the passed in stopNumber and operate for an agency who operates near the passed in location
	 */
	public static BusStop[] getStops(String stopNumber, GeoPoint near) {
		double[] latlon = geoPointToDoubles(near);
		return generalBusStopQuery(ONEBUSAWAY_API_BASEURL+"where/stops-for-location.xml?key="+ONEBUSAWAY_API_KEY+"&lat="+latlon[0]+"&lon="+latlon[1]+"&query="+stopNumber);
	}
	
	/**
	 * This method returns all BusStops with the passed in id. This id is specified by One Bus Away, is unique and encapsulates both the agency id and
	 * in most cases the stopNumber. 
	 * @param id	the id to search search for BusStops with
	 * @return		the BusStop that has the passed in id, or null if none could be found or if null was passed for id
	 */
	public static BusStop getStop(String id) {
		BusStop[] ret = generalBusStopQuery(ONEBUSAWAY_API_BASEURL+"where/stop/"+id+".xml?key="+ONEBUSAWAY_API_KEY);
		if(ret!=null && ret.length>0)
			return ret[0];
		return null;
	}
	
	/**
	 * This method returns all BusStops which lay within the circle created by the location center and the distance radius. Radius should be thought of
	 * as being in the units of meters.
	 * @param center	the GeoPoint to use to form the center of a circle inside of which all BusStops will be returned
	 * @param radius	combined with center as the center of a bounding circle which will be used to return all the BusStops within it.
	 * @return			all BusStops which fall inside the bounding circle created by the passed in center and radius
	 */
	public static BusStop[] getStops(GeoPoint center, int radius) {
		double[] latlon = geoPointToDoubles(center);
		return generalBusStopQuery(ONEBUSAWAY_API_BASEURL+"where/stops-for-location.xml?key="+ONEBUSAWAY_API_KEY+"&lat="+latlon[0]+"&lon="+latlon[1]+"&radius="+radius);
	}
	
	/**
	 * This method returns an array of Bus, where all buses in the array stand for an arrival to the stop passed in. Using the Bus array you can figure
	 * out all the specific buses that will arrive at the passed in stop within several minutes. The decision of how many minutes is made by One Bus Away.
	 * @param stop		the stop to get the arrivals for
	 * @return			all arrivals that will or have very recently come to the passed in BusStop
	 */
	public static Bus[] getStopSchedule(BusStop stop) {
		return generalBusQuery(ONEBUSAWAY_API_BASEURL+"where/arrivals-and-departures-for-stop/"+stop.getId()+".xml?key="+ONEBUSAWAY_API_KEY);
	}
	
	/**
	 * This method returns a map that maps groupings of destinations to BusStops. Each String[] BusStop[] pair specify a set of destinations that
	 * correspond to some set of stops
	 * @param route		the route to get the groupings for
	 * @return			a map of groupings of destinations to BusStops null if no groupings or if there is an error
	 */
	public static Map<String[], BusStop[]> getStopsServedByRoute(BusRoute route) {
		Object[] check = generalQuery(ONEBUSAWAY_API_BASEURL+"where/stops-for-route/"+route.getId()+".xml?key="+ONEBUSAWAY_API_KEY, Object[].class, null);
		if(check!=null && check.length == 1){
			if(check[0] instanceof Map){
				return (Map<String[], BusStop[]>)check[0];
			}
			else{
				print("parseXMLData returned the wrong type for StopsServedByRoute");
			}
		}
		else{
			print("parseXMLData returned null for StopsServedByRoute");
		}
		return null;
	}
	
	
	
	
	/**
	 * This method is a general method for querying One Bus Away, it allows you to specify a url to query,
	 * a class to check that the type of data received from the query is correct, and a String[] of responses to be displayed if
	 * an error occurs. responses[0] will be displayed if the type you expected was not what was returned by the query. responses[1] will be used
	 * if a general error occurs somewhere in the query or parsing process.
	 * @param url			the url to query
	 * @param c				the class of the data that is expected to be returned from the query
	 * @param responses		a set of responses to be displayed upon different errors
	 * 						responses[0] displayed when the expected c type is not returned by the query
	 * 						responses[1] displayed when a general error occurs that causes the query or parsing process to fail
	 * @return				an array with the type that you passed as c or null if there was an error
	 */
	protected static Object[] generalQuery(String url, Class c, String[] responses) {
		Object[] ret = null;
		print(url);
		Object[] check = parseXMLFromURL(url);
		if(check!=null){
			if(check.getClass().equals(c)){
				ret = check;
			}
			else{
				if(responses!=null && responses.length > 0)
					print(responses[0]+" got: '"+check.getClass().toString()+"' expected: '"+c.toString()+"'");
			}
		}
		else{
			if(responses!=null && responses.length > 1)
				print(responses[1]);
		}
		return ret;
	}
	
	/**
	 * This method customizes generalQuery for times when a BusStop is the expected result
	 * @param url		the url to query for data with
	 * @param responses	a set of responses to be displayed upon different errors
	 * 					responses[0] displayed when the expected c type is not returned by the query
	 * 					responses[1] displayed when a general error occurs that causes the query or parsing process to fail
	 * @return			an array with the type passed as c or null if there was an error
	 */
	protected static BusStop[] generalBusStopQuery(String url, String[] responses) {
		return (BusStop[])generalQuery(url, BusStop[].class, responses);
	}
	
	/**
	 * This method customizes generalQuery for times when a BusStop is the expected result and uses a default set of error responses
	 * @param url		the url to query for data with
	 * @return			an array with the type passed as c or null if there was an error
	 */
	protected static BusStop[] generalBusStopQuery(String url) {
		String[] responses = {"got invalid type from parseXMLFromURL()", "got null from parseXMLFromURL()"};
		return generalBusStopQuery(url, responses);
	}
	
	/**
	 * This method customizes generalQuery for times when a BusRoute is the expected result
	 * @param url		the url to query for data with
	 * @param responses	a set of responses to be displayed upon different errors
	 * 					responses[0] displayed when the expected c type is not returned by the query
	 * 					responses[1] displayed when a general error occurs that causes the query or parsing process to fail
	 * @return			an array with the type passed as c or null if there was an error
	 */
	protected static BusRoute[] generalBusRouteQuery(String url, String[] responses) {
		return (BusRoute[])generalQuery(url, BusRoute[].class, responses);
	}
	
	/**
	 * This method customizes generalQuery for times when a BusRoute is the expected result and uses a default set of error responses
	 * @param url		the url to query for data with
	 * @return			an array with the type passed as c or null if there was an error
	 */
	protected static BusRoute[] generalBusRouteQuery(String url) {
		String[] responses = {"got invalid type from parseXMLFromURL()", "got null from parseXMLFromURL()"};
		return generalBusRouteQuery(url, responses);
	}
	
	/**
	 * This method customizes generalQuery for times when a Bus is the expected result
	 * @param url		the url to query for data with
	 * @param responses	a set of responses to be displayed upon different errors
	 * 					responses[0] displayed when the expected c type is not returned by the query
	 * 					responses[1] displayed when a general error occurs that causes the query or parsing process to fail
	 * @return			an array with the type passed as c or null if there was an error
	 */
	protected static Bus[] generalBusQuery(String url, String[] responses) {
		return (Bus[])generalQuery(url, Bus[].class, responses);
	}
	
	/**
	 * This method customizes generalQuery for times when a Bus is the expected result and uses a default set of error responses
	 * @param url		the url to query for data with
	 * @return			an array with the type passed as c or null if there was an error
	 */
	protected static Bus[] generalBusQuery(String url) {
		String[] responses = {"got invalid type from parseXMLFromURL()", "got null from parseXMLFromURL()"};
		return generalBusQuery(url, responses);
	}
	
	
	
	
	/**
	 * This method can be used to parse all current queries that One Bus Away supports. This method makes the query gets the xml and decides based on
	 * the xml tags which methods to use to parse each piece of the xml. In the event of an error this method returns null.
	 * @param url_str	the url to query with and parse the data from
	 * @return			an Object[] with the data parsed from the query specified as url_str
	 */
	protected static Object[] parseXMLFromURL(String url_str) {
		try {
			URL url = new URL(url_str);
			URLConnection conn = url.openConnection();
			
			// it's not clear that this helps at all:
			//conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
			conn.connect();
			
			String encoding = conn.getContentEncoding();
			InputStream inStream = conn.getInputStream();
			if(encoding != null && encoding.equalsIgnoreCase("gzip")) {
				inStream = new GZIPInputStream(inStream);
			}
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document response = builder.parse(inStream);
			Element root = response.getDocumentElement();
			
			NodeList children = root.getChildNodes();
			for(int i = 0; i < children.getLength();i++){
				Node child = children.item(i);
				if(child instanceof Element){
					Element childElement = (Element)child;
					if(childElement.getTagName().equals("code")){
						Text value = (Text)childElement.getFirstChild();
						//print(value.getData());
						switch(Integer.valueOf(value.getData()).intValue()){
							case RESPONSE_CODE_SUCCESS:
								
							break;
							case RESPONSE_CODE_INVALID_REQUEST: 
								
							break;
							case RESPONSE_CODE_INVALID_APPLICATION_KEY:
								
							break;
							case RESPONSE_CODE_NOT_FOUND:
								
							break;
							case RESPONSE_CODE_GENERAL_ERROR:
								
							break;
							default:
								
							break;
						}
					}
					if(childElement.getTagName().equals("data")){
						if(childElement.getAttribute("class").equals("list")){
							return parseListData(childElement);
						}
						else if(childElement.getAttribute("class").equals("org.onebusaway.where.web.common.client.model.StopsBean")){
							NodeList subchildren = childElement.getChildNodes();
							for(int subi = 0; subi < subchildren.getLength();subi++){
								Node subchild = subchildren.item(subi);
								if(subchild instanceof Element){
									Element ele = (Element) subchild;
									if(ele.getTagName().equals("stops")){
										return parseListData(ele);
									}
								}
							}
						}
						else if(childElement.getAttribute("class").equals("org.onebusaway.where.web.common.client.model.RoutesBean")){
							NodeList subchildren = childElement.getChildNodes();
							for(int subi = 0; subi < subchildren.getLength();subi++){
								Node subchild = subchildren.item(subi);
								if(subchild instanceof Element) {
									Element ele = (Element) subchild;
									if(ele.getTagName().equals("routes")){
										return parseListData(ele);
									}
								}
							}
						}
						else if(childElement.getAttribute("class").equals("stop")){
							BusStop[] stop = {parseStop(childElement)};
							return stop;
						}
						else if(childElement.getAttribute("class").equals("route")){
							BusRoute[] route = {parseRoute(childElement)};
							return route;
						}
						else if(childElement.getAttribute("class").equals("org.onebusaway.where.web.common.client.model.StopWithArrivalsAndDeparturesBean")){
							return parseArrivalData(childElement);
						}
						else if(childElement.getAttribute("class").equals("org.onebusaway.model.where.StopsForRouteBean")){
							Object[] tmp = new Object[1];
							tmp[0] = parseStopsForRouteData(childElement);
							return tmp;
						}
					}
				}
			}
			
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * This method parses xml tags formatted by One Bus Away in the "Stops for Route" format. Specifically this format is denoted in the xml with a tag
	 * that specifies it's class like: <data class="org.onebusaway.model.where.StopsForRouteBean">
	 * @param data	the xml element that is of the class org.onebusaway.model.where.StopsForRouteBean and is formatted
	 * 				as specified by One Bus Away for Stops for Route
	 * @return		a map that specifies the groupings of String[] of destinations to BusStop[] of stops for those destinations stopped at by some route.
	 */
	protected static Map<String[], BusStop[]> parseStopsForRouteData(Element data) {
		BusStop[] stops = null;
		Object[] groups = null;
		HashMap<String[], BusStop[]> ret = new HashMap<String[], BusStop[]>();
		NodeList children = data.getChildNodes();
		for(int i = 0; i < children.getLength();i++){
			Node child = children.item(i);
			if(child instanceof Element){
				Element ele = (Element) child;
				if(ele.getTagName().equals("stops")){
					Object[] check = parseListData(ele);
					if(check!=null && check instanceof BusStop[]){
						stops = ((BusStop[])check);
					}
					else{
						print("error getting stops from a StopsForRouteData query");
						return null;
					}
				}
				else if(ele.getTagName().equals("stopGroupings")){
					NodeList groupings = ele.getChildNodes();
					for(int j = 0; j < groupings.getLength();j++){
						Node grouping = groupings.item(j);
						if(grouping instanceof Element){
							Element ele_grouping = (Element) grouping;
							if(ele_grouping.getTagName().equals("stopGrouping")){
								boolean type = false;
								boolean ordered = false;
								Object[] stopGroups = null;
								NodeList grouping_properties = ele_grouping.getChildNodes();
								for(int k = 0; k < grouping_properties.getLength();k++){
									Node grouping_property = grouping_properties.item(k);
									if(grouping_property instanceof Element){
										Element ele_grouping_property = (Element) grouping_property;
										Text ele_grouping_attribute = (Text)ele_grouping_property.getFirstChild();
										if(ele_grouping_property.getTagName().equals("type")){
											type = ele_grouping_attribute.getData().equals("direction");
										}
										else if(ele_grouping_property.getTagName().equals("ordered")){
											ordered = ele_grouping_attribute.getData().equals("true");
										}
										else if(ele_grouping_property.getTagName().equals("stopGroups")){
											stopGroups = parseListData(ele_grouping_property);
										}
									}
								}
								if(type && stopGroups!=null){
									groups = stopGroups;
								}
							}
						}
					}
				}
				else if(ele.getTagName().equals("polylines")){
					
				}
			}
		}
		if(groups!=null){
			for(int i = 0; i < groups.length; i++){
				Object group = groups[i];
				if(group!=null){
					Object[] pair = ((Object[])group);
					if(pair.length==2 && pair[0]!=null && pair[1]!=null){
						String[] names = null;
						BusStop[] bstops = null;
						if(pair[0] instanceof String[]){
							names = ((String[])pair[0]);
						}
						if(pair[1] instanceof String[]){
							String[] ids = ((String[])pair[1]);
							bstops = new BusStop[ids.length];
							for(int j = 0; j < ids.length; j++){
								bstops[j] = BusStop.find(ids[j]);
							}
						}
						if(names!=null && bstops!=null){
							ret.put(names, bstops);
						}
						else{
							print("incorrect types returned for group data!");
						}
					}
					else{
						print("parsing group data failed! got null for part of the group");
					}
				}
			}
		}
		else{
			print("parsing group data failed! got null for groups");
		}
		return ret;
	}
	
	/**
	 * This method parses xml tags formatted by One Bus Away in the "Current arrivals and departures for a stop" format. Specifically this format is denoted in the xml with a tag
	 * that specifies it's class like: <data class="org.onebusaway.where.web.common.client.model.StopWithArrivalsAndDeparturesBean">
	 * @param data	the xml element that is of the class org.onebusaway.where.web.common.client.model.StopWithArrivalsAndDeparturesBean and is formatted
	 * 				as specified by One Bus Away for Stops for Route
	 * @return		a map that specifies the groupings of String[] of destinations to BusStop[] of stops for those destinations stopped at by some route.
	 */
	protected static Bus[] parseArrivalData(Element data) {
		Bus[] arrivals = null;
		NodeList children = data.getChildNodes();
		for(int i = 0; i < children.getLength();i++){
			Node child = children.item(i);
			if(child instanceof Element){
				Element ele = (Element) child;
				if(ele.getTagName().equals("stop")){
					parseStop(ele);
				}
				else if(ele.getTagName().equals("arrivalsAndDepartures")){
					Object[] check = parseListData(ele);
					if(check!=null){
						if(check instanceof Bus[]){
							arrivals = (Bus[])check;
						}
						else{
							//type error
							print("getStopSchedule(BusStop stop): got invalid type from parseListData()");
						}
					}
					else{
						//null error
						print("getStopSchedule(BusStop stop): got null from parseListData()");
					}
				}
			}
		}
		return arrivals;
	}
	
	/**
	 * This method is a general parser for xml into arrays. If you have xml data that needs to be converted into an array this method will read the xml tags
	 * and decide what type each piece of information should become and fills an array with this data. 
	 * @param data	the xml element that holds the list
	 * @return		an array created from the children of the element passed in
	 */
	protected static Object[] parseListData(Element data){
		NodeList children = data.getChildNodes();
		Object[] array = new Object[children.getLength()];
		String iden = "";
		int k = 0;
		for(int i = 0; i < children.getLength();i++){
			Node child = children.item(i);
			if(child instanceof Element){
				Element ele = (Element) child;
				iden = ele.getTagName();
				if(iden.equals("stop")){
					array[k] = parseStop(ele);
				}
				else if(iden.equals("route")){
					array[k] = parseRoute(ele);
				}
				else if(iden.equals("arrivalAndDeparture")){
					array[k] = parseArrival(ele);
				}
				else if(iden.equals("stopGroup")){
					array[k] = parseStopGroup(ele);
				}
				else if(iden.equals("names") || iden.equals("string")){
					array[k] = ((Text)ele.getFirstChild()).getData();
				}
				if(array[k]!=null){
					k++;
				}
				//print("Next Item");
			}
		}
		if((k+1)!=array.length){
			Object[] ret;
			if(iden.equals("stop")){
				ret = new BusStop[k];
			}
			else if(iden.equals("route")){
				ret = new BusRoute[k];
			}
			else if(iden.equals("arrivalAndDeparture")){
				ret = new Bus[k];
			}
			else if(iden.equals("name") || iden.equals("string")){
				ret = new String[k];
			}
			else{
				ret = new Object[k];
			}
			for(int i = 0; i < k ;i++){
				ret[i] = array[i];
			}
			array = ret;
		}
		return array;
	}
	
	/**
	 * This method parses an xml tag if it is formatted as a stopGroup which is a specified format from One Bus Away. Read One Bus Away information for more on
	 * this.
	 * @param stopGroup		an xml tag that has it's children formatted as stopGroup information
	 * @return				an Object[] of size 2 where array[0] is a array of destination names as Strings and array[1] is an array of BusStop ids as Strings
	 */
	protected static Object[] parseStopGroup(Element stopGroup) {
		String[] names = null;
		String[] stop_ids = null;
		NodeList children = stopGroup.getChildNodes();
		for(int i = 0; i < children.getLength();i++){
			Node ele = children.item(i);
			if(ele instanceof Element){
				Element property = (Element) ele;
				if(property.getTagName().equals("name")){
					
					NodeList childs = property.getChildNodes();
					for(int j = 0; j < childs.getLength();j++){
						Node element = childs.item(j);
						if(element instanceof Element){
							Element prop = (Element) element;
							if(prop.getTagName().equals("type")){
								Text attribute = (Text)prop.getFirstChild();
								if(!attribute.getData().equals("destination")){
									print("returned null in parseStopGRoup because of incorrect grouping type");
									return null;
								}
							}
							else if(prop.getTagName().equals("names")){
								Object[] tmp = parseListData(prop);
								if(tmp instanceof String[]){
									names = ((String[])tmp);
									ArrayList<String> temp = new ArrayList<String>();
									for(int p = 0; p < names.length; p++){
										String[] split = names[p].split(", ");
										for(int q = 0; q < split.length; q++){
											temp.add(split[q]);
										}
									}
									names = temp.toArray(new String[1]);
								}
								else{
									print("parseListData returned an incorrect type for names");
								}
							}
						}
					}
				}
				else if(property.getTagName().equals("stopIds")){
					Object[] tmp = parseListData(property);
					if(tmp instanceof String[]){
						stop_ids = ((String[])tmp);
					}
					else{
						print("parseListData returned an incorrect type for stop_ids");
					}
				}
			}
		}
		Object[][] ret = null;
		if(names!=null && stop_ids!=null){
			ret = new Object[2][Math.max(names.length, stop_ids.length)];
			ret[0] = names;
			ret[1] = stop_ids;
		}
		else{
			print("recieved names as null or stop_ids as null");
		}
		return ret;
	}
	
	/**
	 * This method parses xml elements with children formatted as Arrivals, specified by One Bus Away.
	 * @param arrival	the xml element whose children are formatted as Arrival data
	 * @return			a Bus whose fields are filled according to the children of the arrival xml element
	 */
	protected static Bus parseArrival(Element arrival) {
		BusRoute route = null;
		BusStop stop = null;
		GregorianCalendar scheduledTime = null;
		GregorianCalendar predictedTime = null;
		String destination = "";
		
		String route_id = null;
		String route_name = null;
		NodeList attrs = arrival.getChildNodes();
		for(int j = 0; j < attrs.getLength();j++){
			Node attr = attrs.item(j);
			if(attr instanceof Element){
				Element ele = (Element) attr;
				Text attribute = (Text)ele.getFirstChild();
				if(attribute!=null){
					if(ele.getTagName().equals("routeId")){
						route_id = attribute.getData();
						if(route_name!=null)
							route = BusRoute.updateOrCreate(route_id, route_name);
					}
					else if(ele.getTagName().equals("routeShortName")){
						route_name = attribute.getData();
						if(route_id!=null)
							route = BusRoute.updateOrCreate(route_id, route_name);
					}
					else if(ele.getTagName().equals("stopId")){
						String id = attribute.getData();
						stop = BusStop.find(id);
					}
					else if(ele.getTagName().equals("predictedArrivalTime")){
						long ms = Long.valueOf(attribute.getData());
						if(ms!=0){
							predictedTime = new GregorianCalendar();
							predictedTime.setTimeInMillis(ms);
						}
					}
					else if(ele.getTagName().equals("scheduledArrivalTime")){
						long ms = Long.valueOf(attribute.getData());
						if(ms!=0){
							scheduledTime = new GregorianCalendar();
							scheduledTime.setTimeInMillis(ms);
						}
					}
					else if(ele.getTagName().equals("tripHeadsign")){
						destination = attribute.getData();
					}
					//print(ele.getTagName()+": ");
					//print(attribute.getData());
				}
			}
		}
		return new Bus(route, stop, scheduledTime, predictedTime, destination);
	}
	
	/**
	 * This method parses xml tags that are formatted as stop data as specified by One Bus Away into a BusStop
	 * @param stop	the xml element to parse into a BusStop
	 * @return		a BusStop created from the xml element passed in
	 */
	protected static BusStop parseStop(Element stop) {
		NodeList attrs = stop.getChildNodes();
		HashMap params = new HashMap();
		Integer lat = null;
		Integer lon = null;
		for(int j = 0; j < attrs.getLength();j++){
			Node attr = attrs.item(j);
			if(attr instanceof Element){
				Element ele = (Element) attr;
				Text attribute = (Text)ele.getFirstChild();
				if(attribute!=null){
					if(ele.getTagName().equals("lat")){
						lat = Double.valueOf(Double.valueOf(attribute.getData())*Math.pow(10, 6)).intValue();
						if(lon!=null)
							params.put("location", new GeoPoint(lat, lon));
					}
					else if(ele.getTagName().equals("lon")){
						lon = Double.valueOf(Double.valueOf(attribute.getData())*Math.pow(10, 6)).intValue();
						if(lat!=null)
							params.put("location", new GeoPoint(lat, lon));
					}
					else if(ele.getTagName().equals("id")){
						String id = attribute.getData();
						params.put("id", id);
					}
					else if(ele.getTagName().equals("direction")){
						params.put("direction", BusStop.directionValue(attribute.getData()));
					}
					else if(ele.getTagName().equals("name")){
						String innerText = attribute.getData();
						Node ele2 = ele.getFirstChild();
						ele2 = ele2.getNextSibling();
						while(ele2 != null) {
							innerText += "" + ((Text)ele2).getData();
							ele2 = ele2.getNextSibling();
						}
						params.put("address", innerText);
						//params.put("address", (((Text)ele.getFirstChild()).getData()+"&"+((Text)ele.getLastChild()).getData()));//attribute.getData());
					}
					else if(ele.getTagName().equals("code")){
						params.put("stopNumber", attribute.getData());
					}
					else{
						params.put(ele.getTagName(), attribute.getData());
					}
					//print(ele.getTagName()+": ");
					//print(attribute.getData());
				}
			}
		}
		return BusStop.updateOrCreate(params);
	}
	
	/**
	 * This method parses xml tags that are formatted as route data as specified by One Bus Away into a BusRoute
	 * @param stop	the xml element to parse into a BusRoute
	 * @return		a BusRoute created from the xml element passed in
	 */
	protected static BusRoute parseRoute(Element route) {
		NodeList attrs = route.getChildNodes();
		HashMap params = new HashMap();
		for(int j = 0; j < attrs.getLength();j++){
			Node attr = attrs.item(j);
			if(attr instanceof Element){
				Element ele = (Element) attr;
				Text attribute = (Text)ele.getFirstChild();
				if(attribute!=null){
					if(ele.getTagName().equals("id")){
						String id = attribute.getData();
						params.put("id", id);
					}
					else if(ele.getTagName().equals("longName")){
						params.put("destinations", attribute.getData());
					}
					else if(ele.getTagName().equals("shortName")){
						params.put("routeNumber", attribute.getData());
					}
					else{
						params.put(ele.getTagName(), attribute.getData());
					}
					//print(ele.getTagName()+": ");
					//print(attribute.getData());
				}
			}
		}
		return BusRoute.updateOrCreate(params);
	}

}
