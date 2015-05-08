package cs.washington.edu.buddies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import android.app.Activity;
import android.gesture.Gesture;
import android.gesture.GestureLibraries;
import android.gesture.GestureLibrary;
import android.gesture.GestureOverlayView;
import android.gesture.Prediction;
import android.gesture.GestureOverlayView.OnGesturePerformedListener;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.widget.Toast;

public class Groom extends Activity implements OnGesturePerformedListener, TextToSpeech.OnUtteranceCompletedListener{
    // For gestures
	public static GestureLibrary mLibrary;
	//For grooming/stroke count
	private int counter;
	private static final int STROKES = 10;
    //text to speech
    private TextToSpeech tts;
    //for dog sound
    private boolean barked;
    private boolean barkType;
    private MediaPlayer mp;
    private int breed;
    private final int PITBALL = PetData.PITBULL;
    private final int POMERANIAN = PetData.POMERANIAN;
    private final int RETRIEVER = PetData.RETRIEVER;
    //for talking to pet
    private boolean recorded;
    private MediaRecorder recorder;
    private final String RECORDED_FILE = "test.3gp";

	protected void onCreate(Bundle savedInstanceState) { 
		super.onCreate(savedInstanceState);
		setContentView(R.layout.groom);
		
		tts = Game.tts;
        tts.speak("Stroke the screen to pet " + PetData.getName(this) + " or press and hold the trackball to talk to " + PetData.getName(this), TextToSpeech.QUEUE_FLUSH, null);
    
		
		//set number of strokes
		counter = STROKES;
        //create Gestures
        mLibrary = GestureLibraries.fromRawResource(this, R.raw.gesturesbuddies);
        if (!mLibrary.load()) {
        	finish();
        }       
        GestureOverlayView gestures = (GestureOverlayView) findViewById(R.id.gestures);
        gestures.addOnGesturePerformedListener(this);     
        
        // for speak
		recorder = new MediaRecorder();
		String pathForAppFiles = getFilesDir().getAbsolutePath();
		pathForAppFiles += RECORDED_FILE;		
		recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
		recorder.setOutputFormat(MediaRecorder.AudioEncoder.DEFAULT);
		recorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);		
		recorder.setOutputFile(pathForAppFiles);
		try {
			recorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		//for pet sound
		
		breed = PetData.getBreed(this);
		//animalSpeak();
		//flags
		barked = false;
		recorded = false;
		
		
	}

	@Override
	public void onGesturePerformed(GestureOverlayView overlay, Gesture gesture) {
		ArrayList<Prediction> predictions = mLibrary.recognize(gesture);
		// We want at least one prediction
		if (predictions.size() > 0) {
			Prediction prediction = predictions.get(0);
			if (prediction.score > 1.0) {
				// Show the type of stroke
				Toast.makeText(this, prediction.name, Toast.LENGTH_SHORT).show();
				if (prediction.name.equals("VERTICAL")) {
					//if (barked)
					//	resetBark();
					//else
					//	barked = true;
					animalSpeak();
					mp.start();
					counter--;				

				} else if (prediction.name.equals("EXIT") || prediction.name.equals("V")) {
					tts.speak("back to status activity menu", TextToSpeech.QUEUE_FLUSH, null);
					Game.getAnimal().groom();
					finish();
				} 
			}
		}
		/*if (counter ==0) {
			tts.speak("You have reached 10 strokes.", TextToSpeech.QUEUE_FLUSH, null);		
			mp.stop();	
		} */
		
	}
	
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
    	if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
    		tts.speak("Recording", TextToSpeech.QUEUE_FLUSH, null);
    		/*if(recorded)
    			resetRecorder();
    		recorder.start();
    		//recorder.start(); */
    	}
    	else if(keyCode == KeyEvent.KEYCODE_BACK) {
    		tts.speak("Back key pressed,", TextToSpeech.QUEUE_FLUSH, null);
    	}
    	else
    		return super.onKeyDown(keyCode, keyEvent);
    	return true;
    }
    
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent keyEvent) {
    	if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
    		//recorder.stop();
    		//recorder.release();
    		//recorder = null; 
    		/*if (recorded)
    			resetRecorder();
    		if (barked)
    			resetBark();
    		else
				barked = true; */
    		animalSpeak();
    		mp.start();
    	}
    	else if(keyCode == KeyEvent.KEYCODE_BACK) {
    		tts.speak("Back key pressed,", TextToSpeech.QUEUE_FLUSH, null);
    		recorder.release();
    		mp.stop();
    		Game.getAnimal().groom();
    		finish();
    	}
    	else
    		return super.onKeyUp(keyCode, keyEvent); 	
    	return true;
    }
	
	public void onDestroy(){
		recorder.release();
		mp.stop();
		super.onDestroy();
	}
	

	@Override
	public void onUtteranceCompleted(String utteranceId) {		
	}
		
	
	private void animalSpeak(){
		Random rand = new Random();
		int random = rand.nextInt();
		if (breed==POMERANIAN) {
			if (random % 2 == 0)
				mp = MediaPlayer.create(this, R.raw.small_bark_pet); 
			else 
				mp = MediaPlayer.create(this, R.raw.pomgroom); 
		}
		else if (breed==RETRIEVER){
			if (random % 2 == 0)
				mp = MediaPlayer.create(this, R.raw.medium_bark_pet); 
			else
				mp = MediaPlayer.create(this, R.raw.retrievergroom); 
		} else {
			if (random % 2 == 0)
				mp = MediaPlayer.create(this, R.raw.large_bark_pet);
			else
				mp = MediaPlayer.create(this, R.raw.pitbullgroom); 
		}
	}
	
	/*private void resetBark(){
		mp.reset();
		try {
			mp.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void resetRecorder(){
		recorder.reset();
		try {
			recorder.prepare();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	} */
	
}
