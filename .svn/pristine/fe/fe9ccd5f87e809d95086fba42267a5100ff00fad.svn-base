package com.dtt.receivers;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.dtt.activities.AddScheduleActivity;
import com.dtt.activities.ScheduleListActivity;
import com.dtt.db.DataDbAdapter;
import com.dtt.objs.GlobalConstants;
import com.dtt.services.ScheduleUpdateService;

/**
 * This receiver listens to boot completed signal and fire up background running thread
 * for updating all schedule modification made on web side to sync with phone
 * 
 * @author Moon Hwan Oh, Amanda Shen
 * 
 */
public class ScheduleUpdateServiceReceiver extends BroadcastReceiver {

	public static final String TAG = "ScheduleUpdateServiceReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		if("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
			// start web sync service
			ComponentName comp = new ComponentName(context.getPackageName(), ScheduleUpdateService.class.getName());
			ComponentName service = context.startService(new Intent().setComponent(comp));
			if (null == service) {
				Log.e(TAG, "Could not start service " + comp.toString());
			}
			
			// if day passes, don't reinitialize alarms for up-coming events
			String currentDate = ScheduleListActivity.getCurrentDateInString();
			SharedPreferences myPrefs = context.getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
			String cachedDate = myPrefs.getString("date", "");
			if(currentDate.compareTo(cachedDate) != 0)
				return;
			
			
			// reinitialize alarms for up-coming events
			DataDbAdapter dbHelper = new DataDbAdapter(context);
			dbHelper.open();
			
			String currentTime = ScheduleListActivity.getCurrentTimeInString();
			Cursor cursor = dbHelper.fetchAllScheduleDatum();
			cursor.moveToFirst();
			Boolean skippedTaskExist = false;
			while(!cursor.isAfterLast()) {
				// if the scheduled task is done, move to the next one
				int status = cursor.getInt(cursor.getColumnIndex(DataDbAdapter.KEY_FLAG));
				if(status == GlobalConstants.DONE) {
					cursor.moveToNext();
					continue;
				}
				// for scheduled tasks in the past
				String time = cursor.getString(cursor.getColumnIndex(DataDbAdapter.KEY_TIME));
				if(time.compareTo(currentTime) < 0) {
					skippedTaskExist = true;
					cursor.moveToNext();
					continue;
				}
				
				Long scheduleId = cursor.getLong(cursor.getColumnIndex(DataDbAdapter.KEY_ID));
				Long taskId = cursor.getLong(cursor.getColumnIndex(DataDbAdapter.KEY_TASK_ID));
				String taskName = cursor.getString(cursor.getColumnIndex(DataDbAdapter.KEY_TASK_NAME));
				
				AddScheduleActivity.setAlarm(context, scheduleId, taskId, taskName, time);
				cursor.moveToNext();
			}
			
			if(skippedTaskExist)
				AddScheduleActivity.setAlarm(context, GlobalConstants.DEFAULT_LONG, GlobalConstants.DEFAULT_LONG,
						GlobalConstants.DEFAULT_STRING, currentTime);
			cursor.close();
			dbHelper.close();
			
			
		} else {
			Log.e(TAG, "Received unexpected intent " + intent.toString());   
		}
	}
}
