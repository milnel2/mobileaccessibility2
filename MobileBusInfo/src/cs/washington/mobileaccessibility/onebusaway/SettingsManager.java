package cs.washington.mobileaccessibility.onebusaway;

import cs.washington.mobileaccessibility.onebusaway.MainActivity.OutputMode;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/*
 * This class keeps track of all the customizable settings in my program,
 * as well as some that I never got around to implementing.
 * 
 * It is probably really bloated, and could be replaced by a simple hashmap
 * 
 * However, this file does contain a bunch of information about some conditions
 * which must exist between the different settings.
 * 
 * For example, you cannot make the screen be the only way to send information, and
 * also disable it.  Or, you cannot disable the vibrator while in Braille mode.
 * 
 * This class is highly connected to the SettingsState class
 * 
 * Apparently, this class uses the Singleton design pattern, because the constructor is
 * private and there is only one SettingsManager at a time, which can be accessed
 * by getManager().  BUT since getManager() itself is private... there really are no
 * public instances of this class possible.
 * 
 * So really, this is a frivolous singleton.
 */
public class SettingsManager {
	// whether we are outputing text using voice, morse code, braille, or nothing
	private MainActivity.OutputMode outputMode = OutputMode.SPEECH;
	// speed for the MorseVibrator
	private int morseSpeed = 100;
	// Whether to play some music (from a raw resource file)
	// while the program is busy
	private boolean playHoldingMusic = true;
	// Whether to play the ringtone while the program is busy
	private boolean playHoldingRingtone = false;
	// Whether to disable the vibrator, which is used for certain UI notifications
	// (only can be true in Silent or Speech mode, since the vibrator
	// is essential for those modes)
	private boolean killVibrator = false;
	// Whether to use the imaginary scroll bar on the right side of the screen for
	// scrolling
	private boolean useSidebar = true;
	// Whether to make the screen be visually blank
	// Can't be true in SILENT mode
	private boolean blankScreen = false; // cannot be true in SILENT mode
	// The size of the recent-stops history
	// not yet implemented, however
	private int nHistory = 1;
	// Whether to magnify the text on the screen
	// not yet implemented, however!
	// cannot be true if blankScreen is true
	private boolean enlargeText = true;
	// Whether to use accelerated scrolling, so that they can scroll by
	// using gestures, without having to complete the gesture
	// It's kind of confusing, but it's on by default too
	private boolean accelerateScrolling = true;
	// How fast TTS talks
	private int speechSpeed = 256;

	// Load the settings from a SharedPreferences object
	private void instanceLoadSettings(SharedPreferences prefs) {
		String sOutMode = prefs.getString("output_mode", "SPEECH");
		if(sOutMode.equals("V_BRAILLE"))
			outputMode = OutputMode.V_BRAILLE;
		else if(sOutMode.equals("MORSE"))
			outputMode = OutputMode.MORSE;
		else if(sOutMode.equals("SILENT"))
			outputMode = OutputMode.SILENT;
		else
			outputMode = OutputMode.SPEECH;
		morseSpeed = prefs.getInt("morse_speed", morseSpeed);
		playHoldingMusic = prefs.getBoolean("play_holding_music", playHoldingMusic);
		playHoldingRingtone = prefs.getBoolean("play_holding_ringtone", playHoldingRingtone);
		killVibrator = prefs.getBoolean("kill_vibrator", killVibrator);
		useSidebar = prefs.getBoolean("use_sidebar", useSidebar);
		blankScreen = prefs.getBoolean("blank_screen", blankScreen);
		nHistory = prefs.getInt("history_size", nHistory);
		enlargeText = prefs.getBoolean("enlarge_text", enlargeText);
		accelerateScrolling = prefs.getBoolean("accelerate_scrolling", accelerateScrolling);
		speechSpeed = prefs.getInt("speech_speed", speechSpeed);
	}
	
	// Save the settings to a SharedPreferences.Editor
	private void instanceSaveSettings(Editor ed) {
		switch(outputMode) {
		case V_BRAILLE:
			ed.putString("output_mode","V_BRAILLE");
			break;
		case MORSE:
			ed.putString("output_mode","MORSE");
			break;
		case SILENT:
			ed.putString("output_mode","SILENT");
			break;
		default:
			ed.putString("output_mode","SPEECH");
		}
		ed.putInt("morse_speed", morseSpeed);
		ed.putBoolean("play_holding_music", playHoldingMusic);
		ed.putBoolean("play_holding_ringtone", playHoldingRingtone);
		ed.putBoolean("kill_vibrator", killVibrator);
		ed.putBoolean("use_sidebar", useSidebar);
		ed.putBoolean("blank_screen", blankScreen);
		ed.putInt("history_size", nHistory);
		ed.putBoolean("enlarge_text", enlargeText);
		ed.putBoolean("accelerate_scrolling", accelerateScrolling);
		ed.putInt("speech_speed", speechSpeed);
	}
	
	private SettingsManager() { 
	}
	
	// Here it is, the single instance
	private static SettingsManager theManager;
	
	// here's where we get the single instance
	private static SettingsManager getManager() {
		if(theManager == null)
			theManager = new SettingsManager();
		return theManager;
	}
	
	public static void loadSettings(SharedPreferences prefs) {
		getManager().instanceLoadSettings(prefs);
	}
	
	public static void saveSettings(Editor ed) {
		getManager().instanceSaveSettings(ed);
	}
	
	/*
	 * Now, we have a bunch of accessors that are very straightforward
	 */
	
	public static OutputMode getOutputMode() {
		return getManager().outputMode;
	}
	
	public static int getMorseSpeed() {
		return getManager().morseSpeed;
	}
	
	public static boolean getPlayHoldingMusic() {
		return getManager().playHoldingMusic;
	}
	
	public static boolean getPlayHoldingRingtone() {
		return getManager().playHoldingRingtone;
	}
	
	public static boolean getKillVibrator() {
		SettingsManager sm = getManager();
		if(sm.outputMode == OutputMode.SILENT || sm.outputMode == OutputMode.SPEECH)
			return sm.killVibrator;
		else
			return false; // override it!
	}
	
	public static boolean getUseSidebar() {
		return getManager().useSidebar;
	}
	
	public static boolean getBlankScreen() {
		SettingsManager sm = getManager();
		if(sm.outputMode == OutputMode.SILENT)
			return false;
		return sm.blankScreen;
	}
	
	public static int getHistorySize() {
		return getManager().nHistory;
	}
	
	public static boolean getEnlargeText() {
		if(getBlankScreen())
			return false;
		return getManager().enlargeText;
	}
	
	public static boolean getAccelerateScrolling() {
		return getManager().accelerateScrolling;
	}
	
	public static int getSpeechSpeed() {
		return getManager().speechSpeed;
	}
	
	/*
	 * Now, some setters
	 */
	
	// TODO consider moving the following methods into a different module
	public static void setOutputMode(OutputMode x) {
		getManager().outputMode = x;
	}
	
	public static void setMorseSpeed(int x) {
		getManager().morseSpeed = x;
	}
	
	public static void setPlayHoldingMusic(boolean x) {
		getManager().playHoldingMusic = x;
	}
	
	public static void setPlayHoldingRingtone(boolean x) {
		getManager().playHoldingRingtone = x;
	}

	public static void setKillVibrator(boolean x) {
		getManager().killVibrator = x;
	}
	
	public static void setUseSidebar(boolean x) {
		getManager().useSidebar = x;
	}
	
	public static void setBlankScreen(boolean x) {
		getManager().blankScreen = x;
	}
	
	public static void setHistorySize(int x) {
		getManager().nHistory = x;
	}
	
	public static void setEnlargeText(boolean x) {
		getManager().enlargeText = x;
	}
	
	public static void setAccelerateScrolling(boolean x) {
		getManager().accelerateScrolling = x;
	}
	
	public static void setSpeechSpeed(int x) {
		getManager().speechSpeed = x;
	}
	
	public static void clearSettings() {
		theManager = new SettingsManager();
	}
	
	

	
	private static OutputMode intToOutputMode(int x) {
		switch(x) {
		case 1:
			return OutputMode.MORSE;
		case 2:
			return OutputMode.V_BRAILLE;
		case 3:
			return OutputMode.SILENT;
		default:
			return OutputMode.SPEECH;	
		}
	}
	
	private static int outputModeToInt(OutputMode om) {
		switch(om) {
		case SPEECH:
			return 0;
		case MORSE:
			return 1;
		case V_BRAILLE:
			return 2;
		case SILENT:
		default: // YOU LIE!
			return 3;
		}
	}
	
	
	private static String outputModeToString(OutputMode om) {
		switch(om) {
		case SPEECH:
			return "speech";
		case MORSE:
			return "morse code";
		case V_BRAILLE:
			return "vibrator braille";
		case SILENT:
		default:
			return "screen only";
		}
	}
	
	/*
	 * This interface represents a generic Setting, from
	 * one of the ones above.
	 * 
	 * It is for the benefit of the SettingsState class
	 */
	public static interface Setting {
		// what it's called
		public String getName();
		// whether we can change it,
		// or it is locked in place
		public boolean enabled();
		// get the next or previous value of the possible ones,
		// or "" for invalid (because we ran off the end of the list)
		public String getNextPrev(int next);
		// actually change the parameter accordingly
		public void doNextPrev(int delta);
		// TODO it would make sense to do saving and loading from inside here
		// ^ what does that mean?
	}
	
	/*
	 * Now, we define every single setting
	 */
	
	private Setting settingOutputMode = new Setting() {
		public String getName() {
			return "output mode";
		}
		public boolean enabled() {
			return true;
		}
		public String getNextPrev(int next) {
			int x = outputModeToInt(outputMode);
			x = (x + next + 4)%4;
			return outputModeToString(intToOutputMode(x));
		}
		public void doNextPrev(int next) {
			int x = outputModeToInt(outputMode);
			x = (x + next + 4)%4;
			outputMode = intToOutputMode(x);
		}
	};
	
	private Setting settingMorseSpeed = new Setting() {
		public String getName() {
			return "Morse Code speed";
		}
		public boolean enabled() {
			return outputMode == OutputMode.MORSE;
			// now interestingly enough, as they change the settings,
			// the program will try and say "set blah blah blah," and since
			// this starts with an 's', the first pattern they here will be dot dot dot,
			// the same as if they were adjusting things using the volume keys
		}
		public String getNextPrev(int next) {
			int x = morseSpeed + next*10;
			if(x > 5)
				return Integer.toString(x);
			else
				return "";
		}
		public void doNextPrev(int next) {
			int altMorseSpeed = morseSpeed + next*10;
			if(altMorseSpeed > 5)
				morseSpeed = altMorseSpeed;
		}
	};
	
	private Setting settingHoldMusic = new Setting() {
		public String getName() {
			return "Hold Music";
		}
		public boolean enabled() {
			return true;
		}
		private int getMode() {
			if(playHoldingMusic) {
				if(playHoldingRingtone)
					return 3;
				else
					return 1;
			}
			else {
				if(playHoldingRingtone)
					return 2;
				else
					return 0;
			}
		}
		private void setMode(int x) {
			playHoldingMusic = (x%2 != 0);
			playHoldingRingtone = ((x&2) == 2);
		}
		public String getNextPrev(int next) {
			int mode = getMode();
			mode = (mode + 4 + next)%4;
			switch(mode) {
			case 0:
				return "No hold music";
			case 1:
				return "Play song";
			case 2:
				return "Play default ringtone";
			case 3:
				return "Play song and ringtone";
			default:
				return "";
			}
		}
		public void doNextPrev(int next) {
			int mode = getMode();
			mode = (mode + 4 + next)%4;
			setMode(mode);
		}
	};
	
	private Setting settingKillVibrator = new Setting() {
		public String getName() {
			return "Vibrator on/off";
		}
		public boolean enabled() {
			return (outputMode == OutputMode.SPEECH || outputMode == OutputMode.SILENT);
		}
		public String getNextPrev(int next) {
			boolean kvb = killVibrator;
			kvb ^= (next != 0);
			if(kvb)
				return "Off";
			else
				return "On";
		}
		public void doNextPrev(int next) {
			killVibrator ^= (next != 0);
		}
	};
	private Setting settingUseSidebar = new Setting() {
		public String getName() {
			return "Sidebar on/off";
		}
		public boolean enabled() {
			return true;
		}
		public String getNextPrev(int next) {
			boolean usb = useSidebar;
			usb ^= (next != 0);
			if(usb)
				return "On";
			else
				return "Off";
		}
		public void doNextPrev(int next) {
			useSidebar ^= (next != 0);
		}
	};
	private Setting settingBlankScreen = new Setting() {
		public String getName() {
			return "Display text on screen";
		}
		public boolean enabled() {
			return outputMode != OutputMode.SILENT;
		}
		public String getNextPrev(int next) {
			boolean bs = blankScreen;
			bs ^= (next != 0);
			if(bs)
				return "Off - Blank screen";
			else
				return "On - Display text on screen";
		}
		public void doNextPrev(int next) {
			blankScreen ^= (next != 0);
		}
	};
	private Setting settingHistorySize = new Setting() {
		public String getName() {
			return "Size of recent stops history";
		}
		public boolean enabled() {
			// return true;
			return false; // I don't have time to do this before the 372 gets here.
		}
		public String getNextPrev(int next) {
			int x = nHistory + next;
			if(x > 0)
				return Integer.toString(x);
			else if(x == 0)
				return "No history";
			else
				return "";
		}
		public void doNextPrev(int next) {
			int x = nHistory + next;
			if(x >= 0)
				nHistory = x;
		}
	};
	private Setting settingEnlargeText = new Setting() {
		public String getName() {
			return "Enlarge text";
		}
		public boolean enabled() {
			// return (!blankScreen);
			return false; // UNTIL WE ACTUALLY USED IT!
		}
		public String getNextPrev(int next) {
			boolean enl = enlargeText;
			enl ^= (next != 0);
			if(enl)
				return "On";
			else
				return "Off";
		}
		public void doNextPrev(int next) {
			enlargeText ^= (next != 0);
		}
	};
	private Setting settingAccelerateScrolling = new Setting() {
		public String getName() {
			return "Accelerate scrolling";
		}
		public boolean enabled() {
			return true;
		}
		public String getNextPrev(int next) {
			boolean acc = accelerateScrolling;
			acc ^= (next != 0);
			if(acc)
				return "On";
			else
				return "Off";
		}
		public void doNextPrev(int next) {
			accelerateScrolling ^= (next != 0);
		}
	};
	private Setting settingSpeechRate = new Setting() {
		public String getName() {
			return "Speech rate";
		}
		public boolean enabled() {
			return true; // or, return outputMode == OutputMode.SPEECH?
		}
		public String getNextPrev(int next) {
			int x = speechSpeed + next*32;
			if(x > 32)
				return Integer.toString(x);
			else
				return "";
		}
		public void doNextPrev(int next) {
			int x = speechSpeed + next*32;
			if(x > 32)
				speechSpeed = x;
		}
	};
	
	private Setting[] allSettings = new Setting[]
	              {settingOutputMode, settingMorseSpeed, settingHoldMusic, settingKillVibrator,
			settingUseSidebar, settingBlankScreen, settingHistorySize, settingEnlargeText,
			settingAccelerateScrolling, settingSpeechRate
	              };
	

	/*
	 * returns an array with all the Setting objects.
	 * This is used by SettingsState.java
	 */
	public static Setting[] getSettings() {
		return getManager().allSettings;
	}
	
	// TODO: add settings for text color, alarm threshold time, as well as many other things
	
}
