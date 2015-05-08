package cs.washington.edu.VBGhost2;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class GameDB {
	private static final String TAG = "gamedb";
	 
	private static final String DB_NAME = "game_db";
	private static final String DB_TABLE = "games";
	private static final int DB_VERSION = 1;

	// Database fields
	public static final String COL_ROWID = "_id";
	public static final String COL_PLAYERS = "_players";
	public static final String COL_YOURTURN = "_yourturn";
	public static final String COL_WORD = "_word";


	private Context _context;
	private SQLiteDatabase _db;
	private DbHelper _dbHelper;

	public GameDB(Context context) {
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

	public long addGame(int numPlayers, String wordFragment, Boolean yourTurn){
		Log.v(TAG, "addGame: "+ wordFragment);
		ContentValues values = new ContentValues();
		values.put(COL_PLAYERS, numPlayers);
		values.put(COL_WORD, wordFragment);
		values.put(COL_YOURTURN, yourTurn);
		return _db.insertWithOnConflict(DB_TABLE, null,values, SQLiteDatabase.CONFLICT_IGNORE);
	}
	
	public void updateGame(int id, String wordFragment, Boolean yourTurn){
		Log.v(TAG, "updateGame " + wordFragment);
		ContentValues values = new ContentValues();
		values.put(COL_WORD, wordFragment);
		values.put(COL_YOURTURN, yourTurn);
		String whereClause = "_id="+id;
		_db.updateWithOnConflict(DB_TABLE, values, whereClause, null,  SQLiteDatabase.CONFLICT_IGNORE);

	}
	
	public Boolean checkIfValidGame(int id){
		String WHERE = "_id ="+id;
		Cursor cursor = _db.query(DB_TABLE, null, WHERE, null, null, null, null);
		if(cursor.getCount() == 0){
			cursor.close();
			return false;
		}
		cursor.moveToFirst();
		int yourTurn = cursor.getInt(2);
		if(yourTurn == 0){
			cursor.close();
			return true;
		}
		cursor.close();
		return false;
	}

	public void deleteGame(int id){
		Log.v(TAG, "deleteGame");
		String whereClause= "_id="+id;
		_db.delete(DB_TABLE, whereClause, null);
	}
	
	public void printGameDB(){
		Cursor cursor = _db.query(DB_TABLE, null, null, null, null, null, null);
		while(cursor.moveToNext()){
			int id = cursor.getInt(0);
			int players = cursor.getInt(1);
			String wordFrag = cursor.getString(3);
			Log.v(TAG, "GAME: id: "+ id + " players: "+ players + " : " + wordFrag);
		}
		cursor.close();
	}
	
	public Cursor getGames(){
		Cursor cursor = _db.query(DB_TABLE, null, null, null, null, null, null);
		return cursor;
	}
	
	
	public int getGameNumPlayers(int id){
		String WHERE = "_id ="+id;
		Cursor cursor = _db.query(DB_TABLE, null, WHERE, null, null, null, null);
		cursor.moveToFirst();
		int players = cursor.getInt(1);
		cursor.close();
		return players;
	}
	
	public int getGameYourTurn(int id){
		String WHERE = "_id ="+id;
		Cursor cursor = _db.query(DB_TABLE, null, WHERE, null, null, null, null);
		cursor.moveToFirst();
		int yourTurn = cursor.getInt(2);
		cursor.close();
		return yourTurn;
	}
	
	public String getGameWordFragment(int id){
		String WHERE = "_id ="+id;
		Cursor cursor = _db.query(DB_TABLE, null, WHERE, null, null, null, null);
		cursor.moveToFirst();
		String wordFrag = cursor.getString(3);
		cursor.close();
		return wordFrag;
	}


	static class DbHelper extends SQLiteOpenHelper {

		private static final String CREATE_TABLE = "create table " + 
			DB_TABLE + " (" + COL_ROWID + " integer primary key autoincrement, "
			+ COL_PLAYERS + " integer, " + COL_YOURTURN + " boolean, " + COL_WORD + " text not null unique);";


		public DbHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {	
			Log.v(TAG, CREATE_TABLE);
			db.execSQL(CREATE_TABLE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub

		}
	}

}
