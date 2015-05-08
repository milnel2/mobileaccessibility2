package edu.washington.cs.hangman;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.washington.cs.hangman.MenuView.OnItemSelected;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

public class GameMainAct extends AccessibleActivity implements OnItemSelected {
	private static final String TAG = "gamemainact";

	private static final String ACTIVITY_TITLE = "Current game screen";

	private static final int ENTER_LETTER_REQUEST = 0;
	private static final int TOTAL_TRIALS = 9;

	private static final char HIDDEN_CHAR = '_';

	// Menu item ids
	private static final int ITEM_WORD_ID = 0;
	private static final int ITEM_TRIAlS_LEFT_ID = 1;
	private static final int ITEM_GUESSED_LETTERS_ID = 2;	
	private static final int ITEM_ENTER_LETTER_ID = 3;

	private static final String ITEM_WORD_LABEL = "word";
	private static final String ITEM_TRIAlS_LEFT_LABEL = "trials left";
	private static final String ITEM_GUESSED_LETTERS_LABEL = "guessed letters";	
	private static final String ITEM_ENTER_LETTER_LABEL = "enter letter";

	private MenuView _menuView;
	private String _word;
	private char[] _guessedWord;	// need better name
	private int _trialsLeft;

	private HashSet<Character> _enteredLetters = new HashSet<Character>();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState, ACTIVITY_TITLE, Hangman.getTts());

		// Use the full screen (no title bar)
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, 
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

		_menuView = new MenuView(this, Hangman.getTts(), this);

		_menuView.addItem(ITEM_WORD_LABEL, ITEM_WORD_ID);
		_menuView.addItem(ITEM_TRIAlS_LEFT_LABEL, ITEM_TRIAlS_LEFT_ID);
		_menuView.addItem(ITEM_GUESSED_LETTERS_LABEL, ITEM_GUESSED_LETTERS_ID);
		_menuView.addItem(ITEM_ENTER_LETTER_LABEL, ITEM_ENTER_LETTER_ID);

		_menuView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT));
		setContentView(_menuView);

		// Get the word from the intent used to start this activity
		Bundle extras = getIntent().getExtras();
		_word = extras.getString(StartGameAct.KEY_WORD);

		// Initialize the guess word to all blanks
		int wordLen = _word.length();
		_guessedWord = new char[wordLen];
		for(int i = 0; i < wordLen; ++i) {
			_guessedWord[i] = HIDDEN_CHAR;
		}

		_trialsLeft = TOTAL_TRIALS;
	} 

	/**
	 * This method launches EnterLetterAct that enables the user to guess a letter
	 * with V-Braille Write style input.
	 */
	private void enterLetter() {
		Intent intent = new Intent(this, EnterLetterAct.class);
		startActivityForResult(intent, ENTER_LETTER_REQUEST);
	}

	private void onNoLetterEntered() {
		Hangman.getTts().speak("No dots entered.", 
				TextToSpeech.QUEUE_ADD, null);
	}
	
	/**
	 * This method is called when a user enters a letter with the EnterLetterAct activity.
	 * @param letter	The letter entered by the user.
	 */
	private void onLetterEntered(char letter) {
		if(letter == BrailleTable.INVALID_CHAR) {
			Hangman.getTts().speak("The dots your entered are not a Braille letter. Try again.", 
					TextToSpeech.QUEUE_FLUSH, null);
		} else if(_enteredLetters.contains(letter)) {
			// speak error - already entered letter
			Hangman.getTts().speak("You have already guessed the letter " + letter, 
					TextToSpeech.QUEUE_FLUSH, null);
		} else {
			_enteredLetters.add(letter);
			boolean letterIsInWord = false;
			int wordLen = _word.length();
			for(int i = 0; i < wordLen; ++i) {
				if(_word.charAt(i) == letter) {
					letterIsInWord = true;
					_guessedWord[i] = letter;
				}
			}

			if(letterIsInWord) {
				Hangman.getTts().speak("good guess! " + letter + ", is in the word.", 
						TextToSpeech.QUEUE_FLUSH, null);
				// Read word
				onWord(false);

				// Check if word is complete
				boolean wordComplete = true;
				for(int i = 0; i < wordLen; ++i) {
					if(_guessedWord[i] == HIDDEN_CHAR) {
						wordComplete = false;
					}
				}
				if(wordComplete) {
					onWordComplete();
				}

			} else {
				--_trialsLeft;
				Hangman.getTts().speak(letter + ", is not in the word.", 
						TextToSpeech.QUEUE_FLUSH, null);
				onTrialsLeft(/*flush*/ false);
				if(_trialsLeft == 0) {
					// user has lost the game.
					Hangman.getTts().speak("Game over. Try another word.", 
							TextToSpeech.QUEUE_ADD, null);
					finish();
				}
			}
		}

	}

	/**
	 * User has guessed all letters in the word. Speak an appropriate
	 * message and return to the start game screen.
	 */
	private void onWordComplete() {
		Hangman.getTts().speak("Good work! You have guessed all letters in the word. You won with " + 
				_trialsLeft + " trials left.", 
				TextToSpeech.QUEUE_ADD, null);
		finish();
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
					char letter = extras.getChar(EnterLetterAct.KEY_LETTER_ENTERED);
					onLetterEntered(letter);
				}
			}

		}
	}

	private void onWord(boolean flush) {
		String wordMsg = "";
		for(int i = 0; i < _guessedWord.length; ++i) {
			if(_guessedWord[i] == HIDDEN_CHAR) {
				wordMsg += "blank, ";
			} else {
				wordMsg += _guessedWord[i] + ", ";
			}
		}
		Hangman.getTts().speak(wordMsg, 
				flush ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, 
						null);
	}

	private void onTrialsLeft(boolean flush) {
		String trialsLeftMsg = "you have " + _trialsLeft + 
				((_trialsLeft == 1) ? " trial" : " trials") +
				"left.";
		Hangman.getTts().speak(trialsLeftMsg, 
				flush ? TextToSpeech.QUEUE_FLUSH : TextToSpeech.QUEUE_ADD, 
						null);
	}

	private void onGuessedLetters() {
		String guessLettersMsg = "letters guessed: ";
		Iterator<Character> itr = _enteredLetters.iterator();
		while(itr.hasNext()) {
			guessLettersMsg += itr.next() + ", ";
		}
		Hangman.getTts().speak(guessLettersMsg, TextToSpeech.QUEUE_FLUSH, null);

	}

	public void onSelected(int itemId) {
		switch(itemId) {
		case ITEM_WORD_ID:
			onWord(true);
			break;
		case ITEM_TRIAlS_LEFT_ID:
			onTrialsLeft(true);
			break;
		case ITEM_GUESSED_LETTERS_ID:
			onGuessedLetters();
			break;
		case ITEM_ENTER_LETTER_ID:
			enterLetter();
			break;
		}
	}
}
