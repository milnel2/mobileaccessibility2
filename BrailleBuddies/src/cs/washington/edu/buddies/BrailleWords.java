package cs.washington.edu.buddies;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.content.res.Resources;
import android.content.res.XmlResourceParser;
import android.content.res.Resources.NotFoundException;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.os.Bundle;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/* Displays Braille letters that make up words so that
 * user can try to determine the words and earn tokens.
 */
public class BrailleWords extends Activity 
	implements TextToSpeech.OnUtteranceCompletedListener {
	
	protected static final int INPUT_ANSWER = 10;
	public static final int RESULT_CORRECT_ANSWER = RESULT_FIRST_USER + 1;
	public static final int EASY = 0;
	public static final int INTERMEDIATE = 1;
	public static final int HARD = 2;
	public static final int MULTIKEY_MODE = -1;

	
	private static final String TAG = "BrailWords";

    private static final String[] alphadots =
    {"100000","101000","110000","110100","100100","111000","111100","101100",
    	"011000","011100","100010","101010","110010","110110","100110","111010",
    	"111110","101110","011010","011110","100011","101011","011101","110011",
    	"110111","100111"};

    private static final String[] modes = {"easie", "intermediate", "difficult"};
    private static final String[] regions = {"1", "4", "2", "5", "3", "6"};
    
    private BrailleWordsView bwview;
    private TextToSpeech tts;
    HashMap<String, String> utterance = new HashMap<String, String>();
    
    private String bwInstr = "How to play Braille words:" +
			" The touchscreen is divided into six regions to represent the Braille cell," +
			" Touch a region and its number will be spoken," +
			" Any region that contains a dot will vibrate when touched," +
			" The dots contained within the screen will represent a letter" +
			" when you have determined what letter is being displayed, " +
				"either press down on the trackball or on the center button of the directional pad," +
			" the next letter in the word will be displayed and vibrated," +
			" rolling left on the trackball or pressing the left directional kee moves back one letter," +
			" rolling right on the trackball or pressing the right directional kee moves forward one letter," +
			" after the last symbol in the word has been displayed, pressing down on the trackball " +
				"or center directional key will display a touchscreen keepad where you input the word you just read in Braille," +
			" You can navigate the keepad by either touching and dragging on the screen" +
				" or by using the trackball or directional keys," +
			" the letters on the keepad's keys will be spoken when the kee is touched," +
			" when you hear the kee you want, press down again on the trackball or center button of the directional pad" +
				"to add that letter onto your answer" +
			" continue selecting letters in this manner until you have spelled the word you read in Braille," +
			" you can also use the hard keeboard to add letters to your answer," +
			" you can use the backspace key to remove the last letter from your answer," +
			" to remove all letters at once, shake the phone in a diagonal corner-to-corner direction several times," +
			" at the bottom of the keepad are four special buttons: " +
			" the 'Play Correct Answer' button speaks the correct answer to you before selecting a new word," +
			" the 'Replay Word' button replays the word for you," +
			" the 'Read Current Answer' button reeds the letters of your current answer back to you," +
			" and the 'Submit Answer' button is used to enter your answer," +
			" to select a button, navigate to it and press down on the trackball or on the center directional key" +
			" after you have submitted your answer, you will return to the vibrating Braille screen and feedback will be given" +
				" to let you know whether your answer is correct," +
			" if your answer is correct, the word is spoken and then a new word is selected and played," +
			" for incorrect answers, the word will be replayed if you have not exceeded " + 
				"the maximum number of tries and you can try again," +
			" Otherwise the correct answer will be spoken," +
			" You can leave the game at any time by pressing the back button," +
			" You can change the difficulty level by pressing the menu key," +
			" End of instructions.";
    
//    private String[] bwInstr = {"How to play Braille words:",
//			"The touchscreen is divided into six regions to represent the Braille cell,",
//			"Touch a region and its number will be spoken,",
//			"Any region that contains a dot will vibrate when touched,",
//			"The dots contained within the screen will represent a letter",
//			"when you have determined what letter is being displayed, " +
//				"either press down on the trackball or on the center button of the directional pad,",
//			"the next letter in the word will be displayed and vibrated,",
//			"rolling left on the trackball or pressing the left directional kee moves back one letter,",
//			"rolling right on the trackball or pressing the right directional kee moves forward one letter,",
//			"after the last symbol in the word has been displayed, pressing down on the trackball " +
//				"or center directional key will display a touchscreen keepad where you input the word you just read in Braille,",
//			"You can navigate the keepad by either touching and dragging on the screen" +
//				" or by using the trackball or directional keys,",
//			"the letters on the keepad's keys will be spoken when the kee is touched,",
//			"when you hear the kee you want, press down again on the trackball or center button of the directional pad" +
//				"to add that letter onto your answer",
//			"continue selecting letters in this manner until you have spelled the word you read in Braille,",
//			"you can also use the hard keeboard to add letters to your answer,",
//			"you can use the backspace key to remove the last letter from your answer,", 
//			"to remove all letters at once, shake the phone in a diagonal corner-to-corner direction several times,",
//			"at the bottom of the keepad are four special buttons: ",
//			"the 'Play Correct Answer' button speaks the correct answer to you before selecting a new word,",
//			"the 'Replay Word' button replays the word for you,",
//			"the 'Read Current Answer' button reeds the letters of your current answer back to you,",
//			"and the 'Submit Answer' button is used to enter your answer,",
//			"to select a button, navigate to it and press down on the trackball or on the center directional key",
//			"after you have submitted your answer, you will return to the vibrating Braille screen and feedback will be given" +
//				" to let you know whether your answer is correct;",
//			"if your answer is correct, the word is spoken and then a new word is selected and played;",
//			"for incorrect answers, the word will be replayed if you have not exceeded " + 
//				"the maximum number of tries and you can try again,",
//			"Otherwise the correct answer will be spoken,",
//			"You can leave the game at any time by pressing the back button,",
//			"You can change the difficulty level by pressing the menu key;",
//			"End of instructions."};
	
    private int mode;
    
    private int vibrating = 0;
    private Vibrator vibe;
    
    // For reading/storing words
    private Random rand;
    private boolean[] haveCount = {false, false, false};  // file words have been counted
	private int[] wordCount = {0,0,0};  // total number of words in each file
	private byte[][] usedWords = new byte[3][0]; // == 1 if word has been loaded already
    private String[][] wordlist = new String[3][0];  // list of currently loaded words
    private int[] totalUsedCount = {0,0,0}; // total number of words used
    private int[] listUsedCount = {0,0,0}; // number of words used from current list
    
    private boolean wordShown;  // indicates if all letters in word have been shown
    private String mWord;  // current word
    private int wordIndex; // (mWord) index of current character in word
    private int whichWord; // (wordlist[mode]) index of word to display
    private int whichSymbol; // (alphadots) index of symbol to vibrate
    
    private int numTries;
    private String lastWordDisplayed;
    
	private int[] numWrong = {0,0,0};
	private int[] numCorrect = {0,0,0};
	private int[] numDisplayed = {0,0,0};
	private int[] numGivenAnswer = {0,0,0};
	private int earned;
	
    private int lastRegion;
    
    private int index = 0;
    
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // mode: 0 = easy, 1 = intermediate, 2 = hard
    	mode = Settings.getLevel(this);  
        tts = Game.tts;
        tts.speak("the difficulty level is set to " + modes[mode] +
        		", Touch screen to begin game, at any time " +
        		"press on screen for 3 seconds to hear instructions, "  +
        		"touch screen again in a different location to interrupt instructions",
        		TextToSpeech.QUEUE_FLUSH, null);
        
        bwview = new BrailleWordsView(this);
    	mWord = "";
    	lastWordDisplayed = "";  // used to hold the last word for which the "numDisplayed[mode]" value was incremented.
    	wordShown = false;
    	wordIndex = -1;
    	whichWord = -1;
    	whichSymbol = -1;
    	earned = 0;
    	
    	// for speaking instructions
//    	String filenm;
//    	Resources resrcs = getResources();
//    	String pkg = getPackageName();
//    	int id;
//        for (int i = 0; i < bwInstr.length; i++) {
//        	filenm = pkg + ":raw/bwinstr" + i;
//        	id = resrcs.getIdentifier(filenm, null, null);
//        	if (id != 0)
//        		tts.addSpeech(bwInstr[i], pkg, id);
//        }
        
    	rand = new Random();
        this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		setContentView(bwview);
    	selectRandomWord();
    }
    
    /* */
    private void readWordsFromXmlFile() {
    	
    	// If all words in the file have been read in, reset.
    	// It's next to impossible that this will ever happen.
    	if (totalUsedCount[mode] >= wordCount[mode]) {
    		initializeUsedWordsArray();
    	}
    	
    	XmlResourceParser parser;

    	// set the parser
    	if (mode == EASY)
    		parser = getResources().getXml(R.xml.easy_words);
    	else if (mode == INTERMEDIATE)
    		parser = getResources().getXml(R.xml.intermed_words);
    	else
    		parser = getResources().getXml(R.xml.hard_words);

    	/* Select and load up to 100 random words */
    	int load = (wordCount[mode] - totalUsedCount[mode] >= 100) ? 100 :
    		wordCount[mode] - totalUsedCount[mode];

    	// choose random values to stop on when parsing xml word file
    	int wordsToGet[] = new int[load];
    	for (int i = 0; i < load; i++) {
    		do {
    			wordsToGet[i] = rand.nextInt(wordCount[mode]);
    		} while (usedWords[mode][wordsToGet[i]] != 0);
    		usedWords[mode][wordsToGet[i]] = 1;
    	}

    	// sort the numbers
    	Arrays.sort(wordsToGet);

    	// release all old words in the wordlist, if applicable
    	for (int i = 0; i < wordlist[mode].length; i++) {
    		wordlist[mode][i] = null;
    	}

    	// now parse the file, stopping at the randomly chosen words
    	// and loading them into the word list
    	int eventType = -1, wordNum = 0, index = 0;
    	wordlist[mode] = new String[load];  // initialized to null by default
    	listUsedCount[mode] = 0;
    	
    	while (eventType != XmlResourceParser.END_DOCUMENT) {
    		if (eventType == XmlResourceParser.START_DOCUMENT) {
    			Log.d(TAG, "Document Start");
    		}
    		else if (eventType == XmlResourceParser.START_TAG) {
    			String str = parser.getName();
    			if (str.equals("word")) {
    				if (index < load && wordNum == wordsToGet[index]) {
    					wordlist[mode][index] = parser.getAttributeValue(null, "value");
    					index++;
    				}
    				// break out early if wordlist has been filled
    				if (index >= load)
    					break;
    				wordNum++;
    			}
    		}
    		try {
    			eventType = parser.next();
    		} 
    		catch (XmlPullParserException e) {} 
    		catch (IOException e) {}
    	}	    	
    }
    
    /* Creates and initializes array to mark used symbols
     * each time the mode is changed.
     */
    private void initializeUsedWordsArray() {
    	Log.v(TAG, "resetUsedArray");
    	
    	/* Count words */
    	if (!haveCount[mode]) {
    		
    		XmlResourceParser parser;

    		if (mode == EASY)
    			parser = getResources().getXml(R.xml.easy_words);
    		else if (mode == INTERMEDIATE)
    			parser = getResources().getXml(R.xml.intermed_words);
    		else
    			parser = getResources().getXml(R.xml.hard_words);

    		int eventType = -1;
    		while (eventType != XmlResourceParser.END_DOCUMENT) {
    			if (eventType == XmlResourceParser.START_DOCUMENT) {
    				Log.d(TAG, "Document Start");
    			}
    			else if (eventType == XmlResourceParser.START_TAG) {
    				String str = parser.getName();
    				if (str.equals("word")) {
    					// wordCount[mode] is initialized to 0 in declaration
    					wordCount[mode]++;
    				}
    			}
    			try {
    				eventType = parser.next();
    			} 
    			catch (XmlPullParserException e) {} 
    			catch (IOException e) {}
    		}

    		// reset parser and reset usedWords array
    		try {
    			parser.setInput(null);
    		} 
    		catch (XmlPullParserException e) {}
			
    		haveCount[mode] = true;
			
    	} // end if !haveCount
		  	
    	usedWords[mode] = new byte[wordCount[mode]]; // elements default to 0
    	totalUsedCount[mode] = 0;
    }
    
    /* Mark current word as used and choose a
     * new word.
     */
    private void selectRandomWord() {
    	
    	tts.speak("selecting new word", TextToSpeech.QUEUE_ADD, null);
    	
    	// If all words in the current wordlist have been used, 
    	// read in next set of words.
    	// This is also unlikely but less so than the previous.
    	if (listUsedCount[mode] >= wordlist[mode].length) {
    		readWordsFromXmlFile();
    	}   	
    	wordShown = false;
    	
    	// Randomly get next word from current wordlist
    	do {
    		whichWord = rand.nextInt(wordlist[mode].length);
    	} while(wordlist[mode][whichWord] == null);
    	mWord = wordlist[mode][whichWord].toLowerCase();
    	wordlist[mode][whichWord] = null;
    	listUsedCount[mode]++;
    	totalUsedCount[mode]++;
    	wordIndex = -1;
    	
    	findNextSymbol();
    	
    	bwview.invalidate();
    	
    	numTries = 0;
    	tts.speak("Ready, word has " + String.valueOf(mWord.length()) + " letters, " +
    			"playing first letter in word", TextToSpeech.QUEUE_ADD, null);
    } 
    
    
    private void findNextSymbol() {
    	wordIndex++;
    	
    	// find the next letter in the word
    	if (wordIndex < mWord.length())	
        	whichSymbol = mWord.charAt(wordIndex) - 'a';
    		
    	// If this is the last letter of the word, set flag
    	if (wordIndex + 1 >= mWord.length()) {
    		wordShown = true;
    	}
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
    	// change to display settings menu  
    	if(keyCode == KeyEvent.KEYCODE_MENU){
    		mode = (mode + 1 >= modes.length || mode < 0) ? 0 : mode + 1;
    		tts.speak("Setting difficulty level to " + modes[mode], TextToSpeech.QUEUE_FLUSH, null);
    		Settings.setLevel(this, mode);
    		selectRandomWord();
    	}
    	else if(keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
    		
    		// Center key pushed so player can give input
    		if (wordShown){
        		displayKeypad();
    		}
    		else {
    			bwview.invalidate();
    			findNextSymbol();
    			if (wordShown)
    				tts.speak("playing last letter in word", TextToSpeech.QUEUE_FLUSH, null);
    			else
    				tts.speak("playing next letter in word", TextToSpeech.QUEUE_FLUSH, null);
    		}
    	}
    	// go back one letter
    	else if(keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {		
    		wordIndex -= 2;
    		if (wordIndex < -1) 
    			wordIndex = -1;
    		wordShown = false;
    	    findNextSymbol();
    	    bwview.invalidate();
    	    tts.speak("Playing letter " + (wordIndex + 1) + " of word", TextToSpeech.QUEUE_FLUSH, null);
    	}
    	// go forward one letter
    	else if(keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {		
    		if (wordShown) 
    			tts.speak("At last letter of word", TextToSpeech.QUEUE_FLUSH, null);
    		else {
	    	    findNextSymbol();
	    	    bwview.invalidate();
	    	    tts.speak("Playing letter " + (wordIndex + 1) + " of word", TextToSpeech.QUEUE_FLUSH, null);
    		}
    	}
    	else if(keyCode == KeyEvent.KEYCODE_BACK) {
    		tts.speak("Back kee pressed,", TextToSpeech.QUEUE_FLUSH, null);
    		speakStatistics();
    	}
    	else
    		return super.onKeyDown(keyCode, keyEvent);
    	Log.v(TAG, "onKeyDown");
    	return true;
    }
    
    protected void displayKeypad() {
    	tts.stop();
    	// Increment the counter for the number of words in the current mode
    	// that have been displayed if the current word is not the same as 
    	// the word for which this counter was last incremented.  This check
    	// is needed since incrementing the counter earlier (i.e., when the 
    	// word is selected) gives an inaccurate result if the player quits
    	// out of the mode/game before inputting an answer.  It's also needed
    	// so that a word isn't double counted if the player opens the keypad
    	// but then comes back and replays the word rather than answering.
    	if (!mWord.equalsIgnoreCase(lastWordDisplayed)) {
			lastWordDisplayed = mWord;
			numDisplayed[mode]++;
		}
    	Intent input = new Intent(this, Keypad.class);
    	input.putExtra("mode", MULTIKEY_MODE);
       	startActivityForResult(input, INPUT_ANSWER);
    	Log.v(TAG, "displayKeypad - activity started");
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.v(TAG, "onActivityResult; requestCode = " + requestCode + ", resultCode = " + resultCode);
    	if (requestCode == INPUT_ANSWER) {
    		if (resultCode == RESULT_CANCELED){
    			inputCancelled();
    		}
    		else if (resultCode == RESULT_OK){
        		String answer = data.getStringExtra("answer");
    			evaluateAnswer(answer);
    		}
    		else if (resultCode == RESULT_CORRECT_ANSWER) {
    			numGivenAnswer[mode]++;
    			speakCorrectAnswer();
    		}
    	}
    	else {
    		super.onActivityResult(requestCode, resultCode, data);
    	}
    }
    	    
    protected void inputCancelled() {
    	Log.v(TAG, "inputCancelled");
    	wordIndex = -1;
    	wordShown = false;
    	findNextSymbol();
    	bwview.invalidate();
    	tts.speak("Replaying word starting with first letter", TextToSpeech.QUEUE_ADD, null);
    } 
 
    protected void evaluateAnswer(String answer) {
    	Log.v(TAG, "evaluateAnswer, answer = " + answer);
  		if (answer.equalsIgnoreCase(mWord)) {
  			tts.speak("That is correct! The word wuz " + mWord, TextToSpeech.QUEUE_ADD, null);
  			numCorrect[mode]++;
  			int amount = 0;
  			if (mode == Settings.EASY)
  				amount = BankAccount.PER_EASY_WORD;
  			else if (mode == Settings.INTERMEDIATE)
  				amount = BankAccount.PER_INTERMED_WORD;
  			else if (mode == Settings.HARD)
  				amount = BankAccount.PER_HARD_WORD;
  			BankAccount.addToBalance(this, amount);
  			earned += amount;
  			selectRandomWord();
  		}
  		else {
  			tts.speak("Sorry!", TextToSpeech.QUEUE_ADD, null);
  			numWrong[mode]++;
  			
  			int mMaxTries = Settings.getMaxTries(this);
  			
  			if (mMaxTries <= numTries) {
  				numGivenAnswer[mode]++;
  				speakCorrectAnswer();
  			}
  			else {
  				tts.speak("Try again!", TextToSpeech.QUEUE_ADD, null);
  				inputCancelled();
  				numTries++;
  			}
  		}
    }
    
    protected void speakCorrectAnswer() {
    	Log.v(TAG, "speakCorrectAnswer");
    	tts.speak("The word is " + mWord, TextToSpeech.QUEUE_ADD, null);
    	selectRandomWord();
    }
    
	/* Called when activity is paused -- when 
	 * phone call arrives.
	 */
	@Override
	public void onPause() {
		if (tts != null) {
			tts.stop();
		}
		// save values here or in destroy?  If here, reinit vars in onResume
		super.onPause();
	}
	
	/* Tells player how (s)he did overall */
	private void speakStatistics() {
		String s = "Game statistics: ";
		for (int i = 0; i < modes.length; i++) {
			if (numDisplayed[i] == 0 || (numCorrect[i] == 0 && numWrong[i] == 0 && numGivenAnswer[i] == 0))
				continue;
			
			if (numDisplayed[i] == 1) {
				String m = (i == EASY) ? "easy" : (i == INTERMEDIATE) ? "intermediate" : "hard";
				s = s + "There was won " + m + " word attempted, and ";
			}
			else {
				String m = (i == EASY) ? "easy" : (i == INTERMEDIATE) ? "intermediate" : "hard";
				s = s + "There were " + numDisplayed[i] + " " + m + " words attempted, and ";
			}
	
			if (numCorrect[i] == 1)
				s = s + "You gave won correct answer, ";
			else
				s = s + "You gave " + numCorrect[i] + " correct answers, ";
		}
		if (earned > 0)
			s = s + "You earned " + earned + " tokens!";
		if (!s.equalsIgnoreCase("Game statistics: ")) {
			tts.setOnUtteranceCompletedListener(this);
			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "bwstats");
			tts.speak(s, TextToSpeech.QUEUE_ADD, utterance);
		}
		else
			finish();
	}
	
	@Override
	public void onUtteranceCompleted(String utteranceId) {
		// Finish activity when game statistics have been spoken
		if (utteranceId.equalsIgnoreCase("bwstats")) {
			finish();
		}
		else if (utteranceId.equalsIgnoreCase("longpress")){
			speakInstructions();
		}
	}
	
	/* Speak state of game */
	private void speakState() {
		Log.i(TAG, "in speakState");
		index = -1;
		tts.setOnUtteranceCompletedListener(this);
		utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longpress");
		tts.speak("the difficulty level is set to " + modes[mode] +
				", word has " + String.valueOf(mWord.length()) + " letters, " +
				"currently on letter " + (wordIndex + 1) + " of word, " +
				"one moment while I retrieve instructions", 
				TextToSpeech.QUEUE_FLUSH, utterance);
	}
	
	/* Speak full instructions for the game */
	private void speakInstructions() {
		tts.speak(bwInstr, TextToSpeech.QUEUE_ADD, null);
//		if (index++ < bwInstr.length) {
//			Log.i(TAG, "Instructions line " + index);
//			utterance.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "longpress");
//			tts.speak(bwInstr[index], TextToSpeech.QUEUE_ADD, utterance);
//		}
//		else
//			tts.speak("End of instructions", TextToSpeech.QUEUE_ADD, null);
	}
	
    protected class BrailleWordsView extends View {
 
        private final long [] patternCorner = {40,40};
        private final long [] patternMiddle = {0,1000};
        private final Paint mPaint;    
        private int xBound;
        private int y1Bound;
        private int y2Bound;
        private int radius;
        private long longPressStartTm;
        private long longPressDuration;
       
        public BrailleWordsView(Context context) {
        	super(context);
        	longPressDuration = 3000;
        	lastRegion = -1;
        	vibe = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        	mPaint = new Paint();
        	mPaint.setColor(Color.WHITE);
        	setBackgroundColor(Color.BLACK);
        	setClickable(true);
        } 
        
        protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        	xBound = (int) w / 2;
        	y1Bound = (int) h / 3;
        	y2Bound = y1Bound * 2;
        	radius = Math.min(xBound, y1Bound)/4;
        	super.onSizeChanged(w, h, oldw, oldh);
        }
        
        @Override
        protected void onDraw(Canvas canvas) {
        	float x, y;
        	int xUnit = xBound/2;
        	int yUnit = y1Bound/2;

        	for (int i = 0; i < 6; i++) {
        		if (alphadots[whichSymbol].charAt(i) == '1') {
        			x = (i % 2 == 0) ? xUnit : xUnit + xBound;
        			y = (i < 2) ? yUnit : (i < 4) ? yUnit + y1Bound : yUnit + y2Bound;
        			canvas.drawCircle(x, y, radius, mPaint);
        		}
        	}
        }
        
        private void setVibration(int value) {
        	if(value != vibrating) {
        		switch(value) {
        		case 1:
        			Log.i(TAG,"Doing corner pattern");
        			vibe.vibrate(patternCorner,0);
        			break;
        		case -1:
        			Log.i(TAG,"Doing middle pattern");
        			vibe.vibrate(patternMiddle,0);
        			break;
        		default:
        			vibe.cancel();
        		}
        		vibrating = value;
        	}
        }
        

        @Override
        public boolean onTouchEvent(MotionEvent me) {
        	Log.v(TAG, "onTouchEvent");
        	switch(me.getAction()) {
        	case MotionEvent.ACTION_DOWN:
        		longPressStartTm = me.getDownTime();
        	case MotionEvent.ACTION_MOVE:
        		double x = me.getX();
        		double y = me.getY();
        		x/=getWidth();
        		y/=getHeight();
        		double xx = me.getRawX();
        		double yy = me.getRawY();
        		int loc = (xx < xBound) ? 0 : 1;
        		loc = (yy < y1Bound) ? loc : (yy < y2Bound) ? loc + 2 : loc + 4;
        		if (loc != lastRegion) {
        			tts.speak(regions[loc], TextToSpeech.QUEUE_FLUSH, null);
        			lastRegion = loc;
        			longPressStartTm = me.getEventTime();
        		}
        		int doit = 0;
        		for(int i = 0; i < 2; i++) {
        			for(int j = 0; j < 3; j++) {
        		    	if(alphadots[whichSymbol].charAt(i+2*j)=='0')
        		    		continue;
        				if(((int) (2*x)) == i && ((int) (3*y)) == j)
        					doit = (j==1)?(-1):(1);
        			}
        		}
        		setVibration(doit);
        		
        		// Capture long presses that occur within a single region and
        		// repeat the symbol that the user should enter.
        		if (me.getEventTime() - longPressStartTm > longPressDuration) {
        			speakState();
        			longPressStartTm = me.getEventTime();
        		}
        		break;
        	default:
        		setVibration(0);
        	}
        	return true;
        } 
    }
}

