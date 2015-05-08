package edu.washington.cs.kittens;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

/**
 * This class for creating a new tile
 */

public class NewTile extends Activity {
	private static final String TAG = "Pic2Speech_v1";	// For error logging
	private TextToSpeech mTts;
	private ImageView picField;
	private TextView nameField;
	private TextView textfield;
	private TextView parentField;
	private ToggleButton isFolderField;
	private ToggleButton toggleSpeechField;
	private Button startCamera;
	private Button startSpeech;
	private DbManager mdb;
	private CatApplication application;
	private Context context;
	private String mAudioUri = null;
	private Boolean picFieldTaken = false;
	
	private static final int ACTIVITY_TAKE_PICTURE = 0;
	private static final int ACTIVITY_RECORD_AUDIO = 1;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_tile);
		this.context = this;

		this.application = (CatApplication) this.getApplication();
		mdb = this.application.getDatabaseManager();
		mTts = this.application.getTextToSpeech();

		View saveButton = findViewById(R.id.new_tile_save);
		saveButton.setOnClickListener(mClickListener);
		nameField = (TextView) findViewById(R.id.new_tile_name);
		textfield = (TextView) findViewById(R.id.new_tile_speech_text);
		parentField = (TextView) findViewById(R.id.new_tile_parent_folder);
		parentField.setOnClickListener(mClickListener);
		startSpeech = (Button) findViewById(R.id.new_tile_speech_record);
		startSpeech.setOnClickListener(mClickListener);
		toggleSpeechField = (ToggleButton) findViewById(R.id.new_tile_speech_toggle);
		toggleSpeechField.setOnClickListener(mClickListener);
		picField = (ImageView) findViewById(R.id.new_tile_pic);
		picField.setImageResource(R.drawable.blank);
		startCamera = (Button) findViewById(R.id.new_tile_pic_button);
		startCamera.setOnClickListener(mClickListener);
		isFolderField = (ToggleButton) findViewById(R.id.new_tile_isfolder);
	}
	
    /**
     * for buttons to perform when they are "short" clicked
     */
	private View.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.new_tile_pic_button:
				Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(i, ACTIVITY_TAKE_PICTURE);
				break;
			case R.id.new_tile_parent_folder:
				showDialog(1);
				break;
			case R.id.new_tile_speech_toggle:
				if (toggleSpeechField.isChecked()) {
					((EditText) findViewById(R.id.new_tile_speech_text)).setVisibility(8);
					((Button) findViewById(R.id.new_tile_speech_record)).setVisibility(0);
				} else {
					((EditText) findViewById(R.id.new_tile_speech_text)).setVisibility(0);
					((Button) findViewById(R.id.new_tile_speech_record)).setVisibility(8);
				}
				break;
			case R.id.new_tile_speech_record:
				i = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
				startActivityForResult(i, ACTIVITY_RECORD_AUDIO);
				break;
			case R.id.new_tile_save:
				String name = nameField.getText().toString();
				String text = textfield.getText().toString();
				String parentfolder = parentField.getText().toString().toLowerCase();
				boolean isFolder = isFolderField.isChecked();
				String type = "tile";
				if(isFolder){
					type = "category";
				}
				String s = Integer.toString(application.getImagect());
				if(picFieldTaken){
					mdb.addTile(name, text, s, parentfolder, type, "Y", mAudioUri);
					
				}else{
					mdb.addTile(name, text, "blank", parentfolder, type, "N", mAudioUri);
				}
				application.incrementImagect();
				Log.d(TAG, "Add tile: _" + name + "_" + text + "_" + s + "_" + parentfolder + "_" + type);
				application.setChanged(parentfolder);
				finish();
				break;
			default: 
				break;
			}
		}
	};

	/**
	 * Creates a dialog message and displays it
	 */
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case 1:
			// Pop up a dialog to select a parent category
			final String[] categories = mdb.getCategories();
			AlertDialog.Builder builder1 = new AlertDialog.Builder(context);
			builder1.setTitle("Select a parent category");
			builder1.setItems(categories, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item1) {
					parentField.setText(categories[item1]);
				}
			});
			dialog = builder1.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}
	
	/**
	 * when an activity is finished, it processes the given data
	 * 
	 * @param requestCode, a code to recognize the activity just finished
	 * @param resultCode, a code to indicate the result of the activity
	 * @param data, an intent given from the activity just finished
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case ACTIVITY_TAKE_PICTURE:
			if (resultCode == Activity.RESULT_OK) {
				// Display image received on the view
				 Bundle b = data.getExtras(); // Kept as a Bundle to check for other things in my actual code
				 Bitmap picField = (Bitmap) b.get("data");
				 File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + application.getImagect() + ".png");
				 try {
					 FileOutputStream out = new FileOutputStream(f.getAbsolutePath());
					 picField.compress(Bitmap.CompressFormat.PNG, 90, out);
					 out.flush();
				 } catch (Exception e) {
					 e.printStackTrace();
				 }
				 this.picField.setImageBitmap(picField);
				 picFieldTaken = true;
			 }
			 else if (resultCode == Activity.RESULT_CANCELED) {
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
			}else{
				
			}
			break;
		}
	}
}