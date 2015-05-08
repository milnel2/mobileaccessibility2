package edu.washington.cs.hangman;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.AsynchronousCloseException;

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

public class WordDb {
	private static final String TAG = "worddb";
	 
	private static final String DB_NAME = "words_db";
	private static final String DB_TABLE = "words";
	private static final int DB_VERSION = 1;

	// Set to true when debugging database operations
	private static final boolean D = false;
	
	// Task codes
	public static final int TASK_GET_WORD = 0;
	public static final int TASK_FILL_DB = 1;

	// Async task result keys 
	public static final String KEY_FILL_DB_SUCCEEDED = "success";
	public static final String KEY_WORD_RETRIEVED = "word";

	public abstract static class OnDbTaskComplete {
		public abstract void onComplete(int taskCode, Bundle result);
	}

	// Database fields
	public static final String COL_ROWID = "_id";
	public static final String COL_WORD = "_word";

	private Context _context;
	private SQLiteDatabase _db;
	private DbHelper _dbHelper;

	public WordDb(Context context) {
		this._context = context;
	}

	/**
	 * Open the database for writing
	 * @throws SQLException
	 */
	public void open() throws SQLException {
		if(_db == null) {
			_dbHelper = new DbHelper(_context);
			_db = _dbHelper.getWritableDatabase();
		}
	}

	/**
	 * Close the database. Call on this function when the activity
	 * using the database is being destroyed.
	 */
	public void close() {
		_dbHelper.close();
	}

	/**
	 * Inserts a new word. If the word is successfully created return the new
	 * rowId for that note, otherwise return a -1 to indicate failure.
	 */
	private long insertOrIgnore(String word) {
		ContentValues vals = createContentValues(word);

		return _db.insertWithOnConflict(DB_TABLE, null, vals, 
				SQLiteDatabase.CONFLICT_IGNORE);
	}

	/**
	 * Execute a query that retrieves a random word with the given length from the database.
	 * @param wordLen	the length of the word
	 * @param onTaskComplete	An object with a method that is called when the word is retrieved. If no
	 * 							word was found, null is passed to the callback method.
	 * 
	 */ 
	public void getRandomWord(int wordLen, final OnDbTaskComplete onTaskComplete) {
		AsyncTask<Integer, Void, String> queryDbTask = new AsyncTask<Integer, Void, String>() {

			@Override
			protected String doInBackground(Integer... wordLens) {
				Assert.assertEquals(1, wordLens.length);
				int wordLen = wordLens[0];

				String[] columns = { COL_WORD };
				String selection = "Length(" + COL_WORD + ")" + "=" + wordLen;
				Cursor c = _db.query(DB_TABLE, columns, selection, 
						null, null, null, "RANDOM()", "1");

				if(c.getColumnCount() == 0) {
					return null;
				} else {
					c.moveToFirst();
					Assert.assertFalse(c.isAfterLast());
					String word = c.getString(0);
					c.close();

					return word;
				}
			}

			@Override
			protected void onPostExecute(String result) {
				Bundle bundle = new Bundle();
				bundle.putString(KEY_WORD_RETRIEVED, result);
				onTaskComplete.onComplete(TASK_GET_WORD, bundle);
			}
		};

		queryDbTask.execute(wordLen);

	}

	/**
	 * Get the number of items in the database.
	 * @return number of items in the database
	 */
	private int getCount() {
		String columns[] = { "COUNT(" + COL_ROWID + ")" };
		Cursor cursor = _db.query(DB_TABLE, 
				columns, null, null, null, null, null);
		if (cursor != null) {
			cursor.moveToFirst();
		}
		int count = cursor.getInt(0);
		cursor.close();

		return count;
	}

	private ContentValues createContentValues(String word) {
		ContentValues values = new ContentValues();
		values.put(COL_WORD, word);
		return values;
	}

	public void loadWords(final int resId, 
			final OnDbTaskComplete onTaskComplete) {
		
		AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {

			@Override
			protected Boolean doInBackground(Void... params) {	
				Assert.assertNotNull(_db);

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
					// if file the available for reading                                                                                                                                                   
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
								Assert.fail("Failed to insert word: " + line);
							}
						}
					}
					// close the file again                                                                                                                                                                
					instream.close();
				} catch (IOException e) {
					e.printStackTrace();
					return false;
				}

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

	static class DbHelper extends SQLiteOpenHelper {

		private static final String CREATE_SMT = "create table " + 
			DB_TABLE + " (" + COL_ROWID + " integer primary key autoincrement, "
			+ COL_WORD + " text not null unique);";


		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {	
			db.execSQL(CREATE_SMT);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}
	}

}
