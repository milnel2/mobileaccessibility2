package cs.washington.edu.VBGhost2;

import java.util.HashMap;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import cs.washington.edu.VBGhost2.R;
import cs.washington.edu.VBGhost2.DictionaryDB.OnDbTaskComplete;


public class LoadingAct extends Activity implements TextToSpeech.OnInitListener {
	private static TextToSpeech _tts;
	private static DictionaryDB _dictionaryDB;
	private static GameDB _gameDB;
	private String TAG;
    private HashMap<String, String> utterance = new HashMap<String, String>();
    private boolean loaded = false;
    private ProgressDialog dialog;
    public String message;
	public final static String GAME_ID = "word";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		if(extras != null){
			message = extras.getString(GAME_ID);
			Log.v(TAG, "loading act: "+ message);
		}else{
			message = null;
		}
		
	    TAG = this.getClass().getName();
	    TAG = TAG.substring(TAG.lastIndexOf('.') + 1);
		
	    _tts = GlobalState.getTTS();    
	    if (_tts == null) {
	    	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
	        _tts = gs.createTTS(this, this);
	    }
	    
	    loaded = false;
	    
	    //create dictionary database & fill with words
	    getDictionary(R.raw.kto8wordlist);
	    
	    
		LinearLayout mLinearLayout;
		// Create a LinearLayout in which to add the ImageView
		mLinearLayout = new LinearLayout(this);

		// Instantiate an ImageView and define its properties
		ImageView i = new ImageView(this);
		i.setImageResource(R.drawable.ghost);
		i.setAdjustViewBounds(true); // set the ImageView bounds to match the Drawable's dimensions
		i.setLayoutParams(new Gallery.LayoutParams(LayoutParams.WRAP_CONTENT,
		LayoutParams.WRAP_CONTENT));
		mLinearLayout.setGravity(Gravity.CENTER);

		// Add the ImageView to the layout and set the layout as the content view
		mLinearLayout.addView(i);
		setContentView(mLinearLayout);
	}

    private void speakOnLoadingDictionary()
    {
    	Log.v(TAG, "onSpeakLoadingDictionary()");
		_tts.speak("Please wait as word database is downloading.", TextToSpeech.QUEUE_ADD, null);	
    }
    
    private void speakOnLoadingScreen()
    {
    	Log.v(TAG, "onSpeakLoadingScreen()");
		_tts.speak("On loading screen, press back kee to exit.", TextToSpeech.QUEUE_ADD, null);	
    }
    

	private void getDictionary(int textFile){
		_dictionaryDB = GlobalState.getDictionaryDB();
		if(_dictionaryDB != null){
			try {
				_dictionaryDB.fillTable(textFile, _onTaskComplete);
			} catch (SQLException e) {
				Log.e(TAG, "SQLException when opening word database: " + e.getMessage());
				e.printStackTrace();
			}
		}else{
			try {
				GlobalState gs = (GlobalState)getApplication();
				gs.createDictionary(textFile, _onTaskComplete);
			} catch (SQLException e) {
				Log.e(TAG, "SQLException when opening word database: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}
    
	public static void cleanup() {
		if(_tts != null){
			_tts.stop();
			_tts.shutdown();
		}
		if(_dictionaryDB != null){
			_dictionaryDB.close();
		}
		GlobalState.currentlyPlaying = false;
	}
	
	// If the TTS instance got wiped out, restart it.
	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		
		//create TextToSpeech    
	    _tts = GlobalState.getTTS();    
	    if (_tts == null) {
	    	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
	        _tts = gs.createTTS(this, this);
	    }
	    
	}
	
	private OnDbTaskComplete _onTaskComplete = new OnDbTaskComplete() {
		@Override
		public void onComplete(int taskCode, Bundle result) {
			switch(taskCode) {
			case DictionaryDB.TASK_FILL_DB:
				loaded = true;
				if(dialog != null){
					dialog.dismiss();
				}
				Log.d(TAG, "task fill db complete");	
				startMainMenu();
				break;	
			}
		}
	};
	
	@Override
	public void onPause() {
		super.onPause();
		Log.v(TAG, "onPause");
		if (isFinishing()) onFinishing();
	}
	

	public void onFinishing()
	{
		GlobalState gs = (GlobalState)getApplication();
		gs.killTTS();
		gs.killDB();
		_tts= null;
		_dictionaryDB = null;
		GlobalState.currentlyPlaying = false;
	}
	
	private void startMainMenu(){
		GlobalState.currentlyPlaying = true;
		Intent intent = new Intent(this, MainMenu.class);
		startActivity(intent);
		//finish();
	}
	
	private void continueGame(){
		GlobalState.currentlyPlaying = true;
		Intent intent = new Intent(this, MultiPlayerMenu.class);
		//TODO: Replace with real id
		intent.putExtra("id", -1);
		startActivity(intent);
	}
	

	@Override
	public void onInit(int status) {
		if(!loaded){
			dialog = ProgressDialog.show(this, "", 
                "Loading. Please wait...", true);
			speakOnLoadingDictionary();
		}else{
			speakOnLoadingScreen();
		}
	}

	
}
