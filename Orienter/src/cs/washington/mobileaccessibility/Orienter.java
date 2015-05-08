package cs.washington.mobileaccessibility;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import cs.washington.mobileaccessibility.location.Locator;

/**
 * This app is intended to help blind or low vision users orient themself within
 * their physical surroundings.  Using the GPS and compass built into the Android
 * phone, the user is told what direction they are facing and their approximate 
 * address. 
 * 
 * @author joe
 *
 */
public class Orienter extends Activity implements SensorEventListener {

    private static final String TAG = "Orienter";
    
    /**
     * Vibration pattern indicating north
     */
    private static final long[] NORTH = { 0, 1000 };
    
    /**
     * Minimum number of degrees the compass reading
     * has to change (plus or minus the current heading)
     * to update the current heading
     */
    private static final int MIN_HEADING_CHANGE = 2;

    /**
     * Offset angle for each of 8 sections of the compass (ie N, NE, E, SE, S, etc.)
     */
    private static final int ANGLE = 360 / 8;
    
    /**
     * Angles marking the boundaries between the 8 sections of the compass
     */
    private static final int NORTH_NORTH_EAST = 23;
    private static final int EAST_NORTH_EAST = NORTH_NORTH_EAST + ANGLE; // 68
    private static final int EAST_SOUTH_EAST = EAST_NORTH_EAST + ANGLE; // 113
    private static final int SOUTH_SOUTH_EAST = EAST_SOUTH_EAST + ANGLE; // 158
    private static final int SOUTH_SOUTH_WEST = SOUTH_SOUTH_EAST + ANGLE; // 203
    private static final int WEST_SOUTH_WEST = SOUTH_SOUTH_WEST + ANGLE; // 248
    private static final int WEST_NORTH_WEST = WEST_SOUTH_WEST + ANGLE; // 293
    private static final int NORTH_NORTH_WEST = WEST_NORTH_WEST + ANGLE; // 338
    
    private TextToSpeech mSpeaker;
    private View mView;
    private View.OnTouchListener mTouchListener;
    private Float mHeading = Float.valueOf(-10);
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private Vibrator mVibrator;
    private Locator mLocator;


    private TextToSpeech.OnInitListener mSpeechInit = new TextToSpeech.OnInitListener()  {
    	public void onInit(int version) {
    		mSpeaker.speak("Touch the top half of the screen to find your location, or " +
    				"touch the bottom half to find the direction you are facing.", TextToSpeech.QUEUE_FLUSH, null);
    	}
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        Log.d(TAG, "onCreate: " + savedInstanceState);

        mSpeaker = new TextToSpeech(this, mSpeechInit);

        mView = new CompassView(this);
        mTouchListener = new TouchController();
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        
        mVibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        mLocator = new Locator(this);
        mLocator.startLocationUpdates();
        
        setContentView(mView);
        mView.setOnTouchListener(mTouchListener);
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	
        Log.d(TAG, "onDestroy");

        mLocator.stopLocationUpdates();
    	mVibrator.cancel();
    	mSensorManager.unregisterListener(this);
    	mSpeaker.shutdown();
    }


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		if(mSensor.equals(sensor)) 
			Log.d(TAG, "onAccuracyChanged: " + accuracy);
	}

	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if(mSensor.equals(event.sensor)) {
			float[] values = event.values;
			float heading = values[0];
			Log.d(TAG, "onSensorChanged: (" + heading + ", " + values[1] + ", " + values[2] + ")");
			
			//  Only changed our heading if it moves far enough
			if(Math.abs(mHeading - heading) > MIN_HEADING_CHANGE) {
				mHeading = heading;
				
				mVibrator.cancel();
				if(mHeading > 340 || mHeading < 20) {
					mVibrator.vibrate(NORTH, 1);
				}				
				
				if (mView != null) {
	                mView.invalidate();
	            }
			}
		}
	}
	
    private class CompassView extends View {
        private Paint mPaint;
        private Path mPath;

        public CompassView(Context context) {
            super(context);
            
            mPaint = new Paint();
            mPath = new Path();
            
            // Construct a wedge-shaped path
            mPath.moveTo(0, -50);
            mPath.lineTo(-20, 60);
            mPath.lineTo(0, 50);
            mPath.lineTo(20, 60);
            mPath.close();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            Paint paint = mPaint;

            canvas.drawColor(Color.WHITE);

            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.FILL);

            int w = canvas.getWidth();
            int h = canvas.getHeight();
            int cx = w / 2;
            int cy = h / 2;

            canvas.translate(cx, cy);
            if (mHeading != null) {            
                canvas.rotate(-mHeading);
            }
            canvas.drawPath(mPath, mPaint);
        }
    }
    
    private class TouchController implements View.OnTouchListener {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			if(v == mView) {
				float y = event.getY();
				int height = v.getHeight();
				if(y * 2 > height) {
					float heading = mHeading;
					StringBuffer currentDirection = new StringBuffer("You are facing ");
					if( heading>= NORTH_NORTH_EAST && heading < EAST_NORTH_EAST)
						currentDirection.append("north east");
					else if(heading >= EAST_NORTH_EAST && heading < EAST_SOUTH_EAST)
						currentDirection.append("east");
					else if(heading >= EAST_SOUTH_EAST && heading < SOUTH_SOUTH_EAST)
						currentDirection.append("south east");
					else if(heading >= SOUTH_SOUTH_EAST && heading < SOUTH_SOUTH_WEST)
						currentDirection.append("south");
					else if(heading >= SOUTH_SOUTH_WEST && heading < WEST_SOUTH_WEST)
						currentDirection.append("south west");
					else if(heading >= WEST_SOUTH_WEST && heading < WEST_NORTH_WEST)
						currentDirection.append("west");
					else if(heading >= WEST_NORTH_WEST && heading < NORTH_NORTH_WEST)
						currentDirection.append("north west");
					else 
						currentDirection.append("north");
					
					mSpeaker.speak(currentDirection.toString(), TextToSpeech.QUEUE_FLUSH, null);
				}
				else {
					Address addr = mLocator.getCurrentAddress();
					StringBuffer currentAddr = new StringBuffer("You are near");
					currentAddr.append('\t').append(addr.getAddressLine(0));
					currentAddr.append('\t').append(addr.getLocality());
					
					mSpeaker.speak(currentAddr.toString(), TextToSpeech.QUEUE_FLUSH, null);
				}
			}
			return false;
		}
    	
    }
}