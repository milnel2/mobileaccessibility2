package edu.washington.cs.kittens;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

/**
 * DbManager is a service that runs in the background at the duration of the Pic2Speech 
 * application and is responsible saving all of the data submitted from each activity into the 
 * appropriate SQLite database.
 * 
 * ********************************************************************************************
 * 
 * **************************************************   tile   ********************************************************************
 * |    tile_id      |  tile_title  |  tile_text  |  tile_image  |  tile_category  |  tile_type  |  tile_external  |  tile_audio  |    
 * |  PRIMARY KEY    |    STRING    |   STRING    |    STRING    |     STRING      |    STRING   |     STRING      |    STRING    |
 * |  AUTO_INCREMENT |				|			  |				 |				   |			 |				   |			  |
 */

public class DbManager {

	private static final String DATABASE_NAME = "Pic2SpeechDb";
	private static final int DATABASE_VERSION = 1;
		
	private static final String TILE_TABLE = "tile";
	private static final String TILE_KEY_ID = "tile_id";
	private static final String TILE_KEY_TITLE = "tile_title";
	private static final String TILE_KEY_TEXT = "tile_text";
	private static final String TILE_KEY_IMAGE = "tile_image";
	private static final String TILE_KEY_CATEGORY = "tile_category";
	private static final String TILE_KEY_TYPE = "tile_type";
	private static final String TILE_KEY_EXTERNAL = "tile_external";
	private static final String TILE_KEY_AUDIO = "tile_audio";
	private static final String CREATE_TILE_TABLE =
		"create table " + TILE_TABLE + " (" + TILE_KEY_ID + " integer primary key autoincrement, " +
		TILE_KEY_TITLE + " string, " + TILE_KEY_TEXT + " string, " + TILE_KEY_IMAGE + " string, " + 
		TILE_KEY_CATEGORY + " string, " + TILE_KEY_TYPE + " string, " + TILE_KEY_EXTERNAL + " string, " + 
		TILE_KEY_AUDIO + " string);";
	
	private static final String TAG = "Pic2Speech - DbMan";
	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private final Context mContext;
	
	private static class DatabaseHelper extends SQLiteOpenHelper {
		
		DatabaseHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}
		
		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(CREATE_TILE_TABLE);
		}

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF TABLE EXISTS" + TILE_TABLE);
            onCreate(db);
        }
	}
	
	/**
	 * Constructs a DbManager object
	 * 
	 * @param ctx, context
	 */
	public DbManager(Context ctx) {
		this.mContext = ctx;
	}

	/**
	 * Opens database and returns it
	 *
	 * @throws SQLException when something goes wrong
	 */
	public DbManager open() throws SQLException {
		mDbHelper = new DatabaseHelper(mContext);
		mDb = mDbHelper.getWritableDatabase();
		
		// If the database is currently empty, load it in from the initial text file
		Cursor c = fetchAllTiles();
		if (c == null) {
			if(true) {   
				dropTiles();
				createTiles();
				InputStream input = mContext.getResources().openRawResource(R.raw.initial_database);
				BufferedReader reader = new BufferedReader(new InputStreamReader(input));
				try {
					String line = reader.readLine();
					while (line != null) {
						String[] strings = TextUtils.split(line, "\t");
						if (strings.length == 5) {
							addTile(strings[0], strings[1], strings[2], strings[3], strings[4], "N", null);
							Log.d(TAG, "added tile: " + strings[0] + "_" +  strings[1] + "_" +  strings[2] + "_" +  strings[3]);
						} else {
							Log.d(TAG, "Could not insert tile row:_" + strings[0] + "_" + strings[1] + "_" + strings[2] + "_");					
						}
						line = reader.readLine();
					}
				} catch(IOException e) {
					Log.e(TAG, "Error! " + e.toString());
				} finally {
					try {
						reader.close();
					} catch(IOException e) {
						Log.e(TAG, "Error! " + e.toString());
					}
				}
			}
		}
		return this;
	}

	/**
	 * Closes database
	 */
	public void close() {
		mDbHelper.close();
	}

	/**
	 * Drops the tile table from database
	 */
	public void dropTiles(){
		mDb.execSQL("DROP TABLE IF EXISTS " + TILE_TABLE);
	}
	
	/**
	 * Creates the tile table in database
	 */
	public void createTiles(){
		mDb.execSQL(CREATE_TILE_TABLE);
	}
		
	/**
	 * Saves a new tile into the database
	 * 
	 * @param title, name of tile
	 * @param text, text to be saved along with tile
	 * @param image, image to be saved along with tile
	 * @param category, category of tile
	 * @param type, type of tile
	 * @param ex, string to indicate whether the image is in the internal or external storage
	 * @param audiouri, location of audio clip
	 * @return a long if a new tile has been successfully added to database
	 */
	public long addTile(String title, String text, String image, String category, String type, String ex, String audiouri) {
		ContentValues values = new ContentValues();
		values.put(TILE_KEY_TITLE, title);
		values.put(TILE_KEY_TEXT, text);
		values.put(TILE_KEY_IMAGE, image);
		values.put(TILE_KEY_CATEGORY, category);
		values.put(TILE_KEY_TYPE, type);
		values.put(TILE_KEY_EXTERNAL, ex);
		values.put(TILE_KEY_AUDIO, audiouri);
		return mDb.insert(TILE_TABLE, null, values);
	}
	
	/**
	 * Updates a tile
	 * 
	 * @param title, name of tile
	 * @param text, text to be saved along with tile
	 * @param image, image to be saved along with tile
	 * @param category, category of tile
	 * @param type, type of tile
	 * @param ex, string to indicate whether the image is in the internal or external storage
	 * @param audiouri, location of audio clip
	 * @return a long if a new tile has been successfully added to database
	 */
	public long updateTile(String id, String name, String text, String image, String category, String type, String ex, String audiouri) {	
		ContentValues values = new ContentValues();
		values.put(TILE_KEY_TITLE, name);
		values.put(TILE_KEY_TEXT, text);
		values.put(TILE_KEY_IMAGE, image);
		values.put(TILE_KEY_CATEGORY, category);
		values.put(TILE_KEY_TYPE, type);
		values.put(TILE_KEY_EXTERNAL, ex);
		values.put(TILE_KEY_AUDIO, audiouri);
		return mDb.update(TILE_TABLE, values, TILE_KEY_ID + "=" + id + "", null);
	}

	/**
	 * Updates part of a tile
	 * 
	 * @param id, id of tile to update
	 * @param name, name of tile
	 * @param text, text to be saved along with tile
	 * @param category, category of tile
	 * @param type, type of tile
	 * @return a long if the tile has been successfully updated in database
	 */
	public long updatePartTile(String id, String name, String text, String category, String type) {	
		ContentValues values = new ContentValues();
		values.put(TILE_KEY_TITLE, name);
		values.put(TILE_KEY_TEXT, text);
		values.put(TILE_KEY_CATEGORY, category);
		values.put(TILE_KEY_TYPE, type);
		return mDb.update(TILE_TABLE, values, TILE_KEY_ID + "=\'" + id + "\'", null);
	}

	/**
	 * Deletes a tile from database
	 * 
	 * @param id, id of tile
	 * @return true if data successfully deleted
	 */
	public boolean deleteTile(String id) {
		return mDb.delete(TILE_TABLE, TILE_KEY_ID + "=" + id + "", null) > 0;
	}
	
	/**
	 * Returns a cursor that has all the tiles in database
	 */
	public Cursor fetchAllTiles() {
		Cursor mCursor =
			mDb.query(TILE_TABLE, new String[] {TILE_KEY_TITLE, TILE_KEY_TEXT, 
					TILE_KEY_CATEGORY, TILE_KEY_IMAGE, TILE_KEY_TYPE, TILE_KEY_EXTERNAL, TILE_KEY_AUDIO}, 
					null, null, null, null, null);
		if (mCursor.getCount() < 1) {
			mCursor.close();
			return null;
		}
		mCursor.moveToFirst();
		return mCursor;
	}
	
	/**
	 * Returns a cursor that points to the requested tile
	 * 
	 * @param title, title of tile
	 * @throws SQLException if something goes wrong
	 */
	public Cursor fetchTile(String title) throws SQLException {
		Cursor mCursor =
			mDb.query(true, TILE_TABLE, new String[] {TILE_KEY_TITLE, TILE_KEY_TEXT, 
					TILE_KEY_IMAGE, TILE_KEY_CATEGORY, TILE_KEY_TYPE, TILE_KEY_EXTERNAL, TILE_KEY_ID
					,TILE_KEY_AUDIO}, 
					TILE_KEY_TITLE + "=\'" + title + "\'", null, null, null, null, null);
		if (mCursor.getCount() < 1) {
			mCursor.close();
			return null;
		}
		mCursor.moveToFirst();
		return mCursor;
	}

	/**
	 * Returns a cursor that points to the requested tile
	 * 
	 * @param level, category of tile
	 * @throws SQLException if something goes wrong
	 */
	public Cursor fetchTilesAtLevel(String level) throws SQLException {
		Cursor mCursor =
			mDb.query(true, TILE_TABLE, new String[] {TILE_KEY_TITLE, TILE_KEY_TEXT, 
					 TILE_KEY_IMAGE, TILE_KEY_CATEGORY, TILE_KEY_TYPE, TILE_KEY_EXTERNAL, TILE_KEY_AUDIO}, 
					TILE_KEY_CATEGORY + "=\'" + level + "\'", null, null, null, null, null);
		if (mCursor.getCount() < 1) {
			mCursor.close();
			return null;
		}
		mCursor.moveToFirst();
		return mCursor;
	}
	
	/**
	 * Returns true if the tile of name exists in database.
	 * Otherwise, returns false.
	 * 
	 * @param name, name of tile
	 * @throws SQLException if something goes wrong
	 */
	public boolean tileExist(String name) throws SQLException {
		Cursor mCursor =
			mDb.query(true, TILE_TABLE, new String[] {TILE_KEY_TITLE}, TILE_KEY_TITLE + "=\'" + name + "\'", 
					null, null, null, null, null);
		int value = mCursor.getCount();
		mCursor.close();
		return value >= 1;
	}
	
	/**
	 * Returns the number of sets of tile table
	 */
	public int numTiles() {
		Cursor c = mDb.rawQuery("SELECT * FROM " + TILE_TABLE, null);
		int count = c.getCount();
		c.close();
		return count;
	}
	
	/**
	 * Returns an array of the string of each category
	 */
	public String[] getCategories() {
		Cursor c =
			mDb.query(true, TILE_TABLE, new String[] {TILE_KEY_TITLE}, 
					TILE_KEY_TYPE + "=\'" + "category" + "\'", null, null, null, null, null);
		int size = c.getCount();
		c.moveToFirst();
		String[] categories = new String[size];
		for(int i = 0; i < size; i++){
			categories[i] = c.getString(0);
			Log.d(TAG, "getCategories: retrieved category " + c.getString(0));
			c.moveToNext();
		}
		c.close();
		return categories;
	}
}