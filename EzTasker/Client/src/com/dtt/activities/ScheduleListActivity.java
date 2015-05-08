package com.dtt.activities;

import java.util.ArrayList;
import java.util.Calendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.dtt.R;
import com.dtt.adapters.ScheduleListAdapter;
import com.dtt.db.DataDbAdapter;
import com.dtt.objs.GlobalConstants;
import com.dtt.objs.HTTPConnection;
import com.dtt.objs.Schedule;
import com.dtt.preferences.CareTakerPreferences;

/**
 * Activity to display schedule list. This is used for "My Schedule" for user
 * and "Schedule Builder" for care taker
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class ScheduleListActivity extends ListActivity {
	public static final String CARE_TAKER = "care taker";
	public static final int USER_VAL = 0;
	public static final int CARE_TAKER_VAL = 1;

	public static final String SCHEDULE_LIST_MODE = "list mode";
	public static final int ALL_SCHEDULE_MODE = 0;
	public static final int UPCOMING_SCHEDULE_MODE = 1;
	public static final int PASSED_SCHEDULE_MODE = 2;

	// dialog id of schedule list for care taker when he/she delete
	//certain schedule
	private static final int DIALOG_ALERT_DELETE_ID = 0;
	// dialog id for direction for care taker
	private static final int DIALOG_CARE_TAKER_HELP_ID = 1;
	// dialog id for direction for user
	private static final int DIALOG_USER_HELP_ID = 2;

	private Schedule mSchedule;

	private DataDbAdapter mDbHelper;

	private int mCareTaker;

	private int mListMode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.schedule_list);
		// open db
		mDbHelper = new DataDbAdapter(this);
		mDbHelper.open();
		
		// set up this activity for care taker or user
		mCareTaker = getIntent().getIntExtra(CARE_TAKER, USER_VAL);
		// set up this activity for what kind list it is for
		mListMode = getIntent().getIntExtra(SCHEDULE_LIST_MODE, ALL_SCHEDULE_MODE);

		Button helpBtn = (Button) findViewById(R.id.schedule_list_help);
		Button exitBtn = (Button) findViewById(R.id.schedule_list_exit);

		helpBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(mCareTaker == USER_VAL) {
					// show direction for user
					showDialog(DIALOG_USER_HELP_ID);
				} else
					// show direction for care taker
					showDialog(DIALOG_CARE_TAKER_HELP_ID);
			}	
		});
		exitBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// finish this activity
				finish();
			}	
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// fill out schedule list
		fillScheduleList();
	}

	/**
	 * fill out schedules list for a specific tab within Tab activity 
	 */
	private void fillScheduleList() {
		ArrayAdapter<Schedule> stepListAdapter =
			new ScheduleListAdapter(this, R.layout.schedule_list_row, getScheduleList());
		setListAdapter(stepListAdapter);
	}

	/**
	 * return schedule list filtered for specific activity.(Up-coming schedule list, Passed schedule
	 * All schedule list)  
	 * 
	 */
	private ArrayList<Schedule> getScheduleList() {
		String currentTime = getCurrentTimeInString();
		Cursor cursor = mDbHelper.fetchAllScheduleDatum();
		cursor.moveToFirst();
		ArrayList<Schedule> scheduleList = new ArrayList<Schedule>(cursor.getCount());

		while(!cursor.isAfterLast()) {
			Long scheduleId = cursor.getLong(cursor.getColumnIndex(DataDbAdapter.KEY_ID));
			Long taskId = cursor.getLong(cursor.getColumnIndex(DataDbAdapter.KEY_TASK_ID));
			String taskName = cursor.getString(cursor.getColumnIndex(DataDbAdapter.KEY_TASK_NAME));
			String time = cursor.getString(cursor.getColumnIndex(DataDbAdapter.KEY_TIME));
			int flag = cursor.getInt(cursor.getColumnIndex(DataDbAdapter.KEY_FLAG));
			int important = cursor.getInt(cursor.getColumnIndex(DataDbAdapter.KEY_IMPORTANT));
			String pictureUri = cursor.getString(cursor.getColumnIndex(DataDbAdapter.KEY_PICTURE));
			if(mListMode == UPCOMING_SCHEDULE_MODE){
				// takes only schedules in the near future
				if(time.compareTo(currentTime) < 0) {
					cursor.moveToNext();
					continue;
				}
			} else if(mListMode == PASSED_SCHEDULE_MODE) {
				// takes only schedules in the past
				if(time.compareTo(currentTime) >= 0) {
					break;
				}	
			}
			scheduleList.add(new Schedule(scheduleId, taskId, taskName, time, flag, important, pictureUri));
			cursor.moveToNext();
		}
		cursor.close();
		return scheduleList;
	}

	/**
	 * return current time in the format of 00:00
	 */
	public static String getCurrentTimeInString() {
		Calendar cal = Calendar.getInstance();
		int hour = cal.get(Calendar.HOUR_OF_DAY);    
		int minute = cal.get(Calendar.MINUTE);
		String currentTime = "";
		if(hour < 10)
			currentTime += 0;
		currentTime = currentTime + hour + ":";
		if(minute < 10)
			currentTime += "0";
		currentTime += minute;
		return currentTime;
	}
	
	public static String getCurrentDateInString() {
		Calendar cal = Calendar.getInstance();
		int month = cal.get(Calendar.MONTH);    
		int day = cal.get(Calendar.DAY_OF_MONTH);
		String currentDate = "";
		if(month < 10)
			currentDate += 0;
		currentDate = currentDate + month + "/";
		if(day < 10)
			currentDate += "0";
		currentDate += day;
		return currentDate;
	}

	/**
	 * create dialogs
	 */
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		// dialog for delete selected schedule
		case DIALOG_ALERT_DELETE_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage("Are you sure to delete this step?")
			.setCancelable(false)
			.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
					// delete the schedule on client
					mDbHelper.deleteScheduleData(mSchedule.getScheduleId());
					SharedPreferences settings =
						PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					String pNum = settings.getString(CareTakerPreferences.KEY_USER_PHONE_NUMBER, "");
					if(pNum.length() == 10) {
						// delete the schedule on web
						HTTPConnection c = new HTTPConnection(pNum, GlobalConstants.SCHEDULE, GlobalConstants.UPDATE_SERVER, 
								GlobalConstants.DELETE, mSchedule.getTaskId(), mSchedule.getTime(), 
								mSchedule.getFlag(), mSchedule.getImportant());
						c.connect(getBaseContext());
					}
					fillScheduleList();
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			});
			dialog = builder.create();
			break;
		// dialog for care taker direction
		case DIALOG_CARE_TAKER_HELP_ID:
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setMessage(R.string.delete_schedule_help)
			.setCancelable(false)
			.setTitle("Direction")
			.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();

				}
			});
			dialog = builder1.create();
			break;
		// dialog for user direction
		case DIALOG_USER_HELP_ID:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setMessage(R.string.my_schedule_help)
			.setCancelable(false)
			.setTitle("Direction")
			.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
				}
			});
			dialog = builder2.create();
			break;

		default:
			dialog = null;
		}
		return dialog;
	}

	@Override
	protected void onDestroy() {
		// close db
		mDbHelper.close();
		super.onDestroy();
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mSchedule = (Schedule) getListAdapter().getItem(position);
		if(mCareTaker == CARE_TAKER_VAL) {
			// if this activity was intended for care taker, pop up delete schedule dialog
			showDialog(DIALOG_ALERT_DELETE_ID);
		} else {
			// if this activity was intended for user, start to guide user step by step task
			if(!mSchedule.isDone()) {
				Intent i = new Intent(ScheduleListActivity.this, TaskStepViewActivity.class);
				i.putExtra(DataDbAdapter.KEY_ID, mSchedule.getScheduleId());
				i.putExtra(DataDbAdapter.KEY_TASK_ID, mSchedule.getTaskId());
				i.putExtra(TaskStepViewActivity.CARE_TAKER, TaskStepViewActivity.USER_VAL);
				startActivity(i);
			} else {
				Toast.makeText(ScheduleListActivity.this, "Finished Task!", Toast.LENGTH_SHORT).show();
			}
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		// disable back button
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

}
