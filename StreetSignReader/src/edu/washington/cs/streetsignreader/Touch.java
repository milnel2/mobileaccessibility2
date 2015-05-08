package edu.washington.cs.streetsignreader;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public class Touch extends Activity implements OnTouchListener {
	// For tweaking the swipe sensitivity.
	private static final int SWIPE_THRESHOLD_VELOCITY = 2400;
	public static final String TAG = "Touch";
	private static final String path = "/sdcard/photo.jpg";
	public static final String finalPath = "/sdcard/finalPhoto.jpg";

	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	private Bitmap image;
	private Bitmap monoImage;
	private ImageView mImageView;
	private GestureDetector mGestureDetector;
	private ScaleGestureDetector mScaleGestureDetector;

	// Remember some things for zooming
	PointF start = new PointF();
	PointF mid = new PointF();
	float oldDist = 1f;
	private int currThresh = 128;
	public float mScaleFactor = 1.f;

	/** Creates the touch class. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.touch);
		mImageView = (ImageView) findViewById(R.id.touchIV);

		image = BitmapFactory.decodeFile(path);
		Bitmap image_temp = Bitmap.createScaledBitmap(image,
				image.getWidth() / 3, image.getHeight() / 3, false);
		image.recycle();
		image = image_temp;
		image = convertToInverted(image);

		mImageView.setImageBitmap(image);
		mImageView.setOnTouchListener(this);

		mGestureDetector = new GestureDetector(new SSRGestureDetector());
		mScaleGestureDetector = new ScaleGestureDetector(mImageView
				.getContext(), new SSRScaleGestureDetector());

		initApp();
	}

	// Speaks initial instructions.
	private void initApp() {
		StreetSignReader.readyAndVib();
		StreetSignReader
				.sayFlush("adjust the threshhold using the volume buttons");
		StreetSignReader.sayAdd("double tap when finished");
		StreetSignReader.sayAdd("swipe to go back");
	}

	/** Splits our on touch between zooming or pinching */
	public boolean onTouch(View v, MotionEvent event) {
		Log.d(TAG, "in onTouch");
		mScaleGestureDetector.onTouchEvent(event);

		if (mScaleGestureDetector.isInProgress()) { // This does zoom
			Log.d(TAG, "consumed by scalgestdetector");
			return true;
		} else if (mGestureDetector.onTouchEvent(event)) { // This does
			// everything else
			Log.d(TAG, "consumed by gestdetector");
			return true;
		}
		Log.d(TAG, "not consumed by either gesture detector...");
		return false;
	}

	/**
	 * Our gesture detection class.
	 * 
	 * Implements Runnable. We put the OCR web service call onto a thread for
	 * our dialog box
	 */
	class SSRGestureDetector extends SimpleOnGestureListener {
		private Bitmap bm;

		/** Computes the swipe velocity */
		@Override
		public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
				float velocityY) {
			double vel = Math.sqrt(Math.pow(Math.abs(velocityX), 2)
					+ Math.pow(Math.abs(velocityY), 2));
			Log.d(TAG, "velocity is: " + vel);
			if (vel > SWIPE_THRESHOLD_VELOCITY) {
				finish();
			}
			return false;
		}

		/** On scroll, we move the matrix by the offset */
		@Override
		public boolean onScroll(MotionEvent e1, MotionEvent e2,
				float distanceX, float distanceY) {
			if (!mScaleGestureDetector.isInProgress()) {
				matrix.postTranslate(-distanceX, -distanceY);
				mImageView.setImageMatrix(matrix);
				mImageView.invalidate();
				return true;
			}
			return false;
		}

		/**
		 * Several things occur onDoubleTap:
		 * 
		 * Process OCR request here to avoid unnecessary complications
		 * */
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			StreetSignReader.hapticVibrate();
			StreetSignReader.sayFlush("Sending to OCR");
			Bitmap bm = Bitmap.createBitmap(mImageView.getWidth(), mImageView
					.getHeight(), Bitmap.Config.ARGB_8888);
			this.bm = bm;
			Canvas c = new Canvas(bm);
			mImageView.draw(c);

			try { // writes file to sdcard
				FileOutputStream out = new FileOutputStream(finalPath);
				bm.compress(Bitmap.CompressFormat.JPEG, 100, out);
				Log.i(TAG, "Picture Saved");
				out.close();

				if (isOnline()) {
					new runOCR().execute();
				} else {
					StreetSignReader
							.sayFlush("There is no internet connectivity. OCR cannot continue"
									+ "Please enable internet access and try again");
				}
			} catch (Exception ex) { // Problem with saving the image.
				Log.e(TAG, "Exception: " + ex.getMessage(), ex);
			}

			return super.onDoubleTap(e);
		}

		/**
		 * Sends the image to the OCR server.
		 */
		/*
		 * AsyncTask is favored over Threads for simplicity. We need a
		 * background process to run the OCR as to not disturb the UI thread,
		 * which causes the "Force Quit" or "Wait" dialog to automatically
		 * appear.
		 */
		class runOCR extends AsyncTask<Void, String, Void> {
			ProgressDialog dialog;

			// Modify how the dialog will look before it actually shows.
			@Override
			protected void onPreExecute() {
				super.onPreExecute();
				dialog = new ProgressDialog(mImageView.getContext());
				dialog.setMessage("Computing Street Name...");
				dialog.setIndeterminate(true);
				dialog.setCancelable(false);
				dialog.show();
			}

			// The actual computational portion (call) to the Tesseract OCR.
			@Override
			protected Void doInBackground(Void... unused) {
				try {
					// Push to OCR server.
					ByteArrayOutputStream bos = new ByteArrayOutputStream();
					bm.compress(Bitmap.CompressFormat.JPEG, 100, bos); // bm is
					// the
					// bitmap
					// object
					String ocrResult = DoServerOCR.getOCRResponse(bos
							.toByteArray());
					bos.close();

					// clean up the results..
					ocrResult = ocrResult.replaceAll("[^A-Za-z0-9\\s]", "");

					// Move to the DisplayResults activity
					Intent i = new Intent(getApplicationContext(),
							DisplayResult.class);
					Bundle bundle = new Bundle();
					bundle.putString("getocr", ocrResult);
					i.putExtras(bundle);
					startActivity(i);

					bm.recycle(); // recycle our bitmap
				} catch (IOException e) { // Problem with the output stream
					Log.e(TAG, "IOException caused by ByteArrayOutputStream", e);
				}
				return null;
			}

			// Dismiss the waiting dialog
			@Override
			protected void onPostExecute(Void unused) {
				dialog.dismiss(); 
			}
		}

		/** Re-TTS the "Long" instructions. */
		@Override
		public void onLongPress(MotionEvent e) {
			StreetSignReader.hapticVibrate();
			StreetSignReader.sayFlush("double tap to send to OCR");
			StreetSignReader.sayAdd("or swipe to go back");
		}

		@Override
		/** Does nothing */
		public boolean onSingleTapConfirmed(MotionEvent e) {
			return true;
		}
	}

	/**
	 * Returns whether or not something was touched. 
 	 * Overwrites default onTouch for our gesture detectors.
 	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return (mGestureDetector.onTouchEvent(event)
				|| mScaleGestureDetector.onTouchEvent(event));
	}

	/** Implements pinch zoom. */
	class SSRScaleGestureDetector extends SimpleOnScaleGestureListener {
		/**
		 * An API call to the zoom function. This allows us to overwrite onTouch
		 * for accessibility purposes (reading instructions, etc).
		 */
		@Override
		public boolean onScale(ScaleGestureDetector detector) {
			Log.d(TAG, "IN onScale");

			mScaleFactor = detector.getScaleFactor();
			// Don't let the object get too small or too large.
			mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 20.0f));
			Log.d(TAG, "" + mScaleFactor);

			// Scales the image onto the screen.
			matrix.postScale(mScaleFactor, mScaleFactor, detector.getFocusX(),
					detector.getFocusY());
			mImageView.setImageMatrix(matrix);
			mImageView.invalidate();
			return true;
		}

		/** Does nothing */
		@Override
		public boolean onScaleBegin(ScaleGestureDetector detector) {
			Log.d(TAG, "in onScaleBegin");
			return true;
		}
	}

	/**
	 * Key down helper function. Returns whether or not the volume keys are
	 * being pressed.
	 */
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN
				|| keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			return true;
		} else {
			return super.onKeyDown(keyCode, event);
		}
	}

	/**
	 * Responds to an keyup event. In our case, this changes the threshold.
	 */
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.d(TAG, "changing threshold to: " + currThresh);
		if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
			StreetSignReader.hapticVibrate();
			StreetSignReader.sayFlush("LESS THRESHOLD!!!!"); // <3
			currThresh = Math.max(currThresh - 10, 0);
		} else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
			StreetSignReader.hapticVibrate();
			StreetSignReader.sayFlush("MORE THRESHOLD!!!!"); // <3
			currThresh = Math.min(currThresh + 10, 255);
		} else {
			return super.onKeyDown(keyCode, event);
		}
		if (monoImage != null)
			monoImage.recycle();
		monoImage = convertToMonochrome(image);
		mImageView.setImageBitmap(monoImage);
		mImageView.invalidate();
		return true;
	}

	// Inverts the image on a pixel by pixel basis.
	private Bitmap convertToInverted(Bitmap bitmap) {
		int h = bitmap.getHeight();
		int w = bitmap.getWidth();

		int[] pixels = new int[w * h];

		bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int index = y * w + x;
				// inverts the RGB while keeping the A, using bit operation XOR
				pixels[index] = (pixels[index] ^ 0x00ffffff);
			}
		}

		Bitmap result = Bitmap.createBitmap(pixels, w, h,
				Bitmap.Config.ARGB_8888);
		return result;
	}

	// Transforms an image to monochrome.
	private Bitmap convertToMonochrome(Bitmap bitmap) {
		int h = bitmap.getHeight();
		int w = bitmap.getWidth();
		int[] pixels = new int[w * h];
		bitmap.getPixels(pixels, 0, w, 0, 0, w, h);

		int black = 0;
		int white = 0;

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int index = y * w + x;
				int r = (pixels[index] >> 16) & 0xff;
				int g = (pixels[index] >> 8) & 0xff;
				int b = pixels[index] & 0xff;

				double grey = (0.3 * r + 0.59 * g + 0.11 * b);
				int bw;

				// "threshold" to determine if a pixel is black or white
				if (grey > currThresh) {
					bw = 0xffffffff;
					white++;
				} else {
					bw = 0xff000000;
					black++;
				}
				pixels[index] = bw;
			}
		}

		Log.d(TAG, "num white is: " + white);
		Log.d(TAG, "num black is: " + black);

		Bitmap result = Bitmap.createBitmap(pixels, w, h,
				Bitmap.Config.ARGB_8888);
		return result;
	}

	/**
	 * Returns whether or not we have internet connectivity.
	 * 
	 * @return If there is internet access.
	 */
	public boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	/** Vibrates when the activity starts again. */
	protected void onResume() {
		super.onResume();
		StreetSignReader.readyAndVib();
	}

	/** Standard onPause function. Halts and saves necessary processes */
	protected void onPause() {
		super.onPause();
	}
}