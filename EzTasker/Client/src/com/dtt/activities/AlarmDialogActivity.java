package com.dtt.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.SmsManager;
import android.view.KeyEvent;

import com.dtt.R;
import com.dtt.db.DataDbAdapter;
import com.dtt.objs.GlobalConstants;
import com.dtt.objs.HTTPConnection;
import com.dtt.preferences.CareTakerPreferences;

/**
 * This activity is called when alarm manager activates pending intent
 * to notify user that there is a scheduled task awaiting 
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class AlarmDialogActivity extends Activity {
	// dialog id for giving user select either perform a task or not
	private static final int DIALOG_ALERT_OPTION_ID = 0;
	// dialog id for ignoring a scheduled task
	private static final int DIALOG_ALERT_IGNORE_ID = 1;
	// dialog id for notifying there is at least one task not done yet when client boots
	private static final int DIALOG_ALERT_PASSED_ID = 2;
	
	
	// activity id for TaskStepView activity
	private static final int ACTIVITY_TASK_STEP_VIEW = 0;
	// id for notification manager 
	private static final int NOTIFICATION_ID = 0;

	private Long mScheduleId = null;
	private Long mTaskId = null;
	private String mTaskName = "";
	private String mTime = "";
	private int mImportant = 0;
	
	private int mAlarmType;

	private NotificationManager mNManager;
	private Notification mNotification;

	private DataDbAdapter mDbHelper;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// open database
		mDbHelper = new DataDbAdapter(this);
		mDbHelper.open();
		// restore data fields from a bundle
		restoreFields(savedInstanceState);
		// start a notification for user
		showDialog(mAlarmType);
	}

	/**
	 * create dialogs to notify user that there is a scheduled task awaiting
	 * and make sure if user really wants to ignore
	 */
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		// notify user that there is a scheduled task awaiting
		case DIALOG_ALERT_OPTION_ID:
			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			builder1.setMessage("Scheduled to do the Task: \n" + mTaskName + " at " + mTime)
			.setCancelable(false)
			.setIcon(R.drawable.ic_app_small)
			.setTitle(R.string.app_name)
			.setPositiveButton(R.string.start, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
					mNManager.cancel(NOTIFICATION_ID);
					// start activity to guide a user step by step task
					Intent i = new Intent(AlarmDialogActivity.this, TaskStepViewActivity.class);
					i.putExtra(DataDbAdapter.KEY_ID, mScheduleId);
					i.putExtra(DataDbAdapter.KEY_TASK_ID, mTaskId);
					startActivityForResult(i, ACTIVITY_TASK_STEP_VIEW);
				}
			})
			.setNegativeButton(R.string.ignore, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
					showDialog(DIALOG_ALERT_IGNORE_ID);
				}
			});
			dialog = builder1.create();
			break;
		// make sure if user really wants to ignore
		case DIALOG_ALERT_IGNORE_ID:
			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			builder2.setMessage("Are you sure to ignore this task?")
			.setCancelable(false)
			.setIcon(R.drawable.ic_app_small)
			.setTitle(R.string.app_name)
			.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
					mNManager.cancel(NOTIFICATION_ID);
					// if sms notification was set up for this schedule, send predefined message to care taker
					SharedPreferences settings =
						PreferenceManager.getDefaultSharedPreferences(getBaseContext());
					String carePNum = settings.getString(CareTakerPreferences.KEY_CARE_TAKER_PHONE_NUMBER, "");
					String userPNum = settings.getString(CareTakerPreferences.KEY_USER_PHONE_NUMBER, "");
					SmsManager smsManager = SmsManager.getDefault();
					String message = settings.getString(CareTakerPreferences.KEY_USER_NAME, "This person");
					message = message + " skipped the important task, " + mTaskName 
					+ ", scheduled at " + mTime + ". Take an action either call him or reschedule the task.";

					if(mImportant == 1 && carePNum.length() == 10)
						smsManager.sendTextMessage(carePNum, null, message, null, null);
					if(userPNum.length() != 10)
						finish();
					mDbHelper.updateScheduleData(mScheduleId, GlobalConstants.SKIPPED);
					// update server for ignored schedule
					HTTPConnection c = new HTTPConnection(userPNum, GlobalConstants.SCHEDULE, GlobalConstants.UPDATE_SERVER, 
							GlobalConstants.UPDATE, mTaskId, mTime, GlobalConstants.SKIPPED, mImportant);
					c.connect(getBaseContext());
					// postpone alarm
					finish();
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
					showDialog(DIALOG_ALERT_OPTION_ID);
				}
			});
			dialog = builder2.create();
			break;
			// notify user that there is a scheduled task awaiting
		case DIALOG_ALERT_PASSED_ID:
			AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
			builder3.setMessage("You have tasks not performed yet! Check your schedule!")
			.setCancelable(false)
			.setIcon(R.drawable.ic_app_small)
			.setTitle(R.string.app_name)
			.setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					dialog.dismiss();
					mNManager.cancel(NOTIFICATION_ID);
					// go to my schedule list
					Intent i = new Intent();
					i.setClass(AlarmDialogActivity.this, MyScheduleListTabActivity.class);
					startActivity(i);
					finish();
				}
			});
			dialog = builder3.create();
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	/**
	 * restore data fields from bundle
	 * @param savedInstanceState
	 */
	private void restoreFields(Bundle savedInstanceState) {
		Bundle extras = getIntent().getExtras();

		if (savedInstanceState != null) {
			mScheduleId = savedInstanceState.getLong(DataDbAdapter.KEY_ID);
			mTaskId = savedInstanceState.getLong(DataDbAdapter.KEY_TASK_ID);
			mTaskName = savedInstanceState.getString(DataDbAdapter.KEY_TASK_NAME);
			mTime = savedInstanceState.getString(DataDbAdapter.KEY_TIME);
		} else if (extras != null) {
			mScheduleId = extras.getLong(DataDbAdapter.KEY_ID);
			mTaskId = extras.getLong(DataDbAdapter.KEY_TASK_ID);
			mTaskName = extras.getString(DataDbAdapter.KEY_TASK_NAME);
			mTime = extras.getString(DataDbAdapter.KEY_TIME);
		}
		// if this alarm is for notifying existence of tasks not done at boot time
		if(mScheduleId == GlobalConstants.DEFAULT_LONG 
				&& mTaskId == GlobalConstants.DEFAULT_LONG) {
			mAlarmType = DIALOG_ALERT_PASSED_ID;
		// otherwise
		} else {
			mAlarmType = DIALOG_ALERT_OPTION_ID;
			Cursor c = null;
			try{
				c = mDbHelper.fetchScheduleData(mScheduleId);
				if(c.getCount() == 0) {
					finish();
					return;
				} else
					mImportant = c.getInt(c.getColumnIndex(DataDbAdapter.KEY_IMPORTANT));
			} finally {
				if(c != null)
					c.close();
			}
		}
		mNManager = (NotificationManager)this.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotification = new Notification();
		// Notification LED
		mNotification.ledARGB = 100;
		mNotification.flags |= Notification.FLAG_SHOW_LIGHTS;
		// Notification VIBRATE
		mNotification.defaults |= Notification.DEFAULT_VIBRATE;
		// Notification SOUND
		mNotification.audioStreamType = AudioManager.STREAM_ALARM;
		mNotification.defaults |= Notification.DEFAULT_SOUND;
		// notify until canceled
		mNotification.flags |= Notification.FLAG_INSISTENT;

		// start to notify for the assigned task
		mNManager.notify(NOTIFICATION_ID, mNotification);

	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putLong(DataDbAdapter.KEY_ID, mScheduleId);
		outState.putLong(DataDbAdapter.KEY_TASK_ID, mTaskId);
		outState.putString(DataDbAdapter.KEY_TASK_NAME, mTaskName);
		outState.putString(DataDbAdapter.KEY_TIME, mTime);
		mNManager.cancel(NOTIFICATION_ID);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		switch(requestCode) {
		// when TaskStepView activity closes, finish this activity
		case ACTIVITY_TASK_STEP_VIEW:
			Intent i = new Intent();
			i.setClass(AlarmDialogActivity.this, MyScheduleListTabActivity.class);
			startActivity(i);
			finish();
			break;
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

	@Override
	protected void onDestroy() {
		// close databse
		mDbHelper.close();
		super.onDestroy();
	}


}
