package edu.washington.cs.streetsignreader;

import java.util.Locale;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.ImageView;

/****************
 * 
 * DEPRECIATED AS OF FINAL VERSION OF STREET SIGN READER
 * 
 ***************/
public class ImagePreview extends Activity implements OnInitListener {
	public static final String TAG = "ImagePreview";

	static final String TEXT_ON_SIGN = "TEXT_ON_SIGN";
	public String imagePath = null;
	private TextToSpeech mTts;
	private Vibrator vib;

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;
	private GestureDetector gestureDetector;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// hide the window title
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		vib = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

		mTts = new TextToSpeech(this, this);

		setContentView(R.layout.imagepreview);

		getImagePath();
	}

	public void getImagePath() {
		// retrieve the image path passed from StreetSignReader
		Bundle b = getIntent().getExtras();
		imagePath = b.getString(StreetSignReader.IMAGE_PATH);
	}

	public void initPreviewArea() {
		ImageView previewArea = (ImageView) findViewById(R.id.previewArea);

		// get the bmp
		Bitmap bmp = BitmapFactory.decodeFile(imagePath);
		previewArea.setImageBitmap(bmp);

		// add gesture detection
		gestureDetector = new GestureDetector(new SwipeGestureDetector());
		previewArea.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				if (gestureDetector.onTouchEvent(event)) {
					return true;
				}
				return false;
			}
		});
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		if (gestureDetector.onTouchEvent(event))
			return true;
		else
			return false;
	}

	class SwipeGestureDetector extends SimpleOnGestureListener {
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
				return false;

			if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// swipe right to left
				finish();
			} else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE
					&& Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
				// swipe left to right
				finish();
			}

			return false;
		}

		@Override
		public boolean onSingleTapConfirmed(MotionEvent e) {
			// go to the image enhancer
			Intent i = new Intent(ImagePreview.this, Touch.class);
			Bundle b = new Bundle();
			b.putString(StreetSignReader.IMAGE_PATH, imagePath);
			i.putExtras(b);
			startActivity(i);
			return true;
		}

		@Override
		public void onLongPress(MotionEvent e) {
			sayInstructions();
		}
	}

	public void onInit(int status) {
		if (status == TextToSpeech.SUCCESS) {
			mTts.setLanguage(Locale.US);
			// and... we're ready to go! display the preview image
			initPreviewArea();
			vib.vibrate(300);
			sayInstructions();
		} else {
			Log.e(TAG, "Could not initialize TextToSpeech.");
		}
	}

	private void sayInstructions() {
		mTts.speak("Tap if okay. Swipe to ree-take", TextToSpeech.QUEUE_FLUSH,
				null);
	}
}
