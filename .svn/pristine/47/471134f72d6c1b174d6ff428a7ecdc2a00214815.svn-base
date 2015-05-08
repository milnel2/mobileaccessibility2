package edu.washington.cs.streetsignreader;

import java.io.FileOutputStream;
import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.os.Environment;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;

/**
 * 
 * StreetSignReader is the first activity launched by the application. It
 * handles taking a photo and Sending the image to the Touch class. The
 * TextToSpeech (TTS) engine initiated here is also reused by the rest of the
 * activities in our package.
 * 
 * @author Gary Kuo, Jon Luo, Shurui Sun
 * 
 */
public class StreetSignReader extends Activity implements OnInitListener {
	public static final String TAG = "StreetSignReader";
	public String imagePath = Environment.getExternalStorageDirectory()
			+ "/photo.jpg";
	// Key that finds the image path in the bundle
	public static final String IMAGE_PATH = "IMAGE_PATH";

	public static TextToSpeech tts;
	public static Vibrator vib;
	private Preview mPreview;
	private Camera mCamera;
	private DrawOnTop mDraw;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Hide the window title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// Initialize text to speech and get vibrator
		tts = new TextToSpeech(this, this);
		vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		// Create a container that will hold the camera images and set it as the
		// view
		mPreview = new Preview(this);
		setContentView(mPreview);

		// Draws the preview lines
		mDraw = new DrawOnTop(this);
		addContentView(mDraw, new LayoutParams(LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT));
	}

	/** Initialize text-to-speech. */
	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			tts.setLanguage(Locale.UK);
			initApp();
		} else {
			Log.e(TAG, "Could not initialize TextToSpeech.");
		}
	}

	/** When tts is ready, setup the application. */
	public void initApp() {
		sayFlush("Street Sign Reader. Tap and hold the "
				+ "screen to hear additional instructions");
		initPreviewClick();
	}

	/** Initialize the screen to detect long tap or single tap. */
	public void initPreviewClick() {
		/** Initiates the "long" instructions */
		mPreview.setOnLongClickListener(new OnLongClickListener() {
			public boolean onLongClick(View v) {
				sayFlush("Tap the screen to take a photo. "
						+ "Try to center the street sign "
						+ "in the middle of the screen.");
				return true;
			}
		});

		/** Onclick, Takes a picture. */
		mPreview.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				takePicture();
			}
		});
	}

	/** Jumps to the image enhancement activity */
	public void jumpToPreview() {
		// go to the image enhancer
		Intent i = new Intent(StreetSignReader.this, Touch.class);
		// place path of the image taken by camera into the bundle
		Bundle b = new Bundle();
		b.putString(IMAGE_PATH, imagePath);
		i.putExtras(b);
		startActivity(i);
	}

	// A callback for the shutter action. */
	ShutterCallback shutterCallback = new ShutterCallback() {
		public void onShutter() {
			sayFlush("picture taken");
			sayAdd("please wait for a preview");
		}
	};

	// A callback for the camera raw data */
	PictureCallback rawCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
		}
	};

	// A callback for the jpg. Writes the picture to disk here. */
	PictureCallback jpegCallback = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera camera) {
			// Saves image to SD card.
			Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
			try {
				FileOutputStream out = new FileOutputStream(imagePath);
				bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
				Log.i(TAG, "Picture Saved");
			} catch (Exception e) { // Problem with saving the image.
				Log.e(TAG, "Exception: " + e.getMessage(), e);
			}

			// jump to the next activity
			jumpToPreview();
		}
	};

	/** On resume, start up the camera and vibrate */
	@Override
	protected void onResume() {
		super.onResume();
		// Open the default i.e. the first rear facing camera.
		mCamera = Camera.open();
		Camera.Parameters parameters = mCamera.getParameters();
		parameters.setPictureFormat(PixelFormat.JPEG);
		mCamera.setParameters(parameters);

		mPreview.setCamera(mCamera);
		readyAndVib();
	}

	/** Standard onPause function */
	@Override
	protected void onPause() {
		super.onPause();
		if (mCamera != null) {
			mPreview.setCamera(null);
			mCamera.release();
			mCamera = null;
		}
	}

	/** Detect the camera shutter button */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_CAMERA) {
			takePicture();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	// Takes the photo.
	private void takePicture() {
		// vib.vibrate(500); //case study: should we vibrate here?
		sayFlush("taking picture");
		mCamera.takePicture(shutterCallback, rawCallback, jpegCallback);
	}

	/*
	 * The following accessibility functions are *static* so they can be used by
	 * other activities.
	 */
	/**
	 * Adds a string to be spoken by the TTS engine. Flushes the queue
	 * afterwards each time.
	 * 
	 * @param text
	 *            The string to be spoken.
	 */
	public static void sayFlush(String text) {
		tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
	}

	/**
	 * Adds a string to be spoken by the TTS engine. Adds to the queue.
	 * 
	 * @param text
	 *            The string to be spoken.
	 */
	public static void sayAdd(String text) {
		tts.speak(text, TextToSpeech.QUEUE_ADD, null);
	}

	/** This is called whenever the activity is ready for interaction. */
	public static void readyAndVib() {
		sayFlush("");
		long[] vibPattern = { 0, 200, 200, 200 }; // 2 short vibrates
		vib.vibrate(vibPattern, -1);
	}

	/**
	 * A short vibration function.
	 */
	public static void hapticVibrate() {
		vib.vibrate(50);
	}
}