
package com.dtt.db;

import com.dtt.objs.GlobalConstants;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * 
 * Simple DTT database access helper class. Defines the basic CRUD operations
 * for the DTT app, and gives the ability to list all datum for each table 
 * as well as retrieve or modify a specific data.
 * 
 * @author Moon Hwan Oh, Amenda Shen
 * 
 */
public class DataDbAdapter {

	// for Schedule Builder
    public static final String KEY_ID = "_id";
    public static final String KEY_TIME = "time";
    public static final String KEY_FLAG = "flag";
    public static final String KEY_IMPORTANT = "important";
    // for Task Builder
    public static final String KEY_TASK_ID = "task_id";
    public static final String KEY_TASK_NAME = "name";
    public static final String KEY_PICTURE = "picture_uri";
    public static final String KEY_STEP_NUM = "step_num";
    public static final String KEY_INSTRUCTION = "instruction";
    public static final String KEY_AUDIO = "audio_uri";
    public static final String KEY_VIDEO = "video_uri";
    
    private static final String TAG = "DataDbAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE_SCHEDULE =
            "create table schedule (_id integer primary key autoincrement, "
                    + "time text not null, "
                    + "task_id integer not null, "
                    + "flag integer not null, " 
                    + "important integer not null);";
    private static final String DATABASE_CREATE_TASKS = 
            " create table tasks (_id integer primary key autoincrement, "
             		+ "name text not null, " 
             		+ "picture_uri text not null);";
    private static final String DATABASE_CREATE_STEPS = 
            " create table steps (_id integer primary key autoincrement, "
             		+ "task_id integer not null, " 
             		+ "step_num integer not null, "
             		+ "instruction text not null, "
             		+ "picture_uri text not null, "
             		+ "audio_uri text not null, "
             		+ "video_uri text not null);";

    private static final String DATABASE_NAME = "DTT";
    private static final String DATABASE_SCHEDULE_TABLE = "schedule";
    private static final String DATABASE_TASKS_TABLE = "tasks";
    private static final String DATABASE_STEPS_TABLE = "steps";
    private static final int DATABASE_VERSION = 3;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(DATABASE_CREATE_SCHEDULE);
            db.execSQL(DATABASE_CREATE_TASKS);
            db.execSQL(DATABASE_CREATE_STEPS);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS notes");
            onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public DataDbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the DTT database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public DataDbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }
    
    /**
     * close the database
     */
    public void close() {
        mDbHelper.close();
    }

    public void query(String sql) {
    	mDb.execSQL(sql);
    }
    
    /**
     * Create a new data in schedule table with info passed in. 
     * If the data is successfully created return the new rowId for that data,
     * otherwise, return a -1 to indicate failure including no task available for
     * the taskId.
     * 
     * @return rowId or -1 if failed
     */
    public long createScheduleData(String time, long taskId, int important) {
    	// check if tasks table has taskId in it
    	Cursor mCursor =
            mDb.query(true, DATABASE_TASKS_TABLE, null, 
            		KEY_ID + "=" + taskId, null, null, null, null, null);
    	if (mCursor.getCount() == 0) {
    		mCursor.close();
    		return -1;
    	}
    	mCursor.close();
    	
        ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TIME, time);
        initialValues.put(KEY_TASK_ID, taskId);
        initialValues.put(KEY_FLAG, GlobalConstants.NOT_DONE);
        initialValues.put(KEY_IMPORTANT, important);
        return mDb.insert(DATABASE_SCHEDULE_TABLE, null, initialValues);
    }
    
    /**
     * Create a new data in task table with info passed in. 
     * If the data is successfully created return the new rowId for that data,
     * otherwise, return a -1 to indicate failure.
     * 
     * @return rowId or -1 if failed
     */
    public long createTasksData(String name, String pictureUri){
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_TASK_NAME, name);
        initialValues.put(KEY_PICTURE, pictureUri);
        return mDb.insert(DATABASE_TASKS_TABLE, null, initialValues);
    }
    
    /**
     * Create a new data in steps table with info passed in. 
     * If the data is successfully created return the new rowId for that data,
     * If the data already exists in the table, return its id.
     * otherwise, return a -1 to indicate failure. including no task available for
     * the taskId.
     * 
     * @return rowId or -1 if failed
     */
    public long createStepsData(long taskId, String inst, String pictureUri,
    		String audioUri, String videoUri){
    	long stepNum = 0;
    	// check if tasks table has taskId in it
    	Cursor mCursor =
            mDb.query(true, DATABASE_TASKS_TABLE, null, 
            		KEY_ID + "=" + taskId, null, null, null, null, null);
    	if (mCursor.getCount() == 0) {
    		mCursor.close();
    		return -1;
    	}
    	mCursor = fetchAllStepsDatumForTask(taskId);
    	if(mCursor.getCount() == 0)
    		stepNum = 1;
    	else {
    		mCursor.moveToLast();
    		stepNum = mCursor.getLong(mCursor.getColumnIndex(KEY_STEP_NUM)) + 1;
    	}
    	mCursor.close();
    	
    	ContentValues initialValues = new ContentValues();
    	initialValues.put(KEY_TASK_ID, taskId);
    	initialValues.put(KEY_STEP_NUM, stepNum);
    	initialValues.put(KEY_INSTRUCTION, inst);
    	initialValues.put(KEY_PICTURE, pictureUri);
    	initialValues.put(KEY_AUDIO, audioUri);
    	initialValues.put(KEY_VIDEO, videoUri);
    	return mDb.insert(DATABASE_STEPS_TABLE, null, initialValues);
    }

    /**
     * Delete the data in schedule table with the given scheduleId
     * 
     * @param scheduleId - id of data to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteScheduleData(long scheduleId) {
        return mDb.delete(DATABASE_SCHEDULE_TABLE, KEY_ID + "=" + scheduleId, null) > 0;
    }
    
    /**
     * Delete the data in schedule table which matches all parameters passed
     * 
     * @param taskId
     * @param time
     * @param flag
     * @param important
     */
    public void deleteScheduleData(long taskId, String time, int flag, int important) {
    	mDb.execSQL("DELETE FROM schedule WHERE task_id = '" + taskId 
				+ "' and time = '" + time + "' and important = '" + important 
				+ "' and flag = '" + flag + "'");
    }
    
    /**
     * Delete all data from schedule table
     */
    public void deleteAllScheduleData() {
    	mDb.execSQL("DELETE FROM schedule");
    }
    
    /**
     * delete step data matched with parameters passed, and then update rest of step
     * data's step number field
     * 
     * @param stepId
     * @param taskId
     * @return
     */
    public boolean deleteStepsDataAndUpdateStepNum(long stepId, long taskId) {
    	Cursor mCursor = null;
    	long[] ids = new long[50];
    	int stepNum = 1;
    	int leng = 0;
    	try{
    		mCursor = fetchAllStepsDatumForTask(taskId);
    		mCursor.moveToFirst();
    		while(!mCursor.isAfterLast()) {
    			if(mCursor.getLong(mCursor.getColumnIndex(KEY_ID)) == stepId) {
    				mCursor.moveToNext();
    				break;
    			}
    			stepNum++;
    			mCursor.moveToNext();
    		}
    		for(; !mCursor.isAfterLast(); leng++) {
    			ids[leng] = mCursor.getLong(mCursor.getColumnIndex(KEY_ID));
    			mCursor.moveToNext();
    		}	
    	} finally {
    		if(mCursor != null)
    			mCursor.close();
    	}
    	
    	for(int i = 0; i < leng; i++) {
    		ContentValues args = new ContentValues();
            args.put(KEY_STEP_NUM, stepNum++);
            mDb.update(DATABASE_STEPS_TABLE, args, KEY_ID + "=" + ids[i], null);
    	}
    		
    	return deleteStepsData(stepId);
    }
    
    /**
     * Delete the data in steps table with given stepId
     * 
     * @param stepId - id of data to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteStepsData(long stepId) {
    	return mDb.delete(DATABASE_STEPS_TABLE, KEY_ID + "=" + stepId, null) > 0;
    }
    
    /**
     * Delete the data in tasks table with given taskId. Also delete corresponding data
     * in schedule table and steps table
     * 
     * @param taskId - id of data to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteTasksData(long taskId) {
    	mDb.delete(DATABASE_SCHEDULE_TABLE, KEY_TASK_ID + "=" + taskId, null);
    	mDb.delete(DATABASE_STEPS_TABLE, KEY_TASK_ID + "=" + taskId, null);
    	return mDb.delete(DATABASE_TASKS_TABLE, KEY_ID + "=" + taskId, null) > 0;
    }

    /**
     * Return a Cursor over the list of all datum in schedule table 
     * in the database
     * 
     * @return Cursor over all datum
     */
    public Cursor fetchAllScheduleDatum() {
    	Cursor cursor = mDb.query(DATABASE_SCHEDULE_TABLE + " S, " + DATABASE_TASKS_TABLE + " T", 
        		new String[] {"S."+KEY_ID, KEY_TIME, KEY_FLAG, KEY_IMPORTANT, KEY_TASK_ID, KEY_TASK_NAME, KEY_PICTURE},
        		"S."+KEY_TASK_ID+"="+"T."+KEY_ID, null, null, null, KEY_TIME);
    	
    	return cursor;
    }
    
    /**
     * Return a Cursor over the list of all datum in tasks table 
     * in the database
     * 
     * @return Cursor over all datum
     */
    public Cursor fetchAllTasksDatum() {
        return mDb.query(DATABASE_TASKS_TABLE, new String[] {KEY_ID, KEY_TASK_NAME, KEY_PICTURE}, 
        		null, null, null, null, null);
    }
    
    /**
     * Return a Cursor over the list of all datum in steps table which corresponding taskId 
     * in the database
     * 
     * @return Cursor over all datum corresponding taskId
     */
    public Cursor fetchAllStepsDatumForTask(long taskId) {
        return mDb.query(DATABASE_STEPS_TABLE, null,
        		KEY_TASK_ID + "=" + taskId, null, null, null, KEY_STEP_NUM);
    }
    
    /**
     * Return a Cursor positioned at the data that matches the given scheduleId 
     * from schedule table
     * 
     * @return Cursor positioned to matching data, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor  fetchScheduleData(long scheduleId) {
    	Cursor mCursor =
            mDb.query(true, DATABASE_SCHEDULE_TABLE, null, 
            		KEY_ID + "=" + scheduleId, null, null, null, null, null);
    	if (mCursor.getCount() != 0) {
    		mCursor.moveToFirst();
    	}
    
    	return mCursor;
    }
    
    /**
     * Return a Cursor positioned at the data that matches the given taskId from tasks table
     * 
     * @return Cursor positioned to matching data, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchTaskData(long taskId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_TASKS_TABLE, null, 
                		KEY_ID + "=" + taskId, null, null, null, null, null);
        if (mCursor.getCount() != 0) {
            mCursor.moveToFirst();
        }
        
        return mCursor;
    }
    
    public Cursor fetchSearchedTaskData(String searchKey) throws SQLException {
    	Cursor mCursor =
             mDb.query(true, DATABASE_TASKS_TABLE, null, 
             		KEY_TASK_NAME + " like '%" + searchKey + "%'", null, null, null, null, null);
    	 if (mCursor.getCount() != 0) {
    		 mCursor.moveToFirst();
    	 }
     
     return mCursor;
    }
    
    
    /**
     * Return a Cursor positioned at the data that matches the given stepId from steps table
     * 
     * @return Cursor positioned to matching data, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchStepData(long stepId) throws SQLException {
        Cursor mCursor =
                mDb.query(true, DATABASE_STEPS_TABLE, null, 
                		KEY_ID + "=" + stepId, null, null, null, null, null);
        if (mCursor.getCount() != 0) {
            mCursor.moveToFirst();
        }
        
        return mCursor;
    }

    /**
     * Update the data in schedule table using the details provided.
     * 
     * @return true if the data was successfully updated, false otherwise
     */
    public boolean updateScheduleData(long scheduleId, int flag) {
    	ContentValues args = new ContentValues();
        args.put(KEY_FLAG, flag);
        return mDb.update(DATABASE_SCHEDULE_TABLE, args, KEY_ID + "=" + scheduleId, null) > 0;
    }
    
    /**
     * Update the data in tasks table using the details provided.
     * 
     * @return true if the data was successfully updated, false otherwise
     */
    public boolean updateTasksData(long taskId, String name, String pictureUri) {
        ContentValues args = new ContentValues();
        args.put(KEY_TASK_NAME, name);
        args.put(KEY_PICTURE, pictureUri);

        return mDb.update(DATABASE_TASKS_TABLE, args, KEY_ID + "=" + taskId, null) > 0;
    }
    
    /**
     * Update the data in steps table using the details provided.
     * 
     * @return true if the data was successfully updated, false otherwise
     */
    public boolean updateStepsData(long stepId, long taskId, long stepNum, String inst, String pictureUri,
    		String audioUri, String videoUri) {
    	
    	// check if task id exists
    	Cursor mCursor =
            mDb.query(true, DATABASE_TASKS_TABLE, null, 
            		KEY_ID + "=" + taskId, null, null, null, null, null);
    	if (mCursor.getCount() == 0) {
    		mCursor.close();
    		return false;
    	}
    	mCursor.close();
    	
        ContentValues args = new ContentValues();
        args.put(KEY_TASK_ID, taskId);
        args.put(KEY_STEP_NUM, stepNum);
        args.put(KEY_INSTRUCTION, inst);
        args.put(KEY_PICTURE, pictureUri);
        args.put(KEY_AUDIO, audioUri);
        args.put(KEY_VIDEO, videoUri);

        return mDb.update(DATABASE_STEPS_TABLE, args, KEY_ID + "=" + stepId, null) > 0;
    }
}
