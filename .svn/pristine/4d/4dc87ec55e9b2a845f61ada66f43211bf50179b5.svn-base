package cs.washington.edu.VBGhost2;

import java.util.HashMap;

import cs.washington.edu.VBGhost2.R;
import cs.washington.edu.VBGhost2.DictionaryDB.OnDbTaskComplete;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class MultiPlayerMenu extends AccessibleMenu {
	private String TAG = "MultiPlayerMenu";
	
	private static final int ITEM_PLAYER_ID = 0;
	private static final int ITEM_WORD_FRAGMENT_ID = 1;
	private static final int ITEM_ENTER_LETTER_ID = 2;
	private static final int ITEM_CHALLENGE_ID = 3;
	private static final int ITEM_INSTRUCTIONS_ID = 4;
	
	private static final int ENTER_LETTER_REQUEST = 0;
	private static final int CHALLENGE_REQUEST = 1;
	
	public final static String KEY_WORD = "word";
	
	private long _id = -1; //if _id = -1, new game
	private StringBuilder _wordFragment;
	private GameDB _games = null;
	private Boolean _playerOneTurn = true;
	private DictionaryDB _dictionaryDB = null;
	private Boolean _deleteGame = false;
	private Boolean utteranceCompleted = false;
	private Boolean _challenge = false;
	private Boolean _win = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _id = getIntent().getLongExtra("id", -1);
		Log.v(TAG, "id: "+ _id);
		
        if(_id == -1){
        	//start new game
        	_wordFragment = new StringBuilder();
        	_playerOneTurn = true;
        }else{
        	//continue old game 
    		_games = new GameDB(this);
    		_games.open();
    		_wordFragment = new StringBuilder(_games.getGameWordFragment((int)_id));
    		int yourTurn = _games.getGameYourTurn((int)_id);
    		if(yourTurn == 0){
    			_playerOneTurn = false;
    		}else{
    			_playerOneTurn = true;
    		}
    		Log.v(TAG, "word fragment: "+ _wordFragment + "your turn "+ yourTurn);
    		//TODO: _plyaerOneTurn should be true if it's really their turn
    		//_playerOneTurn = true;
    		_games.close();
        }
        _challenge = false;
        _deleteGame = false;
        
	    //get dictionary database & fill with words if null
	    getDictionary(R.raw.kto8wordlist);
    }
    
    @Override
    protected void makeSelection(){
		Log.d(TAG, "in make selection, lastLoc: "+ lastLoc);		
		int itemId = lastLoc;
		switch(itemId) {
		case ITEM_PLAYER_ID:
			Log.d(TAG, "word fragment");
			speakPlayer();
			break;
		case ITEM_WORD_FRAGMENT_ID:
			Log.d(TAG, "word fragment");
			readWordFragment();
			break;
		case ITEM_ENTER_LETTER_ID:
			Log.d(TAG, "enter letter ");
			enterLetter();
			break;
		case ITEM_CHALLENGE_ID:
			Log.d(TAG, "challenge ");
			challengeWord();
			break;
		case ITEM_INSTRUCTIONS_ID:
			Log.d(TAG, "speak instructions ");
			speakInstructions();
			break;
		}
    }
    
    private void challengeWord(){
    	if(_wordFragment.toString().equals("")){
			tts.speak("There is no word fragment to challenge ", TextToSpeech.QUEUE_ADD, null);
		}else{
			Intent intent = new Intent(this, ChallengeGameMenu.class);
			intent.putExtra(KEY_WORD, _wordFragment.toString());
			startActivityForResult(intent, CHALLENGE_REQUEST);
		}
    }
    
    private void speakPlayer(){
    	Log.v(TAG, "in speakPlayer()");
    	String name="";
    	if(_playerOneTurn){
    		name = " player one's turn";
    	}else{
    		name = " player two's turn";
    	}
    	tts.speak(name, TextToSpeech.QUEUE_ADD, null);
    }
    
	// If the TTS instance got wiped out, restart it.
	@Override
	public void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		
		//create TextToSpeech    
	    tts = GlobalState.getTTS();    
	    if (tts == null) {
	    	GlobalState gs = (GlobalState)getApplication();
	    	Log.v(TAG, "Creating new tts for application " + gs);
	        tts = gs.createTTS(this, this);
	    }
	    else if (!mJustCreated && !_challenge) tts.speak(redisplay, TextToSpeech.QUEUE_FLUSH, null);
	    Log.v(TAG, "tts = " + tts);
		tts.setOnUtteranceCompletedListener(self);
		mJustCreated = false;
		if(!_challenge){
			speakPlayer();
		}else{
			onChallenge(_win);
		}
	}
    
	/**
	 * This method reads the existing word fragment out loud
	 */
	private void readWordFragment() {
    	if(_wordFragment == null || _wordFragment.length() ==0){
        	tts.speak("There are no letters in word fragment. ", TextToSpeech.QUEUE_ADD, null);
    	}else{
        	tts.speak("the word fragment is spelled ", TextToSpeech.QUEUE_ADD, null);
        	for(int i=0;i<_wordFragment.length();i++){
        		String s =((Character) _wordFragment.charAt(i)).toString();
        		tts.speak(s, TextToSpeech.QUEUE_ADD, null);
        	}
    	}
	}
	
	/**
	 * This method launches VBWriter that enables the user to guess a letter
	 * with V-Braille Write style input.
	 */
	private void enterLetter() {
		Intent intent = new Intent(this, VBWriter.class);
		startActivityForResult(intent, ENTER_LETTER_REQUEST);
	}
	
	@Override
	protected void onActivityResult(int requestCode, 
			int resultCode, Intent data) {
		if(requestCode == ENTER_LETTER_REQUEST) {
			if(resultCode == RESULT_OK) {
				// Get the letter entered from the intent.
				Bundle extras = data.getExtras();
				if(extras == null) {
					// No dots entered
					onNoLetterEntered();
				} else {
					char letter = extras.getChar(VBWriter.KEY_LETTER_ENTERED);
					onLetterEntered(letter);
				}
			}
		}
		if(requestCode == CHALLENGE_REQUEST){
			Log.v(TAG, "CHALLENGE_REQUEST");
			_challenge = true;
			if(resultCode == RESULT_OK) {
				// Get the letter entered from the intent.
				Bundle extras = data.getExtras();
				if(extras == null) {
					onChallengeNull();
				} else {
					_win = extras.getBoolean(ChallengeGameMenu.CHALLENGE_RESULT);
				}
			}
		}
	}
	
	
	private void onChallengeNull(){
    	Log.v(TAG, "onChallengeNull");
		tts.speak("Sorry challenge did not go through", TextToSpeech.QUEUE_ADD, null);
		gameOver();
	}
	
	private void onChallenge(Boolean win){
    	Log.v(TAG, "onChallenge");
		tts.setOnUtteranceCompletedListener(this);
		String s = "Returning to Main Menu";
    	if(win){
    		Log.v(TAG, "challenge won!");
    		if(_playerOneTurn){
    			s = "Challenge won.  Congratulations Player One. ";
    		}else{
    			s ="Challenge won.  Congratulations Player Two. ";
    		}
    	}else{
    		Log.v(TAG, "challenge lost!");
    		if(_playerOneTurn){
    			s= "Challenge lost.  Congratulations Player Two. ";
    		}else{
    			s ="Challenge lost.  Congratulations Player One. ";
    		}
    	}
    	utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "challenge");
    	tts.speak(s, TextToSpeech.QUEUE_ADD, utterance);
	}
	
    protected void gameOver(){
		tts.speak("Game over" , 
				TextToSpeech.QUEUE_ADD, null);
    	Log.v(TAG, "game over");
    	//clear wordFragment for next game
    	_wordFragment.delete(0, _wordFragment.length());
    	
    	_deleteGame = true;
    	//reset booleans
    	//return to main menu
    	returnToMainMenu();
    	utteranceCompleted = false;
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {}
		if(utteranceCompleted == false){
			finish();
		}
    }
    
    
    private void returnToMainMenu(){
    	Log.v(TAG, "returnToMainMenu");
		String s = "Returning to main menu.";
		tts.setOnUtteranceCompletedListener(this);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "returnToMainMenu");
		tts.speak(s, TextToSpeech.QUEUE_ADD, utterance);
    }
	
	private void onNoLetterEntered() {
		Log.v(TAG, "No Letter entered.");
	}
	
	private void onLetterEntered(char letter){
		Log.v(TAG, "Letter entered: " + letter);
		_wordFragment.append(letter);
		if(_playerOneTurn){
			_playerOneTurn = false;
		}else{
			_playerOneTurn = true;
		}
	}
    
	private void speakInstructions() {
		Intent intent = new Intent(this, InstructionsReader.class);
		startActivity(intent);
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
	
	public void onUtteranceCompleted(String utteranceId) {
		Log.v(TAG, "onUtteranceCompleted");
		if (utteranceId.equalsIgnoreCase("returnToMainMenu")) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			utteranceCompleted = true;
			finish();
		}
		if (utteranceId.equalsIgnoreCase("challenge")) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			gameOver();
		}
		super.onUtteranceCompleted(utteranceId);
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
				//onCheckWord(found);
				break;	
			}
		}
	};
	
	@Override
	public void onPause(){
		//update database
		_games = new GameDB(this);
		_games.open();
		if(_deleteGame == true){
			_games.deleteGame((int)_id);
			_deleteGame = false;
		}else if(_id == -1){
			//new game, add game to database
			_id = (int)_games.addGame(2, _wordFragment.toString(), _playerOneTurn);
		}else{
			//old game, update entry in database
			_games.updateGame((int)_id, _wordFragment.toString(), _playerOneTurn);
		}
		_games.close();
		super.onPause();
	}
	
	@Override
	public void onFinishing()
	{
		Log.v(TAG, "Not destroying the tts");
	}	
    
}
