package cs.washington.edu.VBGhost2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Random;

import junit.framework.Assert;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class DictionaryDB {
	private static final String TAG = "dictionaryDB";
	private static final String DATABASE_NAME = "dictionary.db";
	private static final int DATABASE_VERSION = 1;
	private static final String TABLE_NAME = "dictionary";
	public static final String _ID = "_id";
	public static final String _WORD = "_word";
	
	private Context _context;
	private SQLiteDatabase _DB;
	private DBHelper _DBHelper;
	
	public abstract static class OnDbTaskComplete {
		public abstract void onComplete(int taskCode, Bundle result);
	}
	
	// Set to true when debugging database operations
	private static final boolean D = false;
	
	// Task codes
	public static final int TASK_GET_WORD = 0;
	public static final int TASK_FILL_DB = 1;
	public static final int TASK_FIND_WORD = 2;
	
	// Async task result keys 
	public static final String KEY_FILL_DB_SUCCEEDED = "success";
	public static final String KEY_FIND_WORD_SUCCEEDED = "foundWord";
	public static final String KEY_GET_WORD_SUCCEEDED = "success";
	
	public DictionaryDB(Context context) {
		this._context = context;
		_DBHelper = new DBHelper(_context);
	}

	public void open() throws SQLException {
		if(_DB == null) {
			_DB = _DBHelper.getWritableDatabase();
		}
	}

	public void close() {
		_DBHelper.close();
	}

	
	private ContentValues createContentValues(String word) {
		ContentValues values = new ContentValues();
		values.put(_WORD, word);
		return values;
	}
	
	private long insertOrIgnore(String word) {
		ContentValues vals = createContentValues(word);

		return _DB.insertWithOnConflict(TABLE_NAME, null, vals, 
				SQLiteDatabase.CONFLICT_IGNORE);
	}
	
	public int getCount() {
		String columns[] = { "COUNT(" + _ID + ")" };
		Cursor cursor = _DB.query(TABLE_NAME, 
				columns, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		int count = cursor.getInt(0);
		cursor.close();
		Log.v(TAG, "count " + count);
		return count;
	}
	
	public void checkWord(String word, final OnDbTaskComplete onTaskComplete){
		AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>(){
			@Override
			protected Boolean doInBackground(String... words){
				Assert.assertEquals(1, words.length);
				String word = words[0];
				Log.v(TAG, "checking for word " + word);
				Assert.assertNotNull(_DB);
				String[] columns = {_WORD}; 
				String selection =  _WORD + "=" + word;

				Cursor c = _DB.rawQuery("SELECT _word FROM dictionary WHERE _word=? ",new String[] {word});
				//Cursor c = _DB.rawQuery("SELECT _word FROM dictionary WHERE _word like ? ",new String[] {word+"%"});
				if(c.getCount() == 0){
					Log.v(TAG, "word not found " + word);
					c.close();
					return false;
				}else{
					int i= c.getCount();
					Log.v(TAG, "column getCount " + i);
					Log.v(TAG, "found word " + word);
					c.close();
					return true;
				}
			}
			@Override
			protected void onPostExecute(Boolean result) {
				Bundle bundle = new Bundle();
				bundle.putBoolean(KEY_FIND_WORD_SUCCEEDED, result);
				onTaskComplete.onComplete(TASK_FIND_WORD, bundle);
			}
		};
		task.execute(word);
	}	

	public void getWord(String word, final OnDbTaskComplete onTaskComplete){
		AsyncTask<String, Void, String> task = new AsyncTask<String, Void, String>(){
			@Override
			protected String doInBackground(String... words){
				Assert.assertEquals(1, words.length);
				String word = words[0];
				Log.v(TAG, "checking for word fragment " + word);
				Assert.assertNotNull(_DB);
				String[] columns = {_WORD}; 
				String selection =  _WORD + "=" + word;

				//Cursor c = _DB.rawQuery("SELECT _word FROM dictionary WHERE _word=? ",new String[] {word});
				Cursor c = _DB.rawQuery("SELECT _word FROM dictionary WHERE _word like ? ",new String[] {word+"%"});
				if(c.getCount() == 0){
					Log.v(TAG, "word fragment not found " + word);
					c.close();
					return null;
				}else{
					int i= c.getCount();
					Log.v(TAG, "column getCount " + i);
					if(i !=1){
						Random rand = new Random();
						int r = rand.nextInt(i);
						Log.v(TAG, "moving r spaces " + r);
						if(r==0){
							c.moveToFirst();
						}else{
							c.move(r);
						}
					}else{
						c.moveToFirst();
					}
					String newWord = c.getString(0);
					Log.v(TAG, "found new word " + newWord);
					c.close();
					return newWord;
				}
			}
			@Override
			protected void onPostExecute(String result) {
				Bundle bundle = new Bundle();
				bundle.putString(KEY_GET_WORD_SUCCEEDED, result);
				onTaskComplete.onComplete(TASK_GET_WORD, bundle);
			}
		};
		task.execute(word);
	}
	
	public void fillTable(final int resId, 
			final OnDbTaskComplete onTaskComplete) {
		
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {	
				Assert.assertNotNull(_DB);

				// Load words if the DB is empty
				if(!D && getCount() > 0) {
					return true; // Success - there's nothing more to do.
				}

				InputStream instream;
				try {
					// open the file for reading                                                                                                                                                           
					instream = _context.getResources().openRawResource(resId);
				} catch (NotFoundException e) {
					e.printStackTrace();
					return false;
				} 
				try {
					// if file is available for reading                                                                                                                                                   
					if (instream != null) {
						// prepare the file for reading                                                                                                                                                
						InputStreamReader inputreader = new InputStreamReader(instream);
						BufferedReader buffreader = new BufferedReader(inputreader);

						String line;
						// read every line of the file into the line-variable, on line at the time                                                                                                     
						while ((line = buffreader.readLine()) != null) {
							long id = insertOrIgnore(line);
							if(id == -1) {
								Log.e(TAG, "Failed to insert word: " + line);
								//Assert.fail("Failed to insert word: " + line);
							}
								//Log.v(TAG, "Entered this word: " + line);
			
						}
					}
					// close the file again                                                                                                                                                                
					instream.close();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}
				getCount();
				return true;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				Bundle bundle = new Bundle();
				bundle.putBoolean(KEY_FILL_DB_SUCCEEDED, result);
				onTaskComplete.onComplete(TASK_FILL_DB, bundle);
			}

		};

		task.execute();
	}
	
	
	
	static class DBHelper extends SQLiteOpenHelper {
		
		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {	
			db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
					+ _WORD + " TEXT NOT NULL UNIQUE);");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " +TABLE_NAME);
			onCreate(db);

		}
	}

}
