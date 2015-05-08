package edu.washington.cs.streetsignreader;

import android.app.Activity;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.widget.TextView;

/**
 * 
 * Displays the resulting text of a sign. This view will display the text in an
 * enlarged view.
 * 
 * @author shurui
 * 
 */
public class DisplayResult extends Activity {
	private String ocrResult;

	static final String tag = "Main";
	private TextView mTv;
	private GestureDetector mGestureDetector;
	private View mView;

	private static final int SWIPE_MIN_DISTANCE = 120;
	private static final int SWIPE_MAX_OFF_PATH = 250;
	private static final int SWIPE_THRESHOLD_VELOCITY = 200;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.displayresult);

		// Get the OCR result from the bundle
		Bundle bundle = getIntent().getExtras();
		ocrResult = bundle.getString("getocr");

		// Display the results
		mTv = (TextView) findViewById(R.id.TextView01);
		mTv.setText(ocrResult);
		mTv.setTextSize(60);
		mView = findViewById(R.id.TextView01);

		// Adds gesture detection for tap and hold instructions
		mGestureDetector = new GestureDetector(new SwipeGestureDetector());
		mView.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				return mGestureDetector.onTouchEvent(event);
			}
		});

		initApp();
	}

	/**
	 * OnTouch, tells the gestureDetector it happened.
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		return mGestureDetector.onTouchEvent(event);
	}

	// Vibrates when the app is ready.
	private void initApp() {
		StreetSignReader.readyAndVib();
		StreetSignReader.sayFlush("Press and hold for more instructions.");
	}

	/**
	 * Listens for a swipe.
	 * 
	 * @author shurui
	 */
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

		/**
		 * onDoubleTap, try to speak the contents of the sign.
		 */
		@Override
		public boolean onDoubleTap(MotionEvent e) {
			StreetSignReader.hapticVibrate();
			StreetSignReader.sayFlush("The sign is " + ocrResult);
			return super.onDoubleTap(e);
		}

		/**
		 * onLongPress, say the "long" directions.
		 */
		@Override
		public void onLongPress(MotionEvent e) {
			StreetSignReader.hapticVibrate();
			StreetSignReader.sayFlush("double tap to speak the sine");
			StreetSignReader.sayAdd("or swipe to go back");
		}
	}
}