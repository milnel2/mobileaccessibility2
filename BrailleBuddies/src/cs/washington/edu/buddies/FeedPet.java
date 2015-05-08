package cs.washington.edu.buddies;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

/* Displays food products purchased by the player and lets player
 * select which one to feed to pet.
 */
public class FeedPet extends Activity implements View.OnTouchListener, View.OnClickListener, 
	GestureDetector.OnDoubleTapListener, GestureDetector.OnGestureListener,
	TextToSpeech.OnUtteranceCompletedListener, ToastDisplay {

	//for GUI
	private Button blankBtn;
	private View[] mViews;
	
	// for speech
	private TextToSpeech tts;
    HashMap<String, String> utterance = new HashMap<String, String>();

	// for logging
	private static final String TAG = "FeedPet";
	private int lastLoc = -1; 
	
	// Vars for catching long press events
	private long longPressStartTm;
    private long longPressDuration;
	private boolean longSpeech;
	private boolean ttsOn;
	private GestureDetector detector; 
	private MotionEvent lastDownEvent = null;
	private int scrollDistance;
	
	private boolean confirm = false;
	private boolean goback = false;
	
	private MediaPlayer mp;
	
	private static final String instr = "Scroll right to move to the next product," +
		" scroll left to go back to the previous product, tap once on screen to " +
		" hear product information and button locations, double-tap on the screen to " +
		" feed the current product to your pet, press the Back key or double tap on " +
		" the back button at the bottom of the screen to return to the pet status screen";
	
	private FeedPet self;

   	// For holding products
	private ArrayList<Store.Product> list;
	private int item;
	private int itemToFeed;
	
	// for captions
    private Toast toast;
    private Thread t;
    private String[] toastMsgs = {"Scroll right for next or left for previous product",
    	"Scroll right for next or left for previous product",
		"Double tap on product to feed pet",
		"Double tap on Back button to leave"};
    private int index = 0;
    
    // Handle to UI thread so UI can be updated
	// by worker threads
	protected final Handler mHandler = new Handler();
	
	public void displayToast() {
		if (index < toastMsgs.length) {
			toast = Toast.makeText(getApplicationContext(), toastMsgs[index], Toast.LENGTH_SHORT);
			toast.show();
			index++;
		}
	}

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.aisle);
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
        // Toast display thread
        t = new Thread(new ToastDisplayer(this, mHandler, toastMsgs.length));
		t.start();
		
		tts = Game.tts;
		self = this;  // store for callbacks
		detector = new GestureDetector(this, this);
		
		// Check whether user wants to hear speech -- might not
        // want to if using a screen reader
        ttsOn = Settings.getTts(this);
        longSpeech = false;
        
        // Set time requirements for long presses
        longPressDuration = 2000;
   
        scrollDistance = 0;
        getProductElements();
		getProducts();
	}
	
	/* Get the text fields and button on the aisle view */
	private void getProductElements() {
		mViews = new View[5];
		mViews[0] = (TextView)findViewById(R.id.product_label);
		mViews[1] = (TextView)findViewById(R.id.product_name);
		mViews[2] = (TextView)findViewById(R.id.price);
		mViews[3] = (TextView)findViewById(R.id.purchased);
		mViews[4] = (Button)findViewById(R.id.back_to_store_button);
		// Have to change tag and text on button since we're using the
		// view from the Store aisle
		mViews[4].setTag("Back to pet status and game menu");
		((Button)mViews[4]).setText("Back to Game");
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
	
	protected void getProducts() {
		
		// Get all food products -- need their names to find purchases by player
		ArrayList<Store.Product> al = Store.getProductsByType(this, Store.FOOD_PRODUCT);
		
		list = new ArrayList<Store.Product>();
		// Transfer products purchased by player to new ArrayList
		for (Store.Product prod : al) {
			if (Purchases.purchased(this, prod.mName)) {
				list.add(prod);
				prod = null;  // release memory
			}
		}
		if (list.size() == 0) {
			tts.setOnUtteranceCompletedListener(self);
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "exit");
			tts.speak("You do not have any food products at present, " +
					" returning to pet status and game menu", TextToSpeech.QUEUE_FLUSH, utterance);
		}
		else {
			item = 0;
			if (scrollDistance == 0) {
				scrollDistance = ((LinearLayout)findViewById(R.id.aisle)).getWidth() / 2;
				scrollDistance = (scrollDistance == 0) ? 100 : scrollDistance;
			}	
			tts.setOnUtteranceCompletedListener(self);
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
			tts.speak(instr, TextToSpeech.QUEUE_FLUSH, utterance);
			
			showProduct(TextToSpeech.QUEUE_ADD);
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent keyEvent) { 
		if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
			if (confirm) {
				confirm = false;
				if (goback) {
					mViews[4].performClick();
				}
				else if (item == itemToFeed) {
					boolean success = feedItem();
					if (success) {
						utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "exit");
						// make sure speaking finishes before moving on
						tts.speak("Item fed to your pet, you have " + 
								Purchases.getItemCount(this, list.get(item).mName) +
								" left, returning to pet status and game menu", 
								TextToSpeech.QUEUE_FLUSH, utterance);
					}
						
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
					tts.speak("Pet not fed", TextToSpeech.QUEUE_FLUSH, null);
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
					tts.speak("Pet not fed", TextToSpeech.QUEUE_FLUSH, null);
					queueAction = TextToSpeech.QUEUE_ADD;
				}
			}
			tts.speak("Back kee pressed, returning to pet status and game menu", queueAction, null);
			if (t.isAlive())
				t.interrupt();
			toast.cancel();
			return super.onKeyDown(keyCode, keyEvent);
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			// scroll right
    		scrollRight();
    		return true;
		}
		else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
    		// scroll left
	    	scrollLeft();
			return true;
		}
		else {
			// any key press other than center key down stops feeding
			if (confirm) {
				confirm = false;  
				if (!goback)
					tts.speak("Pet not fed", TextToSpeech.QUEUE_FLUSH, null);
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
			tts.speak("Food purchases displayed, one moment please", TextToSpeech.QUEUE_FLUSH, null);
			tts.speak(instr, TextToSpeech.QUEUE_ADD, utterance);
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
			findKey(me);
		} 
		// Only process movement events that occur more than a
		// predetermined interval (in ms) apart to improve performance
		else if (me.getAction() == MotionEvent.ACTION_MOVE) {
			findKey(me);
		}
	
		
		// Capture long presses that occur within a single region and
		// repeat instructions.
		if (me.getEventTime() - longPressStartTm > longPressDuration) {
			longSpeech = true;
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longSpeech");
			tts.speak("Food purchases displayed, one moment please", TextToSpeech.QUEUE_FLUSH, null);
			tts.speak(instr, TextToSpeech.QUEUE_ADD, utterance);
			longPressStartTm = me.getEventTime();
		}
		return true;
	}

	/* Locates the view on which the motion event occurred 
	 * and gives focus to that button.  Only uses the Y
	 * coordinate because buttons are full screen width and
	 * run vertically down screen.  Button ids must be stored
	 * in array in the same order they are displayed on screen.
	 */
	private boolean findKey(MotionEvent me) {
		double y = me.getRawY();
		int[] loc = new int[2];
		for (int i = 0; i < mViews.length; i++) {
			mViews[i].getLocationOnScreen(loc);
			// If the motion event goes over the button, have the button request focus
			if (y <= (loc[1] + mViews[i].getHeight()) ) {
				mViews[i].requestFocus();
				if (i != lastLoc) {
					longPressStartTm = me.getEventTime();
					lastLoc = i;
				}
				return true;
			}
		}
		return false;
	}

	@Override
	public void onClick(View v) {
		if (t.isAlive())
			t.interrupt();
		toast.cancel();
		Log.v(TAG, "Exit store selected");
		tts.setOnUtteranceCompletedListener(self);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "exit");
		// make sure speaking finishes before moving on
		tts.speak("returning to pet status and game menu", TextToSpeech.QUEUE_FLUSH, utterance);
	}	

	@Override
	public boolean onDoubleTap(MotionEvent e) {
		int[] loc = new int[2];
		mViews[4].getLocationOnScreen(loc);
		if (e.getRawY() < loc[1]) {
			confirm = true;
			goback = false;
			itemToFeed = item;
			tts.speak("Tap on screen or press center kee or trackball to " +
					"confirm you want to feed " + list.get(item).mName +
					" to " + PetData.getName(this),
					TextToSpeech.QUEUE_FLUSH, null);	
		}
		else {
			confirm = true;
			goback = true;
			tts.speak("Exit selected. Tap on screen or press center kee " +
					"to confirm", TextToSpeech.QUEUE_FLUSH, null);	
		}
		if (t.isAlive())
			t.interrupt();
		toast.cancel();
		toast = Toast.makeText(this, "Tap on screen to confirm", Toast.LENGTH_SHORT);
		toast.show();
		return true;
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
				mViews[4].performClick();
			}
			else if (item == itemToFeed) {
				boolean success = feedItem();
				if (success) {
					tts.speak("Pet fed!", TextToSpeech.QUEUE_FLUSH, null);
					if (t.isAlive())
						t.interrupt();
					toast.cancel();
					toast = Toast.makeText(this, "Pet fed!", Toast.LENGTH_SHORT);
					toast.show();
					mViews[4].performClick();
				}
				return true;
			}
		}
		else if (item >= 0 && item < list.size())
			showProduct(TextToSpeech.QUEUE_FLUSH);
		else if (longSpeech) { 
			tts.speak("Speech stopped", TextToSpeech.QUEUE_FLUSH, null);
			longSpeech = false;
		}
		return true;
	} 

	@Override
	public void onUtteranceCompleted(String utteranceId) {
		if (utteranceId.equalsIgnoreCase("exit")) {
			tts.setOnUtteranceCompletedListener(null);
			finish();
		}
		else if (utteranceId.equalsIgnoreCase("longSpeech")) {
			longSpeech = false;
		}	
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
				" you own " + ct, queueAction, null);
		
		// scrolling stops feeding or exit
		if (confirm) {
			confirm = false; 
			if (!goback) {
				tts.speak("Pet not fed", TextToSpeech.QUEUE_FLUSH, null);
				queueAction = TextToSpeech.QUEUE_ADD;
			}
		}
		
	}
	
	private boolean feedItem() {
		boolean result = false;
		if (item >= 0 && item < list.size()) {
			
			String name = list.get(item).mName;
			
			// be sure you purchased this item
			if (Purchases.purchased(this, name)) {
				feedTxn(name);
				showProduct(TextToSpeech.QUEUE_FLUSH);
				result = true;
			}
			else 
				tts.speak("Sorry you do not own any of this product", 
						TextToSpeech.QUEUE_FLUSH, null);
		}
		return result;
	}
	
	private synchronized void feedTxn(String name) {
		// remove item from purchases
		Purchases.discardItem(this, name);
		Animal mAnimal = Game.getAnimal();
		// use price as base for next feed time calculation
		mAnimal.feed(list.get(item).mPrice);  
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




