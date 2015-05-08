/** The Place class encompasses both the interface for making a
 * new Place and editing an existing Place.
 * 
 * BUGS: The current reference to the default audio recording activity is
 * incorrect, at least for the Acer Iconia Tab. Substituting the correct
 * name should clear it up. Because of this, saving the audio capture
 * has not been debugged and likely does not work.
 */

package cs.washington;

import java.io.File;
import java.io.IOException;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ToggleButton;
import android.widget.ImageView.ScaleType;

public class Place extends Activity {
	private CatApplication application;
	private ImageView picField;
	private Button camButton;
	private Button savePlaceButton;
	private Button clearButton;
	private Button newButton;
	private ToggleButton speechToggle;
	private Button saveButtonButton;
	private Button audioButton;
	private EditText textBox;
	private LinearLayout buttonPane;
	private ImageButton curButton;
	private Button resetButtonButton;
	private TextToSpeech mTts;
	private DbManager mdb;
	private FrameLayout curFrame;
	private boolean buttonMaking = false;
	private boolean editMode = false; //Is this place being edited?
	final static String TAG = "Pic2Speech_Place"; // For error logging
	private static final int ACTIVITY_TAKE_PICTURE = 0;
	private static final int ACTIVITY_RECORD_AUDIO = 1;
	private String picloc; //The full file path of the Place imag
	
	private int startX = 0;
	private int startY = 0;
	private int curLeft;
	private int curTop;
	private int curWidth;
	private int curHeight;
	private String mAudioUri = null;
	
    public void onAttachedToWindow() {
    	super.onAttachedToWindow();
    	Window window = getWindow();
    	window.setFormat(PixelFormat.RGBA_8888);
    }
    
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.application = (CatApplication) this.getApplication();
		mdb = this.application.getDatabaseManager();
		setContentView(R.layout.place);
		mTts = this.application.getTextToSpeech();

		//If editing an existing Place, loads image
		String place = null;
		Bundle extras = getIntent().getExtras();
        if (extras != null) {
            place = extras.getString("sourceImage");
        }
        
        //Set listeners to the views
		picField = (ImageView)	findViewById(R.id.placepic);
		picField.setImageResource(R.drawable.blank);
		picField.setOnTouchListener(mTouchListener);
		camButton = (Button) findViewById(R.id.place_pic_button);
		camButton.setOnClickListener(mClickListener);
		savePlaceButton = (Button) findViewById(R.id.place_savePlace_button);
		savePlaceButton.setOnClickListener(mClickListener);
		newButton = (Button) findViewById(R.id.place_newButton_button);
		newButton.setOnClickListener(mClickListener);
		clearButton = (Button) findViewById(R.id.place_deletePlace_button);
		clearButton.setOnClickListener(mClickListener);
		buttonPane = (LinearLayout) findViewById(R.id.place_buttonPane);
		speechToggle = (ToggleButton) findViewById(R.id.place_speech_toggle);
		speechToggle.setOnClickListener(mClickListener);		
		saveButtonButton = (Button) findViewById(R.id.place_saveButton_button);
		saveButtonButton.setOnClickListener(mClickListener);
		resetButtonButton = (Button) findViewById(R.id.place_deleteButton_button);
		resetButtonButton.setOnClickListener(mClickListener);
		audioButton = (Button) findViewById(R.id.place_speech_audio);
		audioButton.setOnClickListener(mClickListener);
		textBox = (EditText) findViewById(R.id.place_speech_text);
		
		//Stop the keyboard from popping up when autofocusing on the text box
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(textBox.getWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY);
		
		//Loads data when editing existing Place
        if (place != null){	
        	picloc = place;
        	editMode = true;
        	BitmapFactory.Options op = new BitmapFactory.Options();
			op.inSampleSize = 4;
        	Bitmap myBitmap = BitmapFactory.decodeFile(place, op);
        	picField.setImageBitmap(myBitmap);
        	Cursor c = mdb.fetchButtonsForPlace(place);
        	int total = 0;
        	Context context = getApplicationContext();	
        	if(c != null){
    			total = c.getCount();
    			for(int i = 0; i < total; i++){
    				final FrameLayout container = new FrameLayout(context);
	        	    FrameLayout layout = (FrameLayout) findViewById(R.id.select_frame);
	        	    layout.addView(container);
	        	    final ImageButton button = new ImageButton(context);
	        	    button.setAlpha(0.4f);
	        	    button.setId(555);
	        	    final String tex = c.getString(5);
	        	    final String au = c.getString(6);
	        	    button.setId(c.getInt(0));
	        	    //Creates OnClickListener that speaks button name.
	        	    button.setOnClickListener(new OnClickListener()
			        {
						String audio = au;
						String text = tex;
			            @Override
			            public void onClick(View v)
			            {
			            	if(audio == null){
								mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
							}else{
								MediaPlayer mp = new MediaPlayer();
				                try {
									mp.setDataSource(audio);
								} catch (IllegalArgumentException e) {
									e.printStackTrace();
								} catch (IllegalStateException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
				                try {
									mp.prepare();
								} catch (IllegalStateException e) {
									e.printStackTrace();
								} catch (IOException e) {
									e.printStackTrace();
								}
				                mp.start();
							}
			               
			            }

			        });
	        	    //Creates onLongClickListener that enables editing of button
	        	    button.setOnLongClickListener(new OnLongClickListener(){
	        	    	ImageButton but = button;
						@Override
						public boolean onLongClick(View v) {
							camButton.setVisibility(8);
							newButton.setVisibility(8);
							clearButton.setVisibility(8);
							savePlaceButton.setVisibility(8);
							resetButtonButton.setVisibility(0);
							speechToggle.setVisibility(0);
							textBox.setVisibility(0);
							textBox.setText(tex);
							saveButtonButton.setVisibility(0);
							curFrame = (FrameLayout) but.getParent();
							curButton = but;
							return true;
						}
	        	    	
	        	    });
	        	    button.setLayoutParams(new LayoutParams(c.getInt(3), c.getInt(4)));
	        	    container.setPadding(c.getInt(1),
	        	                          c.getInt(2),
	        	                          0,
	        	                          0);
	        	    container.addView(button);
	        	    c.moveToNext();  
    			}
    			camButton.setVisibility(8);
				newButton.setVisibility(0);
				clearButton.setVisibility(0);
				savePlaceButton.setVisibility(0);
    		}
        }
	}
	
	//Sets the OnClickListeners
	private View.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			//When "Take Picture" is clicked, starts default camera app
			case R.id.place_pic_button:
				Intent i = new Intent("android.media.action.IMAGE_CAPTURE");
				File dir = new File(Environment.getExternalStorageDirectory() + File.separator + "Pic2Speech");
				if(!dir.exists()){
					dir.mkdir();
				}
				File f;
				String randString = Long.toHexString(Double.doubleToLongBits(Math.random()));
				f = new File(dir + "/" + "place" + randString + ".png");
				picloc = f.getAbsolutePath();
				Log.d(TAG, "Button name: " + f.getName());
				i.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
				startActivityForResult(i, ACTIVITY_TAKE_PICTURE);
				break;
			//When "Save Place" is clicked, puts image in db
			case R.id.place_savePlace_button:
				//If in edit mode, Place already exists in place db.
				if(!editMode){
					mdb.addPlace("Testing", picloc);
					Log.d(TAG, "Add place: _" + picloc);
				}
				finish();
				break;
			//When Reset Button is pressed, clears button, enabling user to drag out a new one
			case R.id.place_deleteButton_button:
				FrameLayout layout = (FrameLayout) findViewById(R.id.select_frame);
				layout.removeView(curFrame);
				buttonMaking = true;
				break;
			//When Delete Place is pressed, clear all views back to default, delete place image 
			//and buttons from database
			case R.id.place_deletePlace_button:
				FrameLayout layout2 = (FrameLayout) findViewById(R.id.select_frame);
				layout2.removeAllViews();
				ImageView blank = new ImageView(getApplicationContext());
				blank.setLayoutParams(new LayoutParams(1000, LayoutParams.MATCH_PARENT));
				blank.setScaleType(ScaleType.CENTER_CROP);
				blank.setId(R.id.placepic);
				picField = blank;
				picField.setImageResource(R.drawable.blank);
				picField.setOnTouchListener(mTouchListener);
				layout2.addView(blank);
				mdb.deleteButtonsForPlace(picloc);
				mdb.deletePlace(picloc);
				camButton.setVisibility(0);
				newButton.setVisibility(8);
				clearButton.setVisibility(8);
				savePlaceButton.setVisibility(8);
				break;
			//When New Button is pressed, shows button-making buttons, hides general buttons
			//Enables creation of buttons with dragging
			case R.id.place_newButton_button:
				buttonMaking = true;
				newButton.setVisibility(8);
				clearButton.setVisibility(8);
				savePlaceButton.setVisibility(8);
				resetButtonButton.setVisibility(0);
				speechToggle.setVisibility(0);
				((EditText) findViewById(R.id.place_speech_text)).setVisibility(0);
				saveButtonButton.setVisibility(0);
				break;
			//When Save Button is pressed, saves button info to db or edits existing info
			case R.id.place_saveButton_button:
				//If button already exists, update existing database entry
				if(mdb.buttonExist(curButton.getId())){
					mdb.updateButton(curButton.getId(), textBox.getText().toString(), mAudioUri);
				//Otherwise, create a new db entry for the button, and set the button ui's ID 
				//so it can be edited later
				}else{
					curButton.setId(mdb.addButton(picloc, curLeft, curTop, curWidth, curHeight, textBox.getText().toString(), mAudioUri));				
				}
				//Hides the new button interface, brings up the general Place interface
				newButton.setVisibility(0);
				clearButton.setVisibility(0);
				savePlaceButton.setVisibility(0);
				resetButtonButton.setVisibility(4);
				speechToggle.setVisibility(4);
				textBox.setVisibility(4);
				saveButtonButton.setVisibility(4);
				
				//Set OnClickListener for the button to speak text/play audio
				curButton.setOnClickListener(new OnClickListener()
		        {
					String audio = mAudioUri;
					String text = textBox.getText().toString();
		            @Override
		            public void onClick(View v)
		            {
		            	if(audio == null){
							mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
						}else{
							MediaPlayer mp = new MediaPlayer();
			                try {
								mp.setDataSource(audio);
							} catch (IllegalArgumentException e) {
								e.printStackTrace();
							} catch (IllegalStateException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
			                try {
								mp.prepare();
							} catch (IllegalStateException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
			                mp.start();
						}
		               
		            }

		        });
				//Sets OnLongClickListener to show edit button interface
				curButton.setOnLongClickListener(new OnLongClickListener(){
        	    	ImageButton but = curButton;
					@Override
					public boolean onLongClick(View v) {
						camButton.setVisibility(8);
						newButton.setVisibility(8);
						clearButton.setVisibility(8);
						savePlaceButton.setVisibility(8);
						resetButtonButton.setVisibility(0);
						speechToggle.setVisibility(0);
						textBox.setVisibility(0);
						textBox.setText(textBox.getText());
						saveButtonButton.setVisibility(0);
						curFrame = (FrameLayout) but.getParent();
						curButton = but;
						return true;
					}
        	    	
        	    });
				break;
			//When the toggle is clicked, switch between 
			case R.id.place_speech_toggle:
				if (speechToggle.isChecked()) {
					textBox.setVisibility(8);
					textBox.setVisibility(0);
				} else {
					textBox.setVisibility(0);
					textBox.setVisibility(8);
				}
				break;
			//BROKEN! Default recorder activity name not correct for Iconia Tab, don't know about other tablets
			//Intention is to open default sound recorder app
			case R.id.place_speech_audio:
				i = new Intent("android.provider.MediaStore.RECORD_SOUND");
				startActivityForResult(i, ACTIVITY_RECORD_AUDIO);
				break;
			//If the button hasn't yet been saved, just says "button clicked" when clicked
			case 555:
				mTts.speak("button clicked", TextToSpeech.QUEUE_FLUSH, null);
				break;
			default: 
				break;
			}
		}
	};
	
	//Code for creating a button via drag
	private OnTouchListener mTouchListener = new View.OnTouchListener() {
		public boolean onTouch(View v, MotionEvent m) {
			switch (v.getId()) {
				case R.id.placepic:
					Context context = getApplicationContext();					
					int a = m.getAction();
					 switch (a) {
				        case MotionEvent.ACTION_DOWN:
				        	if(buttonMaking){
								startX = (int) m.getX();
								startY = (int) m.getY();

								FrameLayout container = new FrameLayout(context);
				        	    curFrame = container;
				        	    FrameLayout layout = (FrameLayout) findViewById(R.id.select_frame);
				        	    layout.addView(curFrame);
								curFrame.setPadding((int)startX, (int)startY, 0, 0);							
				        	}
							break;
				        case MotionEvent.ACTION_UP:
				        	if(buttonMaking){
					        	curFrame.removeAllViews();
					        	float endX = m.getX();
					        	float endY = m.getY();
				        	    curButton = new ImageButton(context);
				        	    curButton.setAlpha(0.4f);
				        	    curButton.setId(555);
				        	    curButton.setOnClickListener(mClickListener);
				        	    curLeft = (int) Math.min(endX, startX);
				        	    curTop = (int) Math.min(endY, startY);
				        	    curWidth = (int) Math.abs(endX-startX);
				        	    curHeight = (int) Math.abs(endY-startY);
				        	    curButton.setLayoutParams(new LayoutParams(curWidth, curHeight));
				        	    curFrame.setPadding(curLeft,
				        	                          curTop,
				        	                          0,
				        	                          0);
				        	    curFrame.addView(curButton);
				        	    Log.d(TAG, "Add button: Left: " + curLeft + " Top: " + curTop);  
				        	    buttonMaking = false;
				        	}
				        	break;
				        case MotionEvent.ACTION_MOVE:
				        	if(buttonMaking){
					        	ImageView selectBox = new ImageView(context);
					        	int curX = (int) m.getX();
					        	int curY = (int) m.getY();
					        	selectBox.setLayoutParams(new LayoutParams(Math.abs(startX - curX), (Math.abs(startY - curY))));
					        	selectBox.setAlpha(0.4f);
					        	selectBox.setBackgroundResource(R.drawable.selectbox);
					        	curFrame.removeAllViews();
					        	curFrame.setPadding(Math.min(startX, curX), (Math.min(startY, curY)), 0, 0);
					        	curFrame.addView(selectBox);
				        	}
				        	break;
					 }				
			}
			return true;
		}
	};
	
	//Processes results from taking a picture and recording audio
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_TAKE_PICTURE:
			if (resultCode == Activity.RESULT_OK) {
				Bitmap myBitmap = BitmapFactory.decodeFile(picloc);
				picField.setImageBitmap(myBitmap);
				camButton.setVisibility(8);
				newButton.setVisibility(0);
				clearButton.setVisibility(0);
				savePlaceButton.setVisibility(0);
			}
			break;
		case ACTIVITY_RECORD_AUDIO:
			if (resultCode == Activity.RESULT_OK) {
				mAudioUri = data.getDataString();
				if(mAudioUri.compareTo("") != 0) {
					MediaPlayer mp = new MediaPlayer();
					try {
						mp.setDataSource(mAudioUri);
					} catch (IllegalArgumentException e) {
						e.printStackTrace();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					try {
						mp.prepare();
					} catch (IllegalStateException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
					mp.start();
				}
			}
		}
	}
}
