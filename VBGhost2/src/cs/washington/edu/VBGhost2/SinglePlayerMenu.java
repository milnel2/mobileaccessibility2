package cs.washington.edu.VBGhost2;
import cs.washington.edu.VBGhost2.R;
import cs.washington.edu.VBGhost2.DictionaryDB.OnDbTaskComplete;
import junit.framework.Assert;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;

public class SinglePlayerMenu extends AccessibleMenu {
	private String TAG = "SinglePlayerMenu";
	
	private static final int ITEM_WORD_FRAGMENT_ID = 0;
	private static final int ITEM_ENTER_LETTER_ID = 1;
	private static final int ITEM_CHALLENGE_ID =2;
	private static final int ITEM_INSTRUCTIONS_ID = 3;
	
	private static final int ENTER_LETTER_REQUEST = 0;
	private long _id = -1; //if _id = -1, new game
	private GameDB _games = null;
	private DictionaryDB _dictionaryDB = null;
    private StringBuilder _wordFragment;
    private Boolean _yourTurn = true;
    private static final int NUMBER_OF_PLAYERS = 1;
    private static Boolean _deleteGame = false;
    private String computerWord="";
    private Boolean challenge = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _id = getIntent().getLongExtra("id", -1);
		Log.v(TAG, "id: "+ _id);
		_yourTurn = true;
		challenge = false;
		computerWord ="";
        if(_id == -1){
        	//start new game
        	_wordFragment = new StringBuilder();
        }else{
        	//continue old game 
    		_games = new GameDB(this);
    		_games.open();
    		Log.v(TAG, "PRINT");
    		_games.printGameDB();
    		_wordFragment = new StringBuilder(_games.getGameWordFragment((int)_id));
    		Log.v(TAG, "word fragment: "+ _wordFragment);
    		_games.close();
        }
        _deleteGame = false;
	    //get dictionary database & fill with words if null
	    getDictionary(R.raw.kto8wordlist);
    }

    
    @Override
    protected void makeSelection(){
		Log.d(TAG, "in make selection, lastLoc: "+ lastLoc);		
		int itemId = lastLoc;
		switch(itemId) {
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
			challenge();
			break;
		case ITEM_INSTRUCTIONS_ID:
			Log.d(TAG, "speak instructions ");
			speakInstructions();
			break;
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
	
	private void challenge(){
		if(_wordFragment.toString().equals("")){
			tts.speak("There is no word fragment to challenge ", TextToSpeech.QUEUE_ADD, null);
		}else if(computerWord.equals("")){
			challenge = true;
			//look up word in dictionary
			Assert.assertNotNull(_dictionaryDB);
    		String word = _wordFragment.toString();
    		_dictionaryDB.getWord(word, _onTaskComplete);
		}else{
			tts.speak("The word I was thinking of was: "+ computerWord, TextToSpeech.QUEUE_ADD, null);
			gameOver();
		}
	}
    
	private void onGetWordChallenge(){
		tts.speak("The word I was thinking of was: "+ computerWord, TextToSpeech.QUEUE_ADD, null);
		challenge = false;
		gameOver();
	}
	
	/**
	 * This method launches VBWriter that enables the user to guess a letter
	 * with V-Braille Write style input.
	 */
	private void enterLetter() {
		Intent intent = new Intent(this, VBWriter.class);
		startActivityForResult(intent, ENTER_LETTER_REQUEST);
	}
    
	private void speakInstructions() {
		Intent intent = new Intent(this, InstructionsReader.class);
		startActivity(intent);
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
	}
	
	private void onNoLetterEntered() {
		Log.v(TAG, "No Letter entered.");
	}
	
	private void onLetterEntered(char letter) {
		Log.v(TAG, "Letter entered: " + letter);
		_wordFragment.append(letter);
		Log.v(TAG, "word Fragment " + _wordFragment.toString());
		if(_wordFragment.length()>=3){
			//check if it is a word
    		Assert.assertNotNull(_dictionaryDB);
    		String word = _wordFragment.toString();
    		Log.v(TAG, "checking if " + _wordFragment.toString() + " is word");
    		_dictionaryDB.checkWord(word, _onTaskComplete);
		}else{
			//if it is not already a word, check if it is a valid word fragment 
    		Assert.assertNotNull(_dictionaryDB);
    		String word = _wordFragment.toString();
    		_dictionaryDB.getWord(word, _onTaskComplete);
		}
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
	
    protected void onGetWord(String newWord){
    	//if the computer picked a word that was only one letter longer than original
    	//they lose
    	if(newWord == null){
       		tts.speak(_wordFragment + " is not a valid word fragment.", 
    				TextToSpeech.QUEUE_ADD, null);
    		gameOver();
    	}else{
    		if(newWord.length() == _wordFragment.length()+1){
    			tts.speak("Congratulations, you win.  I just spelled " + newWord, 
    				TextToSpeech.QUEUE_ADD, null);
    			spellWord(newWord);
    			gameOver();
    		}else{
    			//Log.v(TAG, "printing out new word " + newWord);
    			_wordFragment.append(newWord.charAt(_wordFragment.length()));
    			_yourTurn = false;
    			if(_wordFragment.length()>=3){
    				_dictionaryDB.checkWord(_wordFragment.toString(), _onTaskComplete);  		
    			}else{
        			addedLetter();
    			}
    		}
    	}
    }
    
    protected void spellWord(String word){
    	tts.speak(word + " is spelled ", TextToSpeech.QUEUE_ADD, null);
    	for(int i=0;i<word.length();i++){
    		String s =((Character) word.charAt(i)).toString();
    		tts.speak(s, TextToSpeech.QUEUE_ADD, null);
    	}
    	
    }
    
    protected void addedLetter(){
		Log.v(TAG, "ADDED TO WORD FRAGMENT: " +_wordFragment.toString());
		String newLetter  = ((Character)_wordFragment.charAt(_wordFragment.length()-1)).toString();
		for(int i=0; i<GlobalState.alphabet.length; i++){
			if(GlobalState.alphabet[i].equalsIgnoreCase(newLetter)){
				tts.speak("I added the letter "+ newLetter + ", as in " + GlobalState.nato[i], TextToSpeech.QUEUE_ADD, null);
			}
		}
		_yourTurn = true;
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
				onCheckWord(found);
				break;	
			case DictionaryDB.TASK_GET_WORD:
				Log.d(TAG, "task get word complete");
				String newWord = result.getString(DictionaryDB.KEY_GET_WORD_SUCCEEDED);
				computerWord = newWord;
				if(!challenge){
					onGetWord(newWord);
				}else{
					onGetWordChallenge();
				}
				break;
			}
		}
	};
	
    protected void onCheckWord(Boolean wordFound){
    	if(_yourTurn){
    		if(wordFound){
    			tts.speak("I'm sorry you spelled " + _wordFragment.toString(), 
    				TextToSpeech.QUEUE_ADD, null);
    			spellWord(_wordFragment.toString());
    			gameOver();
    		}else{
    			//if it is not already a word, check if it is a valid word fragment 
    			Assert.assertNotNull(_dictionaryDB);
    			String word = _wordFragment.toString();
    			_dictionaryDB.getWord(word, _onTaskComplete);
    		
    		}
    	}else{
    		if(wordFound){
    			tts.speak("Congratulations, you win.  I just spelled " + _wordFragment, 
        				TextToSpeech.QUEUE_ADD, null);
        		spellWord(_wordFragment.toString());
        		gameOver();
    		}else{
    			addedLetter(); 		
    		}
    		
    	}
    }
    
    protected void gameOver(){
    	_yourTurn = true;
		tts.speak("Game over" , 
				TextToSpeech.QUEUE_ADD, null);
    	Log.v(TAG, "game over");
    	//clear wordFragment for next game
    	_wordFragment.delete(0, _wordFragment.length());
    	
    	_deleteGame = true;
    	//reset booleans
    	//return to main menu
    	returnToMainMenu();
    }
    
    private void returnToMainMenu(){
    	Log.v(TAG, "returnToMainMenu");
		String s = "Returning to main menu.";
		tts.setOnUtteranceCompletedListener(this);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "returnToMainMenu");
		tts.speak(s, TextToSpeech.QUEUE_ADD, utterance);
    }
    
	public void onUtteranceCompleted(String utteranceId) {
		Log.v(TAG, "onUtteranceCompleted");
		if (utteranceId.equalsIgnoreCase("returnToMainMenu")) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {}
			finish();
		}
		super.onUtteranceCompleted(utteranceId);
	}
	
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
			_id = (int)_games.addGame(1, _wordFragment.toString(), true);
		}else{
			//old game, update entry in database
			_games.updateGame((int)_id, _wordFragment.toString(), true);
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
