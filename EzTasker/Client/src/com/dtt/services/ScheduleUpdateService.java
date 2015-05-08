package com.dtt.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.ConditionVariable;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dtt.activities.ScheduleListActivity;
import com.dtt.db.DataDbAdapter;
import com.dtt.objs.GlobalConstants;
import com.dtt.objs.HTTPConnection;
import com.dtt.preferences.CareTakerPreferences;

/**
 * Background running thread service for updating all schedule modification 
 * made on web side to sync with phone and updating web with cache data. 
 * This update happens every 5 minutes.
 * 
 * @author Moon Hwan Oh, Amanda Shen
 * 
 */
public class ScheduleUpdateService extends Service {
 
	private ConditionVariable mCondition;
	private DataDbAdapter mDbHelper;

	@Override
	public void onCreate() {
		Log.e("ScheduleUpdateService", "service started");
		// Start up the thread running the service. Separate thread was inevitable because 
		// the service normally runs in the process's main thread, which we don't want to block.
		Thread updatingScheduleThread = new Thread(null, mUpdate, "ScheduleUpdateService");
		mCondition = new ConditionVariable(false);
		mDbHelper = new DataDbAdapter(this);
		updatingScheduleThread.start();
	}

	@Override
	public void onDestroy() {
		// stop this thread
		mCondition.open();
		Log.e("ScheduleUpdateService", "service destroyed");
	}

	private Runnable mUpdate = new Runnable() {
		public void run() {
			while (true) {
				if (mCondition.block(GlobalConstants.UPDATE_FREQUENCY * 1000)) 
					break;
				Boolean differentDate = false;
				String currentDate = ScheduleListActivity.getCurrentDateInString();
				SharedPreferences myPrefs = getBaseContext().getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
				String cachedDate = myPrefs.getString("date", "");
				if(currentDate.compareTo(cachedDate) != 0)
					differentDate = true;
				
				SharedPreferences settings =
					PreferenceManager.getDefaultSharedPreferences(getBaseContext());
				String pNum = settings.getString(CareTakerPreferences.KEY_USER_PHONE_NUMBER, "");
				Log.e("ScheduleUpdateService", "pNum = " + pNum);
				if(pNum.length() == 10) {
					mDbHelper.open();
					// rip off database and ask server to clear its schedule for now
					if(differentDate) {
						mDbHelper.deleteAllScheduleData();
						SharedPreferences.Editor prefsEditor = myPrefs.edit();
						prefsEditor.putString("webUpdateCache", "");
						prefsEditor.putString("date", currentDate);
						prefsEditor.commit();
						HTTPConnection c = new HTTPConnection(pNum, GlobalConstants.SCHEDULE, GlobalConstants.UPDATE_SERVER, 
								GlobalConstants.CLEAR, GlobalConstants.DEFAULT_LONG, GlobalConstants.DEFAULT_STRING, 
								GlobalConstants.DEFAULT_INT, GlobalConstants.DEFAULT_INT);
						c.connect(getBaseContext());
					} else {
						// otherwise, update phone's data from server
						HTTPConnection c = new HTTPConnection(pNum, GlobalConstants.SCHEDULE, GlobalConstants.UPDATE_ANDROID, 
								GlobalConstants.UPDATE, GlobalConstants.DEFAULT_INT, GlobalConstants.DEFAULT_STRING, 
								GlobalConstants.DEFAULT_INT, GlobalConstants.DEFAULT_INT);
						c.connectToDrainCacheToWeb(getBaseContext());
						c.connect(mDbHelper, getBaseContext());
					}
					mDbHelper.close();
				}
			}
			ScheduleUpdateService.this.stopSelf();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}