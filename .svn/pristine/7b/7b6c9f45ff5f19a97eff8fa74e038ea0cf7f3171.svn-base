package cs.washington.mobileaccessibility.vbraille;

import android.app.Activity;
import android.media.AudioManager;
import android.os.Bundle;

public class VBrailleActivity extends Activity {
	
	private BrailleView tvview;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tvview = new BrailleView(this);
        //requestWindowFeature(Window.FEATURE_NO_TITLE);
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        setContentView(tvview);
    }
}