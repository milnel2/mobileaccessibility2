package cs.washington.edu.buddies;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;

/* UI for Creating and updating pet */
public class PetGUI extends Activity implements RadioGroup.OnCheckedChangeListener, 
	View.OnTouchListener, OnClickListener {
	
		//for GUI
		private TextView name;
		private Button change_name_button;
		private RadioGroup gender_radio;
		private RadioGroup breed_radio;
//		private RadioGroup bark_radio;
		private Button finish_button;
		private Button blankBtn;
		
		private TextToSpeech tts;
		
		//for speaking status
		private String petName;
		private String gender;
		private String breed;
//		private String bark;
		
		private static final String TAG = "PetData";
		private View[] views;
		private int lastLoc = -1;
		private boolean init = true;
		private String parent;
		private boolean gesturesMode;

	    
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.petgui);
	        
			tts = BrailleBuddiesMenu.tts;
			tts.speak("Pet Settings displayed, Touch to hear setting, tap it to enable it", TextToSpeech.QUEUE_FLUSH, null);
			
			parent = this.getIntent().getStringExtra("origin");
			gesturesMode = this.getIntent().getBooleanExtra("gesturesMode", true);
			getUIElements();
			
			// prevent focus on buttons
	        blankBtn.requestFocus();
			setUIValues();
			Toast.makeText(this, "Touch to hear setting, tap to enable it", Toast.LENGTH_LONG).show();
			speakPetData();
			loadViews();
			init = false;
		}
		
		/* Get the references of the UI elements */
		private void getUIElements() {
			name = (TextView)findViewById(R.id.pet_name);
			change_name_button = (Button)findViewById(R.id.change_name_button);
			change_name_button.setOnClickListener(this);
			gender_radio = (RadioGroup)findViewById(R.id.radio_gender);
			gender_radio.setOnCheckedChangeListener(this);
			breed_radio = (RadioGroup)findViewById(R.id.radio_breed);
			breed_radio.setOnCheckedChangeListener(this);
//			bark_radio =(RadioGroup)findViewById(R.id.radio_bark);
//			bark_radio.setOnCheckedChangeListener(this);
			finish_button = (Button)findViewById(R.id.finish_button);
			finish_button.setOnClickListener(this);
			blankBtn = (Button)findViewById(R.id.keypad_blank);
		}
		
		/* Set the UI elements to the current settings values */
		private void setUIValues () {
			name.setText(PetData.getName(this));
			// temp code to put in name until editor is in place
			if (name.getText().equals(""))
				name.setText("Fido");
			
			switch (PetData.getGender(this)) {
			case PetData.FEMALE:
				gender_radio.check(R.id.radio_female);
				break;
			case PetData.MALE:
			default:
				gender_radio.check(R.id.radio_male);
				break;
			}
			
//			switch (PetData.getBark(this)) {
//			case PetData.HIGH:
//				bark_radio.check(R.id.radio_high);
//				break;
//			case PetData.LOW:
//				bark_radio.check(R.id.radio_low);
//				break;
//			case PetData.MIDDLE:
//			default:
//				bark_radio.check(R.id.radio_middle);
//				break;
//			}
			
			switch (PetData.getBreed(this)) {
			case PetData.POMERANIAN:
				breed_radio.check(R.id.radio_pomeranian);
				break;
			case PetData.PITBULL:
				breed_radio.check(R.id.radio_pitbull);
				break;
			case PetData.RETRIEVER:
			default:
				breed_radio.check(R.id.radio_retriever);
				break;
			}
			
		}
		
		/* Speak all current attributes */
		private void speakPetData() {
			petName = PetData.getName(this);
			breed = (PetData.getBreed(this) == PetData.POMERANIAN) ? "Pomeranian" :
				(PetData.getBreed(this) == PetData.PITBULL) ? "Pitbull" : "Golden Retriever";
//			bark = (PetData.getBark(this) == PetData.HIGH) ? "high" :
//				(PetData.getBark(this) == PetData.LOW) ? "low" : "medium";
			gender = (PetData.getGender(this) == PetData.FEMALE) ? "female" : "male";
			
			tts.speak("Your dog's name is " + petName + ", it is a " + gender +
					", it's breed is " + breed /*+ ", and it has a " + bark +
					" pitched bark"*/, TextToSpeech.QUEUE_ADD, null);
		}
		
		/* Loads in Views for UI elements so we can figure out which one 
		 * is being touched at any time.
		 */
	    private void loadViews() {
	    	views = new View[10];
        	views[0] = name;
        	views[1] = (Button)findViewById(R.id.change_name_button);
        	views[2] = (TextView)findViewById(R.id.gender_title);
        	views[3] = (RadioButton)findViewById(R.id.radio_male);
        	views[4] = (RadioButton)findViewById(R.id.radio_female);
        	views[5] = (TextView)findViewById(R.id.breed_title);
        	views[6] = (RadioButton)findViewById(R.id.radio_pomeranian);
        	views[7] = (RadioButton)findViewById(R.id.radio_retriever);
        	views[8] = (RadioButton)findViewById(R.id.radio_pitbull);
//        	views[9] = (TextView)findViewById(R.id.bark_title);
//        	views[10] = (RadioButton)findViewById(R.id.radio_high);
//        	views[11] = (RadioButton)findViewById(R.id.radio_middle);
//        	views[12] = (RadioButton)findViewById(R.id.radio_low);
        	views[9] = (Button)findViewById(R.id.finish_button);
	    	for (int i = 0; i < views.length; i++) {
	    		String str = (String)views[i].getTag();
	    		if (views[i] == name)
	    			str = str + " " + name.getText();
	    		final String tag = str;
	    		views[i].setFocusableInTouchMode(true);
	    		views[i].setOnTouchListener(this);
	    		views[i].setOnFocusChangeListener(new View.OnFocusChangeListener() {
	 
	    			@Override
	    			public void onFocusChange(View v, boolean hasFocus) {
	    				if (hasFocus && !init) {
		    				tts.speak(tag, TextToSpeech.QUEUE_FLUSH, null);
	    				}
	    			}
	    		});
	    	}
	    }
	    	
		/* Save the radio group values when changed */
		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {
			String text = "";
			boolean success = false;
			if (group == gender_radio) {
				if (checkedId == R.id.radio_male){
					text = "Selected male";
					success = PetData.setGender(this, PetData.MALE);
				} else  {
					text = "Selected female";
					success = PetData.setGender(this, PetData.FEMALE);
				}
//			} else if (group == bark_radio){
//				if (checkedId == R.id.radio_high){
//					text = "Selected high pitched bark";
//					success = PetData.setBark(this, PetData.HIGH);
//				} else if (checkedId == R.id.radio_medium){
//					text = "Selected medium pitched bark";
//					success = PetData.setBark(this, PetData.MIDDLE);
//				} else  {
//					text = "Selected low pitched bark";
//					success = PetData.setBark(this, PetData.LOW);
//				}			
			} else {
				if (checkedId == R.id.radio_pomeranian){
					text = "Selected Pomeranian";
					success = PetData.setBreed(this, PetData.POMERANIAN);
				} else if (checkedId == R.id.radio_retriever){
					text = "Selected Golden retriever";
					success = PetData.setBreed(this, PetData.RETRIEVER);
				} else  {
					text = "Selected Pitbull";
					success = PetData.setBreed(this, PetData.PITBULL);
				}			
			}
			
			if (!text.equals("") && !init)
				tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}

		@Override
	    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {  
	    	if(keyCode == KeyEvent.KEYCODE_MENU){
	    		tts.speak("Menu kee pressed", TextToSpeech.QUEUE_FLUSH, null);
	    	}
	    	else if (keyCode == KeyEvent.KEYCODE_BACK) {
	    		tts.speak("Back kee pressed, returning", TextToSpeech.QUEUE_FLUSH, null);
	    		return super.onKeyDown(keyCode, keyEvent);
	    	}
	    	else {
	    		return super.onKeyDown(keyCode, keyEvent);
	    	}
	    	Log.v(TAG, "onKeyDown");
	    	return true;
	    }
		
		/* Called for touches that are not initial button touches */
		@Override
		public boolean onTouchEvent(MotionEvent me) {
			Log.v(TAG, "onTouchEvent");
			if (me.getAction() == MotionEvent.ACTION_DOWN) {
				findKey(me);
			} 
			// Only process movement events that occur more than a
			// predetermined interval (in ms) apart to improve performance
			else if (me.getAction() == MotionEvent.ACTION_MOVE) {
				findKey(me);
			} 
			return false;
		}

		/* Called for touches inside the button display */
		@Override
		public boolean onTouch(View v, MotionEvent me) {
			//super.onTouchEvent(me);
			Log.v(TAG, "onTouch");
			if (me.getAction() == MotionEvent.ACTION_DOWN) {
				findKey(me);
			} 
			// Only process movement events that occur more than a
			// predetermined interval (in ms) apart to improve performance
			else if (me.getAction() == MotionEvent.ACTION_MOVE) {
				findKey(me);
			} 
			
			return false;
		}

		/* Locates the button on which the motion event occurred 
		 * and gives focus to that button.
		 */
		private boolean findKey(MotionEvent me) {
			double y = me.getRawY();
			double x = me.getRawX();
			int[] loc = new int[2];
			int[] dim = new int[2];
			for (int i = 0; i < views.length; i++) {
				views[i].getLocationOnScreen(loc);
				dim[0] = views[i].getWidth();
				dim[1] = views[i].getHeight();
				// If the motion event goes over the button, have the button request focus
				if (y <= (loc[1] + dim[1]) /*&& x <= (loc[0] + dim[0])*/) {
					if (i != lastLoc) {
						views[i].requestFocus();
						lastLoc = i;
					}
					return true;
				}
			}
			return false;
		}

		@Override
		public void onClick(View v) {
			if (v == finish_button) {
				if (parent.equals("menu")) {
					Intent intent = new Intent(this, Game.class);
					// pass on gestures more to new game
					intent.putExtra("gesturesMode", gesturesMode);
					intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
					startActivityForResult(intent, BrailleBuddiesMenu.LAST_MODE);
				}
				// return result flag to tell originating game to update pet info
				else {
//					Intent intent = new Intent();
//				    setResult(RESULT_OK, intent);
				    finish();
				}
			}
			else if (v == change_name_button) {
				Intent input = new Intent(this, Keypad.class);
		    	input.putExtra("mode", BrailleWords.MULTIKEY_MODE - 1);
		       	startActivityForResult(input, BrailleWords.INPUT_ANSWER);
		    	Log.v(TAG, "displayKeypad - activity started");
			}
		}			
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	Log.v(TAG, "onActivityResult; requestCode = " + requestCode + ", resultCode = " + resultCode);
    	// returned from input keypad for name edit
    	if (requestCode == BrailleWords.INPUT_ANSWER) {
    		if (resultCode == BrailleWords.RESULT_OK) {
	    		String s = data.getStringExtra("answer");
	    		name.setText(s);
	    		PetData.setName(this, s);
	    		tts.speak("Your pet's name has been set to " + s.toLowerCase(), TextToSpeech.QUEUE_FLUSH, null);
    		}
    		else 
    			tts.speak("Pet settings displayed", TextToSpeech.QUEUE_FLUSH, null);
    	}
    	// returned from game so pass gesture mode setting back to main menu
    	else if (requestCode == BrailleBuddiesMenu.LAST_MODE) {
    		if (resultCode == RESULT_OK){
    			gesturesMode = data.getBooleanExtra("gesturesMode", true);
    			Intent intent = new Intent();
    		    intent.putExtra("gesturesMode", gesturesMode);
    		    setResult(RESULT_OK, intent); 
    		}
    		finish();
    	}
    	else {
    		super.onActivityResult(requestCode, resultCode, data);
    	}
    }
}

	

