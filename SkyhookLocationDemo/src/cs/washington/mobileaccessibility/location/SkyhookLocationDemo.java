package cs.washington.mobileaccessibility.location;

import java.io.IOException;

import android.app.Activity;
import android.location.Address;
import android.location.Geocoder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;

import com.google.tts.TTS;
import com.skyhookwireless.wps.WPSAuthentication;
import com.skyhookwireless.wps.WPSContinuation;
import com.skyhookwireless.wps.WPSLocation;
import com.skyhookwireless.wps.WPSPeriodicLocationCallback;
import com.skyhookwireless.wps.WPSReturnCode;
import com.skyhookwireless.wps.XPS;

/**
 * 
 * Location demo using the Skyhook API for LBS
 * 
 * @author joe
 *
 */
public class SkyhookLocationDemo extends Activity implements WPSPeriodicLocationCallback, OnClickListener {
	
	/**
	 * Skyhook login credentials
	 *
	 * TODO: these must be set with your skyhook credentials
	 */
	private static final String USERNAME = "username";
	private static final String REALM = "realm";
	
	/**
	 * Tag for debug log
	 */
	private static final String TAG = "LocationDemo";
	
    /**
     * Message keys for UI handler.
     */
    private static final int LOCATION_MESSAGE = 1;
    private static final int ERROR_MESSAGE = 2;
    private static final int DONE_MESSAGE = 3;
	
	/**
	 * Location service provider.
	 */
	private XPS wps;
	
	/**
	 * Skyhook authentication.
	 */
	private WPSAuthentication auth;
	
	/**
	 * TextView that contains the location.
	 */
    private TextView local;
    
    /**
     * UI handler.
     */
    private Handler handler;
    
    /**
     * TTS to use.
     */
    private TTS myTts;
    
    /**
     * Geocoder to reverse geocode location.
     */
    private Geocoder geocoder;
    
    /**
     * Can I make another location request.
     */
    private boolean done;

    /**
     * Indicates if screen has been touched and location has not been read
     */
    private boolean touched;
    
    /**
     * Music to play while waiting for location.
     */
    private Ringtone tone;
    
    /**
     * {@override}
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        myTts = new TTS(this, ttsInitListener, true);
                
        local = (TextView)findViewById(R.id.location);
        geocoder = new Geocoder(this);
        
        // Create Skyhook
        wps = new XPS(this);
        auth = new WPSAuthentication(USERNAME, REALM);
        
        done = true;
        touched = false;
        
        tone = RingtoneManager.getRingtone(this, RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_RINGTONE));
        
        findViewById(R.id.screen).setOnClickListener(this);
		setUIHandler();
		Log.d(TAG, "onCreate");
    }
    
    /**
     * This is spoken on app startup
     */
    private TTS.InitListener ttsInitListener = new TTS.InitListener() {
        public void onInit(int version) {
          myTts.speak("Touch the screen to find your location.", 0, null);
        }
      };

    /**
     * {@override}  
     */
    public void done() {
    	done = true;

		Log.d(TAG, "done");

 	}
 	
 	/**
 	 * {@override}
 	 */
 	public WPSContinuation handleError(WPSReturnCode error) {
		Log.d(TAG, "handleError: error is " + error);

 		return WPSContinuation.WPS_STOP;
 	}    


 	/**
 	 * Initializes UI handler.
 	 */
    private void setUIHandler() {
        handler = new Handler() {
            @Override
            public void handleMessage(final Message msg) {
                switch (msg.what) {
                case LOCATION_MESSAGE:
                	if(tone.isPlaying()) 
                		tone.stop();
                    Address addr = (Address) msg.obj;
                    local.setText(addr.getAddressLine(0) + '\n' + addr.getLocality());
                	myTts.speak(addr.getAddressLine(0) + '\n' + addr.getLocality() , 1, null);
                	touched = false;
                    return;
                case ERROR_MESSAGE:
                	if(tone.isPlaying()) 
                		tone.stop();
                	local.setText(((WPSReturnCode) msg.obj).name());
                	touched = false;
                    return;
                case DONE_MESSAGE:

                }
            }
        };
    }

	/**
	 * {@override}
	 */
	public WPSContinuation handleWPSPeriodicLocation(WPSLocation location) {		
		Log.d(TAG, "handleWPSPeriodicLocation: location is " + location);

		try {
			if(!touched) {
				myTts.speak("Updated location.", 1, null);
			}
			Address addr = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1).get(0);
			handler.sendMessage(handler.obtainMessage(LOCATION_MESSAGE,
	                addr));
			return WPSContinuation.WPS_CONTINUE;
		} catch (IOException e) {
			return WPSContinuation.WPS_STOP;
		}
	}

	/**
	 * {@override}
	 */
	public void onClick(View v) {
		Log.d(TAG, "onClick");
		
		touched = true;
		
		// only get a new location if skyhook is ready
		if(done) {
			done = false;
			
			wps.getXPSLocation(auth, 60, XPS.EXACT_ACCURACY, this);

			local.setText("Finding location ...");
			myTts.speak("Finding location", 0, new String[]{"VOICE_FEMALE"});
						
			if(!tone.isPlaying())
				tone.play();	
		} else if(!myTts.isSpeaking()){
			myTts.speak((String) local.getText(), 1, null);
		}
	}
	
	/**
	 *	{@override}
	 */
	protected void onDestroy() {
		Log.d(TAG, "onDestroy");

		if (tone.isPlaying()) tone.stop();
		wps.abort();
		myTts.shutdown();
		
		super.onDestroy();
	}
}