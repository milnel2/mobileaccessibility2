package cs.washington.edu.vbreadwrite;


import android.content.Context;
import android.content.SharedPreferences;


/* Data management class for Settings */
public class Settings  {
	
	// speech rate values
	public static final int MIN_SPEECH_RATE = 0;
	public static final int NORMAL_SPEECH_RATE = 1;
	public static final int FAST_SPEECH_RATE = 2;
	public static final int MAX_SPEECH_RATE = 3;
	
	public static final float[] SPEECH_RATES = {0.75f, 1.0f, 1.25f, 1.75f};
	
	public static final int QWERTY_KEYPAD = 0;
	public static final int ALPHA_KEYPAD = 1;
	public static final int SIMPLE_KEYPAD = 2;
	public static final int SPEECH_INPUT = 3;
	public static final int TAP_HOLD_INPUT = 4;
	
	// for speaking input type setting
	public static final String[] INPUT_TYPE_TEXTS = {"QWERTY Keypad", "Alphabetic Keypad", "Simple Keypad", "Speech Input", "Tap-Hold Input"};
	public static final String[] INPUT_TYPE_TAGS = {"qwurty keepad", "alpha keepad", "simple keepad", "speech inn put", "tap hold inn put"};
	
	public static final int PARENT = 0;
	public static final int CHILD = 1;
	
	public static final boolean[] VERSION = {false, true};
	
	// setting key names and default values
	private static final String OPT_INPUT_TYPE = "inputtype";
	private static final int OPT_INPUT_TYPE_DEFAULT = SIMPLE_KEYPAD;
	private static final String OPT_SPEECH_RATE = "speechrate";
	private static final float OPT_SPEECH_RATE_DEFAULT = SPEECH_RATES[FAST_SPEECH_RATE];
	private static final String OPT_IS_LOGGING = "islogging";
	private static final boolean OPT_IS_LOGGING_DEFAULT = VERSION[PARENT];
	
	public static final String SETTINGS_FILENM = "VBReadWriteSettings";

	public static boolean getVersionSetting(Context context) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		return settings.getBoolean(OPT_IS_LOGGING, OPT_IS_LOGGING_DEFAULT);
	}

	public static float getSpeechRateSetting(Context context) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		return settings.getFloat(OPT_SPEECH_RATE, OPT_SPEECH_RATE_DEFAULT);
	}
	
	public static int getInputTypeSetting(Context context) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		return settings.getInt(OPT_INPUT_TYPE, OPT_INPUT_TYPE_DEFAULT);
	}
	
	public static String getVersionText(boolean isLogging) {
		return (!isLogging) ? "Parent" : "Child";
	}
	
	public static String getInputTypeText(int type) {
		return INPUT_TYPE_TEXTS[type];
	}
	
	public static String getInputTypeTag(int type) {
		return INPUT_TYPE_TAGS[type];
	}

	
	protected static boolean setVersion(Context context, int version) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		SharedPreferences.Editor ed = settings.edit();
		boolean result = false;
		boolean isLogging = OPT_IS_LOGGING_DEFAULT;
		switch (version) {
		case PARENT:	
		case CHILD:
			isLogging = VERSION[version];
			break;
		}
		ed.putBoolean(OPT_IS_LOGGING, isLogging);
		result = ed.commit();
		return result;
	}
	
	protected static boolean setSpeechRate(Context context, int rate) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		SharedPreferences.Editor ed = settings.edit();
		boolean result = false;
		float speechRate = OPT_SPEECH_RATE_DEFAULT;
		switch (rate) {
		case MIN_SPEECH_RATE:	
		case NORMAL_SPEECH_RATE:
		case FAST_SPEECH_RATE:
		case MAX_SPEECH_RATE:
			speechRate = SPEECH_RATES[rate];
			break;
		}
		ed.putFloat(OPT_SPEECH_RATE, speechRate);
		result = ed.commit();
		return result;
	}
	
	protected static boolean setInputType(Context context, int type) {
		SharedPreferences settings = context.getSharedPreferences(SETTINGS_FILENM, 0);
		SharedPreferences.Editor ed = settings.edit();
		boolean result = false;
		switch (type) {
		case QWERTY_KEYPAD:
		case ALPHA_KEYPAD:
		case SIMPLE_KEYPAD:
		case SPEECH_INPUT:
		case TAP_HOLD_INPUT:
			break;
		default:
			type = OPT_INPUT_TYPE_DEFAULT;
			break;
		}
		ed.putInt(OPT_INPUT_TYPE, type);
		result = ed.commit();
		return result;
	}

}