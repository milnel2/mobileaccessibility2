package cs.washington.mobileaccessibility.onebusaway;

import java.util.ArrayList;
//import java.util.HashSet;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.marvin.shell.TouchGestureControlOverlay;
import com.google.marvin.shell.TouchGestureControlOverlay.Gesture;
import com.google.marvin.shell.TouchGestureControlOverlay.GestureListener;
import com.google.tts.TTS;

import cs.washington.mobileaccessibility.morse.MorseVibrator;
import cs.washington.mobileaccessibility.onebusaway.uistates.CoState;
import cs.washington.mobileaccessibility.onebusaway.uistates.HomeState;
import cs.washington.mobileaccessibility.onebusaway.uistates.State;
import cs.washington.mobileaccessibility.onebusaway.util.Util;
import cs.washington.mobileaccessibility.vbraille.BrailleView;
import firbi.base.com.Bus;
import firbi.base.com.BusStop;

import android.os.Vibrator;

/**
 * This is the sole activity of the application. Originally, I probably intended to
 * have other ones, hence the name MainActivity.
 * 
 * This class is also the monster object anti-pattern
 * 
 * @author Will
 *
 */
public class MainActivity extends Activity implements GestureListener, CoState {

	// a tag string for Log'ing
	public static final String LOG_TAG = "MobileBusInfo";

	// the four ways of expressing output
	// SPEECH is using TTS
	// V_BRAILLE is using VBraille
	// MORSE is using morse code
	// SILENT is using nothing (but the screen)
	public enum OutputMode {
		SPEECH, V_BRAILLE, MORSE, SILENT
	}
	
	// One of the three views we use (the others are overlay and brailleView)
	// This one is used to display text on the screen
	private TextView textView;
	
	// This is the FrameLayout which has all the views in it
	// It is
	// needed so we can also use the overlay and brailleView, without having both
	// active at the same time (which would have been a more sensible design)
	// So basically, we need this variable around in order to switch between a
	// TouchGestureControlOverlay and a BrailleView
	private FrameLayout frameLayout;
	
	// From the eyes-free shell.  This is the thing that listens for gestures on the touchscreen
	private TouchGestureControlOverlay overlay;
	
	// The view used for VBraille
	private BrailleView brailleView;
	
	// at any given time, exactly one of overlay and brailleView should be in frameLayout
	// the vast majority of the time it is overlay
	// but textView is always in, and beneath the others,
	// so that the others can receive touch events

	// whether the BrailleView is in place in frameLayout
	// otherwise, overlay is there
	private boolean brailleViewInUse = false;

	
	// the vibrator
	private Vibrator vibe;
	
	// the MORSE vibrator!
	private MorseVibrator morseVibrator;
	
	// a buffer with the text that the BrailleView is displaying
	// currently, I only made BrailleView to display one character at a time
	// , so the entire string ends up being stored in this class
	private String brailleBuffer = "";
	// the index of the character in brailleBuffer that is being displayed
	private int brailleIndex = 0;
	
	// some celtic music to play while the program is busy
	private	MediaPlayer holdMusic;
	// the default ringtone, to play while the program is busy
	private Ringtone tone;
	

	// the vibration patterns we'll use
	private static final long [] menuBump = {0,1,40,41};
	private static final long [] stateChangeBump = {0,1,40,41};
	private static final long [] scrollBump = {0,1,40,41};
	private static final long [] alarm = {0,40,41,40,41,80,81,300,20,40,41,40,41,80,700};
	private static final long [] brailleAnnounce = {0}; // silence? // menuBump; //{0, 30, 90, 30, 90, 30, 90, 30, 150, 30};
	private static final long [] beginBraille = {0,120,30,90,30,60,30,30};
	private static final long [] endBraille = {0,30,30,60,30,90,30,120};
	private static final long [] brailleStopAndAnnounce = endBraille; //{0,30,30,60,30,90,30,120,100,30,90,30,90,30,80,30,150,30};
	private static final long [] forwardBraille = {0}; // silence?
	private static final long [] backBraille = {0,90,90,90};
	private static final long [] endBump = {0, 90, 45, 90};
	// TODO reconsider these patterns, after testing
	
	// The text-to-speech object
	private TTS tts;
	
	// this is part of an elaborate mechanism to ensure that the text doesn't speak over itself
	// this serves as a drop box to drop off things to speak.
	// a thread somewhere sits in a loop and looks at this drop box.  When it finds a new message,
	// it speaks it.
	// Maybe I included this because I thought that tts was taking too much time.
	// Or, maybe it had to do with synchronization and multithreading
	public static class SpeechDropbox {
		// the current String to speak
		private String currentMessage;
		// whether currentMessage was ever sent to TTS
		private boolean handled;
		public SpeechDropbox() {
			handled = true;
			currentMessage = null;
		}
		
		// set the text to speak
		// to be called from the main thread, or whoever
		// wants to output some text
		public synchronized void setText(String newMessage) {
			currentMessage = newMessage;
			handled = false;
			notifyAll();
		}
		
		// get the next String
		// to be called from the speaker thread
		public synchronized String getText() {
			while(handled == true) {
				try {
					wait();
				} catch(InterruptedException ie) {}
			}
			handled = true;
			notifyAll(); // probably unneccesary, we're the only ones who'll ever wait on this thread
			return currentMessage;
			
		}
	};
		
	private SpeechDropbox speechDropbox;
	

	// The object that encapsulates the GPS facilities
	private GeoFacade geoFacade;
	
	// This is basically a stack of states, where State is in the sense of a
	// finite state machine.  Basically, they are the nodes in the menu tree.
	// Since some nodes can be reached from multiple routes, we need to be able
	// to go back, which requires a stack.  This is actually the system that the
	// phone menu uses, since I found one place where you could add on arbitrarily
	// many levels, and you had to pop them off by pressing star many times (* is
	// the dial-in version's key for back)
	private ArrayList<State> states;
	
	// This should always be the top of the stack, i.e., the last element of states
	private State currentState;
	
	
	// the last Gesture that we were told about before the current one
	// (for use in "accelerated scrolling"...)
	private int oldGestureLevel;
	
	// The text that should go in textView when the screen isn't being used for
	// something else.  The proper way to do this is probably to use multiple TextView
	// or View objects, but I just used one and did this instead.
	//
	// This is what gets set by CoState.displayText()
	private String backgroundText;
	
	// True if backgroundText should be displayed in textView.  False if textView is
	// being used for some other purpose
	private boolean backgroundVisible;
	
	// The BookmarkManager
	private BookmarkManager bookmarks;
	
	// The most recent stop, which you access from the main menu by dialing 5
	// (i.e. touching the screen and letting go)
	private BusStop mostRecentStop;
	
	// a buffer that is used to handle the CoState.outputText() calls
	private String outputBuffer = "";
	
	// whether the menu key is pressed
	private boolean menuPressed = false;
	// whether the camera key is pressed
	private boolean cameraPressed = false;
	// (those two keys are especially annoying when they echo)
	
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // this causes the volume control keys to adjust the loudness
        // of the synthesized speech, instead of the loudness of the
        // ring tone
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        		WindowManager.LayoutParams.FLAG_FULLSCREEN);
		vibe = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
		holdMusic = MediaPlayer.create(this,R.raw.shetland_jumper);
        holdMusic.setLooping(true);
        tone = RingtoneManager.getRingtone(this, RingtoneManager.getActualDefaultRingtoneUri(this,RingtoneManager.TYPE_RINGTONE));
        
		// TODO it also might be reasonable to throw up a splash screen,
		// or even the loading screen, if it doesn't use tts
		speechDropbox = new SpeechDropbox();
		// build the view hierarchy
        frameLayout = new FrameLayout(this);
        textView = new TextView(this);
        // it would make sense to move these details into a resource xml file
        textView.setTextSize(40);
        textView.setTextColor(Color.YELLOW);
        overlay = new TouchGestureControlOverlay(this,this);
        brailleView = new BrailleView(this,vibe); // however we won't use it for a while
        frameLayout.addView(textView);
        frameLayout.addView(overlay);
        setContentView(frameLayout);
        Log.w(LOG_TAG,"Just finished the UI stuff");
        
        // let's hope that tts hasn't initialized already
        currentState = new HomeState();
        states = new ArrayList<State>();
        states.add(currentState);
        oldGestureLevel = TouchGestureControlOverlay.getGestureLevel(Gesture.CENTER); // i.e., 0
        backgroundText = "";
        backgroundVisible = true;
        


        morseVibrator = new MorseVibrator(vibe);
        geoFacade = new GeoFacade(this);

        final SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        Log.w(LOG_TAG,"Just got the GeoFacade and the shared preferences");
        final Runnable rSlow = new Runnable() {
        	public void run() {
        		Log.i(LOG_TAG, "Inside rSlow, starting to run.");
        		SettingsManager.loadSettings(prefs);
        		bookmarks = new BookmarkManager(prefs);
        		Log.i(LOG_TAG, "Inside rSlow, just set bookmarks to non-null");
        		String mostRecentID = prefs.getString("last_stop","");
        		if(!mostRecentID.equals("")) {
        			mostRecentStop = BusStop.find(mostRecentID);
        			// if it's null, then mostRecentStop should be null
        			// TODO: well, couldn't that find() call take forever?
        		}
        		else
        			mostRecentStop = null;
        	}
        };


        tts = new TTS(MainActivity.this, new TTS.InitListener(){
        	public void onInit(int version) {
        		tts.setSpeechRate(256);
        		Log.w(LOG_TAG,"About to tell the first state to initialize itself");
        		Log.w(LOG_TAG,"Unless paused is true (it's actually " + paused + ")");
        		if(!paused) {
        			currentState.onResume(MainActivity.this);
        			currentState.longDescribe(MainActivity.this); // ! - not sure how smart this is
        		}
        		// TODO fix that little issue where tts loads too fast
        		ttsLoaded = true;
        		finishOutput();
        		new Thread(new Runnable() {
        			public void run() {
        				int speed = 256;
        				while(true) {
        					Log.i(LOG_TAG,"About to inquire of the dropbox");
        					String s = speechDropbox.getText();
        					Log.i(LOG_TAG,"Just got string \"" + s + "\" from the dropbox");
        					s = Util.convert(s);
        					Log.i(LOG_TAG,"Inside the thread, about to speak \"" + s + "\"");
        					int newSpeed = SettingsManager.getSpeechSpeed();
        					if(newSpeed != speed) {
        						tts.setSpeechRate(newSpeed);
        						speed = newSpeed;
        						Log.i(LOG_TAG,"Inside thread, just set speed to \"" + speed + "\"");
        					}
        					tts.speak(s, 0, null);
        					Log.i(LOG_TAG,"Inside the thread, just spake \"" + s + "\"");
        				}
        			}
        		}).start();
        	}
        },true);
        new Thread(rSlow).start(); // try moving it down here to see what crashes.
    }
    
    boolean paused = true;
    boolean ttsLoaded = false;
    
    @Override
    public void onResume() {
    	Log.i(LOG_TAG,"ttsLoaded=" + ttsLoaded + ", paused=" + paused);
    	if(playingHoldMusic)
    		holdMusic.start();
    	if(ttsLoaded && paused) {
    		currentState.onResume(this);
    		currentState.longDescribe(this);
    		finishOutput();
    	}
    	paused = false;
    	geoFacade.onResume();
    	/*
    	locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 6000, 10, this);
    	// lol won't that be called twice then?? onResume is always called after
    	// onCreate()
    	// I think I based this off of what firbi does, or maybe what the Android
    	// documentation says
    	 */
    	super.onResume();
    	/*
    	// I almost feel like if more than one Activity needs the GPS, it would make sense
    	// to move this sort of thing into an Application.onCreate() method, since GPS
    	// doesn't seem to be related to UI details like Activity^s.
    	 */
    }
    
    @Override
    public void onPause() {
    	super.onPause();
    	tts.stop();
    	vibe.cancel();
    	geoFacade.onPause();
    	/*
    	locManager.removeUpdates(this);
    	*/
    	paused = true;
    	if(playingHoldMusic) {
    		holdMusic.pause();
    	}
    }
    
    @Override
    public void onStop() {
    	super.onStop();
    	// so, for whatever reason, at
    	// http://developer.android.com/intl/zh-CN/guide/topics/data/data-storage.html#pref
    	// (and I honestly have no idea what zh-CN is doing in the URL)
    	// they *load* the preferences in onCreate, but save them in onStop().
    	// Go figure
    	SharedPreferences prefs = getPreferences(MODE_PRIVATE);
    	if(prefs != null) {
    		// TODO: it turns out that prefs can be null!
    		SharedPreferences.Editor ed = prefs.edit();
    		if(ed != null) {
    			SettingsManager.saveSettings(ed);
    			if(bookmarks != null)
    				bookmarks.saveSettings(ed);
    			if(mostRecentStop != null)
    				ed.putString("last_stop", mostRecentStop.getId());
    			// ed.putInt("morse_speed", morseSpeed);
    			ed.commit();
    		}
    	}
    }
    
    @Override
    public void onDestroy() {
    	shuttingDown = true;
    	geoFacade.onDestroy();
    	// locManager.removeUpdates(this);
    	tts.shutdown();
    	super.onDestroy();
    }


    
    /**
     * This is what gets called when the GestureOverlay decides that the gesture's been
     * changed.  For the most part, we handle gesture by feeding completed ones into
     * currentState.  However, to make the UI nicer, we also have the screen display what
     * the currently selected menu item is, and reads it out loud too.
     * 
     * Also, a state that is displaying a list does something completely different.
     * We want the list to scroll as soon as the gesture is up or down.
     * (But the left and right gestures correspond to menu items, so they behave normally)
     */
	public void onGestureChange(Gesture g) {
		
		if(SettingsManager.getAccelerateScrolling() && currentState.accelerateScroll() &&
				(g == Gesture.CENTER || g == Gesture.UP ||
				 g == Gesture.DOWN   || g == Gesture.DOUBLEDOWN)) {
			// restore what was in the background, just in case
			backgroundVisible = true;
			setDisplayText(backgroundText);
			int gLevel = TouchGestureControlOverlay.getGestureLevel(g);
			// TODO solve the problem of bouncing off the end
			
			// tell the current node to scroll, if necessary
			if(gLevel > oldGestureLevel)
				// if you were super fast this would screw up slightly
				currentState.onGestureFinish(Gesture.DOWN, this);
			else if(gLevel < oldGestureLevel)
				currentState.onGestureFinish(Gesture.UP, this);
			oldGestureLevel = gLevel; // I think that this'll work
		} else {
			String menuSelection = currentState.tentativeGesture(g, this);
			if(!menuSelection.equals("")) {
				if(currentState.delayNumberAnnounce() && SettingsManager.getOutputMode() == OutputMode.SPEECH) {
					if(currentDelayTimer != null) {
						currentDelayTimer.cancel();
					}
					currentDelayTimer = new NumberAnnounceDelayTimer(menuSelection);
					new Thread(currentDelayTimer).start();
				} else {
					outputText(menuSelection);
				}
				backgroundVisible = false;
				setDisplayText(menuSelection);
				vibrate(menuBump);
			}
			else {
				// restore the old background
				backgroundVisible = true;
				setDisplayText(backgroundText);
			}
		}

		finishOutput();
	}
	
	// This little timer is used to keep extra numbers from being pronounced while
	// you are trying to enter a number in the EnterNumberState
	private NumberAnnounceDelayTimer currentDelayTimer = null;
	
	private class NumberAnnounceDelayTimer implements Runnable {
		// the name of the number to speak
		private String numberName;
		// don't say it b/c they confirmed their choice, or moved to a different tentative number
		private boolean canceled;
		public NumberAnnounceDelayTimer(String menuSelection) {
			this.numberName = menuSelection;
			canceled = false;
		}
		public void cancel() {
			canceled = true;
		}
		public void run() {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if(!canceled) {
				Runnable r = new Runnable() {
					public void run() {
						outputText(numberName);
						finishOutput();
					}
				};
				postRunnable(r); // won't this call finishOutput twice?
			}
		}
	}


	/**
	 * This handles a completed gesture.  For the most part, we just hand it
	 * straight over to the current node of the menu tree, and let it handle it.
	 */
	public void onGestureFinish(Gesture g) {	
		// restore the text that was visible behind the menu stuff
		backgroundVisible = true;
		setDisplayText(backgroundText);
		if(SettingsManager.getAccelerateScrolling() && currentState.accelerateScroll() &&
				(g == Gesture.CENTER || g == Gesture.UP ||
				 g == Gesture.DOWN   || g == Gesture.DOUBLEDOWN)) {
			// do nothing
		}
		else {
			if(!menuPressed) {
				tock();
				currentState.onGestureFinish(g, this);
			}
			// TODO what on earth to do with accelerated scrolling and V-Braille?
		}
		finishOutput();
		
	}
	
	public void onGestureStart(Gesture g) {
		oldGestureLevel = TouchGestureControlOverlay.getGestureLevel(Gesture.CENTER);
		onGestureChange(g);
	}
	
	// The gesture overlay needs to know whether to enable the gesture
	// that corresponds to dialing a 0.  (I added this functionality
	// to the TouchGestureControlOverlay class, and added a DOUBLEDOWN
	// gesture to the list...)
	public boolean wantsExtras() {
		return currentState.wantsZeroGesture();
	}
	
	public boolean wantsSidebar() {
		return SettingsManager.getUseSidebar() && currentState.wantsSidebar();
		// wait, that ought to slow it down!
	}
	
	public void onSidebar(double fraction) {
		Log.i(LOG_TAG,"Sidebar with fraction " + fraction);
		currentState.onSidebar(fraction, this);
		finishOutput();
	}
	
	// The following system for handling trackball events was mostly based off
	// of the system for running through the applications in the eyes-free
	// shell.
	
	
	private boolean trackballEnabled = true;
	
	/*
	 * You know, maybe always trapping the trackball isn't a good idea, since it's possible
	 * that if it isn't trapped, that would allow you to scroll the textview when it overflows
	 * the bounds of the screen.  Currently that can't be done.
	 */
	@Override
	public boolean onTrackballEvent(MotionEvent event) {
		int action = event.getAction();
		if(brailleViewInUse) {
			if(action == MotionEvent.ACTION_DOWN) {
				stopBrailleView();
				return true;
			}
			else
				return super.onTrackballEvent(event); // TODO see whether this is reasonable
		}
		if(action == MotionEvent.ACTION_DOWN) {
			currentState.onTrackballDown(this);
			finishOutput();
			return true;
		}
		if(trackballEnabled == false)
			return true;  // this system using threads and whatnot is from eyes-free shell
		if(action == MotionEvent.ACTION_MOVE) {
			float y = event.getY();
			if(y < -.16) {
				trackballEnabled = false;
				new Thread(new TrackballTimeout()).start();
				
				// TODO this is a bit of a hack, adding this in right now
				// (and also just below here), so maybe run through and
				// check that this whole method still makes sense
				if(currentState.wantsTrackballScrolling())
					currentState.onGestureFinish(Gesture.UP, this);
				finishOutput();
				return true;
			}
			if(y > .16) {
				trackballEnabled = false;
				new Thread(new TrackballTimeout()).start();
				if(currentState.wantsTrackballScrolling())
					currentState.onGestureFinish(Gesture.DOWN, this);
				finishOutput();
				return true;
			}
			// TODO: huh, if we fall through, weirdness could ensue...
			// you wonder why this doesn't make the TextView scroll...
			// perhaps there is a different layout which would automatically scroll?
		}
		return super.onTrackballEvent(event);
	}
	
	// Yep, this is definitely copied from
	// http://eyes-free.googlecode.com/svn/trunk/shell/src/com/google/marvin/shell/AppLauncherView.java
	class TrackballTimeout implements Runnable {
		public void run() {
			try {
				Thread.sleep(500);
				// That number could be changed to change how fast and responsive the trackball is
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			trackballEnabled = true;
		}
	}
	
	
	
	private static Gesture keyToGesture(int keyCode) {
		switch(keyCode) {
			case KeyEvent.KEYCODE_0:
				return Gesture.DOUBLEDOWN;
			case KeyEvent.KEYCODE_1:
				return Gesture.UPLEFT;
			case KeyEvent.KEYCODE_2:
				return Gesture.UP;
			case KeyEvent.KEYCODE_3:
				return Gesture.UPRIGHT;
			case KeyEvent.KEYCODE_4:
				return Gesture.LEFT;
			case KeyEvent.KEYCODE_5:
				return Gesture.CENTER;
			case KeyEvent.KEYCODE_6:
				return Gesture.RIGHT;
			case KeyEvent.KEYCODE_7:
				return Gesture.DOWNLEFT;
			case KeyEvent.KEYCODE_8:
				return Gesture.DOWN;
			case KeyEvent.KEYCODE_9:
				return Gesture.DOWNRIGHT;
			default:
				return Gesture.CENTER; // might as well.  null also makes sense
				
		}
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event) {
		Log.w(LOG_TAG,"onKeyUp called with keyCode=" + keyCode);
		if(keyCode == KeyEvent.KEYCODE_MENU)
			menuPressed = false;
		if(keyCode == KeyEvent.KEYCODE_FOCUS) {
			Log.w(LOG_TAG,"Camera up");
			cameraPressed = false;
		}
		return super.onKeyUp(keyCode, event);
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		Log.i(LOG_TAG,"onKeyDown called with keyCode = " + keyCode);
		if(keyCode == KeyEvent.KEYCODE_MENU) {
			if(menuPressed)
				return true; // aargh why does it repeat??
			// at least I already had the variable I needed to put an end to that nonsense
			menuPressed = true;
		}
		if(keyCode == KeyEvent.KEYCODE_FOCUS) {
			Log.w(LOG_TAG,"Camera down");
			if(cameraPressed)
				return true;
			Log.w(LOG_TAG,"...but for the first time, so we'll continue");
			cameraPressed = true;
		}

		OutputMode outputMode = SettingsManager.getOutputMode();
		switch(keyCode) {
		case KeyEvent.KEYCODE_K:
			// apparently the key K is a trapdoor which enables you to switch between
			// different modes.  I must have added this before making
			// SettingsManager
			OutputMode om = SettingsManager.getOutputMode();
			if(om == OutputMode.SPEECH) {
				om = OutputMode.MORSE;
			}
			else if(om == OutputMode.MORSE) {
				om = OutputMode.V_BRAILLE;
				vibe.cancel(); // else it'll keep on buzzing annoyingly
			}
			else if(om == OutputMode.V_BRAILLE) {
				// todo: get out of Braille mode!
				om = OutputMode.SILENT;
			}
			else
				om = OutputMode.SPEECH;
			SettingsManager.setOutputMode(om);
			currentState.longDescribe(this);
			finishOutput();
			return true;
		case KeyEvent.KEYCODE_0:
		case KeyEvent.KEYCODE_1:
		case KeyEvent.KEYCODE_2:
		case KeyEvent.KEYCODE_3:
		case KeyEvent.KEYCODE_4:
		case KeyEvent.KEYCODE_5:
		case KeyEvent.KEYCODE_6:
		case KeyEvent.KEYCODE_7:
		case KeyEvent.KEYCODE_8:
		case KeyEvent.KEYCODE_9:
			Log.i(LOG_TAG,"Well, we realize we have a gesture!");
			Gesture equivalent = keyToGesture(keyCode);
			if(currentState.wantsZeroGesture() && equivalent == Gesture.DOUBLEDOWN)
				equivalent = Gesture.DOWN; // hm, this is sketchy but sort of faithful

			// TODO find a way of binding the code below to be the same as what happens
			// in onGestureChange or onGestureFinish
			if(event.isShiftPressed() || event.isAltPressed()) {
				String menuSelection = currentState.tentativeGesture(equivalent, this);
				if(!menuSelection.equals("")) {
					outputText(menuSelection);
					vibrate(menuBump);
				}
			}
			else {
				currentState.onGestureFinish(equivalent, this);
			}
			finishOutput();
			return true;
			
		

		case KeyEvent.KEYCODE_SPACE:
			startOverState();
			finishOutput();
			return true;
		case KeyEvent.KEYCODE_VOLUME_DOWN:
			if(outputMode == OutputMode.MORSE) {
				int morseSpeed = SettingsManager.getMorseSpeed();
				if(morseSpeed > 15) {
					morseSpeed -= 10;
					morseVibrator.vibrate("s", morseSpeed);
				}
				else
					morseVibrator.vibrate("t",30);
				SettingsManager.setMorseSpeed(morseSpeed);
				return true;
			}
			else if(outputMode == OutputMode.V_BRAILLE) {
				if(brailleViewInUse) {
					brailleIndex--;
					if(brailleIndex < 0) {
						stopBrailleView();
					}
					else if(brailleIndex < brailleBuffer.length()) {
						String character = Character.toString(brailleBuffer.charAt(brailleIndex));
						brailleView.setCharacter(character);
						vibrate(backBraille);
					}
				}
				return true;
			}
			else
				break;
		case KeyEvent.KEYCODE_VOLUME_UP:
			if(outputMode == OutputMode.MORSE) {
				int morseSpeed = SettingsManager.getMorseSpeed();
				morseSpeed += 10;
				morseVibrator.vibrate("s", morseSpeed);
				SettingsManager.setMorseSpeed(morseSpeed);
				return true;
			}
			else if(outputMode == OutputMode.V_BRAILLE) {
				if(brailleViewInUse) {
					brailleIndex++;
					if(brailleIndex >= brailleBuffer.length()) {
						brailleIndex--;
						vibrate(endBump);
					}
					else if(brailleIndex >= 0) {
						String character = Character.toString(brailleBuffer.charAt(brailleIndex));
						brailleView.setCharacter(character);
						vibrate(forwardBraille);
					}
				} else {
					startBrailleView();
				}
				return true;
			}
			else
				break;
		case KeyEvent.KEYCODE_CAMERA:
			return true; // don't want to trigger the camera
		case KeyEvent.KEYCODE_FOCUS:
			if(brailleViewInUse) {
				stopBrailleView();
				return true;
			}
			currentState.shortDescribe(this);
			finishOutput();
			return true;
		case KeyEvent.KEYCODE_MENU:
			if(overlay.gestureInProgress())
				return true; // do nothing, they're experimenting
			// note that we DON'T call currentState.onResume(this);
			currentState.longDescribe(this);
			finishOutput();
			return true;
		}


		if(currentState.onKeyDown(keyCode, this)) {
			finishOutput();
			// TODO see if there's a way to do everything with a wrapper,
			// and how much of the calls in this function are really necessary
			return true;
		}
		if(keyCode == KeyEvent.KEYCODE_BACK) {
			backState(); // go to the previous menu
			finishOutput();
			return true;
		}
		if(keyCode == KeyEvent.KEYCODE_CALL) {
			finishOutput();
			return true; // so that the dialer or camera doesn't
			// come up by accident even when they were in a state
			// where the call button didn't make sense
		}
		finishOutput(); // probably best to do this before letting the system take over
		return super.onKeyDown(keyCode, event);
	}
	
	

	/*
	 * This is called by the state/node, when it wants to display some text
	 */
	public void displayText(String text) {
		backgroundText = text;
		if(backgroundVisible)
			setDisplayText(backgroundText);
	}

	// evidently this was a good idea that didn't pan out
	private void tock() {
		/*if(outputMode == OutputMode.SPEECH)
			outputBuffer += "[tock]";*/
		// else, what TODO?
	}
	
	/*
	 * This is called by the state/node, when it wants to have some text spoken
	 */
	public void outputText(String text) {
		outputBuffer += text;
		
		// I'm not sure that this makes any sense... we'll see
		if(currentDelayTimer != null) {
			currentDelayTimer.cancel();
			currentDelayTimer = null;
		}
	}
	
	// this is called after we interact with the State
	// whenever the State says that it wants to output some text, we put it into
	// this buffer
	// But at the end, the buffer has to be flushed by calling finishOutput()
	// This is done on the MainActivity side, since the State doesn't know
	// when it is finished handling a given interaction with the user
	private void finishOutput() {
		Log.i(LOG_TAG, "finishOutput() called with outputBuffer==\"" + outputBuffer + "\"");
		
		// TODO: add a timer or something, to make the cases where a separate
		// thread calls be handled reasonably, in the case that the loading
		// took an infinitesimal time
		// 
		// That is, if the loader took barely any time, we probably want to queue
		// it's messages with whatever came before.
		// 
		// Though actually I think we were going to have the loading screen play
		// some music anyways, so... perhaps this is unnecessary
		// 
		// This will require careful thought.
		if(outputBuffer.length() > 0) // don't forget this line either!
		{
			switch(SettingsManager.getOutputMode()) {
			case SPEECH:
				Log.i(LOG_TAG, "About to speak \"" + outputBuffer + "\"");
				speechDropbox.setText(outputBuffer);
				Log.i(LOG_TAG, "Just told speechDropbox to speak \"" + outputBuffer + "\"");
				break;
			case MORSE:
				morseVibrator.vibrate(outputBuffer, SettingsManager.getMorseSpeed());
				break;
			case V_BRAILLE:
				boolean wasUsingBraille = brailleViewInUse;
				if(brailleViewInUse) {
					stopBrailleView();
				}
				brailleBuffer = Util.brailleConvert(outputBuffer);
				Log.i(LOG_TAG,"Just set the braille buffer to " + brailleBuffer);
				vibrate(wasUsingBraille?brailleStopAndAnnounce:brailleAnnounce); // no queueing of vibration patterns
				break;
			default: // i.e., SILENT
				// do nothing
				break;
			}
		}
		outputBuffer = ""; // don't forget that!!
	}

	/*
	 * This is called by the state/node when it want the vibrator to go off
	 * in the pattern that signifies a scrolling action
	 */
	public void vibrateForScroll() {
		vibrate(scrollBump);
	}
	
	/*
	 * This is called by the state/node when it want the vibrator to go off
	 * in the alarm pattern
	 */
	public void vibrateForAlarm() {
		/*
		 * It's too bad that patterns can't include whether they should be repeated
		 * ...
		 */
		if(SettingsManager.getOutputMode() != OutputMode.MORSE && !SettingsManager.getKillVibrator()) {
			vibe.vibrate(alarm, shuttingDown?-1:0);
		}
		// vibrate(alarm); // but what TODO in Morse code?
	}
	
	
	// vibrate an aribtrary pattern
	private void vibrate(long [] pattern) {
		if(SettingsManager.getOutputMode() != OutputMode.MORSE && !SettingsManager.getKillVibrator()) {
			vibe.vibrate(pattern, -1);
		} // TODO see how reasonable this is
	}


	/*
	 * This is called by the current state/node of the menu, in order to launch
	 * a new menu node.
	 * 
	 */
	public void setState(State s) {
		Log.i(LOG_TAG,"Well, we supposedly are about to set the state!");
		vibrate(stateChangeBump); // TODO who knows what this will do
		// if somebody's playing music, we need to stop it
		// it doesn't matter if the vibrator gets called twice actually, so...
		stopPlaying();
		
		
		
		// TODO: fix this hack
		states.add(s);
		currentState = s;
		currentState.onResume(this);
		currentState.longDescribe(this);
		
	}


	/*
	 * This is called by a state/node when it wants to send the user
	 * to the previous menu
	 */
	public void backState() {
		// amazingly, removing this line will make alerts extra annoying
		// because they won't turn off their vibrator!
		vibrate(stateChangeBump);
		stopPlaying();
		if(states.size() == 1)
			finish();
		else {
			states.remove(states.size() - 1);
			currentState = states.get(states.size() - 1);
			currentState.onResume(this);
			currentState.longDescribe(this); // hopefully this works
		}
		
	}

	/*
	 * Called by state/node that wants to do things with the collection of
	 * bookmarked stops.
	 */
	public BookmarkManager getBookmarks() {
		return bookmarks;
	}

	/*
	 * Called by a state/node that would like to know what the most recent
	 * stop the user inquired about was.
	 */
	public BusStop mostRecentStop() {
		return mostRecentStop;
	}

	/*
	 * Called by StopState, to let the controller know that a stop was visited!
	 */
	public void setRecentStop(BusStop bs) {
		mostRecentStop = bs;
	}

	/*
	 * Called by a state/node that would like to send the user back to the beginning
	 * of everything.  I don't think anyone uses this, however, because it turned
	 * out to be better handled uniformly in the onKeyDown method of this class.
	 */
	public void startOverState() {
		stopPlaying();
		currentState = new HomeState();
        states = new ArrayList<State>();
        states.add(currentState);
        currentState.onResume(this);
        currentState.longDescribe(this);
	}

	/*
	 * Called by a node/menu state thing when it wants to know where we are
	 * RETURNS NULL if we can't figure it out
	 */
	public GeoPoint getLocation() {
		return geoFacade.getLocation();
	}
	

	private Handler handler = new Handler();
	
	public void postRunnable(final Runnable r) {
		Log.i(LOG_TAG,"Just posted a runnable");
		handler.post(new Runnable() {
			public void run() {
				r.run();
				finishOutput();
			}
		});
	}
	

	// whether the hold music is playing
	private boolean playingHoldMusic = false;
	// whether we are stalling.. I feel like this should
	// be closely related to playingHoldMusic...
	private boolean programOnHold = false;
	
	/*
	 * @see cs.washington.mobileaccessibility.onebusaway.uistates.CoState#startPlaying()
	 */
	public void startPlaying() {
		programOnHold = true;
		if(SettingsManager.getPlayHoldingMusic()) {
			if(!playingHoldMusic) {

				Log.i(LOG_TAG,"Start playing the hold music");
				holdMusic.start();

			}
			else
				Log.i(LOG_TAG,"Already playing the hold music EXCEPT NOT (maybe)");
			playingHoldMusic = true;
		}
		if(SettingsManager.getPlayHoldingRingtone()) {
			if(!tone.isPlaying()) {
				Log.i(LOG_TAG,"*ahem*, this is where you start playing");
				Log.i(LOG_TAG,tone.getTitle(this));
				tone.play();
			}
		}
			
	}

	/*
	 * (non-Javadoc)
	 * @see cs.washington.mobileaccessibility.onebusaway.uistates.CoState#stopPlaying()
	 */
	public void stopPlaying() {
		if(programOnHold) {
			vibrate(stateChangeBump); // TODO: a better way to handle this?
			programOnHold = false;
		}
		if(SettingsManager.getPlayHoldingMusic()) {
			if(!playingHoldMusic)
				Log.i(LOG_TAG,"Already not playing the hold music");
			else {
				Log.i(LOG_TAG,"Stop playing the hold music");
				holdMusic.pause();
			}
			playingHoldMusic = false;
		}
		if(SettingsManager.getPlayHoldingRingtone()) {
			if(tone.isPlaying())
				tone.stop();
		}

	}
	
	
	/*
	 * Turn on the brailleView!
	 */
	private void startBrailleView() {
		brailleViewInUse = true;
		frameLayout.removeView(overlay);
		frameLayout.addView(brailleView);
		brailleIndex = 0;
		String character = " ";
		if(brailleBuffer.length() > brailleIndex)
			character = Character.toString(brailleBuffer.charAt(brailleIndex));
		brailleView.setCharacter(character);
		vibrate(beginBraille);
		
	}
	
	/*
	 * Turn off the brailleView!
	 */
	private void stopBrailleView() {
		brailleViewInUse = false;
		frameLayout.removeView(brailleView);
		frameLayout.addView(overlay);
		brailleIndex = 0; // meh
		vibrate(endBraille);
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see cs.washington.mobileaccessibility.onebusaway.uistates.CoState#amIOnTop(cs.washington.mobileaccessibility.onebusaway.uistates.State)
	 */
	public boolean amIOnTop(State s) {
		return s == currentState;
	}

	// which bus is being monitored for the alarm!
	private Bus currentAlarmBus = null;
	// which Runnable is doing the monitoring!
	private Runnable currentAlarmRunnable = null;
	// true if the current runnable needs to actually not
	// sound its alarm, because the application is shutting down!
	private boolean shuttingDown = false;

	public void setCurrentAlarmBus(Bus b) {
		currentAlarmBus = b;
	}
	
	public Bus getCurrentAlarmBus() {
		return currentAlarmBus;
	}
	
	public void setCurrentAlarmRunnable(Runnable r) {
		currentAlarmRunnable = r;
	}
	
	public Runnable getCurrentAlarmRunnable() {
		return currentAlarmRunnable;
	}

    // this sequence seems to happen a lot
	private void setDisplayText(String text) {
		if(SettingsManager.getBlankScreen())
			textView.setText("");
		else
			textView.setText(text);
		
	}
    
    
}