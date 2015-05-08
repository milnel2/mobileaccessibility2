package cs.washington.edu.buddies;


import android.content.Context;
import android.content.SharedPreferences;


/* Data management class for Settings */
public class Settings  {
	
	// possible mode values
	public static final int ALPHABET = 0;
	public static final int NUMBERS = 1;
	public static final int PUNCTUATION = 2;
	
	// possible max tries values;
	public static final int ONE_MAX_TRY = 1;
	public static final int TWO_MAX_TRY = 2;
	public static final int THREE_MAX_TRY = 3;
	
	// speech rate values
	public static final int MIN_SPEECH_RATE = 0;
	public static final int NORMAL_SPEECH_RATE = 1;
	public static final int DOUBLE_SPEECH_RATE = 2;
	public static final int MAX_SPEECH_RATE = 3;
	
	// braille word level
	public static final int EASY = 0;
	public static final int INTERMEDIATE = 1;
	public static final int HARD = 2;
	
	
	// setting key names and default values
	private static final String OPT_TTS = "tts";
	private static final boolean OPT_TTS_DEF = true;
	private static final String OPT_REPLAY = "replay";
	private static final boolean OPT_REPLAY_DEF = true;
	private static final String OPT_KEYPAD = "keypad";
	private static final boolean OPT_KEYPAD_DEF = true;
	private static final String OPT_MODE = "mode";
	private static final int OPT_MODE_DEF = ALPHABET;
	private static final String OPT_TRIES = "tries";
	private static final int OPT_TRIES_DEF = TWO_MAX_TRY;
	private static final String OPT_SPEECH_RATE = "speechrate";
	private static final int OPT_SPEECH_RATE_DEF = NORMAL_SPEECH_RATE;
	private static final String OPT_LEVEL = "level";
	private static final int OPT_LEVEL_DEF = EASY;
	
	public static final String SETTINGS_FILENM = "BBSettings";

	public static boolean getTts(Context context) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		return settings.getBoolean(OPT_TTS, OPT_TTS_DEF);
	}

	public static boolean getReplay(Context context) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		return settings.getBoolean(OPT_REPLAY, OPT_REPLAY_DEF);
	}
	
	public static boolean getKeypad(Context context) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		return settings.getBoolean(OPT_KEYPAD, OPT_KEYPAD_DEF);
	}

	public static int getMode(Context context) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		return settings.getInt(OPT_MODE, OPT_MODE_DEF);
	}

	public static int getMaxTries(Context context) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		return settings.getInt(OPT_TRIES, OPT_TRIES_DEF);
	}
	
	public static int getSpeechRate(Context context) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		return settings.getInt(OPT_SPEECH_RATE, OPT_SPEECH_RATE_DEF);
	}
	
	public static int getLevel(Context context) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		return settings.getInt(OPT_LEVEL, OPT_LEVEL_DEF);
	}
	
	protected static boolean setMode(Context context, int mode) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		SharedPreferences.Editor ed = settings.edit();
		boolean result = false;
		switch (mode) {
		case ALPHABET:
		case NUMBERS:
		case PUNCTUATION:
			ed.putInt(OPT_MODE, mode);
			result = ed.commit();
		}
		return result;
	}
	
	protected static boolean setMaxTries(Context context, int tries) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		SharedPreferences.Editor ed = settings.edit();
		boolean result = false;
		switch (tries) {
		case ONE_MAX_TRY:
		case TWO_MAX_TRY:
		case THREE_MAX_TRY:
			ed.putInt(OPT_TRIES, tries);
			result = ed.commit();
		}
		return result;
	}
	
	protected static boolean setTts(Context context, boolean tts) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		SharedPreferences.Editor ed = settings.edit();
		boolean result = false;
		ed.putBoolean(OPT_TTS, tts);
		result = ed.commit();
		return result;
	}
	
	protected static boolean setReplay(Context context, boolean replay) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		SharedPreferences.Editor ed = settings.edit();
		boolean result = false;
		ed.putBoolean(OPT_REPLAY, replay);
		result = ed.commit();
		return result;
	}
	
	protected static boolean setKeypad(Context context, boolean keypad) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		SharedPreferences.Editor ed = settings.edit();
		boolean result = false;
		ed.putBoolean(OPT_KEYPAD, keypad);
		result = ed.commit();
		return result;
	}
	
	protected static boolean setSpeechRate(Context context, int rate) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		SharedPreferences.Editor ed = settings.edit();
		boolean result = false;
		switch (rate) {
		case MIN_SPEECH_RATE:
		case NORMAL_SPEECH_RATE:
		case DOUBLE_SPEECH_RATE:
		case MAX_SPEECH_RATE:
			ed.putInt(OPT_SPEECH_RATE, rate);
			result = ed.commit();
		}
		return result;
	}
	
	protected static boolean setLevel(Context context, int level) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		SharedPreferences.Editor ed = settings.edit();
		boolean result = false;
		switch (level) {
		case EASY:
		case INTERMEDIATE:
		case HARD:
			ed.putInt(OPT_LEVEL, level);
			result = ed.commit();
		}
		return result;
	}

}