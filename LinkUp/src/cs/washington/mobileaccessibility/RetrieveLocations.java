package cs.washington.mobileaccessibility;

import java.io.FileInputStream;
import java.io.IOException;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.widget.TextView;

import com.google.tts.TTS;

@SuppressWarnings("deprecation")
public class RetrieveLocations extends GestureUI{
	
	/** Global Variables **/
	private TTS myTts; 				// Text to Speech
	private TextView screenText; 	// Main Screen's Text
	private MediaPlayer player;		// MediaPlayer for sound playback of the location names
	
	protected static String[][] savedPlaces; //the array which stores the saved places
	protected static int currentIndex;//current element you are pointing at
	protected static int maxIndex;	// max index of current elements in array
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
		myTts = new TTS(this, ttsInitListener, true);

		screenText = (TextView) findViewById(R.id.screenContent);
		screenText.setText(R.string.reviewLocations);
		
		player = new MediaPlayer();
		
		// Must be instantiated to detect motions
		gestureScanner = new GestureDetector(this);
		//initializing the array for storing saved favorite locations
		//savedPlaces = new String[25][5]; // this gets initialized in populatedSaveLOcations
		maxIndex = 0; // max index for items in array
		currentIndex = 0; // Used to scroll through the array
		populateSavedLocations();
	}
	
	
	/** 
	 * Kill this intent and release resources 
	 */
	public void onDestroy() {
        player.release();
        player = null;
        super.onDestroy();
	}
	
	
	/** 
	 * Initialize and setup the Text to Speech 
	 */
	private TTS.InitListener ttsInitListener = new TTS.InitListener() {

		public void onInit(int version) {
			myTts.speak("To review the saved locations, please swipe " +
					"left or swipe right.  Each location has a soundfile " +
					"that will read the preferred name for the address.", 0, null);	
		}
	};
	
	/**
	 * Populate the array that stores the current locations to display.
	 */
	private void populateSavedLocations() {
	    PhpScriptAccess conn = new PhpScriptAccess("http://students.washington.edu/kwanste/linkup.php");
	    try {
	    	String[] results = conn.getQuery();
	    	maxIndex = results.length;
	    	savedPlaces = new String[maxIndex][7];
	    	for(int i = 0; i < maxIndex; i++) {
	    		savedPlaces[i] = results[i].split(";");
	    	}
	    	Log.d("savedLocations", "Population success");
	    }  catch (IOException e) {
            Log.e("Php Script", "IO Exception from trying to deal with the script.", e);
	    }
	}
	
	/**
	 * Play back a sound file that is assocated with the location file.
	 * This assumes that the sound file is stored locally on the phone.
	 * @param sfilename - Filename
	 * @param address - Address of the location to be read back.
	 */
	private void playbackLocationName(String sfilename, String address) {
       try {
    	   Log.d("Audio filename:", sfilename);
           FileInputStream fis = openFileInput(sfilename);
           
           player.reset();
           player.setDataSource(fis.getFD());
           player.prepare();
           player.start();
           
           // Loop to wait for the playing to stop
           while (player.isPlaying()) {
        	   //Log.d("Player", "Straight playin'");
           }
           
           player.stop();
           myTts.speak(address, 0, null);
       } catch (Exception e) {
           Log.e("Audio", "Playback failed. Filename not found.", e);
       }
	}
	
	/** Define these methods as needed in every class that extends the GesturesUI **/
	/**
	 * On Up Swipe:
	 * Kill the current intent and return to the main screen of Link Up.
	 */
	@Override
	protected void onUpFling() {
		finish();
	}

	/**
	 * On Right Swipe:
	 * Move to the next location.
	 */
	@Override
	protected void onRightFling() {
		// Increase
		if (currentIndex == maxIndex - 1) {
			currentIndex = 0;
		} else {
			currentIndex++;
		}
		
		String locationToPrint = savedPlaces[currentIndex][3];
		screenText.setText(locationToPrint);

		playbackLocationName(savedPlaces[currentIndex][4], locationToPrint);	
	}
	
	/**
	 * On Left Swipe:
	 * Move to the previous location.
	 */
	@Override
	protected void onLeftFling() {
		// Decrease
		if (currentIndex == 0) {
			currentIndex = maxIndex-1;
		} else {
			currentIndex--;
		}
		
		String locationToPrint = savedPlaces[currentIndex][3];
		screenText.setText(locationToPrint);

		playbackLocationName(savedPlaces[currentIndex][4], locationToPrint);
	}
	
	/**
	 * On tap:
	 * Repeat the location.
	 */
	@Override
	protected void onTap() {
		// Repeat
		String locationToPrint = savedPlaces[currentIndex][3];
		playbackLocationName(savedPlaces[currentIndex][4], locationToPrint);	
	}

}
