/**
 * @author Michael Q. Lam (mqtlam@cs.washington.edu)
 * @author Levi Lindsey (levisl@cs.washington.edu)
 * @author Chris Raastad (craastad@cs.washington.edu)
 * 
 * Designed to meet the requirements of the Winter 2011 UW course, 
 * CSE 481H: Accessibility Capstone
 * 
 * PhoneWandActivity: this is an class intended to unify
 * 	all the repetitive code used in other activity.  This includes
 * 	vibrations, TextToSpeech, DatabaseManagement, constants,
 * 	etc...
 */

package edu.uw.cse481h.phonewand;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Address;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;

public abstract class PhoneWandActivity 
		extends Activity 
		implements LocationListener, OnInitListener {
	// -----Debugging Constants-----
	// toggle display of debugging log messages
	protected static final boolean D = true;
	// log messages tag
	protected String TAG;
	
	
	// -----TextToSpeech Constants and Fields-----
	protected static final String[] TTS_APP_START = 	
		{"Welcome to the phone wand application.",
		 "Tap and hold on any screen to repeat directions."};
	protected static final String TEXT_ENTERED_EXTRA =		"text_entered";
	protected static final float  SLOW_SPEECH_RATE =		0.5f;
	protected static final float  MEDIUM_SPEECH_RATE =		1f;
	protected static final float  FAST_SPEECH_RATE =		1.75f;
	
	private float mSpeechRate;
	public static final Locale TTS_LANGUAGE = Locale.UK; //Locale.US;
	private static TextToSpeech mTTS;
	
	// used for utterances
	protected HashMap<String, String> mUtterance = new HashMap<String, String>();
	
	
	// -----Vibration System Constants and Fields-----
	// Handle for vibration system.
	public static Vibrator mVibrator;
	// Vibration arrays.
	public static final long[] KEY_ENTRY_VIBES =		{0, 50};
	public static final long[] SWIPE_VIBES =			{0, 100};
	public static final long[] DESTINATION_VIBES =		{0, 100, 200, 100, 100, 90, 10, 100};
	public static final long[] NOTIFICATION_VIBES =		{0, 100, 100, 500};
	public static final long[] PROGRESS_DIALOG_VIBES =	{0, 50, 50, 50, 50, 500};
	public static final long[] VIBES =					{0, 50, 100};
	public static final long[] GEO_TURN_VIBES =			{0, 100, 120, 220, 240, 340, 360, 460};
	public static final long[] STEP_TURN_VIBES =		{0, 400, 450, 850, 900, 1400}; 
	public static final long[] ORIENTER_PULSE_VIBES =	{0, 100, 1500, 1600, 3000};
	public static final long[] MAGIC_ON_VIBES =			{0, 300};
	public static final long[] MAGIC_OFF_VIBES =		{0, 70, 90, 160};
	
	
	// -----Gestures, Swipes, and Taps Constants and Fields-----
	private int mMinScaledVelocity; 
	// minimum swiping distance
	public static final float DISTANCE_DIP = 16.0f;
	// maximum allowed vertical deviation
	public static final float PATH_DIP = 40.0f;
	// GestureDetector for Swipe and Double Tap listeners
	protected GestureDetector mGestureDetector;
	
	// Swipe direction constants
	public static final int SW_UP = 0;
	public static final int SW_DN = 1;
	public static final int SW_LF = 2;
	public static final int SW_RT = 3;
	// Swipe direction text
	public static final String SW_TEXT 		= "Swiping ";
	public static final String SW_UP_TEXT 	= SW_TEXT + "up.";
	public static final String SW_DN_TEXT 	= SW_TEXT + "down.";
	public static final String SW_LF_TEXT 	= SW_TEXT + "left.";
	public static final String SW_RT_TEXT 	= SW_TEXT + "right.";
	
	
	// -----Database Access Constants-----
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "phone_wand";
	private static final String TIMESTAMP = "timestamp";
	
	private static final String BOOKMARKS_TABLE_NAME = "bookmarks";
	private static final String BOOKMARK_ID = "bookmark_id";
	private static final String BOOKMARK_ADDRESS = "bookmark_address";
	private static final String BOOKMARK_LAT = "bookmark_lat";
	private static final String BOOKMARK_LON = "bookmark_lon";
	private static final String CREATE_BOOKMARKS_TABLE = 
		"CREATE TABLE " 	+ BOOKMARKS_TABLE_NAME + " ( " + 
		BOOKMARK_ID 		+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
		BOOKMARK_ADDRESS 	+ " TEXT, " + 
		BOOKMARK_LAT 		+ " INTEGER, " + 
		BOOKMARK_LON 		+ " INTEGER, " + 
		TIMESTAMP 			+ " TEXT);";
	
	private static final String RECENTS_TABLE_NAME = "recents";
	private static final String RECENT_ID = "recent_id";
	private static final String CREATE_RECENTS_TABLE = 
		"CREATE TABLE " + RECENTS_TABLE_NAME + " ( " + 
		RECENT_ID 		+ " INTEGER PRIMARY KEY AUTOINCREMENT, " + 
		BOOKMARK_ID 	+ " INTEGER REFERENCES " + BOOKMARKS_TABLE_NAME + "(" + BOOKMARK_ID + ")," + 
		TIMESTAMP 		+ " TEXT);";
	
	// Save the bookmarkId of the most recent address to which the user found a route.
	public static long mCurrentBookmarkId = -1;
	
	// database access objects
	private static DbHelper mOpenHelper;
	private static SQLiteDatabase mDataBase;
	
	
	// -----Control Flow Message Constants-----
	// Constants used for identifying assorted Messages.
	public static final int MESSAGE_START_ROUTE_RETRIEVAL =		0;
	public static final int MESSAGE_DISPLAY_ROUTE =				1;
	public static final int MESSAGE_GET_REQUEST_ERROR =			2;
	public static final int MESSAGE_ROUTE_PARSING_ERROR =		3;
	public static final int MESSAGE_END_OF_ROUTE =				4;
	public static final int MESSAGE_START_ADDRESS_RETRIEVAL =	5;
	public static final int MESSAGE_CURRENT_LOCATION_UNKNOWN =	6;
	public static final int MESSAGE_GET_ADDRESSES_SUCCESS =		7;
	public static final int MESSAGE_GET_ADDRESSES_FAIL =		8;	
	
	// Constants used for identifying assorted notifications.
	public static final int NOTIFY_NO_INTERNET =				0;
	public static final int NOTIFY_NO_GPS =						1;
	public static final int NOTIFY_END_OF_ROUTE =				2;
	public static final int NOTIFY_PARSING_ERROR =				3;
	public static final int NOTIFY_NO_DESTINATION_STRING =		4;
	public static final int NOTIFY_NO_ADDRESSES_FOUND =			5;
	public static final int NOTIFY_ROUTE_DISPLAYED =			6;
	
	// Constants used for determining which Activity was/is called for returning a result.
	public static final int REQUEST_CODE_ROUTE_ARCHIVE =		1;
	public static final int REQUEST_CODE_POSSIBLE_ADDRESSES =	2;
	public static final int REQUEST_CODE_TOUCH_KEYBOARD =		3;
	public static final int REQUEST_CODE_ROUTE_ORIENTER =		4;
	public static final int REQUEST_CODE_CURRENT_LOCATION =		5;
	
	// For passing the latitude and longitude extras in the Intent.
	public static final String LATITUDE_EXTRA =					"latitude";
	public static final String LONGITUDE_EXTRA =				"longitude";
	public static final String INDEX_EXTRA =					"index";
	public static final String RECORD_ID_EXTRA =				"record_id";
	public static final String STARTING_TEXT_EXTRA =			"starting_text";
	
	// For determining if the latitude and longitude extras were passed 
	// 	correctly in the Intent.
	public static final int MICRODEGREE_UPPER_BOUND = 360000000;
	
	// Constants used for identifying assorted Dialogs.
	public static final int GPS_DIALOG =						0;
	public static final int INTERNET_DIALOG =					1;
	
	// Shows the user when the app is performing a time-intensive task; e.g. 
	// getting a fix on a GPS location or getting a route from Google Maps.
	public static ProgressDialog mProgressDialog;
	
	// For determining a significant time gap between locations.
	private static final int FORTY_FIVE_SECONDS =		45000;		// milliseconds
	
	// Constants used in determining how often to update the user's location.
	private static final int MIN_UPDATE_TIME =			60000;		// in milliseconds (1 minute)
	private static final int MIN_UPDATE_DISTANCE =		100;		// in meters (100 meters)
	
    // For the location services.
	public static LocationManager mLocationManager;
	
	// For determining relative goodness of current location reading.
	private static Location mCurrentBestLocation;
	
	// Start location.
	public static GeoPoint mCurrentLocation;

	// For one time calls on application start.
	private static boolean mApplicationStarted = false;
	
	protected long mScreenCreateTime;
	
	private String mWelcomeSpeech;
	private String mInstructionsSpeech;
	
	public static boolean mOpeningOrienterOrKeyBoard = false;
	
	
	// -----Activity Life Cycle Methods-----
	@Override
	public void onCreate(Bundle savedInstanceState) {
		mScreenCreateTime = System.currentTimeMillis();
		
		super.onCreate(savedInstanceState);
		
		// Setup some basic fields depending on which child Activity is currently being created.
		setupScreen();
		
		if (D) Log.v(TAG, "+++ ON CREATE  +++");
		
		
		if(!mApplicationStarted) onApplicationStart();
		
    	// initialize gesture detector for double taps and swipes
        mGestureDetector = new GestureDetector(new GestureController());
        
        // first tts spoken directions at start of screen
		welcomeSpeech();
	}
	
	/** Called when the system resumes this Activity. */
	@Override
	public void onResume() {
		if (D) Log.v(TAG, " ++ ON RESUME  ++");
		
		super.onResume();
		
		// Register LocationManager to receive both GPS and network location 
		// updates.
		Log.w(TAG,"\t+ REGISTERED LOCATION LISTENER +");
		mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 
				MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);
		mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 
				MIN_UPDATE_TIME, MIN_UPDATE_DISTANCE, this);
	}
	
	/** Called when the system pauses this Activity. */
	@Override
	public synchronized void onPause() {
		if (D) Log.v(TAG, "  - ON PAUSE   -");
		
		super.onPause();
		
		// If the user is leaving the PhoneWand application, then we should no 
		// longer listen to location changes.
		if(!mOpeningOrienterOrKeyBoard && !(this instanceof DirectionsDisplay) && 
				!((this instanceof CurrentLocation) && RouteOrienter.mOnCreate)) {
			Log.w(TAG,"\t- UNREGISTERED LOCATION LISTENER -");
			mLocationManager.removeUpdates(this);
		}
	}
	
	/** Called when the system destroys this Activity. */
	@Override
	public synchronized void onDestroy() {
		if (D) Log.v(TAG, "--- ON DESTROY ---");
		
		super.onDestroy();
		
		mProgressDialog.dismiss();
	}
	
	/** We call this internally at the start of the application. */
	private void onApplicationStart() {
		mApplicationStarted = true;
		
		// Open the database.
		mOpenHelper = new DbHelper(this);
		try {
			mDataBase = mOpenHelper.getWritableDatabase();
		} catch(SQLiteException e) {
			Log.e(TAG,getString(R.string.db_fail)+": "+getStackTrace(e));
		}
		
		// Clean out any invalid bookmark records.
		cleanBookmarkRecords();
		
		// Initialize the application's text-to-speech object.
		mTTS = new TextToSpeech(this, this);
		
		// Initialize the application's progress dialog object.
		PhoneWandActivity.mProgressDialog = new ProgressDialog(this);
		
        // Connect to the system's vibrator.
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		
		// Handle to system Location Manager
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
	}
	
	/**
	 * Setup some basic fields depending on which child Activity is currently 
	 * being created.
	 */
	private void setupScreen() {
		TAG = this.getLocalClassName();
		
		if(this instanceof RouteInput) {
			mWelcomeSpeech = getString(R.string.route_input_welcome);
			mInstructionsSpeech = getString(R.string.route_input_instructions);
		} else if(this instanceof RouteArchive) {
			mWelcomeSpeech = getString(R.string.route_archive_welcome);
			mInstructionsSpeech = getString(R.string.route_archive_instructions);
		} else if(this instanceof CurrentLocation) {
			mWelcomeSpeech = getString(R.string.current_location_welcome);
			mInstructionsSpeech = getString(R.string.current_location_instructions);
		} else if(this instanceof PossibleAddresses) {
			if(!RouteInput.mFromCurrentLocationScreen) {
				mWelcomeSpeech = getString(R.string.possible_addresses_welcome_1);
				mInstructionsSpeech = getString(R.string.possible_addresses_instructions_1);
			} else {
				mWelcomeSpeech = getString(R.string.possible_addresses_welcome_2);
				mInstructionsSpeech = getString(R.string.possible_addresses_instructions_2);
			}
		} else if(this instanceof DirectionsDisplay) {
			mWelcomeSpeech = getString(R.string.directions_display_welcome);
			mInstructionsSpeech = getString(R.string.directions_display_instructions);
		} else {
			Log.e(TAG, "Error: unknown Activity extending PhoneWandActivity " +
					"class in setupScreen()");
			finish();
			return;
		}
	}
	
	/** Close screen and return failure. */
	protected void cancelScreen() {
		swipeBuzz();
		setResult(RESULT_CANCELED);
		finish();
		return;
	}
	
	
	// -----TextToSpeech Methods-----
	public float getSpeechRate() {
		return mSpeechRate;
	}
	
	public void setSpeechRate() {
		mTTS.setSpeechRate(mSpeechRate);
	}
	
	/** Return true if the TextToSpeech object has been instantiated and is ready to be called. */
	public static boolean isTTSSetup() {
		return mTTS != null;
	}
	
	public static boolean ttsSpeak(String text, int queueMode, HashMap<String, String> params) {
		if(mTTS != null) mTTS.speak(text, queueMode, params);
		return true;
	}
	
	@Override
	public void onInit(int arg0) {
		mTTS.setLanguage(TTS_LANGUAGE);
    	appStartSpeech();
	}
	
	public static boolean ttsSpeak(String text, int queueMode) {
		return ttsSpeak(text, queueMode, null);
	}
	
	public static boolean ttsSpeak(String text) {
		return ttsSpeak(text, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	public static void swipeSpeech(int dir){
		switch(dir){
		case SW_UP:
			ttsSpeak(SW_UP_TEXT);
		break;
		case SW_DN:
			ttsSpeak(SW_DN_TEXT);
		break;
		case SW_LF:
			ttsSpeak(SW_LF_TEXT);
		break;
		case SW_RT:
			ttsSpeak(SW_RT_TEXT);
		break;
		default:
			;
		}
	}
	
	
	// -- Voice Sequences --
	/** TTS speaks this message when activity is in focus. */
	protected void welcomeSpeech() {
		ttsSpeak(mWelcomeSpeech, TextToSpeech.QUEUE_ADD);
		helpDirections();
	};
	
	/** TTS speaks directions for navigating the current screen. */
	protected void helpDirections() {
		swipeBuzz();
		ttsSpeak(mInstructionsSpeech, TextToSpeech.QUEUE_ADD);
	}
	
	/** TTS speaks this message when program first starts. */
	private void appStartSpeech(){
		for(String dir : TTS_APP_START)
			ttsSpeak(dir, TextToSpeech.QUEUE_ADD);
	}
	
	
	// -----Vibration Methods-----
	public static void buzz(long[] vibesPattern) {
		mVibrator.cancel();
		mVibrator.vibrate(vibesPattern, -1);
	}
	
	public static void swipeBuzz() {
		buzz(SWIPE_VIBES);
	}
	
	
	// -----Gesture Methods-----
	// used to detect swipes and double taps 
	@Override
    public boolean onTouchEvent(MotionEvent event) {
        mGestureDetector.onTouchEvent(event);
        return true;
    }
	
	// Quick action Gesture methods intended to be overwritten by subclasses.
	protected abstract boolean 	doubleTap ();
	protected abstract void 	swipeUp	  ();
	protected abstract void 	swipeDown ();
	protected abstract void 	swipeLeft ();
	protected abstract void 	swipeRight();
	
	
	// -----Database Management Methods-----
	/** 
	 * Insert a record into the bookmarks table and return the new record's id. 
	 */
	public long addBookmarkRecord(String address, int lat, int lon) {
		// Ensure that the given address value is valid.
		if(address == null || address.length() < 4) {
			Log.e(TAG, "Error: an invalid address value was passed to addBookmarkRecord: "+address);
			return -1;
		}
		
		// Overwrite any pre-existing bookmark record with the given address value.
		deleteBookmarkRecord(getBookmarkIDByAddress(address));
		
		String timestamp = getTimeStamp();
		
		ContentValues values = new ContentValues();
		values.put(BOOKMARK_ADDRESS, address);
		values.put(BOOKMARK_LAT, lat);
		values.put(BOOKMARK_LON, lon);
		values.put(TIMESTAMP, timestamp);
		
		try {
			long bookmarkId = mDataBase.insert(BOOKMARKS_TABLE_NAME, null, values);
			
			Log.w(TAG, "Record added to " + BOOKMARKS_TABLE_NAME + " table: ("+bookmarkId + ", " + 
					address + ", " + lat + ", " + lon + ", " + timestamp + ")");
			
			return bookmarkId;
		} catch (Exception e) {
			Log.e(TAG, getString(R.string.add_fail) + ": " + getStackTrace(e));
			return -1;
		}
	}
	
	/** 
	 * Return the record stored for the given bookmarkId in the bookmarks table 
	 * {address,lat,lon,timestamp} or null if there exists no such bookmarkId.
	 */
	public Cursor getBookmarkRecord(long bookmarkId) {
		try{
			Cursor c = mDataBase.query(true, BOOKMARKS_TABLE_NAME, new String[] {BOOKMARK_ADDRESS, 
					BOOKMARK_LAT, BOOKMARK_LON, TIMESTAMP}, BOOKMARK_ID + 
					"=\'" + bookmarkId + "\'", null, null, null, null, null);
			if(c.getCount() < 1){
				c.close();
				return null;
			}
			c.moveToFirst();
			return c;
		}catch(Exception e){
			Log.e(TAG, getString(R.string.get_fail) + ": " + getStackTrace(e));
			return null;
		}
	}
	
	/** 
	 * Return the record ID stored for the given bookmarkAddress in the bookmarks table 
	 * or -1 if there exists no such bookmarkAddress.
	 */
	public long getBookmarkIDByAddress(String bookmarkAddress) {
		try{
			Cursor c = mDataBase.query(true, BOOKMARKS_TABLE_NAME, new String[] {BOOKMARK_ID},
					BOOKMARK_ADDRESS + "=\'" + bookmarkAddress + "\'", null, null, null, null, null);
			if(c.getCount() < 1){
				c.close();
				return -1;
			}
			c.moveToFirst();
			long bookmarkID = c.getLong(0);
			c.close();
			
			return bookmarkID;
		}catch(Exception e){
			Log.e(TAG, getString(R.string.get_fail) + ": " + getStackTrace(e));
			return -1;
		}
	}
	
	/** 
	 * Return the addresses stored in the bookmarks table 
	 * or an empty string array if the table is empty.
	 */
	public String[] getBookmarkAddresses() {
		Cursor c = mDataBase.query(true, BOOKMARKS_TABLE_NAME, new String[] {BOOKMARK_ADDRESS},
				null, null, null, null, null, null);
		try{
			int count = c.getCount();
			if(count < 1){
				c.close();
				return new String[0]; // return no records
			}
			
			String[] records = new String[count];
			
			c.moveToFirst();
			records[0] = c.getString(0);
			for (int i = 1; i < count; i++) {
				if(!c.moveToNext()) {
					c.close();
					return null;
				}
				records[i] = c.getString(0);
			}
			c.close();
			
			return records;
		}catch(Exception e){
			Log.e(TAG, getString(R.string.get_fail) + ": " + getStackTrace(e));
			if(c != null) c.close();
			return null;
		}
	}
	
	/** 
	 * Delete the bookmark record corresponding to the given bookmarkId. 
	 */
	public void deleteBookmarkRecord(long bookmarkId) {
		try {
			if(mDataBase.delete(BOOKMARKS_TABLE_NAME, BOOKMARK_ID+"=\'" + bookmarkId + 
					"\'", null) > 0) {
				Log.w(TAG,"Record deleted from " + BOOKMARKS_TABLE_NAME + 
						" table: bookmarkId = " + bookmarkId);
			}
		} catch (Exception e) {
			Log.e(TAG, getString(R.string.delete_fail) + ": " + getStackTrace(e));
		}
	}
	
	/**
	 * Cleans out the bookmark records; i.e. if any record has an invalid 
	 * address value, then that record is deleted.
	 */
	public void cleanBookmarkRecords() {
		String[] addresses = getBookmarkAddresses();
		
		for(String address : addresses) {
			if(address == null || address.length() < 4) {
				deleteBookmarkRecord(getBookmarkIDByAddress(address));
				Log.w(TAG, "A record with the invalid address, \""+address+"\", was deleted.");
			}
		}
	}
	
	// -----Location Methods-----
	/**
	 * Called when a new location is found by the network location provider.  
	 * Updates the current location.
	 */
	@Override
	public void onLocationChanged(Location l) {
		findCurrentLocation();
	}
	
	/**
	 * Uses the LocationManager to get a GeoPoint representing the latitude 
	 * and longitude of the user's current location.
	 */
	public static void findCurrentLocation() {
		// Specifies how to determine the user's location.
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setPowerRequirement(Criteria.NO_REQUIREMENT);
		criteria.setAltitudeRequired(false);
		
		String bestProvider = mLocationManager.getBestProvider(criteria, true);
		
		Location lastKnownLocation = mLocationManager.getLastKnownLocation(bestProvider);
		
		if(lastKnownLocation != null) {
			if(D && mCurrentBestLocation != null){
				Log.d("PhoneWandActivity", "in findCurrentLocation():" +
					"\n\tbestProvider =\t\t\t\t\t   " + bestProvider + 
					"\n\tlastKnownLocation.getTime() =\t\t" + lastKnownLocation.getTime() + 
					"\n\tlastKnownLocation.getAccuracy() =\t" + lastKnownLocation.getAccuracy() + 
					"\n\tlastKnownLocation.getProvider() =\t" + lastKnownLocation.getProvider() + 
					"\n\tmCurrentBestLocation.getTime() =\t " + mCurrentBestLocation.getTime() + 
					"\n\tmCurrentBestLocation.getAccuracy() = " + mCurrentBestLocation.getAccuracy() + 
					"\n\tmCurrentBestLocation.getProvider() = " + mCurrentBestLocation.getProvider());
			}
			
			mCurrentBestLocation = 
					isBetterLocation(lastKnownLocation) ? 
					lastKnownLocation : mCurrentBestLocation;
			
			// Parse the Location data into a GeoPoint object.
			int lat = (int)(mCurrentBestLocation.getLatitude()*1E6);
			int lon = (int)(mCurrentBestLocation.getLongitude()*1E6);
			mCurrentLocation = new GeoPoint(lat, lon);
		}
	}
	
	/**
	 * Determines whether the given Location reading is better than the 
	 * current best Location reading.
	 * @param testLocation The new Location that you want to evaluate
	 */
	private static boolean isBetterLocation(Location testLocation) {
		if (mCurrentBestLocation == null) {
			// A new location is always better than no location.
			return true;
		}
		
		// Check whether the new location fix is newer or older.
		long timeDelta = testLocation.getTime() - mCurrentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > FORTY_FIVE_SECONDS;
		boolean isSignificantlyOlder = timeDelta < -FORTY_FIVE_SECONDS;
		boolean isNewer = timeDelta > 0;
		
		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved.
		if (isSignificantlyNewer) {
			return true;
		// If the new location is more than two minutes older, it must be worse.
		} else if (isSignificantlyOlder) {
			return false;
		}
		
		// Check whether the new location fix is more or less accurate.
		int accuracyDelta = 
			(int) (testLocation.getAccuracy() - mCurrentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;
		
		// Check if the old and new location are from the same provider.
		boolean isFromSameProvider = isSameProvider(testLocation.getProvider(),
				mCurrentBestLocation.getProvider());
		
		// Determine location quality using a combination of timeliness and accuracy.
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
			return true;
		}
		return false;
	}
	
	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) {
			return provider2 == null;
		}
		return provider1.equals(provider2);
	}
	
	@Override
	public void onProviderDisabled(String provider) {}
	
	@Override
	public void onProviderEnabled(String provider) {}
	
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {}
	
	
	// -----OTHER Methods-----
	/**
	 * Extract a single address String from the given Address object.
	 * @param address The Address object from which to extract the String.
	 * @return The String representation.
	 */
	public static String getAddressString(Address address) {
		String addressString = "";
		int length = address.getMaxAddressLineIndex();
		
		for(int i = 0; i < length; i++) {
			addressString += address.getAddressLine(i)+" ";
		}
		
		return addressString;
	}
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
		case NOTIFY_NO_INTERNET:
			logString 	= getString(R.string.get_route_address_fail) + "\n" + extra;
			toastString = getString(R.string.please_connect_to_internet);
			break;
		case NOTIFY_NO_GPS:
			logString 	= getString(R.string.get_gps_fail) + "\n" + extra;
			toastString = getString(R.string.unable_to_find_location);
			break;
		case NOTIFY_PARSING_ERROR:
			logString 	= getString(R.string.route_parsing_fail) + "\n" + extra;
			toastString = getString(R.string.invalid_google_response);
			break;
		case NOTIFY_END_OF_ROUTE:
			logString 	= getString(R.string.end_of_route);
			toastString = getString(R.string.end_of_route);
			break;
		case NOTIFY_NO_DESTINATION_STRING:
			logString 	= getString(R.string.invalid_destination_string_log);
			toastString = getString(R.string.invalid_destination_string_toast);
			break;
		case NOTIFY_NO_ADDRESSES_FOUND:
			logString 	= getString(R.string.no_matches_found_log);
			toastString = getString(R.string.no_matches_found_toast);
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
	
	/**
	 * Return a String representing the current time in the form 
	 * 	'yyyy-mm-dd hh:mm:ss.nnnnnnnnn'. 
	 */
	public static String getTimeStamp() {
		return new Timestamp(System.currentTimeMillis()).toString();
	}
	
	/**
	 * Get a Throwable's stack trace in a readable form.  This is used for 
	 * displaying an Exception's stack trace in the Log even if it is caught 
	 * and handled by our code.
	 * 
	 * @param t A Throwable whose stack trace is to be printed.
	 * @return The String representing the stack trace.
	 */
	public static String getStackTrace(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw, true);
		t.printStackTrace(pw);
		pw.flush();
		sw.flush();
		return sw.toString();
	}
	
	/**
	 * Convert the given latitude or longitude from a double representation 
	 * 	into an int representation.
	 * @param location The double representation.
	 * @return The int representation.
	 */
	public static int doubleToInt(double location) {
		return (int)(location*1E6);
	}
	
	/**
	 * Convert the given latitude or longitude from an int representation into 
	 * 	a double representation.
	 * @param location The int representation.
	 * @return The double representation.
	 */
	public static double intToDouble(int location) {
		return 1.0/1E6*location;
	}
	
	
	// -----Transition Methods-----
	/** Open the given screen which will return a result. */
	public void openActivityForResult(int requestCode) {
		openActivityForResult(requestCode, -1, -1);
	}
	
	/** Open the given screen which will return a result. */
	public void openActivityForResult(int requestCode, int latitude, int longitude) {
		Intent intent;
		
		swipeBuzz();
		
		switch(requestCode) {
		case REQUEST_CODE_TOUCH_KEYBOARD:
			mOpeningOrienterOrKeyBoard = true;
			
			intent = new Intent(this, TouchKeyboard.class);
			break;
		case REQUEST_CODE_ROUTE_ARCHIVE:
			mOpeningOrienterOrKeyBoard = false;
			
			intent = new Intent(this, RouteArchive.class);
			break;
		case REQUEST_CODE_POSSIBLE_ADDRESSES:
			mOpeningOrienterOrKeyBoard = false;
			
			intent = new Intent(this, PossibleAddresses.class);
			break;
		case REQUEST_CODE_ROUTE_ORIENTER:
			mOpeningOrienterOrKeyBoard = true;
			
			intent = new Intent(this, RouteOrienter.class);
			intent.putExtra(LATITUDE_EXTRA, latitude);
			intent.putExtra(LONGITUDE_EXTRA, longitude);
			break;
		case REQUEST_CODE_CURRENT_LOCATION:
			mOpeningOrienterOrKeyBoard = false;
			RouteInput.mFromCurrentLocationScreen = true;
			
			intent = new Intent(this, CurrentLocation.class);
			break;
		default:
			Log.e(TAG, "Error: an invalid requestCode was passed to openActivityForResult " + 
					requestCode);
			finish();
			return;
		}
		
		startActivityForResult(intent, requestCode);
	}
	
	/**
	 * Override the default instructions for the phone's back, menu, and 
	 * 	search hard-key buttons.  The home button cannot be overridden.
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		switch(keyCode) {
		case KeyEvent.KEYCODE_BACK:
			return true;	// Do nothing (disable the button).
		case KeyEvent.KEYCODE_MENU:
			return true;	// Do nothing (disable the button).
		case KeyEvent.KEYCODE_HOME:
			// THE SYSTEM WILL NEVER PASS THIS PARAMETER.
			return false;	// Do nothing (disable the button).
		case KeyEvent.KEYCODE_SEARCH:
			return true;	// Do nothing (disable the button).
		default:			// Use the button's default functionality.
			return super.onKeyDown(keyCode, event);
		}
	}
	
	
	// -----Private Helper Classes-----
	/** 
	 * A helper class to manage database creation and version management
	 * 	of database. 
	 */
	public class DbHelper extends SQLiteOpenHelper {
		DbHelper(Context context) {
			super(context.getApplicationContext(), DATABASE_NAME, null, 
					DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_BOOKMARKS_TABLE);
			db.execSQL(CREATE_RECENTS_TABLE);
			
			Log.i(TAG, DATABASE_NAME + " database created!");
		}
		
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, 
				int newVersion) {
			db.execSQL("DROP IF TABLE EXISTS " + BOOKMARKS_TABLE_NAME);
			db.execSQL("DROP IF TABLE EXISTS " + RECENTS_TABLE_NAME);
			onCreate(db);

			Log.i(TAG, DATABASE_NAME + " database upgraded!");
		} 
	}
	
	/** Controls all gesture related actions. */
	private class 		GestureController 
			implements	GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener {
		/** Used to implement Swipes */
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
    		int scaledDistance;
    		int scaledPath;

    		// get distance between points of the fling
    		double vertical 	= Math.abs(e1.getY() - e2.getY());
    		double horizontal 	= Math.abs(e1.getX() - e2.getX());

    		// convert dip measurements to pixels
    		final float scale = getResources().getDisplayMetrics().density;
    		scaledDistance 	= (int) (DISTANCE_DIP * scale + 0.5f);
    		scaledPath 		= (int) (PATH_DIP * scale + 0.5f);

    		// If horizontal motion is greater than vertical motion, then try for a horizontal 
    		// swipe.
    		if(horizontal >= vertical) {
    			// test vertical distance
    			if (vertical > scaledPath) {
    				return false;

    			// test horizontal distance and velocity
    			} else if (horizontal > scaledDistance && Math.abs(velocityX) > mMinScaledVelocity) {
    				if (velocityX < 0) { // right to left swipe
    					if (D) Log.v(TAG, "Leftward Swipe");
    					swipeLeft();
    				} else { // left to right swipe
    					if (D) Log.v(TAG, "Rightward Swipe");
    					swipeRight();
    				}
    				return true;
    				
    			//not a good enough swipe
    			} else {
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
    					swipeDown();
    				} else { // bottom to top swipe
    					if (D) Log.v(TAG, "Upward swipe");
    					swipeUp();
    				}
    				return true;
    				
    			//not a good enough swipe
    			} else {
    				return false;
    			}
    		}
		}

		@Override
		public boolean onDoubleTap(MotionEvent e){
			Log.d(TAG, "Double Tap");
			doubleTap();
			return true;
		}

		@Override
		public void onLongPress(MotionEvent arg0) {
			if (D) Log.v(TAG, "Long Press");
			helpDirections();
		}
		
		
		// -----Unimplemented methods from the various implemented listeners-----
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
}