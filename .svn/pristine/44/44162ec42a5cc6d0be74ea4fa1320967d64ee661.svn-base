package cs.washington.mobileaccessibility.locationorienter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.*;
import android.content.SharedPreferences.Editor;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.*;
import java.util.*;

import android.view.GestureDetector;

public class LocalEyes extends Activity implements  GestureDetector.OnGestureListener, 
													GestureDetector.OnDoubleTapListener{

	
	private LocationFinder mLocFind;
	private HorizontalScrollView mHSV;
	private TextView mTV;
	private ImageButton mZI;
	private ImageButton mZO;
	private ImageButton mSound;
	private TextToSpeech mTTS;
	private boolean mute;
	private String strSpokenCurrentLocation;
	private String strTextCurrentLocation;
	private List<Map<String,String>> businesses;
	private Map<String,String> directions;
	private int maxBusiness;
	private int minBusiness;
	private int currentBusiness;
	private int intCurrentLocation;
    private GestureDetector gestureScanner;
    private ProgressDialog pd;
	private boolean vision;
	
	protected static final float MAX_TEXT_SIZE = 240f;
	protected static final float MIN_TEXT_SIZE = 30f;
	private static final String PREF = "LocationOrienterPref";
	protected static final String INSTR = "Tap the screen to get your current location ";
	protected static final String EXTRA_INSTR =		" and to establish"+
										  " the points of interest around you. Upon retrieving your localized data " +
										  "You can get the businesses in front of" +
										  " you by flinging your finger forwards across the screen and vice versa for scrolling" +
										  " through the list backwards. You can also double tap a location to call it and single" +
										  " tap to get more information about that place.";
	
	/**
	 * Creates the application's main screen based on
	 * sighted and non sighted test and builds
	 * from saved state
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        SharedPreferences s = this.getSharedPreferences(PREF, 0);

        mLocFind = new LocationFinder(this);
		mLocFind.startLocationUpdates();
		mTTS = LocationOrienter.mTTS;

        gestureScanner = new GestureDetector(this);

        vision = s.getBoolean("vision", false);
        
		if(vision){
			//Sighted, so get colors and initialize the screen
			int b_Color = s.getInt("background_Color",Color.BLACK);
			int t_Color = s.getInt("text_Color",Color.WHITE);
			float t_Size = s.getFloat("text_Size",95f);
			mute = s.getBoolean("mute", false);
			initializeSighted(b_Color, t_Color, t_Size, mute);
        }else{
        	//not sighted, don't initialize any buttons or
        	//text views
        	mute = false;
    		this.setContentView(R.layout.textless);
        }

		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

		//Used for translating Acronym directions to full directions
		directions = new HashMap<String, String>();
		directions.put("N", "North");
		directions.put("S", "South");
		directions.put("NE", "Northeast");
		directions.put("NW", "Northwest");
		directions.put("SE", "Southeast");
		directions.put("SW", "Southwest");
		directions.put("E", "East");
		directions.put("W", "West");
		
		
		//Speak all instructions for first time using app
		boolean shortInstr = s.getBoolean("notFirstTime", false);
		String instruct = INSTR;
		if(!shortInstr){
			Editor e = s.edit();
			e.putBoolean("notFirstTime", true);
			e.commit();
			instruct += EXTRA_INSTR;
		}
				
		if(!mTTS.isSpeaking()){
			speak(instruct);
		}
	}
	
	/**
	 * Builds Sighted Interface
	 * @param bgColor
	 * @param txtColor
	 * @param txtSize
	 * @param mute
	 */
	private void initializeSighted(int bgColor, int txtColor, float txtSize, boolean mute){
		setContentView(R.layout.main);
		RelativeLayout a = (RelativeLayout) findViewById(R.id.MainRelativeLayout01);

        
		mHSV = (HorizontalScrollView) findViewById(R.id.MainHorizontalScrollView01);
		mTV = (TextView) findViewById(R.id.MainTextView01);
		mZI = (ImageButton) findViewById(R.id.MainImageButton01);
		mZO = (ImageButton) findViewById(R.id.MainImageButton02);
		mSound = (ImageButton) findViewById(R.id.MainImageButton03);

		if(mute){
			mSound.setImageResource(R.drawable.mute);
		}else{
			mSound.setImageResource(R.drawable.speaker);
		}
		
		
		a.setBackgroundColor(bgColor);
		mHSV.setBackgroundColor(bgColor);
		mTV.setTextColor(txtColor);
		mTV.setTextSize(txtSize);
		mHSV.fullScroll(View.FOCUS_LEFT);
		
		
        mHSV.setOnTouchListener(mScrollTouchListener);
		mZI.setOnClickListener(mZoomInClick);
		mZO.setOnClickListener(mZoomOutClick);
		mSound.setOnClickListener(mSoundClick);

		mTV.setText(INSTR);
	}

	/**
	 * On destroy saves preferences
	 */
	@Override
	public void onDestroy(){
		mLocFind.stopLocationUpdates();
        if(vision){
        	SharedPreferences s = this.getSharedPreferences(PREF, 0);
        	SharedPreferences.Editor e = s.edit();
        	e.putBoolean("mute", mute);
        	e.putFloat("text_Size", mTV.getTextSize());
        	e.commit();
        }
		super.onDestroy();
	}
	
	/**
	 * Speaks the string s if not muted or the person is not sighted
	 * @param s
	 */
	private void speak(String s){
		if(!mute || !vision)
			mTTS.speak(s, TextToSpeech.QUEUE_FLUSH, null);
	}
	
	/**
	 * Zoom out handler object
	 */
	private View.OnClickListener mZoomOutClick = new View.OnClickListener(){

		public void onClick(View v) {
			float size = Math.max(mTV.getTextSize()*.8f, MIN_TEXT_SIZE);
			mTV.setTextSize(size);
		}
		
	};
	
	
	/**
	 * Listens to the horizontal scroll views touches so that
	 * it can override them
	 */
	private OnTouchListener mScrollTouchListener = new OnTouchListener(){

		public boolean onTouch(View v, MotionEvent event) {
			// TODO Auto-generated method stub
			return gestureScanner.onTouchEvent(event);
		}
		
	};
	
	/**
	 * Handles touch events for the non vision interface
	 */
	@Override
	public boolean onTouchEvent(MotionEvent me){
		return gestureScanner.onTouchEvent(me);
	}
	
	/**
	 * Zoom in handler object
	 */
	private View.OnClickListener mZoomInClick = new View.OnClickListener(){

		public void onClick(View v) {
			float size = Math.min(mTV.getTextSize()*1.25f, MAX_TEXT_SIZE);
			mTV.setTextSize(size);
		}
		
	};

	/**
	 * Mute button listener object
	 */
	private View.OnClickListener mSoundClick = new View.OnClickListener(){

		public void onClick(View v) {
			if(!mute){
				mSound.setImageResource(R.drawable.mute);
				mTTS.stop();
			}else{
				mSound.setImageResource(R.drawable.speaker);
			}
			mute = !mute;
		}
		
	};
		
	
	/**
	 * Overrides Menu to go back to the settings screen
	 * and overrides Back when the current business isn't
	 * the users current location, to take them back
	 * to that point in the list
	 */
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if( keyCode == KeyEvent.KEYCODE_MENU){
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_BACK && businesses != null && currentBusiness != intCurrentLocation){
			currentBusiness = intCurrentLocation;
			if(vision){
				mTV.setText(strTextCurrentLocation);
				mTV.setTextSize(mTV.getTextSize() + 1);
				mTV.setTextSize(mTV.getTextSize() - 1);
				mHSV.fullScroll(View.FOCUS_LEFT);
			}
			speak(strSpokenCurrentLocation);
			return true;
		}else{
			return super.onKeyDown(keyCode, event);
		}
	}
	
	/**
	 * Override for menu and back also on up needs to happen
	 */
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		if( keyCode == KeyEvent.KEYCODE_MENU){
			mTTS.stop();
	    	startActivity(new Intent(this, SettingsConfiguration.class));
			finish();
			return true;
		}else if(keyCode == KeyEvent.KEYCODE_BACK ){
			return true;
		}else{
			return super.onKeyUp(keyCode, event);
		}
	}

	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return true;
	}

	/**
	 * Process in which direction the fling was
	 * fastest and increments the list forward
	 * for a forward fling. Also, checks the bounds
	 * of the list.
	 */
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if(Math.abs(velocityX) < Math.abs(velocityY)){
			if(businesses == null || businesses.size() <= 1){
				speak("Sorry, I cannot detect anything around you");
			}else {
				int queue = TextToSpeech.QUEUE_FLUSH;
				if(velocityY > 0){						
					if(currentBusiness == maxBusiness){
						speak("Sorry, I don't know of anything located beyond " + businesses.get(currentBusiness).get("name"));	
						queue = TextToSpeech.QUEUE_ADD;
					}else{
						currentBusiness++;
					}
				}else if(velocityY < 0){						
					if(currentBusiness == minBusiness){
						speak("Sorry, I don't know of anything located beyond " + businesses.get(currentBusiness).get("name"));
						queue = TextToSpeech.QUEUE_ADD;
					}else{
						currentBusiness--;
					}
				}
				if(currentBusiness == intCurrentLocation){
					if(vision){
						mTV.setText(strTextCurrentLocation+" ");
						mTV.setTextSize(mTV.getTextSize() + 1);
						mTV.setTextSize(mTV.getTextSize() - 1);
						mHSV.fullScroll(View.FOCUS_LEFT);
					}
					mTTS.speak(strSpokenCurrentLocation, queue ,null);
				}else{
					Map<String,String> business = businesses.get(currentBusiness);
					String name = business.get("name");
					String bear = business.get("bearing");
					String distance = business.get("distance");
					String info = name + " is " + distance + " meters " + bear + " "; 
					if(vision){
						mTV.setText(info);
						mTV.setTextSize(mTV.getTextSize() + 1);
						mTV.setTextSize(mTV.getTextSize() - 1);
						mHSV.fullScroll(View.FOCUS_LEFT);
					}
					info = name + " is " + distance + " meters " + directions.get(bear);
					mTTS.speak(info, queue ,null);				}
				return true;
			}
			return true;
		}else{
			if(vision){
				mHSV.fling(-1*(int)velocityX);
			}
			return true;
		}
	}

	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	/**
	 * Need to redo scroll because otherwise the
	 * horizontal scroll wont scroll.
	 */
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		if(vision){
			mHSV.scrollBy((int)distanceX, (int)distanceY);
		}
		return true;
	}

	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	public boolean onSingleTapUp(MotionEvent e) {
		
		return false;
	}
	
	private void findCurrentBusinessLoc() {
		for(Map<String,String> business : businesses){
			if(business.get("relative_loc").equals("Exact")){
				currentBusiness = businesses.indexOf(business);
				intCurrentLocation = businesses.indexOf(business);
			}
		}
	}

	/**
	 * Calls the business if it has a phone number
	 */
	public boolean onDoubleTap(MotionEvent e) {
		mTTS.stop();
		if(currentBusiness != intCurrentLocation){
			Map<String,String> business = businesses.get(currentBusiness);
			String phoneNum = business.get("phone");
			if(phoneNum != null && !phoneNum.equals("")){
				phoneNum = "tel://1 " + phoneNum;
			
				Intent i = new Intent(Intent.ACTION_CALL, Uri.parse(phoneNum));
				i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			
				this.startActivity(i);
			}else{
				speak("Sorry, I do not have a phone number for " + business.get("name"));
			}
			return true;
		}
		return false;
	}

	public boolean onDoubleTapEvent(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * Spits out more information about the current business.
	 * Currently just the address
	 */
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if(businesses != null && currentBusiness != intCurrentLocation){
			Map<String,String> business = businesses.get(currentBusiness);
			String name = business.get("name");
			String info = name + " is at " + LocationFinder.parse(business.get("address"));
			speak(info);
		}else{
			pd = new ProgressDialog(this, ProgressDialog.STYLE_SPINNER);
			pd.setMessage("Calculating GPS and finding local businesses");
			Thread comp = new Thread(getBusinesses);
			comp.start();
		}
		return true;
	}

	/**
	 * Runnable for thread to get business so
	 * waiting screen doesn't appear.
	 */
	private Runnable getBusinesses = new Runnable(){
		private String info;
		
		public void run() {
			try{			
				info = mLocFind.getInfo(false, false);
				
				if(info != null){
					double mlat = mLocFind.getCurrentLocation().getLatitude();
					double mlong = mLocFind.getCurrentLocation().getLongitude();
					String heading = mLocFind.getHeading();
					businesses = BusinessFinder.getPOIs(mlat, mlong, heading, "*");
					maxBusiness = businesses.size()-1;
					minBusiness = 0;
					findCurrentBusinessLoc();
				}
				
				handle.sendEmptyMessage(0);
			} catch (Exception ex){
				info = null;
				Log.e(ex.toString(), ex.getMessage());
			}

		}
		
		/**
		 * Handles the reception of all the businesses
		 */
		private Handler handle = new Handler(){
			@Override
            public void handleMessage(Message msg) {
                    pd.dismiss();
    				if(info == null){
    					info = "Sorry, I cannot determine your current location"; 
    					speak(info);
    					businesses = null;
    				}else{
    					strTextCurrentLocation = info;
    					strSpokenCurrentLocation = mLocFind.getInfo(true, true);
    					speak(strSpokenCurrentLocation);
    				}
    				if(vision){
	        			mTV.setText(info);
	        			mHSV.fullScroll(View.FOCUS_LEFT);
	        			mTV.setTextSize(mTV.getTextSize() + 1);
	        			mTV.setTextSize(mTV.getTextSize() - 1);
    				}
            }
		};
	};	
}
