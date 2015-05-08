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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
 * This class is for editing a tile.
 */


public class EditTile extends Activity {
	private static final String TAG = "Pic2Speech_v1";	// For error logging
	private TextToSpeech mTts;
	
	private DbManager mdb;
	private CatApplication application;
	private Context context;
	private String title;
	private String id;
	private String pic;
	private String type;
	private String mAudioUri = null;
	private static final int ACTIVITY_TAKE_PICTURE = 0;
	private static final int ACTIVITY_RECORD_AUDIO = 1;

	private TextView nameField;
	private ToggleButton speechToggle;
	private TextView speechField;
	private ImageView picField;
	private TextView parentField;
	private Button startSpeech;
	private ToggleButton isFolderField;
	private Button startCamera;
	private Boolean picFieldTaken = false;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_tile);
		this.context = this;

		this.application = (CatApplication) this.getApplication();
		mdb = this.application.getDatabaseManager();
		mTts = this.application.getTextToSpeech();

		title = "error";
		Bundle extras = getIntent().getExtras();
        if (extras != null) {
            title = extras.getString("title");
        }
		Log.d(TAG, "EditItem title is: " + title);
	

		View saveButton = findViewById(R.id.edit_tile_save);
		saveButton.setOnClickListener(mClickListener);
		View deleteButton = findViewById(R.id.edit_tile_delete);
		deleteButton.setOnClickListener(mClickListener);
		
		nameField = (TextView) findViewById(R.id.edit_tile_name);
		speechField = (TextView) findViewById(R.id.edit_tile_speech_text);
		startSpeech = (Button) findViewById(R.id.edit_tile_speech_record);
		startSpeech.setOnClickListener(mClickListener);
		picField = (ImageView) findViewById(R.id.edit_tile_pic);
		parentField = (TextView) findViewById(R.id.edit_tile_parent_folder);
		parentField.setOnClickListener(mClickListener);
		isFolderField = (ToggleButton) findViewById(R.id.edit_tile_isfolder);
		speechToggle = (ToggleButton) findViewById(R.id.edit_tile_speech_toggle);
		speechToggle.setOnClickListener(mClickListener);
		startCamera = (Button) findViewById(R.id.edit_tile_pic_button);
		startCamera.setOnClickListener(mClickListener);
		
		Cursor c = mdb.fetchTile(title);
		if(c != null) {
			Log.d(TAG, "EditItem title is: " + title + " is not a null cursor...");			
			nameField.setText(c.getString(0));
			speechField.setText(c.getString(1));
			if(c.getString(5).equals("N")){
				picField.setImageResource(getResources().getIdentifier(c.getString(2), "drawable", "edu.washington.cs.kittens"));
			}else{
				Bitmap myBitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + c.getString(2) + ".png");
				picField.setImageBitmap(myBitmap);
			}
			parentField.setText(c.getString(3));
			Log.d(TAG, "The tile ID is " + c.getString(6));
			id = c.getString(6);
			type = c.getString(5);
			pic = c.getString(2);
			c.close();
		}
	}
	
    /**
     * for buttons to perform when they are "short" clicked
     */
	private View.OnClickListener mClickListener = new View.OnClickListener() {
		public void onClick(View v) {
			switch (v.getId()) {
			case R.id.edit_tile_pic_button:
				Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
				startActivityForResult(i, ACTIVITY_TAKE_PICTURE);
				break;
			case R.id.edit_tile_parent_folder:
				showDialog(1);
				break;
			case R.id.edit_tile_speech_record:
				i = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
				startActivityForResult(i, ACTIVITY_RECORD_AUDIO);
				break;
			case R.id.edit_tile_speech_toggle:
				if (speechToggle.isChecked()) {
					((EditText) findViewById(R.id.edit_tile_speech_text)).setVisibility(8);
					((Button) findViewById(R.id.edit_tile_speech_record)).setVisibility(0);
				} else {
					((EditText) findViewById(R.id.edit_tile_speech_text)).setVisibility(0);
					((Button) findViewById(R.id.edit_tile_speech_record)).setVisibility(8);
				}
				break;
			case R.id.edit_tile_save:
				String name = nameField.getText().toString();
				String text = speechField.getText().toString();
				String parentfolder = parentField.getText().toString().toLowerCase();
				boolean isFolder = isFolderField.isChecked();
				String type = "tile";
				if(isFolder){
					type = "category";
				}
				//mTts.speak(name + " " + text, TextToSpeech.QUEUE_FLUSH, null);
				if(picFieldTaken){
					String s = Integer.toString(application.getImagect());	
					mdb.updateTile(id, name, text, s, parentfolder, type, "Y", mAudioUri);
					Log.d(TAG, "Add tile: _" + name + "_" + text + "_" + s + "_" + parentfolder + "_" + type);
				}else{
					mdb.updateTile(id, name, text, pic, parentfolder, type, type, mAudioUri);
				}
				application.incrementImagect();
				application.setChanged(parentfolder);
				finish();
				break;
			case R.id.edit_tile_delete:
				name = nameField.getText().toString();
				parentfolder = parentField.getText().toString().toLowerCase();
				mdb.deleteTile(id);
				Log.d(TAG, "Deleted tile: _" + id);
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
				 picFieldTaken = false;
			 }
			break;
		case ACTIVITY_RECORD_AUDIO:
			if (resultCode == Activity.RESULT_OK) {
				mAudioUri = data.getDataString();
				if(mAudioUri.compareTo("") != 0) {
					/*Intent i = new Intent(Intent.ACTION_VIEW);
					i.setData(Uri.parse(mAudioUri));
					startActivity(i);*/
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