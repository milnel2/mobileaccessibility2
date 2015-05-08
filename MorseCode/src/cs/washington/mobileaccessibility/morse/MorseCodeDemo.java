package cs.washington.mobileaccessibility.morse;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.Button;
import android.widget.EditText;

public class MorseCodeDemo extends Activity implements OnClickListener{
	public MorseVibrator mVibrator;
	public EditText entry;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        entry = (EditText) findViewById(R.id.entry);
        Button speaker = (Button)findViewById(R.id.speak);
        speaker.setOnClickListener(this);
        entry.setOnKeyListener(new OnKeyListener() {
        	public boolean onKey(View v, int keyCode, KeyEvent event) {
        		if(keyCode == KeyEvent.KEYCODE_ENTER) {
        			onClick(v);
        			return true;
        		}
        		return false;
        	}
        });
        
        mVibrator = new MorseVibrator(this);
    }
    
    public static int speed = 100;
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	if(keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
    		if(speed > 15) {
    			speed -= 10;
    			mVibrator.vibrate("s", speed);
    		}
    		else
    			mVibrator.vibrate("t",30);
    		return true;
    	}
    	if(keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
    		speed += 10;
    		mVibrator.vibrate("s", speed);
    		return true;
    	}
    	return super.onKeyDown(keyCode, event);
    }
    
    public void onClick(View v) {
    	mVibrator.vibrate(entry.getText().toString(), speed);
    	/*String [] splat = entry.getText().toString().split(" ");
    	long [] pattern = new long[splat.length];
    	for(int i = 0; i < splat.length; i++) {
    		pattern[i] = Long.parseLong(splat[i]);
    	}
    	mVibrator.vibe.vibrate(pattern,-1);*/
    }
}