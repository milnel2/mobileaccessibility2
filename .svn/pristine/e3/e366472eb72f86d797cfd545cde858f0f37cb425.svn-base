package edu.washington.cs.kittens;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class is to display the grid page of this application in which different 
 * icons that represent different categories of items.
 * Each different level (root, animals, etc) load into this layout based on the current
 * navigation location
 */

public class Home extends Activity {
	final static String TAG = "Pic2Speech"; // For error logging

	private DbManager mdb;
	private TextToSpeech mTts;
	private CatApplication application;
	private SharedPreferences prefs;
	private String curCat;
	private String[] captions;
	private Integer[] images;
	private String[] sentences;
	private String[] types;
	private Boolean[] external;
	private String[] filenames;
	private String[] audio;
	
	private static final int ACTIVITY_TAKE_PICTURE = 0;
	
	/** for the background color of the screen*/
    @Override
    public void onAttachedToWindow() {
    	super.onAttachedToWindow();
    	Window window = getWindow();
    	window.setFormat(PixelFormat.RGBA_8888);
    }
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.home);
		
		// Get database and tts objects from the parent application
		this.application = (CatApplication) this.getApplication();
		mdb = this.application.getDatabaseManager();
		mTts = this.application.getTextToSpeech();
		curCat = this.application.getCat();
		
		// Get options stuff from saved SharedPreferences
		prefs = PreferenceManager.getDefaultSharedPreferences(this); 
		
		// Make volume slider on phone change media volume instead of ringer volume
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);

		
		// Create layout
		loadLayout();

	
	}

	/**
	 * Called when the activity is resumed
	 */
	@Override
	public void onResume() {
		application.setCat(curCat);
		GridView g = (GridView) findViewById(R.id.gridview);
		g.setNumColumns(Integer.parseInt(prefs.getString("opt_layout_columns", "3")));
		if(application.changed()) {
			application.setChanged("");
			loadLayout();
		}
		super.onResume();
	}
	
	/**
	 * Loads all the different icons into the grid view and
	 * display it on the home page
	 */
	private void loadLayout() {
		Cursor c = mdb.fetchTilesAtLevel(application.getCat());
		int totalIcons = 0;
		if(c != null){
			totalIcons = c.getCount();
		}
		Log.d(TAG, "total icons: " + totalIcons);
		captions = new String[totalIcons];
		images = new Integer[totalIcons];
		sentences = new String[totalIcons];
		types = new String[totalIcons];
		external = new Boolean[totalIcons];
		filenames = new String[totalIcons];
		audio = new String[totalIcons];
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setNumColumns(Integer.parseInt(prefs.getString("opt_layout_columns", "3")));
		gridView.setAdapter(new GridAdapter(this));
		for(int i = 0; i < totalIcons; i++){
			captions[i] = c.getString(0);
			sentences[i] = c.getString(1);
			filenames[i] = c.getString(2);
			audio[i] = c.getString(6);
			if(c.getString(5).equals("N")){
				external[i] = false;
				images[i] = getResources().getIdentifier(c.getString(2), "drawable", "edu.washington.cs.kittens");
			}else{
				external[i] = true;
			}
			types[i] = c.getString(4);
			c.moveToNext();
		}
		Log.d(TAG, "Icons retrieved: " + totalIcons);
		if(c != null){
			c.close();
		}
	}
	
	/**
	 * Called when the menu button is clicked
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.layout.menu, menu);
		// Show appropriate settings for user/editor interface
		Boolean isEditor = prefs.getBoolean("isEditor", false);
		// If TypeIt has been turned off in preferences or we are in editing mode, hide prefs
		menu.findItem(R.id.menu_type).setVisible(prefs.getBoolean("opt_type", true) && !isEditor);
		// If camera dump has been turned off in preferences, hide it
		menu.findItem(R.id.menu_cam).setVisible(prefs.getBoolean("opt_cam", true) && !isEditor);
		// If user, hide options button
		menu.findItem(R.id.menu_options).setVisible(isEditor);
		return true;
	}
	
	/**
	 * performs depending upon a menu item clicked
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
		Intent i;
	    switch (item.getItemId()) {
	    case R.id.menu_home:
	    	Log.d(TAG, "CurrCategory is: " + application.getCat());
	    	if(!application.getCat().equals("root")) {
		    	application.setCat("root");
				i = new Intent(this, Home.class);
				startActivity(i);
	    	}
	        return true;
	    case R.id.menu_type:
			i = new Intent(this, TypeIt.class);
			startActivity(i);
	        return true;
	    case R.id.menu_cam:
	    	i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(i, ACTIVITY_TAKE_PICTURE);
            return true;
	    case R.id.menu_options:
			i = new Intent(this, Options.class);
			startActivity(i);
	        return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }

	}

	/**
	 * when an activity is finished, it processes the given data
	 * 
	 * @param requestCode, a code to recognize the activity just finished
	 * @param resultCode, a code to indicate the result of the activity
	 * @param data, an intent given from the activity just finished
	 */
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode) {
                // store uri of the taken picture from taking picture activity
                case ACTIVITY_TAKE_PICTURE:
                	if (resultCode == Activity.RESULT_OK) {
                        // Display image received on the view
                         Bundle b = data.getExtras(); // Kept as a Bundle to check for other things in my actual code
                         Bitmap pic = (Bitmap) b.get("data");
                         File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + application.getImagect() + ".png");
                         try {
                             FileOutputStream out = new FileOutputStream(f.getAbsolutePath());
                             pic.compress(Bitmap.CompressFormat.PNG, 90, out);
                             out.flush();
                         } catch (Exception e) {
                        	 e.printStackTrace();
                         }
                         String s = Integer.toString(application.getImagect());
                         mdb.addTile("", "", s, "photos", "icon", "Y", null);
                         application.incrementImagect();
                     }
                     else if (resultCode == Activity.RESULT_CANCELED) {
                    	 
                     }
        }
	}

	/**
	 * Loads images in the grid view and displays them
	 */
	public class GridAdapter extends BaseAdapter {
		private View currView;

		public GridAdapter(Context c) {
		}

		public int getCount() {
			return images.length;
		}

		public Object getItem(int position) {
			return null;
		}

		public long getItemId(int position) {
			return 0;
		}
		
		// create a new ImageView for each item referenced by the Adapter
		public View getView(int position, View convertView, ViewGroup parent) {
			// Create a fresh view or recycle an old one
			if (convertView == null) {  
				LayoutInflater li = getLayoutInflater();
				currView = li.inflate(R.layout.tile, null);
			} else {
				currView = convertView;
			}
			// Fill the view with data
			TextView titleView = (TextView) currView.findViewById(R.id.icon_caption);
			titleView.setText(captions[position]);
			TextView audioView = (TextView) currView.findViewById(R.id.icon_audio);
			audioView.setText(audio[position]);
			ImageView iv = (ImageView) currView.findViewById(R.id.icon_image);
			Display display = getWindowManager().getDefaultDisplay(); 
			int width = display.getWidth();
			int columns = Integer.parseInt(prefs.getString("opt_layout_columns", "3"));
			if(external[position]){
				Bitmap myBitmap = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + filenames[position] + ".png");
				iv.setImageBitmap(myBitmap);
				iv.setMaxHeight((width - (columns+1)*10)/columns);
			}else{
				iv.setImageResource(images[position]);	
			}
			TextView sentenceView = (TextView) currView.findViewById(R.id.icon_sentence);
			sentenceView.setText(sentences[position]);	
			TextView typeView = (TextView) currView.findViewById(R.id.icon_type);
			typeView.setText(types[position]);	

			currView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					// Say sentence of category
					TextView speechBox = (TextView) findViewById(R.id.speech_box);
					TextView audioView = (TextView) view.findViewById(R.id.icon_audio);
					TextView sentenceView = (TextView) view.findViewById(R.id.icon_sentence);
					String sentence = sentenceView.getText().toString();
					String audio = audioView.getText().toString();
					Log.d("Pic2Speech", "Reading audio file " + audio);
					if(audioView.getText().equals("")){
						mTts.speak(sentence, TextToSpeech.QUEUE_FLUSH, null);
					}else{
						MediaPlayer mp = new MediaPlayer();
		                try {
							mp.setDataSource(audioView.getText().toString());
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
					speechBox.setText(sentence); 
					// Change screens
					TextView typeView = (TextView) view.findViewById(R.id.icon_type);
					TextView captionView = (TextView) view.findViewById(R.id.icon_caption);
					if(typeView.getText().toString().equals("category")) {
						Intent i = new Intent(getBaseContext(), Home.class);
						application.setCat(captionView.getText().toString().toLowerCase());
						Log.d("Pic2Speech", "Next currCategory is: " + captionView.getText());
						startActivity(i);
					}
				}
			});

			// If app is in editor mode, allow long click to edit icon
			if(prefs.getBoolean("isEditor", false)) {
				currView.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View view) {
						// Pass branch to a new intent, and start it
						Intent i = new Intent(getBaseContext(), EditTile.class);
						TextView titleView = (TextView) view.findViewById(R.id.icon_caption);
						i.putExtra("title", titleView.getText());
						Log.d("Pic2Speech", "Icon text should be..." + titleView.getText());
						startActivity(i);
						return true;
					}
				});
			}
			
			return currView;
		}
	}
}