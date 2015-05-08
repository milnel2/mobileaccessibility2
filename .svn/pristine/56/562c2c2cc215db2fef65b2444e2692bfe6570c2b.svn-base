/** The PlaceGrid class 
 * 
 * Shows 2 places side by side. Clicking a place tile starts PlaceView activity for using a Place
 * and long click starts Place activity in edit mode.
 */

package cs.washington;

import java.io.File;
import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.speech.tts.TextToSpeech;
import android.util.Log;
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

public class PlaceGrid extends Activity{
	final static String TAG = "Pic2Speech"; // For error logging
	private String[] names;
	private String[] locations;
	private DbManager mdb;
	private TextToSpeech mTts;
	private CatApplication application;
	private SharedPreferences prefs;
	
	/**
	 * For the background color of the screen
	 */
    @Override
    public void onAttachedToWindow() {
    	super.onAttachedToWindow();
    	Window window = getWindow();
    	window.setFormat(PixelFormat.RGBA_8888);
    }
	
	/**
	 * Called when the activity is first created. 
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.place_grid);
		
		// Get database and tts objects from the parent application
		this.application = (CatApplication) this.getApplication();
		mdb = this.application.getDatabaseManager();
		mTts = this.application.getTextToSpeech();
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
		loadLayout();
		super.onResume();
	}
	
	/**
	 * Loads all the different icons into the grid view and
	 * display it on the home page
	 */
	private void loadLayout() {
		Cursor c = mdb.fetchAllPlaces();
		int totalIcons = 0;
		if(c != null){
			totalIcons = c.getCount();
		}
		names = new String[totalIcons];
		locations = new String[totalIcons];
		Log.d(TAG, "total places: " + totalIcons);
		GridView gridView = (GridView) findViewById(R.id.gridview);
		gridView.setNumColumns(2);
		gridView.setAdapter(new GridAdapter(this));
		for(int i = 0; i < totalIcons; i++){
			names[i] = c.getString(0);
			locations[i] = c.getString(1);
			c.moveToNext();
		}
		Log.d(TAG, "Places retrieved: " + totalIcons);
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
		menu.findItem(R.id.menu_places).setVisible(isEditor);
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
            startActivityForResult(i, 0);
            return true;
	    case R.id.menu_options:
			i = new Intent(this, Options.class);
			startActivity(i);
	        return true;
	    case R.id.menu_places:
			i = new Intent(this, Place.class);
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
                case 0:
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
			return names.length;
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
				currView = li.inflate(R.layout.place_tile, null);
			} else {
				currView = convertView;
			}
			// Fill the view with data
			ImageView iv = (ImageView) currView.findViewById(R.id.place_image);
			Log.d("Pic2Speech", "trying to load " + locations[position]);
			iv.setImageDrawable(getResources().getDrawable(R.drawable.cat));
			BitmapFactory.Options op = new BitmapFactory.Options();
			op.inSampleSize = 7;
			Bitmap myBitmap = BitmapFactory.decodeFile(locations[position], op);
			iv.setImageBitmap(myBitmap);
			TextView sourceView = (TextView) currView.findViewById(R.id.place_source);
			sourceView.setText(locations[position]);	

			currView.setOnClickListener(new View.OnClickListener() {
				public void onClick(View view) {
					Intent i = new Intent(getBaseContext(), PlaceView.class);
					TextView sourceView = (TextView) view.findViewById(R.id.place_source);
					i.putExtra("sourceImage", sourceView.getText());
					//Log.d("Pic2Speech", "Icon text should be..." + titleView.getText());
					startActivity(i);
				}
			});

			// If app is in editor mode, allow long click to edit icon
			if(prefs.getBoolean("isEditor", false)) {
				currView.setOnLongClickListener(new View.OnLongClickListener() {
					public boolean onLongClick(View view) {
						//Pass branch to a new intent, and start it
						Intent i = new Intent(getBaseContext(), Place.class);
						TextView sourceView = (TextView) view.findViewById(R.id.place_source);
						i.putExtra("sourceImage", sourceView.getText());
						//Log.d("Pic2Speech", "Icon text should be..." + titleView.getText());
						startActivity(i);
						return true;
					}
				});
			}else{
				
			}
			
			return currView;
		}
	}
}
