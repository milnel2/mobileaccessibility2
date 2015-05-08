// Copyright (c) 2012-2013 University of Washington
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// - Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// - Neither the name of the University of Washington nor the names of its
// contributors may be used to endorse or promote products derived from this
// software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
package edu.washington.cs.tgv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class ScanDB {
	private static final String TAG = "scandb";
	 
	private static final String DB_NAME = "scan_db";
	private static final String DB_TABLE = "scans";
	private static final int DB_VERSION = 1;

	// Database fields
	public static final String COL_ROWID = "_id";
	public static final String COL_TEXT = "_text";

	private Context _context;
	private SQLiteDatabase _db;
	private DbHelper _dbHelper;

	public ScanDB(Context context) {
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

	public long addText(String text){
		Log.v(TAG, "addText: "+ text);
		ContentValues values = new ContentValues();
		values.put(COL_TEXT, text);
		//return _db.insertWithOnConflict(DB_TABLE, null, values, SQLiteDatabase.CONFLICT_NONE);
		return _db.insert(DB_TABLE, null, values);
		//return _db.insertWithOnConflict(DB_TABLE, null,values, SQLiteDatabase.CONFLICT_IGNORE);
	}
	

	public void deleteText(int id){
		Log.v(TAG, "deleteText");
		String whereClause= "_id="+id;
		_db.delete(DB_TABLE, whereClause, null);
	}
	
	public void printTextDB(){
		Cursor cursor = _db.query(DB_TABLE, null, null, null, null, null, null);
		while(cursor.moveToNext()){
			int id = cursor.getInt(0);
			String text = cursor.getString(1);
			Log.v(TAG, "Text: id: "+ id + " text: "+ text);
		}
		cursor.close();
	}
	
	public Cursor getItems(){
		Cursor cursor = _db.query(DB_TABLE, null, null, null, null, null, null);
		return cursor;
	}
	
	
	public Cursor getReverseItems(){
		String orderBy = COL_ROWID + " DESC";
		Cursor cursor = _db.query(DB_TABLE, null, null, null, null, null, orderBy);
		return cursor;
	}

	static class DbHelper extends SQLiteOpenHelper {

		private static final String CREATE_TABLE = "create table " + 
			DB_TABLE + " (" + COL_ROWID + " integer primary key autoincrement, "
			+ COL_TEXT + " text not null);";


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
