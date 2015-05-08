package edu.washington.cs.hangman;

import junit.framework.Assert;
import edu.washington.cs.hangman.MenuView.OnItemSelected;
import edu.washington.cs.hangman.WordDb.OnDbTaskComplete;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.SQLException;
import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.LinearLayout;

public class StartGameAct extends AccessibleActivity implements OnItemSelected {
	private static final String TAG = "startgameact";

	private static final String ACTIVITY_TITLE = "Start a new game screen";
	
	private MenuView _menuView;

	// Menu item names
	private static String[] _menuItems = { 
		"3 letters", 
		"4 letters",
		"5 letters",
		"6 letters",
		"7 letters"
	};

	// Use the database. If there are problems with using the database, you can
	// set USE_DB to false. 
	public static boolean USE_DB = true;
	public final static String KEY_WORD = "word";	
	private boolean _dbLoaded = false;	

	// Set to true when the database is loaded with words and 
	// ready for use. Do not try to retrieve a word from the database
	// before it is ready.---- make sure set to true before we call on getRandomword
	private WordDb _wordDb = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, ACTIVITY_TITLE, Hangman.getTts());

		// Use the full screen (no title bar)
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		
		_menuView = new MenuView(this, Hangman.getTts(), this);
		for(int i = 0; i < _menuItems.length; ++i) {
			_menuView.addItem(_menuItems[i], i);
		}

		_menuView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		setContentView(_menuView);

		if(USE_DB) {
			// Fill the database with words (if it's not already full)
			_wordDb = new WordDb(this);
			try {
				_wordDb.open();
			} catch (SQLException e) {
				Log.e(TAG, "SQLEception when opening word database: " + e.getMessage());
				e.printStackTrace();
			}
			_wordDb.loadWords(R.raw.words, _onTaskComplete);
		} else {
			// When not using the database, there is nothing to load.
			_dbLoaded = true;
		}
	}


	/**
	 * Call this function to get a random word from the database with a given length.
	 * When the word is retrieved, onWordSelected(...) will be called.
	 * @param wordLen	The length of a random word to retrieve from the database.
	 */ 
	private void getRandomWord(int wordLen) {
		// Load a word randomly and start the game.
		Assert.assertTrue(_dbLoaded);
		if(USE_DB) {
			Assert.assertNotNull(_wordDb);
			_wordDb.getRandomWord(wordLen, _onTaskComplete);
		} else {
			// If we're not using the word database, return a single word for
			// each word length. 
			String word = "blah";
			switch(wordLen) {
			case 2:
				word = "if";
			case 3:
				word = "cat";
			case 4:
				word = "that";
			case 5:
				word = "apple";
			case 6: 
				word = "poster";
			}
			onWordRetrieved(word); 		
		}
	}

	/**
	 * This method is called when a word was selected from the database. It launches
	 * the GameMainAct activity, passing it the retrieved word.
	 */ 
	private void onWordRetrieved(String word) {
		// Launch a new game
		Intent intent = new Intent(this, GameMainAct.class);
		intent.putExtra(KEY_WORD, word);
		startActivity(intent);
	}

	private OnDbTaskComplete _onTaskComplete = new OnDbTaskComplete() {
		@Override
		public void onComplete(int taskCode, Bundle result) {
			switch(taskCode) {
			case WordDb.TASK_FILL_DB:
				Log.d(TAG, "task fill db copmlete");
				_dbLoaded = true;		
				break;
			case WordDb.TASK_GET_WORD:
				Log.d(TAG, "task get word complete");
				String word = result.getString(WordDb.KEY_WORD_RETRIEVED);
				onWordRetrieved(word);
				break;
			}
		}
	};

	@Override
	public void onDestroy() {
		super.onDestroy();
		// Close the word database
		_wordDb.close();
	}

	public void onSelected(int itemId) {
		int wordLen = itemId + 3;
		getRandomWord(wordLen);
	}
}