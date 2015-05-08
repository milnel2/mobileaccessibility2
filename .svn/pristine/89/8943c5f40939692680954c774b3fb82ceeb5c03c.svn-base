package cs.washington.edu.buddies;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

/* Displays aisles and products that the player can purchase
 * for his pet.
 */
public class Store extends Activity implements View.OnTouchListener, View.OnClickListener, 
	GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener,
	TextToSpeech.OnUtteranceCompletedListener, ToastDisplay {

	//for GUI
	private Button[] mButtons;
	private Button blankBtn;
	private View[] mViews;
	
	// for speech
	private TextToSpeech tts;
    HashMap<String, String> utterance = new HashMap<String, String>();
    private int selection;

	// for logging
	private static final String TAG = "Store";

	// product types
	protected static final int FOOD_PRODUCT = 0;
	protected static final int PLAY_PRODUCT = 1;
	protected static final int CARE_PRODUCT = 2;
	protected static final int ACCESS_PRODUCT = 3;

	private int[] lastLoc = {-2, -2};  // different than menus since there's content view changes
	private boolean storeViewShowing;
	
	// Vars for catching long press events
	private long longPressStartTm;
    private long longPressDuration;
	private boolean longSpeech;
	private boolean ttsOn;
	private GestureDetector detector; 
	private MotionEvent lastDownEvent = null;
	private int scrollDistance;
	
	// for retouches on same button after lifting
	private int clearFocusInterval = 1500;
    private long lastUpTime = 0;

    // for captions
    private Toast toast;
    private Thread t;
    private String[] toastMsgsStore = {"Touch and drag finger or use trackball " +
    		"to hear the aisle names",
    	"Touch and drag finger or use trackball " +
    		"to hear the aisle names",
		"Double tap or press down on trackball on the aisle" +
			" you want when you hear its name",
			"Double tap or press down on trackball on the aisle" +
			" you want when you hear its name"};
    private String[] toastMsgsAisle = {"Scroll right for next or left for previous product",
        	"Scroll right for next or left for previous product",
    		"Double tap on product to purchase",
    		"Double tap on Back to Store button to leave"};
    private int index = 0;
	
	private boolean confirm = false;
	private boolean goback = false;
	
	private MediaPlayer mp;
	
	private static final String storeInstr = "Touch and drag finger or use trackball" +
		" to hear the aisle names, double tap or press down on trackball on the aisle" +
		" you want when you hear its name";
	
	private static final String aisleInstr = "Scroll right for next product," +
		" scroll left for previous product, tap once to hear product information," +
		" double-tap to purchase a product, press the Back key or double tap the" +
		" back to store button at the bottom of the screen to leave the aisle";
	private boolean firstx = true;
	
	private Store self;

	// For holding products
	private ArrayList<Product> foodProducts;
	private ArrayList<Product> playProducts;
	private ArrayList<Product> careProducts;
	private ArrayList<Product> accessProducts;
	private ArrayList<Product> list;
	private int item;
	private int itemToBuy;
	
	// Handle to UI thread so UI can be updated
	// by worker threads
	private final Handler mHandler = new Handler();
	
	// Called by worker threads to switch views
	final Runnable mEnterAisle = new Runnable() {
		public void run() {
			enterAisle();
		}
	};
	
	// Called by worker threads to refresh product
	final Runnable mRefreshProduct = new Runnable() {
		public void run() {
			showProduct(TextToSpeech.QUEUE_FLUSH);
		}
	};
	
	public void displayToast() {
		if (storeViewShowing) {
			if (index < toastMsgsStore.length) {
				toast = Toast.makeText(getApplicationContext(), toastMsgsStore[index], Toast.LENGTH_SHORT);
				toast.show();
				index++;
			}
		}
		else {
			if (index < toastMsgsAisle.length) {
				toast = Toast.makeText(getApplicationContext(), toastMsgsAisle[index], Toast.LENGTH_SHORT);
				toast.show();
				index++;
			}
		}
	}

	protected static class Product {

		public String mName;
		public int mPrice;
		public String mSound;
		public String mUse;

		public Product(String name, int price, String sound, String use) {
			mName = name;
			mPrice = price;
			mSound = sound;
			mUse = use;
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.store);

		
		tts = Game.tts;
		self = this;  // store for callbacks
		detector = new GestureDetector(this, this);
		
		// Check whether user wants to hear speech -- might not
        // want to if using a screen reader
        ttsOn = Settings.getTts(this);
        longSpeech = false;
        
        // Set time requirements for long presses and triple taps
        longPressDuration = 2000;
        
        // for clearing focus so repeat can occur
        clearFocusInterval = 1500;
        
        scrollDistance = 0;
        
		readProductsFromXmlFile();
		getStoreElements();
		storeViewShowing = true;

        // Toast display thread
        t = new Thread(new ToastDisplayer(this, mHandler, toastMsgsStore.length));
		t.start();
		tts.speak(storeInstr, TextToSpeech.QUEUE_FLUSH, null);
		
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}
	
	public static ArrayList<Product> getProductsByType(Context context, int type) {
		ArrayList<Product> list = new ArrayList<Product>();
		XmlResourceParser parser = context.getResources().getXml(R.xml.products);

		// parse the xml file and load in products
		int eventType = -1;

		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_DOCUMENT) {
				Log.d(TAG, "Document Start");
			}
			else if (eventType == XmlResourceParser.START_TAG) {
				String str = parser.getName();
				if (str.equals("item")) {
					String name = parser.getAttributeValue(null, "name");
					String price = parser.getAttributeValue(null, "price");
					String sound = parser.getAttributeValue(null, "sound");
					String use = parser.getAttributeValue(null, "use");
					if (type == Integer.valueOf(use)) {
						list.add(new Product(name, Integer.valueOf(price), 
								sound, use));
					}
				}
			}
			try {
				eventType = parser.next();
			} 
			catch (XmlPullParserException e) {} 
			catch (IOException e) {}
		}
		return list;
	}
	

	/* */
	private void readProductsFromXmlFile() {

		XmlResourceParser parser = getResources().getXml(R.xml.products);

		// Initialize arraylist
		foodProducts = new ArrayList<Product>();
		playProducts = new ArrayList<Product>();
		careProducts = new ArrayList<Product>();
		accessProducts = new ArrayList<Product>();

		// parse the xml file and load in products
		int eventType = -1;

		while (eventType != XmlResourceParser.END_DOCUMENT) {
			if (eventType == XmlResourceParser.START_DOCUMENT) {
				Log.d(TAG, "Document Start");
			}
			else if (eventType == XmlResourceParser.START_TAG) {
				String str = parser.getName();
				if (str.equals("item")) {
					String name = parser.getAttributeValue(null, "name");
					String price = parser.getAttributeValue(null, "price");
					String sound = parser.getAttributeValue(null, "sound");
					String use = parser.getAttributeValue(null, "use");
					switch (Integer.valueOf(use)) {
					case FOOD_PRODUCT:
						foodProducts.add(new Product(name, Integer.valueOf(price), 
								sound, use));
						break;
					case PLAY_PRODUCT:
						playProducts.add(new Product(name, Integer.valueOf(price), 
								sound, use));
						break;
					case CARE_PRODUCT:
						careProducts.add(new Product(name, Integer.valueOf(price), 
								sound, use));
						break;
					case ACCESS_PRODUCT:
						accessProducts.add(new Product(name, Integer.valueOf(price), 
								sound, use));
						break;
					}
					
				}
			}
			try {
				eventType = parser.next();
			} 
			catch (XmlPullParserException e) {} 
			catch (IOException e) {}
		}	    	
	}

	/* Get the references for the buttons on the store view */
	private void getStoreElements() {
		mButtons = new Button[5];
		mButtons[0] = (Button)findViewById(R.id.food_product);
		mButtons[1] = (Button)findViewById(R.id.play_product);
		mButtons[2] = (Button)findViewById(R.id.care_product);
		mButtons[3] = (Button)findViewById(R.id.accessory_product);
		mButtons[4] = (Button)findViewById(R.id.exit_store);
		for (int i = 0; i < mButtons.length; i++) {
			String str = (String)mButtons[i].getTag();
			final String tag = str;
			mButtons[i].setFocusableInTouchMode(true);
			mButtons[i].setOnTouchListener(this);
			mButtons[i].setOnClickListener(this);
			mButtons[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {

				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						tts.speak(tag, TextToSpeech.QUEUE_FLUSH, null);
					}
				}
			});
		}
		blankBtn = (Button)findViewById(R.id.blank_button);
		blankBtn.requestFocus();
	}
	
	/* Get the text fields and button on the aisle view */
	private void getAisleElements() {
		mViews = new View[5];
		mViews[0] = (TextView)findViewById(R.id.product_label);
		mViews[1] = (TextView)findViewById(R.id.product_name);
		mViews[2] = (TextView)findViewById(R.id.price);
		mViews[3] = (TextView)findViewById(R.id.purchased);
		mViews[4] = (Button)findViewById(R.id.back_to_store_button);
		mViews[4].setOnClickListener(this);
		for (int i = 0; i < mViews.length; i++) {
			mViews[i].setFocusableInTouchMode(true);
			mViews[i].setOnTouchListener(this);
		}
		mViews[4].setOnFocusChangeListener(new View.OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus)
					tts.speak((String)mViews[4].getTag(), TextToSpeech.QUEUE_FLUSH, null);
			}
		});
		blankBtn = (Button)findViewById(R.id.quiet_focus);
		blankBtn.requestFocus();
		
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) { 
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (confirm) {
				confirm = false;
				if (goback) {
					leaveAisle();
				}
				else if (item == itemToBuy) {
					boolean success = buyItem();
					if (success)
						tts.speak("Item purchased, your balance is now " + 
								String.valueOf(BankAccount.getBalance(this)) +
								" tokens", TextToSpeech.QUEUE_FLUSH, null);
				}
			}
			return super.onKeyDown(keyCode, keyEvent);
		}
		int queueAction = TextToSpeech.QUEUE_FLUSH;
		if(keyCode == KeyEvent.KEYCODE_MENU){
			// any key press other than center key down stops purchase
			if (confirm) {
				confirm = false;  
				if (!goback) {
					tts.speak("Purchase cancelled", TextToSpeech.QUEUE_FLUSH, null);
					queueAction = TextToSpeech.QUEUE_ADD;
				}
			}
			tts.speak("Menu kee pressed", queueAction, null);
			
		}
		else if (keyCode == KeyEvent.KEYCODE_BACK) {
			// any key press other than center key down stops purchase
			if (confirm) {
				confirm = false; 
				if (!goback) {
					tts.speak("Purchase cancelled", TextToSpeech.QUEUE_FLUSH, null);
					queueAction = TextToSpeech.QUEUE_ADD;
				}
			}
			tts.speak("Back kee pressed, returning to spend and earn tokens menu", queueAction, null);
			if (t.isAlive())
				t.interrupt();
			toast.cancel();
			return super.onKeyDown(keyCode, keyEvent);
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
    		boolean result = false;
			// scroll right
    		if (!storeViewShowing) {
    			scrollRight();
    			result = true;
    		}
    		return result;
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
    		boolean result = false;
    		// scroll left
    		if (!storeViewShowing) {
	    		scrollLeft();
	    		result = true;
			}
			return result;
		}
		else {
			// any key press other than center key down stops purchase
			if (confirm) {
				confirm = false;  
				if (!goback)
					tts.speak("Purchase cancelled", TextToSpeech.QUEUE_FLUSH, null);
			}
			return super.onKeyDown(keyCode, keyEvent);
		}
		Log.v(TAG, "onKeyDown");
		return true;
	}

	/* Called for touches that are not initial button touches */
	@Override
	public boolean onTouchEvent(MotionEvent me) {
		Log.v(TAG, "onTouchEvent");
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			longPressStartTm = me.getDownTime();
		}
		
		// Capture long presses that occur within a single region and
		// repeat instructions.
		if (me.getEventTime() - longPressStartTm > longPressDuration) {
			longSpeech = true;
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
			if (storeViewShowing) {
				tts.speak("Store view displayed, one moment please", TextToSpeech.QUEUE_FLUSH, null);
				tts.speak(storeInstr, TextToSpeech.QUEUE_ADD, utterance);
			}
			else {
				String aisle = "accessories";
				if (selection == FOOD_PRODUCT)
					aisle = "Food";
				else if (selection == PLAY_PRODUCT)
					aisle = "Toy";
				else if (selection == CARE_PRODUCT)
					aisle = "care and comfort";
				tts.speak(aisle + " aisle displayed, one moment please", TextToSpeech.QUEUE_FLUSH, null);
				tts.speak(aisleInstr, TextToSpeech.QUEUE_ADD, utterance);
			}		
			longPressStartTm = me.getEventTime();
		}
		return true;
	}

	/* Called for touches inside the button display */
	@Override
	public boolean onTouch(View v, MotionEvent me) {
		Log.v(TAG, "onTouch");
		// call detector's touch event so double tap on button
		// will be caught.
		if (detector != null) this.detector.onTouchEvent(me);
		if (me.getAction() == MotionEvent.ACTION_DOWN) {
			longPressStartTm = me.getDownTime();
			if (storeViewShowing && me.getDownTime() - lastUpTime > clearFocusInterval 
					&& lastLoc[0] >= 0 && lastLoc[0] < mButtons.length)
				mButtons[lastLoc[0]].clearFocus();
			findKey(me);
		} 
		// Only process movement events that occur more than a
		// predetermined interval (in ms) apart to improve performance
		else if (me.getAction() == MotionEvent.ACTION_MOVE) {
			findKey(me);
		}
		else if (me.getAction() == MotionEvent.ACTION_UP) {
			lastUpTime = me.getEventTime();
		}
		
		// Capture long presses that occur within a single region and
		// repeat instructions.
		if (me.getEventTime() - longPressStartTm > longPressDuration) {
			longSpeech = true;
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
			
			if (storeViewShowing) {
				tts.speak("Store view displayed, one moment please", TextToSpeech.QUEUE_FLUSH, null);
				tts.speak(storeInstr, TextToSpeech.QUEUE_ADD, utterance);
			}
			else {
				String aisle = "accessories";
				if (selection == FOOD_PRODUCT)
					aisle = "Food";
				else if (selection == PLAY_PRODUCT)
					aisle = "Toy";
				else if (selection == CARE_PRODUCT)
					aisle = "care and comfort";
				tts.speak(aisle + " aisle displayed, one moment please", TextToSpeech.QUEUE_FLUSH, null);
				tts.speak(aisleInstr, TextToSpeech.QUEUE_ADD, utterance);
			}		
			longPressStartTm = me.getEventTime();
		}
		return true;
	}

	/* Locates the button on which the motion event occurred 
	 * and gives focus to that button.  Only uses the Y
	 * coordinate because buttons are full screen width and
	 * run vertically down screen.  Button ids must be stored
	 * in array in the same order they are displayed on screen.
	 */
	private boolean findKey(MotionEvent me) {
		double y = me.getRawY();
		int[] loc = new int[2];
		if (storeViewShowing) {
			for (int i = 0; i < mButtons.length; i++) {
				mButtons[i].getLocationOnScreen(loc);
				// If the motion event goes over the button, have the button request focus
				if (y <= (loc[1] + mButtons[i].getHeight())) {
					mButtons[i].requestFocus();
					if (i != lastLoc[0]) {
						longPressStartTm = me.getEventTime();
						lastLoc[0] = i;
					}
					return true;
				}
			}
		}
		else {
			for (int i = 0; i < mViews.length; i++) {
				mViews[i].getLocationOnScreen(loc);
				// If the motion event goes over the button, have the button request focus
				if (y <= (loc[1] + mViews[i].getHeight()) ) {
					mViews[i].requestFocus();
					if (i != lastLoc[1]) {
						longPressStartTm = me.getEventTime();
						lastLoc[1] = i;
					}
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (t.isAlive())
			t.interrupt();
		toast.cancel();
		switch (v.getId()) {
		case R.id.exit_store:
			Log.v(TAG, "Exit store selected");
			tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "exit");
    		// make sure speaking finishes before moving on
    		tts.speak("You are leaving the store", TextToSpeech.QUEUE_FLUSH, utterance);
			break;
		case R.id.food_product:
			Log.v(TAG, "Food aisle selected");
			tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "aisle");
    		selection = FOOD_PRODUCT;
    		// make sure speaking finishes before moving on
    		tts.speak("You are entering the food aisle", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
		case R.id.play_product:
			Log.v(TAG, "Toy aisle selected");
			tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "aisle");
    		selection = PLAY_PRODUCT;
    		// make sure speaking finishes before moving on
    		tts.speak("You are entering the toy aisle", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
		case R.id.care_product:
			Log.v(TAG, "Care and comfort aisle selected");
			tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "aisle");
    		selection = CARE_PRODUCT;
    		// make sure speaking finishes before moving on
    		tts.speak("You are entering the care and comfort products aisle", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
		case R.id.accessory_product:
			Log.v(TAG, "Accessory aisle selected");
			tts.setOnUtteranceCompletedListener(self);
    		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "aisle");
    		selection = ACCESS_PRODUCT;
    		// make sure speaking finishes before moving on
    		tts.speak("You are entering the accessories aisle", TextToSpeech.QUEUE_FLUSH, utterance);
    		break;
		case R.id.back_to_store_button:
			Log.v(TAG, "Go back to store selected");
    		leaveAisle();
    		break;
		}
	}	

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		boolean result = false;
		if (storeViewShowing) {
			for (int i = 0; i < 5; i++) {
				if (i == lastLoc[0]) {
					mButtons[i].performClick();	
					result = true;
					break;
				}
			}
		}
		else {
			int[] loc = new int[2];
			mViews[4].getLocationOnScreen(loc);
			if (e.getRawY() < loc[1]) {
				confirm = true;
				goback = false;
				itemToBuy = item;
				tts.speak("Tap on screen or press center key or down on trackball to " +
						"confirm purchase of " + list.get(item).mName,
					TextToSpeech.QUEUE_FLUSH, null);	
				result = true;
			}
			else {
				confirm = true;
				goback = true;
				tts.speak("Exit selected. Tap on screen or press center button " +
						"to leave aisle", TextToSpeech.QUEUE_FLUSH, null);	
				result = true;
			}
			if (t.isAlive())
				t.interrupt();
			toast.cancel();
			toast = Toast.makeText(this, "Tap on screen to confirm", Toast.LENGTH_SHORT);
			toast.show();
		}
		return result;
	}

	@Override
	public boolean onDoubleTapEvent(MotionEvent e) {
		// set to true
		return true;
	}

	@Override
	public boolean onSingleTapConfirmed(MotionEvent e) {
		if (confirm) {
			confirm = false;
			if (goback) {
				leaveAisle();
			}
			else if (item == itemToBuy) {
				boolean success = buyItem();
				if (success)
					tts.speak("Item purchased, your balance is now " + 
							String.valueOf(BankAccount.getBalance(this)) +
							" tokens", TextToSpeech.QUEUE_FLUSH, null);
				toast.cancel();
				toast = Toast.makeText(this, "Item purchased", Toast.LENGTH_SHORT);
				toast.show();
				return true;
			}
		}
		else if (!storeViewShowing && item >= 0 && item < list.size())
			showProduct(TextToSpeech.QUEUE_FLUSH);
		else if (longSpeech) { 
			tts.speak("Speech stopped", TextToSpeech.QUEUE_FLUSH, null);
			longSpeech = false;
		}
		return true;
	} 

	@Override
	public void onUtteranceCompleted(String utteranceId) {
		if (utteranceId.equalsIgnoreCase("aisle")) {
			tts.setOnUtteranceCompletedListener(null);
			mHandler.post(mEnterAisle);
		}
		else if (utteranceId.equalsIgnoreCase("exit")) {
			tts.setOnUtteranceCompletedListener(null);
			finish();
		}
		else if (utteranceId.equalsIgnoreCase("longSpeech")) {
			longSpeech = false;
		}	
	}
	
	protected void enterAisle() {
		if (selection == FOOD_PRODUCT)
			list = foodProducts;
		else if (selection == PLAY_PRODUCT)
			list = playProducts;
		else if (selection == CARE_PRODUCT)
			list = careProducts;
		else
			list = accessProducts;
		if (list.size() == 0) {
			tts.speak("There are no products in this aisle", TextToSpeech.QUEUE_FLUSH, null);
			return;
		}
		item = 0;
		
		setContentView(R.layout.aisle);
		getAisleElements();
		if (scrollDistance == 0) {
			scrollDistance = ((LinearLayout)findViewById(R.id.aisle)).getWidth() / 2;
			scrollDistance = (scrollDistance == 0) ? 100 : scrollDistance;
		}
		
		storeViewShowing = false;
		lastLoc[1] = -1;  // resets after content view change
		
//		int balance = BankAccount.getBalance(this);
//		tts.speak("you have " + balance + " tokens to spend", TextToSpeech.QUEUE_FLUSH, null);
		
		index = 0;
		if (t.isAlive())
			t.interrupt();
		toast.cancel();
		
		if (firstx) {
			firstx = false;
			tts.setOnUtteranceCompletedListener(self);
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
			tts.speak(aisleInstr, TextToSpeech.QUEUE_ADD, utterance);
			t = new Thread(new ToastDisplayer(this, mHandler, toastMsgsAisle.length));
			t.start();
		}
				
		showProduct(TextToSpeech.QUEUE_ADD);
	}
	
	private void leaveAisle() {
		confirm = false;
		tts.speak("Leaving aisle", TextToSpeech.QUEUE_FLUSH, null);
		setContentView(R.layout.store);
		getStoreElements();
		lastLoc[0] = -1;  // resets after content view change
		storeViewShowing = true;
		
		index = 0;
		if (t.isAlive())
			t.interrupt();
		toast.cancel();
//		t = new Thread(new ToastDisplayer(this, mHandler, toastMsgsStore.length));
//		t.start();
		
//		tts.setOnUtteranceCompletedListener(self);
//		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
//		tts.speak(storeInstr, TextToSpeech.QUEUE_ADD, utterance);
		tts.speak("Store view displayed", TextToSpeech.QUEUE_ADD, null);
	}
	
	protected void showProduct(int queueAction) {
		
		// set product name
		String name = list.get(item).mName;
		((TextView)mViews[1]).setText(name);
		((TextView)mViews[1]).setTag(name);
		
		// set product price
		int cost = list.get(item).mPrice;
		String price = String.valueOf(cost);
		((TextView)mViews[2]).setText("Price: " + price + " tokens"); 
		((TextView)mViews[2]).setTag("price: " + price + " tokens");
		
		// set product price
		int count = Purchases.getItemCount(this, name);
		String ct = String.valueOf(count);
		((TextView)mViews[3]).setText("Purchased: " + ct); 
		((TextView)mViews[3]).setTag("you have " + ct + " already");
		
		// play product sound
		String soundFile = list.get(item).mSound;
		int id = 0;
		if (!soundFile.equals("")) {
			soundFile = getPackageName() + ":raw/" + soundFile;
			id = getResources().getIdentifier(soundFile, null, null);
		}
		if (id != 0) {
			if (mp != null)
				mp.reset();
			mp = MediaPlayer.create(this, id);
			try {
				mp.prepare();
			} 
			catch (IllegalStateException e) {}
			catch (IOException e) {}
			mp.start();
		}
		
		// read product info if long speech was interrupted
		tts.speak("showing " + name + ", prys is " + price + " tokens," +
				" you own " + ct + ((count == 0)? "":" already"), 
				queueAction, null);
		
		// scrolling stops purchase or exit
		if (confirm) {
			confirm = false; 
			if (!goback) {
				tts.speak("Purchase not made", TextToSpeech.QUEUE_FLUSH, null);
				queueAction = TextToSpeech.QUEUE_ADD;
			}
		}
	}
	
	private boolean buyItem() {
		boolean result = false;
		if (item >= 0 && item < list.size()) {
			
			// make sure you have enough money
			int price = list.get(item).mPrice;
			if (BankAccount.enoughFunds(this, price)) {			
				buyTxn(price);
				showProduct(TextToSpeech.QUEUE_FLUSH);
				result = true;
			}
			else 
				tts.speak("Sorry you do not have enough tokens", TextToSpeech.QUEUE_FLUSH, null);
		}
		return result;
	}

	private synchronized void buyTxn(int price) {
		String name = list.get(item).mName;
		// add item to purchases
		// and decrement bank account
		BankAccount.addToBalance(this, -price);
		Purchases.buyItem(this, name);
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2,
			float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent start, MotionEvent stop, float arg2,
			float arg3) {
		boolean result = false;
		// scroll right
		if (!storeViewShowing) {
			if (stop.getRawX() - start.getRawX() > scrollDistance 
				&& lastDownEvent != start) {
				lastDownEvent = start;
				scrollRight();
				result = true;
			}
			// scroll left
			else if (stop.getRawX() - start.getRawX() < -scrollDistance 
					&& lastDownEvent != start) {
				lastDownEvent = start;
				scrollLeft();
				result = true;
			}
		}
		return result;
	}
	
	private void scrollRight() {
		int queueAction = TextToSpeech.QUEUE_FLUSH;
		item++;
		if (item >= list.size())
			item = list.size() - 1;
		if (item == list.size() - 1) {
			tts.speak("At last product", TextToSpeech.QUEUE_FLUSH, null);
			queueAction = TextToSpeech.QUEUE_ADD;
		}
		showProduct(queueAction);
	}
	
	private void scrollLeft() {
		item--;
		int queueAction = TextToSpeech.QUEUE_FLUSH;
		if (item < 0)
			item = 0;
		if (item == 0) {
			tts.speak("At first product", TextToSpeech.QUEUE_FLUSH, null);
			queueAction = TextToSpeech.QUEUE_ADD;
		}
		showProduct(queueAction);
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		
		return false;
	}

}




