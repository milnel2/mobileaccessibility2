package cs.washington.edu.VBGhost2;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class NewGameMenu extends AccessibleMenu {
	private String TAG = "NewGameMenu";
	
	private static final int ITEM_PLAY_ALONE_ID = 0;
	private static final int ITEM_PLAY_AGAINST_SOMEONE_ID = 1;
	private static final int ITEM_INSTRUCTIONS_ID = 2;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    protected void makeSelection(){
		Log.d(TAG, "in make selection, lastLoc: "+ lastLoc);		
		int itemId = lastLoc;
		switch(itemId) {
		case ITEM_PLAY_ALONE_ID:
			Log.d(TAG, "play alone ");
			startSinglePlayerGame();
			break;
		case ITEM_PLAY_AGAINST_SOMEONE_ID:
			Log.d(TAG, "play against someone ");
			startMultiPlayerGame();
			break;
		case ITEM_INSTRUCTIONS_ID:
			Log.d(TAG, "speak instructions ");
			speakInstructions();
			break;
		}
    }
    
	private void speakInstructions() {
		Intent intent = new Intent(this, InstructionsReader.class);
		startActivity(intent);
	}
	
	private void startSinglePlayerGame() {
	/*	GameDB games = new GameDB(this);
		games.open();
		long id = games.addGame(1, "hey", true);
		games.printGameDB();
		games.close();*/
		long id = -1;
		Intent intent = new Intent(this, SinglePlayerMenu.class);
		intent.putExtra("id", id);
		startActivity(intent);
		finish();
	}
	
	private void startMultiPlayerGame() {
		Intent intent = new Intent(this, MultiPlayerMenu.class);
		startActivity(intent);
		finish();
	}
	
	@Override
	public void onFinishing()
	{
		Log.v(TAG, "Not destroying the tts");
	}	
    
}
