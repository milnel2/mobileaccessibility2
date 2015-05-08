package cs.washington.edu.VBGhost2;

import cs.washington.edu.VBGhost2.R;
import cs.washington.edu.VBGhost2.DictionaryDB.OnDbTaskComplete;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.util.Log;

public class ChallengeGameMenu extends AccessibleMenu{
	
	public static String CHALLENGE_RESULT = "result";
	private final int ITEM_COMPLETE_WORD_ID =0;
	private final int ITEM_INVALID_WORD_FRAGMENT_ID = 1;
	private String _word = "";
	private DictionaryDB _dictionaryDB = null;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		// Get the word from the intent used to start this activity
		Bundle extras = getIntent().getExtras();
		_word = extras.getString(MultiPlayerMenu.KEY_WORD);
	    //get dictionary database & fill with words if null
	    getDictionary(R.raw.kto8wordlist);
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
	
	  @Override
	    protected void makeSelection(){
			Log.d(TAG, "in make selection, lastLoc: "+ lastLoc);		
			int itemId = lastLoc;
			switch(itemId) {
			case ITEM_COMPLETE_WORD_ID:
				checkIfCompleteWord();
				break;
			case ITEM_INVALID_WORD_FRAGMENT_ID:
				checkIfInvalidWordFragment();
				break;
			}
	    }
	  
	  private void checkIfCompleteWord(){
  		Log.v(TAG, "checking if " + _word + " is word");
  		_dictionaryDB.checkWord(_word, _onTaskComplete);
	  }
	  
	  private void checkIfInvalidWordFragment(){
		  Log.v(TAG, "checking if " + _word + " is a valid word fragment");
  		_dictionaryDB.getWord(_word, _onTaskComplete);
	  }
	  
	  private void returnToMultiPlayerScreen(Boolean won){
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putBoolean(CHALLENGE_RESULT, won);	
			intent.putExtras(bundle);
			setResult(RESULT_OK, intent);
			finish();
	  }
	  
	  private OnDbTaskComplete _onTaskComplete = new OnDbTaskComplete() {
			@Override
			public void onComplete(int taskCode, Bundle result) {
				switch(taskCode) {
				case DictionaryDB.TASK_FILL_DB:
					Log.d(TAG, "task fill db complete");	
					break;	
				case DictionaryDB.TASK_FIND_WORD:
					Log.d(TAG, "task find word complete");
					Boolean found = result.getBoolean(DictionaryDB.KEY_FIND_WORD_SUCCEEDED);
					returnToMultiPlayerScreen(found);
					break;	
				case DictionaryDB.TASK_GET_WORD:
					Log.d(TAG, "task get word complete");
					String newWord = result.getString(DictionaryDB.KEY_GET_WORD_SUCCEEDED);
					if(newWord == null){
						//it is not part of a valid word fragment so the challenge succeeded
						returnToMultiPlayerScreen(true);
					}else{
						returnToMultiPlayerScreen(false);
					}
					break;
				}
			}
		};
	  
	  
	@Override
	public void onFinishing(){
		Log.v(TAG, "Not destroying the tts");
	}
}