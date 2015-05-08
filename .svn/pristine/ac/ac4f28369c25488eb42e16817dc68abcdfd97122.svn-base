package cs.washington.mobileaccessibility;

import java.io.IOException;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.widget.TextView;

import com.google.tts.TTS;

@SuppressWarnings("deprecation")
public class GetFriends extends GestureUI{

	/** Global Variables **/
	private TTS myTts; 				// Text to Speech
	private TextView screenText; 	// Main Screen's Text
	
	protected static String[][] friends;
	protected static int currentIndex;
	protected static int maxIndex;	// max index of current elements in array

	private double longitude;
	private double latitude;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		gestureScanner = new GestureDetector(this);
		longitude = this.getIntent().getDoubleExtra("longitude", 0);
		latitude = this.getIntent().getDoubleExtra("latitude", 0);
		
		populateFriends();
		
		myTts = new TTS(this, ttsInitListener, true);

		screenText = (TextView) findViewById(R.id.screenContent);
		screenText.setText(R.string.getFriends);
	}
	
	/** Initialize and setup the Text to Speech **/
	private TTS.InitListener ttsInitListener = new TTS.InitListener() {

		public void onInit(int version) {
			myTts.speak("Find Nearby Friends, please scroll left and right",0, null);	
		}
	};
	
	/**
	 * Populate the array that stores the current friends to display.
	 */
	private void populateFriends() {
	    //internet query
	    PhpScriptAccess conn = new PhpScriptAccess("http://students.washington.edu/kwanste/friends.php");
	     
	    try {
	    	String[] results = conn.getQuery(longitude, latitude);
	    	maxIndex = results.length;
	    	friends = new String[maxIndex][3];
	    	for(int i = 0; i < maxIndex; i++) {
	    		friends[i] = results[i].split(";");
	    	}
	    	Log.d("GetFriends", "Population success");
	    	
	    	/* This is the old one for a 1-D array
	    	String[] results = conn.getQuery();
	    	maxIndex = results.length;
	    	//friends = new String[maxIndex*2][2];
	    	friends = results;
	    	
	    	Log.d("savedLocations", "Population success");*/
	    }  catch (IOException e) {
            Log.e("Php Script", "Shit don't work fucker.", e);
	    } 
	}
	
	
	/** Define these methods as needed in every class that extends the GesturesUI **/
	@Override
	protected void onUpFling() {
		finish();
	}
	
	@Override
	protected void onRightFling() {
		// Increase
		if (currentIndex == maxIndex - 1) {
			currentIndex = 0;
		} else {
			currentIndex++;
		}
		
		double distance = Math.round(Double.parseDouble(friends[currentIndex][2]));
		
		if(Double.parseDouble(friends[currentIndex][2]) < 1.0) {
			myTts.speak(friends[currentIndex][0] + " is less than a mile away." , 0, null);
		} else {
			myTts.speak(friends[currentIndex][0] + " is " + distance + " miles away.", 0, null);
		}
		
		String friendToPrint = friends[currentIndex][0] + "\n\n" + distance + " miles away";
		screenText.setText(friendToPrint);
	}
	
	@Override
	protected void onLeftFling() {
		// Decrease
		if (currentIndex == 0) {
			currentIndex = maxIndex-1;
		} else {
			currentIndex--;
		}
		
		double distance = Math.round(Double.parseDouble(friends[currentIndex][2]));
		
		if(Double.parseDouble(friends[currentIndex][2]) < 1.0) {
			myTts.speak(friends[currentIndex][0] + " is less than a mile away." , 0, null);
		} else {
			myTts.speak(friends[currentIndex][0] + " is " + distance + " miles away.", 0, null);
		}
		
		String friendToPrint = friends[currentIndex][0] + "\n\n" + distance + " miles away";
		screenText.setText(friendToPrint);
		
	}
	
	/**
	 *  Repeat the friend's distance and name
	 */
	@Override
	protected void onTap() {
		// Repeat
		double distance = Math.round(Double.parseDouble(friends[currentIndex][2]));
		
		if(Double.parseDouble(friends[currentIndex][2]) < 1.0) {
			myTts.speak(friends[currentIndex][0] + " is less than a mile away." , 0, null);
		} else {
			myTts.speak(friends[currentIndex][0] + " is " + distance + " miles away.", 0, null);
		}
	}
	
	/** 
	 * Pull up a dialer to call friend
	 */
	@Override
	protected void onLongPressDown() {
		Uri number = Uri.parse("tel:" + friends[currentIndex][1]);
        Intent dial = new Intent(Intent.ACTION_DIAL, number);
        startActivity(dial);	
	}
}
