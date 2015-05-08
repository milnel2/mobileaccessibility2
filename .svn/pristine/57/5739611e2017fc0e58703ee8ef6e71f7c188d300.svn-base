package edu.washington.cs.hangman;

import android.content.Context;
import android.graphics.Color;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;

public class Hangman {
	public static final int CHECK_TTS_AVAILABILITY = 0;

	public static final int TEXT_SIZE = 65;
	public static final int UNSELECTED_BG_COLOR = Color.DKGRAY;
	public static final int SELECTED_BG_COLOR = Color.BLUE;
	public static final int UNSELECTED_FG_COLOR = Color.GRAY;
	public static final int SELECTED_FG_COLOR = Color.WHITE;
	public static final float BTN_PADDING = 5;
	public static final float BTN_STROKE_WIDTH = 5;

	interface HangmanOnInitListener {
		public void onInit();
	}
	
	private static BrailleTable _brailleTable = null;
	
	private static TextToSpeech _tts;
	private static boolean _ttsReady = false;

	
	
	public static void init(Context context, final HangmanOnInitListener hmOnInitListener) {
		OnInitListener initListener = new OnInitListener() {
			public void onInit(int status) {
				_ttsReady = true;
				hmOnInitListener.onInit();
			}
		};
//		
		// Initialize the text to speech
		_tts = new TextToSpeech(context, initListener);

		// Create the braille table
		_brailleTable = new BrailleTable(context, R.raw.brl);
	}
	
	public static void cleanup() {
		if(_tts != null){
			_tts.stop();
			_tts.shutdown();
		}
	}
	
	public static boolean isTtsReady() {
		return _ttsReady;
	}

	public static TextToSpeech getTts() {
		return _tts;
	}
	
	public static BrailleTable getBrailleTable() {
		return _brailleTable;
		
	}
}
