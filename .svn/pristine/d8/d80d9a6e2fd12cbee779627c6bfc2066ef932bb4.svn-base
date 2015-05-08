package cs.washington.mobileaccessibility;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.content.ContentValues;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.GestureDetector;
import android.widget.TextView;

import com.google.tts.TTS;

@SuppressWarnings("deprecation")
public class SaveALocation extends GestureUI {
	/** Global Variables **/
	private TTS myTts; 				// Text to Speech
	private TextView screenText; 	// Main Screen's Text
	
	private double longitude;
	private double latitude;
	private String currentAddress;
	
	// the file name will be composed of the long and lat of the location
	// ideally we would want to match it with the id in the row but there's no way that we can grab that info if we save before posting
	private String filename;

	// UI and Recording Flags for Sound Recording
	private boolean fileRecorded = false;
	private boolean recording = false;

	private MediaRecorder audioListener;
	private MediaPlayer player;
	
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        		
		myTts = new TTS(this, ttsInitListener, true);

		gestureScanner = new GestureDetector(this);
		longitude = this.getIntent().getDoubleExtra("longitude", 0);
		latitude = this.getIntent().getDoubleExtra("latitude", 0);
		currentAddress = "No physical address found.";
		currentAddress = this.getIntent().getStringExtra("address");
		
		// create the filename that will be used for this instance
		 filename = longitude + "and" + latitude + ".3gp";
	}

	//basic setup and init for tts
	private TTS.InitListener ttsInitListener = new TTS.InitListener() {

		public void onInit(int version) {
			myTts.speak("Please tap once to begin recording the name of the location. Tap again to end recording.",0, null);
		}
	};

	
	private void postToServer() {
		PhpScriptAccess conn = new PhpScriptAccess("http://students.washington.edu/kwanste/linkup.php");
        // Add your data   
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(6);
        // not sending our fake ID
        nameValuePairs.add(new BasicNameValuePair("latitude", "" + latitude));
        nameValuePairs.add(new BasicNameValuePair("longitude", "" + longitude));
        nameValuePairs.add(new BasicNameValuePair("address", currentAddress));
        nameValuePairs.add(new BasicNameValuePair("sound", filename));
        nameValuePairs.add(new BasicNameValuePair("cat", "Unknown"));  // hardcoded for now
        nameValuePairs.add(new BasicNameValuePair("addresstype", "Exact")); // hardcoded for now
        
        try {
			conn.postQuery(nameValuePairs);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/** Define these methods as needed in every class that extends the GesturesUI **/
	@Override
	protected void onUpFling() {
		finish();
	}
	
	@Override
	protected void onDownFling() {
		// Swiping down
		if(fileRecorded){
			   
			// REMEMBER we have to submit this to teh server as well, so send it here yo.
			postToServer();
			   
			myTts.speak("Location has been stored",0, null);
			screenText.setText("Location has been stored. Scroll up to return to main screen.");
		}
	}
	
	@Override
	protected void onRightFling() {
		//swipe right == playback
		if (fileRecorded) {
			if (player == null) {
				player = new MediaPlayer ();
			}
			try {
				Log.d("Audio filename:",filename );

				FileInputStream fis = openFileInput(filename);

				//InputStream in = new FileInputStream(file);
				player.setDataSource(fis.getFD());
				player.prepare();
				player.start();

				// Loop to wait for the playing to stop
				while (player.isPlaying()) {
					//Log.d("Player", "Straight playin'");
				}

				player.stop();
				player.release();
				player = null;
			} catch (Exception e) {
				Log.e("Audio", "Playback failed.", e);
			}

		}
	}
	
	@Override
	protected void onLeftFling() {

	}
	
	@Override
	protected void onTap() {
		// first tap starts the recording, second tap ends it
		if(!recording){
			myTts.speak("Begin Speaking...", 0, null);
			// Start recording
			Log.d("Record Button", "Recording has begun.");
			if (audioListener == null) {
				audioListener = new MediaRecorder();
			}
			String pathForAppFiles = getFilesDir().getAbsolutePath();
			pathForAppFiles += "/" + filename; 

			Log.d("Audio filename:",pathForAppFiles );

			audioListener.setAudioSource(MediaRecorder.AudioSource.MIC);
			audioListener.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
			audioListener.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);


			audioListener.setOutputFile(pathForAppFiles);

			try {
				audioListener.prepare();
				audioListener.start();
				screenText.setText("Recording");
				recording = true;
			} catch (Exception eA) {
				Log.e("Audio", "Failed to prepare and start audio recording", eA);
			} 	 

		}else{
			//in here a tap means that we are done recording
			audioListener.stop();
			audioListener.release();
			audioListener = null;
			
			recording = false;

			String pathForAppFiles = getFilesDir().getAbsolutePath();
			pathForAppFiles += "/" + filename; 
			Log.d("Audio filename:", pathForAppFiles);

			ContentValues values = new ContentValues(10);

			values.put(MediaStore.MediaColumns.TITLE, "Location Place");
			values.put(MediaStore.Audio.Media.ALBUM, "");
			values.put(MediaStore.Audio.Media.ARTIST, "LinkUp Saved Location");
			values.put(MediaStore.Audio.Media.DISPLAY_NAME, "The Audio File You Recorded In Media App");
			values.put(MediaStore.Audio.Media.IS_RINGTONE, 1);
			values.put(MediaStore.Audio.Media.IS_MUSIC, 1);
			values.put(MediaStore.MediaColumns.DATE_ADDED, System.currentTimeMillis() / 1000);
			values.put(MediaStore.MediaColumns.MIME_TYPE, "audio/3gp");
			values.put(MediaStore.Audio.Media.DATA, pathForAppFiles);

			Uri audioUri = getContentResolver().insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values);
			if (audioUri == null) {
				Log.d("Audio", "Content resolver failed");
			}

			// Force Media scanner to refresh now. Technically, this is
			// unnecessary, as the media scanner will run periodically but
			// helpful for testing.
			Log.d("Audio URI", "Path = " + audioUri.getPath());
			sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, audioUri));

			RingtoneManager.setActualDefaultRingtoneUri(getApplicationContext(), RingtoneManager.TYPE_RINGTONE, audioUri);

			screenText.setText("Scroll Right to playback. \nTap to re-record. \nScroll down to finalize save.");

			myTts.speak("Scroll right to play back. Tap to re-record. Scroll down to finalize save." , 0, null);
			fileRecorded = true;
		}
	}
}
