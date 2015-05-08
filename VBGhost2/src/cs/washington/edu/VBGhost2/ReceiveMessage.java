package cs.washington.edu.VBGhost2;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;


public class ReceiveMessage extends AccessibleMenu{
	private static final int ITEM_ALERT_ID = 0;
	private static final int ITEM_RESUME_GAME_ID = 1;
	private static final int ITEM_CONTINUE_ID = 2;
	
	public final static String GAME_ID = "word";
	public String message = "";
	private static GameDB _games;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		Bundle extras = getIntent().getExtras();
		message = extras.getString("message");
		Log.v(TAG, "receivedMessage: "+ message);
		//setContentView(R.layout.continuegame);
		//check if message is valid (game in DB and their turn) 
		//otherwise finish()?
		//parse message
		String [] tokens = message.split(",");
		if(tokens.length != 2){
			Log.e(TAG, "Invalid Message Length "+ message);
			shutDown();
		}
		Character ch = tokens[1].charAt(0);
		String letter = "";
		if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')){
			letter = ch.toString();
			Log.v(TAG, "letter for new game: "+ letter);
		}else{
			Log.e(TAG, "Invalid Message letter "+ message);
			shutDown();
		}
		int id = -1;
		try{
			id = Integer.parseInt(tokens[0]);
		}catch(NumberFormatException e){
			Log.e(TAG, "Invalid Message ID number "+ message);
			shutDown();
		}
		if(id != -1){
			_games = new GameDB(this);
			_games.open();
			Boolean valid = _games.checkIfValidGame(id);
			String word = _games.getGameWordFragment(id);
			if(valid){
				_games.updateGame(id, word + letter, true);
			}
			_games.close();
			if(!valid){
				Log.e(TAG, "Invalid Message Invalid Game "+ message);
				shutDown();
			}
		}
	}
	
	private void shutDown(){
		if(!GlobalState.currentlyPlaying){
			finish();
		}else{
			GlobalState gs = (GlobalState)getApplication();
			gs.killTTS();
			finish();
		}
	}
	
	 @Override
	    protected void makeSelection(){
			Log.d(TAG, "in make selection, lastLoc: "+ lastLoc);		
			int itemId = lastLoc;
			switch(itemId) {
			case ITEM_ALERT_ID:
				break;
			case ITEM_RESUME_GAME_ID :
				Log.d(TAG, "resume game");
				//startMultiPlayerGame();
				//TODO: check if already playing VBGhost otherwise go through loading act
				if(!GlobalState.currentlyPlaying){
					Intent intent = new Intent(this, LoadingAct.class);
					intent.putExtra(GAME_ID, message);
					startActivity(intent);
					finish();
				}else{
					Log.v(TAG, "start new game within VBRAILLE");
				}
				break;
			case ITEM_CONTINUE_ID:
				Log.d(TAG, "continue current activity ");
				finish();
				break;
			}
	    }
}