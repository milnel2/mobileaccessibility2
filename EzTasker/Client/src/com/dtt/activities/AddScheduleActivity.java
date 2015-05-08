package com.dtt.activities;

import java.util.Date;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.dtt.R;
import com.dtt.db.DataDbAdapter;
import com.dtt.objs.GlobalConstants;
import com.dtt.objs.HTTPConnection;
import com.dtt.preferences.CareTakerPreferences;

/**
 * Adding an instance of schedule functionality for care taker. 
 * In order to add a task to the schedule list, user needs to 
 * add a certain task at specific time. It also takes flag for
 * sms notification.
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class AddScheduleActivity extends Activity {

	private static final int ACTIVITY_PICK_TASK = 0;

	private Long mScheduleId = null;
	private Long mTaskId = null;
	private TextView mTaskNameView;
	private DataDbAdapter mDbHelper;
	private TimePicker mTimeView;
	private String mTime;
	private CheckBox mImportantView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.add_schedule);

		// initialize views for this activity
		mTimeView = (TimePicker) findViewById(R.id.add_schedule_time_picker);
		mTaskNameView = (TextView) findViewById(R.id.add_schedule_task_name);
		Button chooseBtn = (Button) findViewById(R.id.add_schedule_task_selector);
		mImportantView = (CheckBox) findViewById(R.id.add_schedule_important);
		Button AddBtn = (Button) findViewById(R.id.add_schedule_add_task);
		Button helpBtn = (Button) findViewById(R.id.add_schedule_help);
		Button exitBtn = (Button) findViewById(R.id.add_schedule_exit);

		// open database
		mDbHelper = new DataDbAdapter(this);
		mDbHelper.open();

		// restore required fields from bundle
		restoreTaskId(savedInstanceState);

		// link to SearchTaskActivity to take a certain task from it
		chooseBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				Intent i = new Intent(AddScheduleActivity.this, SearchTaskActivity.class);
				i.putExtra(SearchTaskActivity.CARE_TAKER, SearchTaskActivity.CARE_TAKER_VAL);
				startActivityForResult(i, ACTIVITY_PICK_TASK);
			}
		});
		// button for adding a schedule that user wants
		AddBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(mTaskId != null) {
					// getting all data needed for a schedule
					mTime = "";
					int hour = mTimeView.getCurrentHour();
					if(hour < 10)
						mTime += "0";
					mTime = mTime + hour + ":";
					int min = mTimeView.getCurrentMinute();
					if(min < 10)
						mTime += "0";
					mTime += min;
					int sms = (mImportantView.isChecked() ? 1:0);
					if(sms == 1)
						mScheduleId = mDbHelper.createScheduleData(mTime, mTaskId, 1);
					else
						mScheduleId = mDbHelper.createScheduleData(mTime, mTaskId, 0);
					// setting an alarm
					setAlarm(AddScheduleActivity.this, mScheduleId, mTaskId, mTaskNameView.getText().toString(), mTime);
					// send a message to the web to update this schedule
					SharedPreferences settings =
						PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					String pNum = settings.getString(CareTakerPreferences.KEY_USER_PHONE_NUMBER, "");
					if(pNum.length() == 10) {
						HTTPConnection c = new HTTPConnection(pNum, GlobalConstants.SCHEDULE, GlobalConstants.UPDATE_SERVER, 
								GlobalConstants.ADD, mTaskId, mTime, GlobalConstants.NOT_DONE, sms);
						c.connect(getBaseContext());
					} 

					Toast.makeText(AddScheduleActivity.this, "Task added to Schedule!", Toast.LENGTH_SHORT).show();

					// re-initialize all data fields
					mTaskId = null;
					mTaskNameView.setText("");
					mImportantView.setChecked(false);

				} else 
					Toast.makeText(AddScheduleActivity.this, "Choose a task to add!", Toast.LENGTH_SHORT).show();
			}
		});
		// help button to pop up dialog for direction
		helpBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				showDialog(GlobalConstants.DIALOG_HELP_ID);
			}
		});
		// exit button to finish this activity
		exitBtn.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				finish();
			}
		});
	}

	/**
	 * send a pendingIntent to alarm manager to set alarm activity
	 * 
	 * 
	 */
	public static void setAlarm(Context context, Long scheduleId, Long taskId, String taskName, String time) {
		AlarmManager alarms = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

		Date d = new Date();
		d.setHours(Integer.parseInt(time.split(":")[0]));
		d.setMinutes(Integer.parseInt(time.split(":")[1]));
		d.setSeconds(0);
		Intent i = new Intent(context, AlarmDialogActivity.class);
		i.putExtra(DataDbAdapter.KEY_ID, scheduleId);
		i.putExtra(DataDbAdapter.KEY_TASK_ID, taskId);
		i.putExtra(DataDbAdapter.KEY_TASK_NAME, taskName);
		i.putExtra(DataDbAdapter.KEY_TIME, time);

		PendingIntent pendingIntent = PendingIntent.getActivity(context, scheduleId.intValue(), i, PendingIntent.FLAG_UPDATE_CURRENT);
		alarms.set(AlarmManager.RTC_WAKEUP, d.getTime(), pendingIntent);
	}

	@Override
	protected void onResume() {
		super.onResume();
		// populate stored values to views
		populateFields();
	}

	/**
	 * restore mTaskId from bundle
	 * @param savedInstanceState
	 */
	private void restoreTaskId(Bundle savedInstanceState) {
		if(savedInstanceState != null) {
			mTaskId = savedInstanceState.getLong(DataDbAdapter.KEY_TASK_ID); 
			if(mTaskId == -1)
				mTaskId = null;
		}
	}

	/**
	 * populate stored values to views
	 */
	private void populateFields() {
		if(mTaskId != null) {
			Cursor c = null;
			try {
				c = mDbHelper.fetchTaskData(mTaskId);
				mTaskNameView.setText(c.getString(c.getColumnIndex(DataDbAdapter.KEY_TASK_NAME)));
			} finally {
				if(c != null)
					c.close();
			}
		}
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		if(mTaskId != null)
			outState.putLong(DataDbAdapter.KEY_TASK_ID, mTaskId);
		else
			outState.putLong(DataDbAdapter.KEY_TASK_ID, -1);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch(requestCode) {
		// activity result from SearchTaskActivity to add selected task to this activity
		case ACTIVITY_PICK_TASK: 
			mTaskId = (Long) intent.getLongExtra(DataDbAdapter.KEY_TASK_ID, -1);
			if(mTaskId == -1)
				mTaskId = null;
			populateFields();
			break;

		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		// disable back key
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onDestroy() {
		// close database
		mDbHelper.close();
		super.onDestroy();
	}

	/**
	 * create a dialog for direction
	 */
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case GlobalConstants.DIALOG_HELP_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.add_schedule_help)
			.setCancelable(false)
			.setTitle("Direction")
			.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();

				}
			});
			dialog = builder.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

}
