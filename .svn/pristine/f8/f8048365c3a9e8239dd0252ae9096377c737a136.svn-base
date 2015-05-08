package cs.washington.edu.VBGhost2;

import java.util.Locale;

import cs.washington.edu.VBGhost2.R;
import cs.washington.edu.VBGhost2.DictionaryDB.OnDbTaskComplete;
import LogToFile.FileLog;
import android.app.Application;
import android.content.Context;
import android.database.SQLException;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;


public class GlobalState extends Application {
	private String TAG = "GLOBAL STATE";
	
	private static TextToSpeech sTts;
	private static Context sContext;
	private static OnInitListener sListener;
	
	private static DictionaryDB sDictionary;
	
	//logging information
	public static final String LOG_FILE = "Ghost.log";
	public static final String APP_ABBREV = "VBG";
	public static boolean LOG_USER_ACTIVITY = false;
    public static FileLog mLog;
    public static boolean mLogEnabled = false;
    public static boolean mJustCreated= true;
    public static String mSessionId = ""; 
    public static int mRoundNr = 0;
    public static boolean currentlyPlaying = false;
    
    public static final String[] alphabet =
    {"a","b","c","d","e","f","g","h","i","j","k","l","m","n","o",
    	"p","q","r","s","t","u","v","w","x","y","z"};
    public static final String[] nato =
    {"alpha","bravo","charlie","delta","echo","fox trot","golf","hotel","india","juliet","kee low",
    	"lima","mike","november","oscar","pahpah", "quebec","romeo","sierra","tango","uniform",
    	"victor","whiskey","ex-ray","yang kee","zoo loo"};
	
	protected void killTTS()
	{
		if (sTts != null) {
			sTts.stop();
			sTts.shutdown();
		}
		sTts = null;
	}
	
	protected static TextToSpeech getTTS()
	{
		return sTts;
	}
	
	protected TextToSpeech createTTS(Context context, OnInitListener listener) 
	{
		sContext = context;
		sListener = listener;
		killTTS();
		sTts = new TextToSpeech(context, new OnInitListener() {

			@Override
			public void onInit(int status) {
				if (status == TextToSpeech.SUCCESS) {
					sTts.setSpeechRate((float) 1.25);  // use phone setting instead, automatic in 2.1+
			        sTts.setLanguage(Locale.US);
					sTts.addEarcon("ding", sContext.getPackageName(), R.raw.ding);
					sTts.addEarcon("duobong", sContext.getPackageName(), R.raw.duobong);
					sTts.addEarcon("suboptions", sContext.getPackageName(), R.raw.duobong); 
					sTts.addEarcon("flingdown", sContext.getPackageName(), R.raw.flingdown);
					sTts.addEarcon("flingup", sContext.getPackageName(), R.raw.flingup);
				}
				if (sListener != null) sListener.onInit(status);
				sContext = null;
				sListener = null;
			}
			
		});
		return sTts;
	}
	
	protected static void configureTTS(Context context)
	{
		sTts.setSpeechRate((float) 1.25);  // use phone setting instead, automatic in 2.1+
        sTts.setLanguage(Locale.US);
		sTts.addEarcon("ding", context.getPackageName(), R.raw.ding);
		sTts.addEarcon("duobong", context.getPackageName(), R.raw.duobong);
		sTts.addEarcon("suboptions", context.getPackageName(), R.raw.duobong); 
		sTts.addEarcon("flingdown", context.getPackageName(), R.raw.flingdown);
		sTts.addEarcon("flingup", context.getPackageName(), R.raw.flingup);
	}
	
	protected static DictionaryDB getDictionaryDB(){	
		return sDictionary;
	}
	
	protected DictionaryDB createDictionary(int resId, 
			OnDbTaskComplete onTaskComplete){
	    //create dictionary database & fill with words
		sDictionary = new DictionaryDB(this);
		try {
			sDictionary.open();
		} catch (SQLException e) {
			Log.e("GLOBAL STATE", "SQLEception when opening word database: " + e.getMessage());
			e.printStackTrace();
		}
		sDictionary.fillTable(resId, onTaskComplete);
		return sDictionary;
	}
	
	protected void killDB()
	{
		Log.v(TAG, "database being killed");
		if (sDictionary != null) {
			sDictionary.close();
		}
		sDictionary = null;
	}
	
}
