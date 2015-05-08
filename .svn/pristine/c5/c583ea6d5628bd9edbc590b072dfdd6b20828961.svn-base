package cs.washington.edu.VBGhost2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import com.google.android.gcm.GCMRegistrar;

public class MainMenu extends AccessibleMenu {
	private String TAG = "MainMenu";
	
	private static final int ITEM_START_NEW_GAME_ID = 0;
	private static final int ITEM_CONTINUE_OLD_GAME_ID = 1;
	private static final int ITEM_INSTRUCTIONS_ID = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GCMRegistrar.checkManifest(this);
        registerDevice();
        if (Utils.notificationReceived) {
        	onNotification();
        }
        
    }
    
    public void onNotification(){
    	Utils.notificationReceived=false;
    	//PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
    	//WakeLock wl = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
    	//wl.acquire();

    	AlertDialog.Builder mAlert=new AlertDialog.Builder(this);
       	mAlert.setCancelable(true);

    	mAlert.setTitle(Utils.notiMessage);
    	//mAlert.setMessage(Utils.notiMessage);
    	CharSequence[] items = {"Resume Game with Player 2", "Continue Current Game"};
    	mAlert.setItems(items, new DialogInterface.OnClickListener(){
    		public void onClick(DialogInterface dialog, int which) {
    			dialog.dismiss();
    		}
    	});

    	AlertDialog alert = mAlert.create();
    	alert.show();
    }
    
    public void registerDevice(){
    	GCMRegistrar.checkDevice(this);
    	GCMRegistrar.checkManifest(this);
    	final String regId = GCMRegistrar.getRegistrationId(this);
  	  //  Log.v(TAG, "after getRegistrationID");
    	if (regId.equals("")) {
    	  GCMRegistrar.register(this, Utils.SENDER_ID);
    	  //regId = GCMRegistrar.getRegistrationId(this);
    	    Log.v(TAG, "after register: " + regId);
    	} else {
    	  Log.v(TAG, "Already registered: "+ regId);
    	}
    }
    
    @Override
    protected void makeSelection(){
		Log.d(TAG, "in make selection, lastLoc: "+ lastLoc);		
		int itemId = lastLoc;
		switch(itemId) {
		case ITEM_START_NEW_GAME_ID:
			Log.d(TAG, "start new game ");
			newGameScreen();
			break;
		case ITEM_CONTINUE_OLD_GAME_ID:
			Log.d(TAG, "continue old game ");
			GameDB games = new GameDB(this);
			games.open();
			games.printGameDB();
			Cursor cursor = games.getGames();
			int count = cursor.getCount();
			startManagingCursor(cursor);
			games.close();
			if(count >= 1){
				continueGame();
			}else{
				noSavedGameMessage();
			}
			break;
		case ITEM_INSTRUCTIONS_ID:
			Log.d(TAG, "speak instructions ");
			speakInstructions();
			break;
		}
    }
    
    private void continueGame(){
    	Intent intent = new Intent(this, ContinueGameMenu.class);
    	startActivity(intent);
    }
    
    private void noSavedGameMessage(){
    	tts.speak("You have no saved games.", TextToSpeech.QUEUE_ADD, null);
    }
    
	private void speakInstructions() {
		Intent intent = new Intent(this, InstructionsReader.class);
		startActivity(intent);
	}
	
	private void newGameScreen() {
		Intent intent = new Intent(this, NewGameMenu.class);
		startActivity(intent);
	}
	
	
	@Override
	public void onPause() {
		super.onPause();
		//Log.v(TAG, "onPause");
		//if (isFinishing()) onFinishing();
	}
	

	public void onFinishing()
	{
		GlobalState gs = (GlobalState)getApplication();
		gs.killTTS();
		gs.killDB();
	}
    
}
