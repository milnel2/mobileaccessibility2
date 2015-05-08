/**
 * @author Michael Q. Lam (mqtlam@cs.washington.edu)
 * @author Levi Lindsey (levisl@cs.washington.edu)
 * @author Chris Raastad (craastad@cs.washington.edu)
 * 
 * Designed to meet the requirements of the Winter 2011 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * RouteOrienter allows the user to query in which direction he/she should be 
 * going in order to reach either a given destination or an intermediate 
 * intersection on a route to the given destination.  The user can also 
 * provide additional input to declare when he/she is at an intersection.  
 * RouteOrienter uses vibrations to tell the user both direction and distance.
 */

package edu.uw.cse481h.phonewand;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.DataFormatException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

public 	class 		RouteOrienter 
	 	extends 	MapActivity 
	 	implements 	SensorEventListener {
	
	private boolean D = false;
	
	// Useful for debugging.  When true, the entire route and a small amount 
	// of padding will be displayed on the screen.  When false, only a few 
	// blocks to either side of the current location will be displayed.
	private static final boolean SHOW_ENTIRE_ROUTE = false;
	
	// For debugging with the Log messages.
	private static final String TAG = "RouteOrienter";
	
	// For power saving.
	private boolean mOnPause;
	
    // For the sensor services.
	private SensorManager mSensorManager;
	
	// End location.
	private GeoPoint mDestination;
	// The array of Step objects which constitutes the route.
	private RouteStep[] mRouteSteps;
	// Index to current Route Step.
	private int mCurRouteStep = -1;
	// Current Geostep, next geostem is CurStep + 1.
	private int   mCurGeoStep = -1;
	
	// Determines whether a route is being displayed at any given time.
	private boolean mRouteExists;
	// Determines whether we are currently trying to get a route from Google Maps.
	private boolean mGettingRoute;
	
	// Route color and opacity.
	private static final int ROUTE_COLOR = 0xFFAA6600;	// Brown
	private static final int ROUTE_ALPHA_VALUE = 120;
	
	// The MapView on which the route is displayed.
	private MapView mMapView;
	// For panning and zooming the MapView.
	private MapController mMapController;
	// For displaying important locations on the MapView.
	private Overlay mCurrentLocationOverlay;
	private Overlay mDestinationOverlay;
	// For specifying padding between Overlays' and MapView's boundaries when 
	// SHOW_ENTIRE_ROUTE is set to true.
	private static final double VERTICAL_PADDING_RATIO = 1.0 / 18;
	private static final double HORIZONTAL_PADDING_RATIO = 1.0 / 26;
	// For specifying the width of a city block so only a few blocks on either 
	// side of the current location can be displayed when SHOW_ENTIRE_ROUTE is 
	// set to false.
	private static final double LAT_DEG_PER_CITY_BLOCK_RATIO = 1.0 / 1200;
	
	// Used to prevent the screen from sleeping.
	private PowerManager.WakeLock mWakeLock;
	
	// Provides access to the project's resources (e.g. icons).
	private Resources mResources;
	
	// Handle for compass directions.
	private Sensor mSensor;
	
	// Gesture detector for Swipes and Double Taps.
	private GestureDetector mGestureDetector;

	private static boolean mSecondInternetAttempt = false;
	
	// Allowed vibrational direction to target location
	private static final int ORIENTING_EPS = 5;
	// Number of meters within waypoint geopoint to move onto next point.
	private static final int DISTANCE_EPS = 25;
	
	// Hard coded directions 
	private static final int NORTH = 0;
	
    // Current compass heading in degrees.
	private ModulusInteger mHeading;
	
	// Desired compass heading for user.
	private ModulusInteger mNextHeading;
	
	// Controls when to give vibrational guidance.
	private boolean mOrienter = false;
	
	// variable needed for swipes...
	private int mMinScaledVelocity; 
	
	// Contains the textual directions for the current route.
	public static String[] mDirectionsText;
	
    /**
     * Minimum number of degrees the compass reading
     * has to change (plus or minus the current heading)
     * to update the current heading
     */
    private static final int MIN_HEADING_CHANGE = 1;
    
    private static String mWelcomeSpeech;
	private static String mInstructionsSpeech;
	
	private static boolean mHaveSpokenCurDest = false;
	
	// Set to true while the RouteOrienter screen has been created and not destroyed.
	public static boolean mOnCreate = false;
	
	/** Called when the system creates this Activity. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
        if (D) Log.v(TAG, "+++ ON CREATE  +++");
        
		super.onCreate(savedInstanceState);
		
		mOnCreate = true;
		
		// initialize heading variables
		mHeading     = new ModulusInteger(NORTH,360);
		mNextHeading = new ModulusInteger(NORTH,360);
		
		// Setup the screen's title and layout.
		setTitle(R.string.app_name);
		setContentView(R.layout.route_orienter);
		
		mWelcomeSpeech = getString(R.string.route_orienter_welcome);
		mInstructionsSpeech = getString(R.string.route_orienter_instructions);
		
		// Find the route.
		new GetDirections().execute(PhoneWandActivity.mCurrentLocation, mDestination);
		
		// Instantiate access to project resources.
		mResources = this.getResources();
		
		// Get the destination.
		Intent intent = getIntent();
		mDestination = new GeoPoint(
				intent.getIntExtra(PhoneWandActivity.LATITUDE_EXTRA,  PhoneWandActivity.MICRODEGREE_UPPER_BOUND), 
				intent.getIntExtra(PhoneWandActivity.LONGITUDE_EXTRA, PhoneWandActivity.MICRODEGREE_UPPER_BOUND));
		
		if (D) Log.i(TAG, "Destination: lat=" + mDestination.getLatitudeE6()  + 
				              ", lon=" + mDestination.getLongitudeE6() );
		
		// Close the Activity if the destination was passed incorrectly.
		if(mDestination.getLatitudeE6()  >= PhoneWandActivity.MICRODEGREE_UPPER_BOUND || 
				mDestination.getLongitudeE6() >= PhoneWandActivity.MICRODEGREE_UPPER_BOUND) {
			if (D) Log.e(TAG, getString(R.string.get_extra_fail));
			killOrienterScreen();
			return;
		}
		
		mOnPause = false;
		
		mRouteExists = false;
		mGettingRoute = false;
		
		mMapView = (MapView) findViewById(R.id.map);
		mMapController = mMapView.getController();
		
		// Set gesture detector that catches double taps and swipes.
		mGestureDetector = new GestureDetector(new GestureController());
		mMinScaledVelocity = ViewConfiguration.get(this).getScaledMinimumFlingVelocity();
		
		// Handle to system Sensor manager.
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		
		// Set up compass sensor system.
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

		// Ensure that the screen does not go to sleep while this screen is active
		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		mWakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK,TAG);
	
        // play initial welcome
		welcomeSpeech();
        
        // Initialize destination Overlay.
		mDestinationOverlay = new StartAndEndOverlay(mDestination, true);
	}
	
	/** Called when the system starts this Activity. */
	@Override
	public void onStart() {
        if (D) Log.v(TAG, "  + ON START   +");
        
		super.onStart();
		
		mOnPause = false;
		mOrienter = false;
	}
	
	/** Called when the system resumes this Activity. */
	@Override
	protected void onResume() {
		if (D) Log.v(TAG, "+++ ON RESUME  +++");
		
		super.onResume();
		
		mOnPause = false;
		
		mWakeLock.acquire();
		
		// Register sensor listener
		mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
		
		// Initialize the current location Overlay.
		updateCurrentLocation();
		
		if(mRouteExists) {
			// Let the user know the current step in the route.
			PhoneWandActivity.ttsSpeak("Current step is "+mRouteSteps[mCurRouteStep].getDescription(), 
					TextToSpeech.QUEUE_ADD);
		}
	}
	
	/** Called when the system pauses this Activity. */
	@Override
	public synchronized void onPause() {
		if (D) Log.v(TAG, "  - ON PAUSE   -");
		
		super.onPause();
		
    	mOnPause = true;
    	mOrienter = false;
    	
    	// Unregister sensor listener to conserve resources.
    	mSensorManager.unregisterListener(this);
		
		mWakeLock.release();
	}
	
	/** Called when the system stops this Activity. */
	@Override
	public void onStop() {
        if (D) Log.v(TAG, "--- ON STOP---");
        
        mOrienter = false;
		super.onStop();
	}
		
	/** Called when the system removes this Activity. */
	@Override
	public void onDestroy() {
        if (D) Log.v(TAG, "--- ON DESTROY ---");
        
        mOnCreate = false;
        
		super.onDestroy();
		
    	mSensorManager.unregisterListener(this);
	}
	
	/**
	 * Override the default instructions for the phone's back, menu, and 
	 * search hard-key buttons.  The home button cannot be overridden.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_BACK:
			return true;	// Do nothing (disable the button).
		case KeyEvent.KEYCODE_HOME:
			return true;	// Do nothing (disable the button).
		case KeyEvent.KEYCODE_SEARCH:
			return true;	// Do nothing (disable the button).
		default:			// Use the button's default functionality.
			return super.onKeyDown(keyCode, event);
		}
	}
	
	/**
	 * Called when the user is checking his/her current location/direction.
	 */
	private void checkRoute() {
        if (D) Log.v(TAG, getString(R.string.check_route));
        
		PhoneWandActivity.swipeBuzz();
        
		updateCurrentLocation();
    	mOrienter = false;
		
		// Find the route.
		new GetDirections().execute(PhoneWandActivity.mCurrentLocation, mDestination);
	}
	
	/**
	 * Open the DirectionsDisplay screen with the list of textual directions 
	 * 	for the current route.
	 */
	private void openDirectionsDisplayScreen() {
		PhoneWandActivity.swipeBuzz();
		startActivityForResult(new Intent(this, DirectionsDisplay.class), -1);
	}
	
	/**
	 * Updates the PhoneWandActivity.mCurrentLocation and mCurrentLocationOverlay fields  
	 * 	to the current location, redraws all of the MapViews Overlays, then zooms and 
	 *	pans the map to fit optimally with the new current location.
	 */
	private void updateCurrentLocation() {
		PhoneWandActivity.findCurrentLocation();
		
		mCurrentLocationOverlay = 
			new StartAndEndOverlay(PhoneWandActivity.mCurrentLocation, false);
		
		if (D) Log.d(TAG,"updateCurrentLocation: lat="+
				PhoneWandActivity.mCurrentLocation.getLatitudeE6()+", lon="+
				PhoneWandActivity.mCurrentLocation.getLongitudeE6());
		
		// Don't try to display anything, if we haven't retrieved a route from 
		// Google Maps yet.
		if(mRouteExists) {
			// Update the MapView contents accordingly.
			drawPath();
			
		// If we don't have a route and aren't currently waiting for Google 
		// Maps to return one, then get one.
		} else if(!mGettingRoute) {
			new GetDirections().execute(PhoneWandActivity.mCurrentLocation, mDestination);
		}
		
		computeHeading();
	}
	
	/**
	 * Zooms and Pans the MapView to optimally fit the current location, 
	 * destination, and route Overlays.
	 */
	private void fitToOverlays() {
		int latSpan, lonSpan, latCenter, lonCenter;
		
		// Show the whole route on the screen.
		if(SHOW_ENTIRE_ROUTE) {
			// Find the latitudinal and longitudinal distances between the current 
			// 	location and the destination.
			latSpan = (int)(Math.abs(PhoneWandActivity.mCurrentLocation.getLatitudeE6() - 
							mDestination.getLatitudeE6()) * 
							(1 + (HORIZONTAL_PADDING_RATIO * 2)));
			lonSpan = (int)(Math.abs(PhoneWandActivity.mCurrentLocation.getLongitudeE6() - 
							mDestination.getLongitudeE6()) * 
							(1 + (VERTICAL_PADDING_RATIO * 2)));
			
			// Find the center point between the current location and the 
			// destination.
			latCenter = (PhoneWandActivity.mCurrentLocation.getLatitudeE6() + 
							mDestination.getLatitudeE6()) / 2;
			lonCenter = (PhoneWandActivity.mCurrentLocation.getLongitudeE6() + 
							mDestination.getLongitudeE6()) / 2;
			
		// Zoom in on the user's current location.
		} else {
			// Display a span of about 6 blocks on either side of the current location.
			latSpan = (int)(LAT_DEG_PER_CITY_BLOCK_RATIO * 12 * 1E6);
			lonSpan = (int)(LAT_DEG_PER_CITY_BLOCK_RATIO * 12 * 1E6);
			
			// Set the center point at the current location.
			latCenter = PhoneWandActivity.mCurrentLocation.getLatitudeE6();
			lonCenter = PhoneWandActivity.mCurrentLocation.getLongitudeE6();
		}
		
		// Pan the MapView.
		mMapController.animateTo(new GeoPoint(latCenter, lonCenter));
		// Zoom the MapView.
		mMapController.zoomToSpan(latSpan, lonSpan);
		
		if (D) Log.d(TAG, "fitToOverlays:" +
				"\n\tZoomed to: latSpan=" + latSpan+", lngSpan=" + lonSpan + 
				"\n\tPanned to: latCenter=" + latCenter + ", lngCenter=" + lonCenter);
	}
	
	/**
	 * Adds the Overlays, which display the GeoPoints for our route, to the 
	 * MapView.
	 * @param geoPoints The List of GeoPoints which are part of our route.
	 */
	private void drawPath() {
		if (D) Log.v(TAG,"drawPath");
		
		if(mRouteExists) {
			fitToOverlays();
			
			// Get the list of Overlays and empty it.
			List<Overlay> overlays = mMapView.getOverlays();
			overlays.clear();
			
			// Draw the route Overlays.
			for(int i = 0; i < mRouteSteps.length; i++) {
				List<GeoPoint> polyline = mRouteSteps[i].getPolyline();
				int length = polyline.size();
				for(int j = 1; j < length; j++) {
					overlays.add(new RouteOverlay(polyline.get(j - 1), 
							polyline.get(j)));
				}
			}
			
			// Draw the start and end markers.
			overlays.add(mCurrentLocationOverlay);
			overlays.add(mDestinationOverlay);
		}
	}
	
	/**
	 * Inner (non-static) class which implements the Overlay abstract class 
	 * and is used to represent either the current location or the destination.
	 */
	public class StartAndEndOverlay extends Overlay {
		private GeoPoint geoPoint;
		private boolean isDestination;
		
		public StartAndEndOverlay(GeoPoint geoPoint, 
				boolean isDestination) {
			
			this.geoPoint = geoPoint;
			this.isDestination = isDestination;
		}
		
		/**
		 * Returns true if this Overlay represents the destination.  
		 * Returns false if it represents the current location.
		 */
		public boolean isDestination() {return isDestination;}
		
		// "Draw the Overlay over the map."
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			if(shadow) {
				int r = isDestination ? R.drawable.destination : 
					R.drawable.pointer;
				Projection projection = mapView.getProjection();
				Point point = new Point();
				projection.toPixels(geoPoint, point);

				Bitmap bm1 = BitmapFactory.decodeResource(mResources, r);
				
				Matrix matrix = new Matrix();
				matrix.postRotate((float)mHeading.getValue());
				Bitmap bm2 = Bitmap.createBitmap(bm1, 0, 0, bm1.getWidth(), bm1.getHeight(), matrix, true);
				
				canvas.drawBitmap(bm2, point.x - (bm2.getWidth() / 2), 
						point.y - (bm2.getHeight() / 2), null);
			}
			super.draw(canvas, mapView, shadow);
		}
	}
	
	/**
	 * Inner (non-static) class used to represent two nodes and the line 
	 *  between them in the path from the user's current location to the 
	 *  destination. Each RouteOverlay includes two GeoPoints.
	 */
	public class RouteOverlay extends Overlay {
		private GeoPoint geoPoint1;
		private GeoPoint geoPoint2;
		
		public RouteOverlay(GeoPoint geoPoint1, GeoPoint geoPoint2) {
			this.geoPoint1 = geoPoint1;
			this.geoPoint2 = geoPoint2;
		}
		
		// "Draw the Overlay over the map." --Anonymous
		@Override
		public void draw(Canvas canvas, MapView mapView, boolean shadow) {
			if(shadow) {
				Projection projection = mapView.getProjection();
				Paint paint = new Paint();
				Point point = new Point();
				projection.toPixels(geoPoint1, point);
				paint.setColor(ROUTE_COLOR);
				Point point2 = new Point();
				projection.toPixels(geoPoint2, point2);
				paint.setStrokeWidth(5);
				paint.setAlpha(ROUTE_ALPHA_VALUE);
				canvas.drawLine(point.x, point.y, point2.x, point2.y, paint);
			}
			super.draw(canvas, mapView, shadow);
		}
	}
	
	/**
	 * Inner (non-static) class which operates on a separate thread in order 
	 * to send a GET request to the Google Maps servers, parse the encoded 
	 * result into a List of GeoPoints, and then return the result to the main 
	 * thread's Handler.
	 */
	private class GetDirections extends AsyncTask<GeoPoint, Void, Void> {
		@Override
		protected Void doInBackground(GeoPoint... startAndEnd) {
	        // Don't do anything until we've connected to the RouteService.
			Message msg = mHandler.obtainMessage();
			
			// If we are unable to get a GPS fix on the current location yet, 
			// then don't try to get directions.
			if(startAndEnd[0] == null) {
				msg.what = PhoneWandActivity.MESSAGE_CURRENT_LOCATION_UNKNOWN;
				mHandler.sendMessage(msg);
				return null;
			}
			
			mGettingRoute = true;
			            
			// Send a Message which will start a progress dialog.
			msg.what = PhoneWandActivity.MESSAGE_START_ROUTE_RETRIEVAL;
			mHandler.sendMessage(msg);
			msg = mHandler.obtainMessage();
			
			try {
				String jsonString = 
						callWebService(getURL(startAndEnd[0],startAndEnd[1]));
				
				if (D) Log.i(TAG, getString(R.string.google_maps_result)+"\n"+jsonString);
				
				mGettingRoute = false;

				msg.obj = parseRoute(jsonString);
				
				// Route steps have been returned.
				if(msg.obj != null) {
					// Send a success message to to the main thread's Handler.
					msg.what = PhoneWandActivity.MESSAGE_DISPLAY_ROUTE;
					
				// There are no more steps in the route; we're there!
				} else {
					// Send a success message to to the main thread's Handler.
					msg.what = PhoneWandActivity.MESSAGE_END_OF_ROUTE;
				}
			} catch(DataFormatException e) {
				mGettingRoute = false;
				
				// Send an error message to to the main thread's Handler.
				msg.what = PhoneWandActivity.MESSAGE_ROUTE_PARSING_ERROR;
				msg.obj = e;
			} catch(Exception e) {
				mGettingRoute = false;
				
				// Send an error message to to the main thread's Handler.
				msg.what = PhoneWandActivity.MESSAGE_GET_REQUEST_ERROR;
				msg.obj = e;
			}
			
			mHandler.sendMessage(msg);
			return null;
		}
		
		/**
		 * Execute a GET request using the given URL.
		 * @param A String representing the URL to be used for the GET request.
		 * @return A String representing the returned content from the request,
		 * 			or null if the request failed.
		 * @throws IOException 
		 * @throws ClientProtocolException 
		 */
		public String callWebService(String url) throws ClientProtocolException, IOException{
			// Setup request
			HttpClient httpclient = new DefaultHttpClient();
			HttpGet request = new HttpGet(url);
			String result = null;
			ResponseHandler<String> handler = new BasicResponseHandler();
			
			// Execute request
			result = httpclient.execute(request, handler);
			
			// Close request
			httpclient.getConnectionManager().shutdown();
			
			return result;
		}
		
		/**
		 * Creates a Google Maps URL which contains source and destination 
		 * address parameters based upon the values of the given src and dest 
		 * GeoPoint parameters.
		 * @param src The GeoPoint representing the source address
		 * @param dest The GeoPoint representing the destination address
		 * @return The String representing the Google Maps URL with parameters 
		 * 			which match the given parameters.
		 */
		private String getURL(GeoPoint src, GeoPoint dest) {
			// Parse PhoneWandActivity.mCurrentLocation and mDestination into Strings.
			String origin = 
					Double.toString((double) src.getLatitudeE6() / 1.0E6)+","+
					Double.toString((double) src.getLongitudeE6() / 1.0E6);
			String destination = 
					Double.toString((double) dest.getLatitudeE6() / 1.0E6)+","+
					Double.toString((double) dest.getLongitudeE6() / 1.0E6);
			
			StringBuilder urlString = new StringBuilder();
			// The URL before parameters.
			urlString.append("http://maps.googleapis.com/maps/api/directions/json?origin=");
			// Get directions from the current location.
			urlString.append(origin);
			// Get directions to the given destination.
			urlString.append("&destination=");
			urlString.append(destination);
			// Get walking directions (as opposed to driving directions), get 
			// only the single best direction, use metric units, and add the 
			// system parameter which is required by Google.
			urlString.append("&mode=walking&alternatives=false&units=metric&sensor=true");
			
			String url = urlString.toString();
			
			if (D) Log.d(TAG, getString(R.string.url_created) + " " + url);
			
			return url;
		}
		
		/**
		 * Parses the directions from the given JSON response.  If the routes 
		 * array is empty, then null is returned.
		 * @param encoded The encoded String from Google Maps.
		 * @return An array of Steps parsed from the Google Maps route.
		 * @throws DataFormatException if the JSON response is constructed in an 
		 * 			unexpected fashion.
		 */
		private RouteStep[] parseRoute(String jsonResponse) throws DataFormatException {
			if (D) Log.d(TAG, getString(R.string.start_JSON_parsing));
			try {
				JSONObject json = new JSONObject(jsonResponse);
				String status = json.getString("status");
				
				if(!status.equals("OK")) {
					throw new Exception("Invalid status: "+status);
				}
				
				JSONArray routes = json.getJSONArray("routes");
				
				// If routes array is empty, then presumably we are at the destination.
				if(routes.length() == 0) {
					return null;
				}
				
				JSONArray legs = routes.getJSONObject(0).getJSONArray("legs");
				JSONArray steps = legs.getJSONObject(0).getJSONArray("steps");
				
				int length = steps.length();
				RouteStep[] stepArray = new RouteStep[length];
				mDirectionsText = new String[length];
				
				JSONObject jsonStep, location;
				GeoPoint currentStart, currentEnd;
				List<GeoPoint> currentPolyline;
				int currentDuration, currentDistance, lat, lon;
				String currentDescription;
				
				// Loop over the steps, creating a new Step object for each 
				// and adding it to the mSteps array.
				for(int i = 0; i < length; i++) {
					if (D) Log.d(TAG, getString(R.string.parsing_new_step));
					
					// Start location.
					jsonStep = steps.getJSONObject(i);
					location = jsonStep.getJSONObject("start_location");
					lat = (int)(location.getDouble("lat")*1E6);
					lon = (int)(location.getDouble("lng")*1E6);
					currentStart = new GeoPoint(lat, lon);
					// End location.
					location = jsonStep.getJSONObject("end_location");
					lat = (int)(location.getDouble("lat")*1E6);
					lon = (int)(location.getDouble("lng")*1E6);
					currentEnd = new GeoPoint(lat, lon);
					// Distance and duration.
					currentDuration = jsonStep.getJSONObject("duration").getInt("value");
					currentDistance = jsonStep.getJSONObject("distance").getInt("value");
					// Textual description.
					currentDescription = jsonStep.getString("html_instructions");
					currentDescription = currentDescription.replaceAll("<div", " <div");
					currentDescription = currentDescription.replaceAll("<(.|\n)*?>", "");
					// Set of GeoPoints detailing the step's path.
					currentPolyline = parsePolyLine(
							jsonStep.getJSONObject("polyline").getString("points"));
					
					// Create and add new Step.
					stepArray[i] = new RouteStep(currentStart, currentEnd, currentPolyline, 
							currentDuration, currentDistance, currentDescription);
					// Add the step's textual direction to the mDirectionText array.
					mDirectionsText[i] = currentDescription;
				}
				
				if (D) Log.d(TAG, getString(R.string.end_JSON_parsing));
				return stepArray;
				
			} catch(Exception e) {
				throw new DataFormatException(PhoneWandActivity.getStackTrace(e));
			}
		}
		
		/**
		 * Parse the given polyline into an array of GeoPoints.
		 * The algorithm is from http://facstaff.unca.edu/.
		 * @param encodedString The polyline String which is to be decoded.
		 * @return An array of GeoPoints representing the given polyline.
		 */
		private List<GeoPoint> parsePolyLine(String encodedString) throws DataFormatException {
			// Replace double backslashes by single.
			encodedString = encodedString.replace("\\\\", "\\"); //escape char lol
			
			// Decode
			List<GeoPoint> polyline = new ArrayList<GeoPoint>();
			int index = 0, len = encodedString.length();
			int lat = 0, lon = 0;
			
			if (D) Log.d(TAG, getString(R.string.start_decoding));
			
			while (index < len) {
				int b, shift = 0, result = 0;
				
				do {
					b = encodedString.charAt(index++) - 63;
					result |= (b & 0x1f) << shift;
					shift += 5;
				} while (b >= 0x20);
				
				int dlat = ((result & 1) != 0 ? ~(result >> 1) : 
						(result >> 1));
				lat += dlat;
				
				shift = 0;
				result = 0;
				
				if(encodedString.length() > index) {
					do {
						b = encodedString.charAt(index++) - 63;
						result |= (b & 0x1f) << shift;
						shift += 5;
					} while (b >= 0x20);
				}
				
				int dlon = ((result & 1) != 0 ? ~(result >> 1) : 
						(result >> 1));
				lon += dlon;
				
				GeoPoint p = new GeoPoint((int)(((double)lat/1E5)*1E6), 
									(int)(((double)lon/1E5)*1E6));
				polyline.add(p);
				
				if (D) Log.d(TAG, getString(R.string.added_geopoint)+
						": lat=" + p.getLatitudeE6() +
						", lng=" + p.getLongitudeE6());
			}
			if (D) Log.d(TAG, getString(R.string.end_decoding));
			
			return polyline;
		}
	}
	
	/**
	 * Inner (non-static) class used to represent a step in the route.  Each 
	 * step consists of a start location GeoPoint, an end location GeoPoint, a 
	 * distance int (meters), an estimated duration int (seconds), and 
	 * description String.
	 */
	private class RouteStep {
		private GeoPoint startLocation;
		private GeoPoint endLocation;
		private List<GeoPoint> polyline;
		private int distance;
		private int duration;
		private String description;
		
		// Constructor.
		public RouteStep(GeoPoint startLocation, GeoPoint endLocation, 
				List<GeoPoint> polyline, int distance, int duration, 
				String description) {
			this.startLocation = startLocation;
			this.endLocation = endLocation;
			this.distance = distance;
			this.duration = duration;
			this.description = description;
			this.polyline = polyline;
		}
		
		/** Returns a GeoPoint representing the start location of this step. */
		public GeoPoint getStartLocation() {return startLocation;}
		
		/** Returns a GeoPoint representing the end location of this step. */
		public GeoPoint getEndLocation() {return endLocation;}
		
		/** Returns the polyline representing the path of this step. */
		public List<GeoPoint> getPolyline() {return polyline;}
		
		/** Returns an int representing the distance of this step (in meters). */
		public int getDistance() {return distance;}
		
		/** Returns an int representing the duration of this step (in seconds). */
		public int getDuration() {return duration;}
		
		/**
		 * Returns a String which contains a textual description of this step 
		 * in the route.
		 */
		public String getDescription() {return description;}
	}
	
	// Sends and receives Messages and Runnables between threads.
	public final Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PhoneWandActivity.MESSAGE_START_ROUTE_RETRIEVAL:
				if (D) Log.d(TAG,getString(R.string.get_directions_start));
				
				showDialog(PhoneWandActivity.INTERNET_DIALOG);
				
				break;
			case PhoneWandActivity.MESSAGE_DISPLAY_ROUTE:
				if (D) Log.d(TAG, getString(R.string.get_directions_end_success));
				
				PhoneWandActivity.mProgressDialog.dismiss();
				
				// Update the current route.
				mRouteSteps = (RouteStep[]) msg.obj;
				mRouteExists = true;
				mCurRouteStep = findNearestRouteStep();
				mCurGeoStep   = findNearestGeoStep();

				// Draw the path.
				drawPath();
				// Invalidate the MapView so that it (re)draws itself.
				mMapView.invalidate();
				mSecondInternetAttempt = false;
				
				notifyUser(PhoneWandActivity.NOTIFY_ROUTE_DISPLAYED);
				
				break;
			case PhoneWandActivity.MESSAGE_ROUTE_PARSING_ERROR:
				if (D) Log.e(TAG, getString(R.string.route_parsing_fail));
				
				PhoneWandActivity.mProgressDialog.dismiss();
				notifyUser(PhoneWandActivity.NOTIFY_PARSING_ERROR, PhoneWandActivity.getStackTrace((Throwable)msg.obj));
				
				killOrienterScreen();
				
				return;
			case PhoneWandActivity.MESSAGE_END_OF_ROUTE:
				PhoneWandActivity.mProgressDialog.dismiss();
				
				onReachedDestination();
				
				return;
			case PhoneWandActivity.MESSAGE_GET_REQUEST_ERROR:
				if (D) Log.w(TAG, getString(R.string.get_directions_end_fail));
				
				PhoneWandActivity.mProgressDialog.dismiss();
				notifyUser(PhoneWandActivity.NOTIFY_NO_INTERNET, 
						PhoneWandActivity.getStackTrace((Throwable)msg.obj));
				
				// Try again to find the route.
				if(!mSecondInternetAttempt) {
					new GetDirections().execute(PhoneWandActivity.mCurrentLocation,mDestination);
					mSecondInternetAttempt = true;
				
				// Return to the RouteInput screen.
				} else {
					mSecondInternetAttempt = false;
					killOrienterScreen();
					return;
				}
				
				break;
			case PhoneWandActivity.MESSAGE_CURRENT_LOCATION_UNKNOWN:
				notifyUser(PhoneWandActivity.NOTIFY_NO_GPS);
				killOrienterScreen();
				return;
			default:
				if (D) Log.e(TAG,getString(R.string.invalid_message_fail)+msg.what);
				killOrienterScreen();
				return;
			}
		}
	};
	
	private int findNearestRouteStep(){
		return 0;
	}
	
	private int findNearestGeoStep(){
		return 0;
	}
	
	private void advanceGeoStep(){
		Log.d(TAG,"+++ advanceGeoStep +++");
		
		//vibration feedback
		PhoneWandActivity.buzz(PhoneWandActivity.GEO_TURN_VIBES);
		
		mCurGeoStep++;
		PhoneWandActivity.ttsSpeak(getString(R.string.tts_advance_geo), TextToSpeech.QUEUE_FLUSH);
		
		if(mRouteExists) {
			// Let the user know the current step in the route.
			PhoneWandActivity.ttsSpeak("Current step is "+mRouteSteps[mCurRouteStep].getDescription(), 
					TextToSpeech.QUEUE_ADD);
		}

		if(mCurGeoStep==mRouteSteps[mCurRouteStep].getPolyline().size()-1)
			advanceRouteStep();
	}
	
	private void advanceRouteStep(){
		Log.d(TAG,"+++ advanceRouteStep +++");
		
		//vibration feedback
		PhoneWandActivity.buzz(PhoneWandActivity.STEP_TURN_VIBES);
		
		mCurGeoStep=0;
		mCurRouteStep++;
		if(mCurGeoStep==mRouteSteps.length-1) 
			onReachedDestination();
		
		PhoneWandActivity.ttsSpeak(getString(R.string.tts_advance_route), TextToSpeech.QUEUE_FLUSH);
	}
	
	// Sets mNextHeanding by computing the compass heading to the next geoPoint.
	private void computeHeading(){
		if (mRouteSteps != null) {
			GeoPoint p1 = PhoneWandActivity.mCurrentLocation;
			GeoPoint p2 = mRouteSteps[mCurGeoStep].getPolyline().get(mCurGeoStep+1);
			double lat1 = p1.getLatitudeE6()  / 1e6;
			double lon1 = p1.getLongitudeE6() / 1e6;
			double lat2 = p2.getLatitudeE6()  / 1e6;
			double lon2 = p2.getLongitudeE6() / 1e6;
			float[] results = new float[3];
			
			// This computes distance, initial heading, final heading 
			Location.distanceBetween(lat1, lon1, lat2, lon2, results);
			
			// Check if we are near enough the next GeoStep to move over!
			if(results[0] < DISTANCE_EPS){
				advanceGeoStep();
			}else{// compute compass direction
				//mNextHeading.setValue(computeBearing(lat1,lon1,lat2,lon2));
				// PhoneWandActivity.mCurrentLocation;
				mNextHeading.setValue((int)results[2]);
				
				if(D){
				Log.d(TAG,"computeHeading: mNextHeading   = " + mNextHeading.getValue());
				Log.d(TAG,"computeHeading: distance       = " + results[0]);
				Log.d(TAG,"computeHeading: bearing  init? = " + results[1]);
				Log.d(TAG,"computeHeading: bearing final? = " + results[2]);
				}
			}
		}
	}
	
	/** Called when the user reaches the destination. */
	private void onReachedDestination() {
		notifyUser(PhoneWandActivity.NOTIFY_END_OF_ROUTE);
		
		PhoneWandActivity.buzz(PhoneWandActivity.DESTINATION_VIBES);
		
		setResult(RESULT_OK);
		finish();
	}
	
	/** Called when a dialog is to be created for the first time. */
	protected Dialog onCreateDialog(int id, Bundle args) {
		switch(id) {
		case PhoneWandActivity.GPS_DIALOG:
			PhoneWandActivity.mProgressDialog = new ProgressDialog(this);
			PhoneWandActivity.mProgressDialog.setMessage(getString(R.string.finding_gps));
			PhoneWandActivity.mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
			return PhoneWandActivity.mProgressDialog;
		case PhoneWandActivity.INTERNET_DIALOG:
			PhoneWandActivity.mProgressDialog = new ProgressDialog(this);
			PhoneWandActivity.mProgressDialog.setMessage(getString(R.string.downloading_route));
			PhoneWandActivity.mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			
			return PhoneWandActivity.mProgressDialog;
		default:
			return null;
		}
	}
	
	/** Called every time a dialog is displayed. */
	@Override
	protected void onPrepareDialog(int id, Dialog dialog, Bundle args) {
		String logString = null;
		String ttsString = null;
		
		switch(id) {
		case PhoneWandActivity.GPS_DIALOG:
			logString = "Displaying the gps dialog";
			ttsString = getString(R.string.finding_gps);
			
			break;
		case PhoneWandActivity.INTERNET_DIALOG:
			logString = "Displaying the internet dialog";
			ttsString = getString(R.string.downloading_route);
			
			break;
		default:
			break;
		}
		
		// Log the fact that the dialog is being shown.
		Log.w(TAG, logString);
		// Play an audio message.
		PhoneWandActivity.ttsSpeak(ttsString, TextToSpeech.QUEUE_FLUSH);
		// Vibrate the phone.
		PhoneWandActivity.buzz(PhoneWandActivity.PROGRESS_DIALOG_VIBES);
	}
	
	/** This method must be truthfully implemented for legal issues. */
	@Override
	protected boolean isRouteDisplayed() {
		return mRouteExists;
	}

	/** Methods for updating and controlling compass heading */
	@Override
	public void onSensorChanged(SensorEvent event) {
		if(mSensor.equals(event.sensor)) {
			float[] values = event.values;
			int heading = (int) values[0];
			
			// Only set heading if it moves far enough.
			if(!mOnPause && !mHeading.inRange(heading, MIN_HEADING_CHANGE)) {
				mHeading.setValue(heading);
				
				// Redraw compass.
				drawPath();
				
				// Vibrate if in range of target heading.
				if(mOrienter && mHeading.inRange(mNextHeading.getValue(), ORIENTING_EPS)){
					PhoneWandActivity.mVibrator.vibrate(PhoneWandActivity.VIBES,1);
				} else {
					PhoneWandActivity.mVibrator.cancel();
				}
			}
		}		
	}
	
	/**
     * Used for detecting the user's gestures on the screen for the purpose 
     * of opening new screens or interacting with the user in other ways.
     */
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }
	
	public void welcomeSpeech(){
		PhoneWandActivity.ttsSpeak(mWelcomeSpeech, TextToSpeech.QUEUE_ADD);
		helpDirections();
	}
	
	protected void helpDirections() {
		PhoneWandActivity.swipeBuzz();
		PhoneWandActivity.ttsSpeak(mInstructionsSpeech, TextToSpeech.QUEUE_ADD);
	}
	
	private void mOrienterOn(){
		PhoneWandActivity.buzz(PhoneWandActivity.MAGIC_ON_VIBES);
		PhoneWandActivity.ttsSpeak(getString(R.string.tts_orienter_on), TextToSpeech.QUEUE_FLUSH);
	}
	
	private void mOrienterOff(){
		PhoneWandActivity.buzz(PhoneWandActivity.MAGIC_OFF_VIBES);
		PhoneWandActivity.ttsSpeak(getString(R.string.tts_orienter_off), TextToSpeech.QUEUE_FLUSH);
	}
	
    /** Used to detect swipes and double-taps. */
    private class GestureController 
    		implements GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {
    	public boolean onDoubleTap(MotionEvent e){
    		if (D) Log.d(TAG, "onDoubleTap: We double tapped!!!");
    		if(!mOrienter)
    			mOrienterOn();
    		else
    			mOrienterOff();
    		mOrienter = !mOrienter;
    		return true;
    	}

    	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
    			float velocityY) {
    		int scaledDistance;
    		int scaledPath;

    		// get distance between points of the fling
    		double vertical = Math.abs(e1.getY() - e2.getY());
    		double horizontal = Math.abs(e1.getX() - e2.getX());

    		// convert dip measurements to pixels
    		final float scale = getResources().getDisplayMetrics().density;
    		scaledDistance = (int) (PhoneWandActivity.DISTANCE_DIP * scale + 0.5f);
    		scaledPath = (int) (PhoneWandActivity.PATH_DIP * scale + 0.5f);

    		// If horizontal motion is greater than vertical motion, then try for a horizontal 
    		// swipe.
    		if(horizontal >= vertical) {
    			// test vertical distance
    			if (vertical > scaledPath) {
    				return false;

    				// test horizontal distance and velocity
    			} else if (horizontal > scaledDistance && Math.abs(velocityX) > mMinScaledVelocity) {
    				if (velocityX < 0) { // right to left swipe
    					if (D) Log.v(TAG, "Forward swipe");
    					// open directions display screen
    					openDirectionsDisplayScreen();
    				} else { // left to right swipe
    					if (D) Log.v(TAG, "Backward swipe");
    					// Return to the RouteInput screen.
    					killOrienterScreen();
    				}
    				return true;
    			} else {
    				// not a good enough swipe
    				return false;
    			}

    			// Vertical motion is greater than horizontal motion, so try for a vertical swipe.
    		} else {
    			// test horizontal distance
    			if (horizontal > scaledPath) {
    				return false;

    				// test vertical distance and velocity
    			} else if (vertical > scaledDistance && Math.abs(velocityY) > mMinScaledVelocity) {
    				if (velocityY < 0) { // top to bottom swipe
    					if (D) Log.v(TAG, "Downward swipe");
    					// open check route status screen
    					checkRoute();
    				} else { // bottom to top swipe
    					if (D) Log.v(TAG, "Upward swipe");
    						// Open CurrentLocation screen
    						PhoneWandActivity.swipeBuzz();
    						PhoneWandActivity.swipeSpeech(PhoneWandActivity.SW_UP);
    						Intent intent = new Intent(RouteOrienter.this, CurrentLocation.class);
    						startActivity(intent);
    				}
    				return true;
    			} else {
    				// not a good enough swipe
    				return false;
    			}
    		}
    	}

		@Override
		public void onLongPress(MotionEvent arg0) {
			if (D) Log.v(TAG, "Long Press");
			helpDirections();
		}
		
		// ----- Unimplemented methods. -----
		@Override
		public boolean onDoubleTapEvent(MotionEvent arg0) {
			// Do nothing.
			return false;
		}
		
		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// Do nothing.
			return false;
		}
		
		@Override
		public boolean onDown(MotionEvent arg0) {
			// Do nothing.
			return false;
		}
		
		@Override
		public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
				float arg3) {
			// Do nothing.
			return false;
		}
		
		@Override
		public void onShowPress(MotionEvent arg0) {
			// Do nothing.
		}
		
		@Override
		public boolean onSingleTapUp(MotionEvent arg0) {
			// Do nothing.
			return false;
		}
    }
    
	/**
	 * Class for integer and modulus for easier wrapping around
	 * and range comparison methods.
	 */
	public class ModulusInteger {
		private int value;	 	//0<= value <= modulus
		private int modulus;	//modulus base of integer
		
		public ModulusInteger(int val, int mod){
			if(mod > 0)
				modulus = mod;
			else
				throw new IllegalArgumentException();
			setValue(val);
		}
		
		public int getValue(){
			return value;
		}
		
		public void setValue(int val){
			value = putInMod(val);
		}
		
		//returns value to be in range of modulus
		public int putInMod(int val){
			while(val < 0)
					val+=modulus;
			while(val > modulus)
					val-=modulus;
			return val;
		}
		
		// Check if current value is in range of Anchor anc
		//	+/- epsilon eps.
		public boolean inRange(int anc, int eps){
			if( eps >= modulus/2)
				return true;
			int low  = putInMod(anc - eps);
			int high = putInMod(anc + eps);
			if( high < low ) //spillover top
				return (value >= low || value <= high);
			else
				return (value >= low && value <= high);
		}
		
		public int getErrorPart(int anc, int parts){
			if(parts <= 1)
				return 1;
			anc = putInMod(anc);
			int gran = (modulus/2)/parts;
			for(int i=1; i<= parts-1; i++)
				if( inRange(anc,i*gran) )
					return i;
			return parts;
			
		}
	}
	
	
	// -----Transition Methods-----
	/**
	 * Called when the user is declaring that he/she is currently at an 
	 * intersection.
	 */
	public void killOrienterScreen(){
		PhoneWandActivity.swipeBuzz();
		setResult(RESULT_CANCELED);
    	finish();
	}
	
	
	// -----Vibration Methods-----
	/**
	 * Notify the user via both a Toast message and an audio clip and record 
	 * an appropriate Log message.
	 * @param notification An int global constant representing the type of 
	 * 		notification to display.
	 */
	public void notifyUser(int notification) {
		notifyUser(notification, null);
	}
	
	/**
	 * Notify the user via both a Toast message and an audio clip and record 
	 * an appropriate Log message.
	 * @param notification An int global constant representing the type of 
	 * 		notification to display.
	 * @param extra This Object can be any sort of extra data that is needed 
	 * 		for determining what exactly to tell the user.
	 */
	public void notifyUser(int notification, Object extra) {
		String logString, toastString;
		
		// choose which dialog box Toast to display
		switch(notification) {
		case PhoneWandActivity.NOTIFY_NO_INTERNET:
			logString 	= getString(R.string.get_route_address_fail) + "\n" + extra;
			toastString = getString(R.string.please_connect_to_internet);
			break;
		case PhoneWandActivity.NOTIFY_NO_GPS:
			logString 	= getString(R.string.get_gps_fail) + "\n" + extra;
			toastString = getString(R.string.unable_to_find_location);
			break;
		case PhoneWandActivity.NOTIFY_PARSING_ERROR:
			logString 	= getString(R.string.route_parsing_fail) + "\n" + extra;
			toastString = getString(R.string.invalid_google_response);
			break;
		case PhoneWandActivity.NOTIFY_END_OF_ROUTE:
			logString 	= getString(R.string.end_of_route);
			toastString = getString(R.string.end_of_route);
			break;
		case PhoneWandActivity.NOTIFY_NO_DESTINATION_STRING:
			logString 	= getString(R.string.invalid_destination_string_log);
			toastString = getString(R.string.invalid_destination_string_toast);
			break;
		case PhoneWandActivity.NOTIFY_NO_ADDRESSES_FOUND:
			logString 	= getString(R.string.no_matches_found_log);
			toastString = getString(R.string.no_matches_found_toast);
			break;
		case PhoneWandActivity.NOTIFY_ROUTE_DISPLAYED:
			logString 	= getString(R.string.route_displayed_log);
			toastString = getString(R.string.route_displayed_toast);
			
			// Let the user know which destination was selected when the route 
			// has been setup for the first time.
			if(!mHaveSpokenCurDest) {
				mHaveSpokenCurDest = true;
				toastString += "  Destination is "+RouteInput.mDestinationString+".  ";
			}
			
			// Let the user know the current step in the route.
			toastString += "  Current step is "+mRouteSteps[mCurRouteStep].getDescription();
			
			break;
		default:
			logString 	= getString(R.string.unknown_notification_fail) 
							+ " " + notification;
			toastString = logString;
			break;
		}
		
		// Log the problem.
		Log.w(TAG, logString);
		// Play an audio message.
		PhoneWandActivity.ttsSpeak(toastString, TextToSpeech.QUEUE_FLUSH);
		// Display a toast message.
		Toast.makeText(this, toastString, Toast.LENGTH_SHORT).show();
		// Vibrate the phone.
		PhoneWandActivity.buzz(PhoneWandActivity.NOTIFICATION_VIBES);
	}
	
	
	// -----Unimplemented Methods from implemented listeners-----
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Do nothing.
	}
}