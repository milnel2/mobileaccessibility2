package cs.washington.mobileaccessibility.color;




import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.WindowManager;
import android.view.Window;
import android.media.AudioManager;

/**
 * This class is the main activity of the color namer application
 * It has four different algorithms that try to name the color being
 * viewed in the camera.
 * 
 * Two are based off of decision trees.  One that I made up arbitrarily,
 * by reading Wikipedia articles on different colors and their definitions.
 * The other is based off a simplified version of a machine-learning algorithm,
 * using data that I collected by hand.
 * 
 * The other two are two variants of an expectation-maximization algorithm in
 * a paper by Joost van de Weijer, Cordelia Schmid,
 * Jakob Verbeek, and Diane Larlus, maybe.  This algorithm uses a ton of data
 * stored in a file, which takes several seconds to load.  So the application
 * spins its wheels for a while when it first loads.
 * 
 * All the algorithms except for my ad hoc one assign one of the eleven canonical
 * simple colors in English:
 * white, gray, black, orange, red, green, blue, brown, yellow, purple, and pink
 * 
 * All of the algorithms only look at a certain centered,
 * rectangular region inside the camera's field of view.  This is an attempt on my part
 * to focus on whatever is in the foreground of the picture.
 * 
 * The user can specify which algorithms she is using by pressing the keys 1-4, which
 * toggle each of the four algorithms.  Pressing 0 reads off which ones are being used.
 * 
 * Aside from the functionality to name the color of a given object, the
 * application does two more things: when the user presses a key associated with a color
 * [blacK, Blue, browN, grAy, Green, Orange, pInk, Purple, Red, White, Yellow]
 * the phone starts to vibrate at a level determined by the amount of the specified color
 * in the central field of view.
 * 
 * Also, the combination shift+ one of the color-associated keys causes a snapshot of
 * the central area to be saved into a file called peri.txt, which can be fed into the
 * ColorAnalyzer program to generate a decision tree using a lame machine-learning algorithm.
 * 
 * @author Will
 *
 */
public class ColorNamer extends Activity {    
    public TextToSpeech tts; // the text to speech unit
    public CameraFacade cameraFacade; // this encapsulates the camera
    public Vibrator vibe; // the vibrator
    
    // these variables keep track of the classifiers that are being used
    // there are four different
    final static public String [] classifierNames = {"ad hoc","machine learning","ee em simplified", "ee em"};
    public boolean[] activeClassifiers = {false, true, true, true};

    @Override
	protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Hide the window title.
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        // Hide the status bar
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        SurfaceView view = new SurfaceView(this);
        vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        cameraFacade = new CameraFacade(view.getHolder());

        // the color data for the EM algorithm is in a long
        // file that takes a while to read in, so we need to use
        // a ProgressDialog and multithreading
        final ProgressDialog loadingDialog = new ProgressDialog(this) {
        	@Override
        	public boolean onTouchEvent(MotionEvent me) {
        		tts.speak("Please wait", 0, null);
        		return false;
        	}
        };
        loadingDialog.setTitle("Loading color definitions");
        loadingDialog.setMessage("Please wait");
        loadingDialog.setCancelable(true);
        loadingDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        loadingDialog.setMax(4096);
        loadingDialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
        loadingDialog.setOnCancelListener(new OnCancelListener() {
        	public void onCancel(DialogInterface wtf) {
        		finish();
        	}
        });
        loadingDialog.show();
        Log.i("ColorNamer","About to start the thread");
        Thread loadingThread = new Thread(new Runnable() {
        	public void run() {
                Log.i("ColorNamer","About to open the file converted_4096.txt");
                java.io.InputStream is = getResources().openRawResource(R.raw.converted_4096);
                
                boolean closing = !ImageProcessor.loadProbabilities(loadingDialog, is);
                // (closing is now true iff we were interrupted, because onDestroy was called)
                Log.i("ColorNamer",ImageProcessor.loaded?"Completed loading color model":"Stopped loading color model");
        		loadingDialog.dismiss();
        		// unless onDestroy was called, read the instructions!
        		if(!closing)
        	    	tts.speak("Point the camera and tap the screen to identify the color.", 0, null);
        	}
        });
        setContentView(view);
        // this makes the volume control buttons adjust the tts volume,
        // rather than the ringtone volume
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        tts = new TextToSpeech(this,new TextToSpeech.OnInitListener() {
        	public void onInit(int version) {
        		tts.speak("Loading; Please Wait.", 0, null);
        	}
        });
        loadingThread.start();
        
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	// if the user restarts the application after minimizing it, we
    	// should give them instructions
    	if(ImageProcessor.loaded)
    		tts.speak("Point the camera and tap the screen to identify the color.", 0, null);
    	// For reasons I don't understand,
    	// this used to only happen when ImageProcessor.loaded was true.
    	// That seemed irrelevant so I made this always happen
    	cameraFacade.onResume();	
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
        /* "This is also a good place to do things like stop animations
         * and other things that consume a noticeable mount of CPU in
         * order to make the switch to the next activity as fast as
         * possible, or to close resources that are exclusive access
         * SUCH AS THE CAMERA."
         * 
         * http://developer.android.com/reference/android/app/ActivityGroup.html#onPause()
         */
    	if(cameraFacade != null)
    		cameraFacade.onPause();
    	tts.stop(); // bad style to keep on talking after the application goes away
    	Log.v("ColorNamer","TTS told to stop");
    }
    
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
    	// one of several things happens here
    	// if they press a letter associated with a color, then either
    	// we start to vibrate based off of how close they are to that color
    	// or, if they pressed shift, we instead save the image to a file
    	
    	// if they press space, we stop vibrating
    	
    	// if they press numbers, they toggle the different algorithms being used,
    	// or here a list of the ones that are turned on and off
    	
    	// but first, we check that this is actually a keypress
    	if(event.getAction() != KeyEvent.ACTION_DOWN)
    		return super.onKeyDown(keyCode, event);
    	
    	final String colorName;
    	switch (keyCode) {
    	case KeyEvent.KEYCODE_K:
    		colorName = "black";
    		break;
    	case KeyEvent.KEYCODE_B:
    		colorName = "blue";
    		break;
    	case KeyEvent.KEYCODE_N:
    		colorName = "brown";
    		break;
    	case KeyEvent.KEYCODE_A:
    		colorName = "gray";
    		break;
    	case KeyEvent.KEYCODE_G:
    		colorName = "green";
    		break;
    	case KeyEvent.KEYCODE_O:
    		colorName = "orange";
    		break;
    	case KeyEvent.KEYCODE_I:
    		colorName = "pink";
    		break;
    	case KeyEvent.KEYCODE_P:
    		colorName = "purple";
    		break;
    	case KeyEvent.KEYCODE_R:
    		colorName = "red";
    		break;
    	case KeyEvent.KEYCODE_W:
    		colorName = "white";
    		break;
    	case KeyEvent.KEYCODE_Y:
    		colorName = "yellow";
    		break;
    	case KeyEvent.KEYCODE_1:
    	case KeyEvent.KEYCODE_2:
    	case KeyEvent.KEYCODE_3:
    	case KeyEvent.KEYCODE_4:
    		int num = keyCode - KeyEvent.KEYCODE_1;
    		boolean wasOn = activeClassifiers[num];
    		if(wasOn)
    			tts.speak("Turning off classifier " + classifierNames[num], 0, null);
    		else
    			tts.speak("Turning on classifier " + classifierNames[num], 0, null);
    		activeClassifiers[num] = !wasOn;
    		return super.onKeyDown(keyCode, event);
    	case KeyEvent.KEYCODE_0:
    		for(num = 0; num < 4; num++) {
    			tts.speak("Classifier" + classifierNames[num] + 
    					" is " + (activeClassifiers[num]?"on":"off"), 1, null);
    		}
    		return super.onKeyDown(keyCode, event);
    		
    	case KeyEvent.KEYCODE_SPACE: // this is supposed to turn off the vibrator
    		vibe.vibrate(2); // vibrate for 2 milliseconds, and then stop
    		cameraFacade.getPreview(null);
    		// return super.onKeyDown(keyCode, event);
    	default:
    		return super.onKeyDown(keyCode, event);
    		
    	}

    	
    	final boolean storeMode = event.isShiftPressed();
    	cameraFacade.getPreview(new Camera.PreviewCallback() {
    		public void onPreviewFrame(byte[] data, Camera camera) {
    			Log.i("ColorNamer","Stopping the preview");
    			cameraFacade.getPreview(null);
    			ImageProcessor.loadRGB(data, camera);
    			if(storeMode)
    				ImageProcessor.savePictureData(colorName);
    			else {
    				int matches = ImageProcessor.evaluatePicture(colorName);
    				vibe.vibrate(new long[] {0, matches, 100 - matches, matches, 100 - matches}, 0);
    				Log.e("ColorNamer", "Just started vibration of " + matches + "/100");
    			}	
    		}
    	});
    	return true;
    }
    
    @Override
    protected void onDestroy() {
    	ImageProcessor.stopLoading(); // kill the loadingThread, if necessary
    	tts.shutdown();
    	Log.v("ColorNamer","TTS told to shutdown");
    	super.onDestroy();
    	
    }
    
    // they touched the screen, so they want to know what
    // color they're looking at
    public boolean onTouchEvent(MotionEvent event) {
    	
    	if(event.getAction() != MotionEvent.ACTION_DOWN)
    		return false;
    	cameraFacade.getPreview(new Camera.PreviewCallback() {

    		public void onPreviewFrame(byte[] data, Camera camera) {

    			Log.i("ColorNamer","Stopping the preview");
    			cameraFacade.getPreview(null);
    			String firstColor = "", secondColor= "", thirdColor = "";
    			Log.v("TouchVibe", "just cleared out color strings");

    			if(!ImageProcessor.loaded) {
    				Log.i("ColorNamer","The color data wasn't loaded, so DO NOTHING");
    				return;
    			}
    			
    			ImageProcessor.loadRGB(data, camera);
    			
    			String any = "";
    			String name;
    			if(activeClassifiers[0]) {
    				name = ImageProcessor.classifyPictureAdHoc();
    				tts.speak(name, 1, null);
    				any = " or ";
    				firstColor = name;
    			}
    			if(activeClassifiers[1]) {
    				name = ImageProcessor.classifyPictureLearntTree();
    				if(!name.equals(firstColor))
    				{
    					tts.speak(any + name, 1, null);
    					any = " or ";
    				}
    				secondColor = name;
    			}
    			if(activeClassifiers[2]) {
    				name = ImageProcessor.classifyPictureProbsum();
    				if(!name.equals(firstColor) && !name.equals(secondColor))
    				{
    					tts.speak(any + name, 1, null);
    					any = " or ";
    				}
    				thirdColor = name;
    			}
    			if(activeClassifiers[3]) {
    				name = ImageProcessor.classifyPictureEM();
    				if(!name.equals(firstColor) && !name.equals(secondColor) && !name.equals(thirdColor))
    				{
    					tts.speak(any + name, 1, null);
    					any = " or ";
    				} 				
    			}
    			if(any.length() == 0) {
    				tts.speak("No color classifiers turned on!", 0, null);
    			}
    		}

    	});
    	return true;
    }



}