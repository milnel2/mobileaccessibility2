package cs.washington.edu.buddies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.app.Activity; 
import android.content.Context; 
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.hardware.Sensor;
import android.hardware.SensorListener; 
import android.hardware.SensorManager; 
import android.os.Bundle; 
import android.speech.tts.TextToSpeech;
import android.widget.TextView; 
import android.widget.Toast;


public class Play extends Activity implements TextToSpeech.OnUtteranceCompletedListener, OnGesturePerformedListener, ToastDisplay{ 
	 //to tell the acceleration coordinate
     private TextView mTxtView;      
     private SensorManager mSensorManager; 
     //for acceleration initial and final values
     private int accel_initial[];
     private int accel_final[];
     
     private int delay_initial[];
     private int delay_final[];
     private int delay;  
     //for timer
     private static final int INTERVAL = 150;
     private int counter;     
     //text to speech
     private TextToSpeech tts;
     //for dog sound
     private MediaPlayer mp;
     private int breed;
     private final int PITBALL = PetData.PITBULL;
     private final int POMERANIAN = PetData.POMERANIAN;
     private final int RETRIEVER = PetData.RETRIEVER;
     // For gestures
     public static GestureLibrary mLibrary;
 	
     private Toast toast;
     private String[] toastMsgs = {"Shake phone repeatedly to exercise pet",
    		"Draw an X on the screen to go back to main activity menu"};
     private int index = 0;
     private Thread t;
 	// Handler for UI thread
 	private final Handler mHandler = new Handler();
 	private int mCount = 1;  // counts shakes to use for determining next play time
     
     
     @Override 
     protected void onCreate(Bundle savedInstanceState) { 
          super.onCreate(savedInstanceState);
          t = new Thread(new ToastDisplayer(this, mHandler, toastMsgs.length));
          t.start();
          
          tts = Game.tts;
          tts.speak("Shake your phone repeatedly to exercise your pet. Draw an X on the screen to go back to status activity menu", TextToSpeech.QUEUE_FLUSH, null);
          mTxtView = new TextView(this); 
          setContentView(R.layout.exercise);           
          mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE); 
          //Get the list of all sensors, and find the accelerometer within
          List<Sensor> sensorList = mSensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);
          accel_initial = new int[3];
          accel_final = new int[3];
          counter = INTERVAL;
          delay_initial = new int[3];
          delay_final = new int[3];
          delay = INTERVAL;
  			
  		  breed = PetData.getBreed(this);
  		  animalSpeak();
  		  
          //create Gestures
          mLibrary = GestureLibraries.fromRawResource(this, R.raw.gesturesbuddies);
          if (!mLibrary.load()) {
          	finish();
          }       
          GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
          gestures.addOnGesturePerformedListener(this); 
     } 
          
     private final SensorListener mSensorListener = new SensorListener() { 
    	 
         public void onSensorChanged(int sensor, float[] values) 
         { 
           	delay_final[0] = Math.round(values[SensorManager.DATA_X]);
        	delay_final[1] = Math.round(values[SensorManager.DATA_Y]);
        	delay_final[2] = Math.round(values[SensorManager.DATA_Z]); 
        	 if(tts.isSpeaking()){ 
        	 } 
        	 else if (delay == INTERVAL) {
             	delay_initial[0] = Math.round(values[SensorManager.DATA_X]);
            	delay_initial[1] = Math.round(values[SensorManager.DATA_Y]);
            	delay_initial[2] = Math.round(values[SensorManager.DATA_Z]);       		 
            	delay--;
        	 } else if (delay < INTERVAL && (Math.abs(delay_final[0]-delay_initial[0]) > 10 && Math.abs(delay_final[1]-delay_initial[1]) > 10 && Math.abs(delay_final[2]-delay_initial[2]) > 10)) {
	        	 if (counter == INTERVAL){
	            	accel_initial[0] = Math.round(values[SensorManager.DATA_X]);
	            	accel_initial[1] = Math.round(values[SensorManager.DATA_Y]);
	            	accel_initial[2] = Math.round(values[SensorManager.DATA_Z]);
	            	//mTxtView.setText(counter);
	            	counter--;
	            	mCount++;
					mp.start();
					try {
						mp.prepare();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					} 
	            } else if (counter == 0) {
	            	accel_final[0] = Math.round(values[SensorManager.DATA_X]);
	            	accel_final[1] = Math.round(values[SensorManager.DATA_Y]);
	            	accel_final[2] = Math.round(values[SensorManager.DATA_Z]);
	            	
	            	//orignal values: 50, 20, 10
	            	if (Math.abs(accel_final[0]-accel_initial[0]) > 25 || Math.abs(accel_final[1]-accel_initial[1]) > 10 || Math.abs(accel_final[2]-accel_initial[2]) > 5) {
	            		if(mp.isPlaying()) 
	            			mp.stop();
	            		tts.speak("that was a hard workout!", TextToSpeech.QUEUE_FLUSH, null);
//	            		Animal.needsPlay = false;
	               	 	mSensorManager.unregisterListener(mSensorListener);
	               	 	done();
	            	}
	            	counter = INTERVAL;
	            } else {
	            	counter--;
	            	mCount++;
	            }   
        	 } else {
        		 
        	 }
         } 
           
          public void onAccuracyChanged(int sensor, int accuracy) {} 
     }; 
      
     @Override 
     protected void onResume() 
     { 
          super.onResume(); 
          mSensorManager.registerListener(mSensorListener, 
                    SensorManager.SENSOR_ORIENTATION, 
                    SensorManager.SENSOR_DELAY_GAME); 
     } 
      
     @Override 
     protected void onDestroy() 
     { 
    	 mp.stop();
         mSensorManager.unregisterListener(mSensorListener); 
         super.onDestroy(); 
     }

	@Override
	public void onUtteranceCompleted(String utteranceId) {	
	} 
	
	private void done() {
		while (mp.isPlaying() || tts.isSpeaking()) {}
		Game.getAnimal().play(5 + mCount);
		finish();
	}
 			
	
	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		// We want at least one prediction
		if (predictions.size() > 0) {
			Prediction prediction = predictions.get(0);
			if (prediction.score > 1.0) {
				Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT).show();
				if (prediction.name.equals("EXIT") || prediction.name.equals("V")) {
					tts.speak("back to status activity menu", TextToSpeech.QUEUE_FLUSH, null);
					Game.getAnimal().play(mCount);
					finish();
				} 
			}
		}
	}
	
	private void animalSpeak(){
		Random rand = new Random();
		int random = rand.nextInt();
		if (breed==POMERANIAN) {
			if (random % 2 == 0)
				mp = MediaPlayer.create(this, R.raw.small_bark_workout); 
			else 
				mp = MediaPlayer.create(this, R.raw.pomworkout); 
		}
		else if (breed==RETRIEVER){
			if (random % 2 == 0)
				mp = MediaPlayer.create(this, R.raw.medium_bark_workout); 
			else
				mp = MediaPlayer.create(this, R.raw.labradorworkout); 
		} else {
			if (random % 2 == 0)
				mp = MediaPlayer.create(this, R.raw.large_bark_workout); 
			else
				mp = MediaPlayer.create(this, R.raw.pitbullworkout); 
		}
	}
	
	@Override
	public void displayToast() {
		if (index < toastMsgs.length) {
			toast = Toast.makeText(getApplicationContext(), toastMsgs[index], Toast.LENGTH_SHORT);
			toast.show();
			index++;
		}
	}
}
     

