package cs.washington.edu.VBGhost2;


import cs.washington.edu.VBGhost2.R;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public class ContinueGameMenu extends AccessibleMenu{
	private GameDB _games = null;
	private static int[] TO = {R.id.word};
	private static String[] FROM = {"_word"};
	private String TAG = "ContinueGameMenu";
	private int count; //number of continue games
	int[] gameIds;
	int[] numPlayers;
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.continuegame);
		
	}
	
    @Override
    protected void makeSelection(){
    	Log.v(TAG, "selected game: " + gameIds[lastLoc]);
    	if(numPlayers[lastLoc]==1){
    		long id = (long)gameIds[lastLoc];
    		Intent intent = new Intent(this, SinglePlayerMenu.class);
    		intent.putExtra("id", id);
    		startActivity(intent);
    		finish();
    	}else{
    		long id = (long)gameIds[lastLoc];
    		Intent intent = new Intent(this, MultiPlayerMenu.class);
    		intent.putExtra("id", id);
    		startActivity(intent);
    		finish();
    	}
		//Intent intent = new Intent();
		//intent.setAction(optionAction[lastLoc][lastSubLoc]);
		//intent.putExtra("Option", lastLoc);
		//startActivity(intent);
    }
	
	/* Read in the data for this instance of AccessibleMenu and set up the options
	 * and suboptions for the menu.
	 */
	@Override
	protected void createMenuOptions(Resources res) 
	{
		_games = new GameDB(this);
		_games.open();
		Cursor cursor = _games.getGames();
		count = cursor.getCount();
		startManagingCursor(cursor);
		_games.close();

	    LinearLayout layout = (LinearLayout)findViewById(R.id.accessiblemenu_options_layout);
	    layout.setOnTouchListener(this);
	    LinearLayout omlayout = (LinearLayout)findViewById(R.id.accessiblemenu_offmenu_layout);
	    omlayout.setOnTouchListener(this);
	    
		gameIds = new int[count];
		numPlayers = new int[count];
		String[] optionText = new String[count];
		String[] optionTag = new String[count];
		int ii=0;
		while(cursor.moveToNext()){
			gameIds[ii]= cursor.getInt(0);
			int players = cursor.getInt(1);
			String playerString = "";
			if(players ==1){
				playerString = "Single Player: ";
				numPlayers[ii]= 1;
			}else{
				playerString = "MultiPlayer: ";
				numPlayers[ii]= 2;
			}
			String wordFrag = cursor.getString(3);
			optionText[ii] =  wordFrag;
			optionTag[ii] = playerString + wordFrag;
			Log.v(TAG, "GAME:  players: "+ players + " : " + wordFrag);
			ii++;
		}
	
		String arrayName = getPackageName() + ":array/" + TAG.toLowerCase() + "_option_attributes_array";
	    int id = res.getIdentifier(arrayName, null, null);
		if (id == 0) {
			Log.v(TAG, "Cannot find resource: " + arrayName);
			return;
		}
	    TypedArray optionAttributes = res.obtainTypedArray(id);
	 
	    // Convert the dps to pixels
		float height = optionAttributes.getInt(4, 1);
		height = height * dpScale + 0.5f;
		if (height < dpsOPTION_MINIMUM_HEIGHT) height = dpsOPTION_MINIMUM_HEIGHT;
		
		float textSize = optionAttributes.getFloat(0, 1);
		textSize = (textSize * res.getDisplayMetrics().scaledDensity) + 0.5f;
		if (textSize < spOPTION_MINIMUM_TEXT_SIZE) textSize = spOPTION_MINIMUM_TEXT_SIZE;
		
	    
	    numOptions = optionText.length + 1;       
	    options =  new Button[numOptions];
		optionSuboptions = new String[numOptions][];
		optionAction = new String[numOptions][];
		intentExtras = new String[numOptions][][];
		
		for (int i = 0; i < numOptions - 1; i++) {
			options[i] = new Button(self);
			options[i].setText(optionText[i]);
			Log.v(TAG, "text: " + options[i].getText());
			options[i].setOnTouchListener(this);
			options[i].setFocusableInTouchMode(true);
			options[i].setFocusable(true);
			// speak the tag value, which has phonetic spelling for 
			// some letters that aren't pronounced correctly if just a single
			// letter is given
			final int t = i;
			final String tag = optionTag[t];   
			final int text = optionAttributes.getColor(1, R.color.text);
			final int hilite = optionAttributes.getColor(3, R.color.text_highlight);
			options[i].setTag(tag);
			options[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						Log.v(TAG, "Option " + t + " has focus.");
						if (ttsOn && focusOption != t) tts.speak(tag, TextToSpeech.QUEUE_FLUSH, null);
						options[t].setTextColor(hilite);
						focusOption = t;
					}
					else {
						options[t].setTextColor(text);
					}
				}
			});
			
			
			// set attributes
			options[i].setTextSize(textSize);
			options[i].setTextColor(text);
			options[i].setBackgroundColor(optionAttributes.getColor(2, 0));
			options[i].setHeight((int)height);
			options[i].setHighlightColor(hilite);
			layout.addView(options[i]);
		}
				
		// set up end of menu button
		options[numOptions - 1] = (Button)findViewById(R.id.no_more_options);
		options[numOptions - 1].setBackgroundColor(optionAttributes.getColor(2, 0));
		options[numOptions - 1].setOnTouchListener(this);
		options[numOptions - 1].setFocusableInTouchMode(true);
		options[numOptions - 1].setFocusable(true);
		final int t = numOptions - 1;
		options[numOptions - 1].setOnFocusChangeListener(new View.OnFocusChangeListener() {
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus && ttsOn) {
					Log.v(TAG, "Option " + t + " has focus.");
					tts.speak((String)options[t].getTag(), TextToSpeech.QUEUE_FLUSH, null);
				}
			}
		});
		optionSuboptions[numOptions - 1] = null;
	}
}