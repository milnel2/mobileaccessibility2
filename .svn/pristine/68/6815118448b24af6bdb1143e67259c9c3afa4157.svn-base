package com.dtt.objs;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.util.Log;

import com.dtt.activities.AddScheduleActivity;
import com.dtt.db.DataDbAdapter;

/**
 * HTTP connection class. It sets all parameter for session information in url, 
 * and then it requests for the web to update task or schedule. It also request
 * web to provide to sync with phone's schedule 
 * 
 * @author Moon Hwan Oh, Amenda Shen
 *
 */
public class HTTPConnection {
	
	private int type = -1;
	private int update = -1;
	private long tId = -1;
	private String tName = "";
	private String time = "";
	private int command = -1;
	private int status = -1;
	private int sms = -1;
	private String urlParams;
	
	/**
	 * Constructor for establishing task update
	 * 
	 */
	public HTTPConnection(String pNum, int type, int update, int command,
			long tId, String tName) {
		this.type = type;
		this.update = update;
		this.command = command;
		this.tId = tId;
		this.tName = tName;
		urlParams = "p_id=" + pNum + "&type=" + type + "&update=" + update + "&string="
			+ command + "," + tId + "," + tName.replaceAll(" ", "%20");
	}
	
	/**
	 * Constructor for establishing schedule update
	 * 
	 */
	public HTTPConnection(String pNum, int type, int update, int command, 
			long tId, String time, int status, int sms) {
		this.type = type;
		this.update = update;
		this.command = command;
		this.tId = tId;
		this.time = time;
		this.status = status;
		this.sms = sms;
		urlParams = "p_id=" + pNum + "&type=" + type + "&update=" + update + "&string="
			+ command + "," + tId + "," + time.replace(":", "") + "," + sms + "," + status;
	}
	
	/**
	 * Connect to the web to update web's task and schedule
	 */
	public void connect(Context context) {
		try{
        	URL u = new URL(GlobalConstants.SERVER_URL + urlParams + ";!");
        	URLConnection connection = u.openConnection();
        	HttpURLConnection httpConnection = (HttpURLConnection)connection;
        	httpConnection.setConnectTimeout(GlobalConstants.CONNECTION_TIMEOUT);
        	httpConnection.setReadTimeout(GlobalConstants.CONNECTION_TIMEOUT);
            Log.e("http", GlobalConstants.SERVER_URL + urlParams + ";!");
        	int responseCode = httpConnection.getResponseCode();
        	if(responseCode != HttpURLConnection.HTTP_OK) {
        		// if connection fails, cache data
        		SharedPreferences myPrefs = context.getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
        		SharedPreferences.Editor prefsEditor = myPrefs.edit();
        		String cachedData = myPrefs.getString("webUpdateCache", "");
        		prefsEditor.putString("webUpdateCache", cachedData + urlParams + ";");
        		prefsEditor.commit();
        	} 
        	httpConnection.disconnect();
        } catch(MalformedURLException e) {
        	Log.e("HTTPCONNECTION", e.getMessage());
        } catch(IOException e) {
        	Log.e("HTTPCONNECTION", e.getMessage());
        } 
	}
	
	/**
	 * Connect to the web to update phone's schedule
	 * @param db
	 */
	public void connect(DataDbAdapter db, Context context) {
		try{
        	URL u = new URL(GlobalConstants.SERVER_URL + urlParams + ";!");
        	URLConnection connection = u.openConnection();
        	HttpURLConnection httpConnection = (HttpURLConnection)connection;
        	httpConnection.setConnectTimeout(GlobalConstants.CONNECTION_TIMEOUT);
        	httpConnection.setReadTimeout(GlobalConstants.CONNECTION_TIMEOUT);
            
        	int responseCode = httpConnection.getResponseCode();
        	if(responseCode == HttpURLConnection.HTTP_OK) {
        		BufferedReader in = new BufferedReader(
						new InputStreamReader(httpConnection.getInputStream())); 
        		// get all schedules modified from care taker remotely(in the web)
        		String str = in.readLine();
        		if(str.compareTo("") == 0 || str.compareTo("\n") == 0)
        			return;
        		Log.e("ScheduleUpdateService", "update list = " + str);
        		// for every operation, parse them and then update schedule table in the DB
        		String[] updateList = str.split(";");
        		for(String s : updateList) {
        			String[] element = s.split(",");
        			if(element.length != 5)
        				return;
        			int command = Integer.parseInt(element[0]);
        			long taskId = new Long(element[1]);
        			String time = element[2];
        			Log.e("ScheduleUpdateService", "time before fix = " + time);
        			if(time.length() == 3)
        				time = "0" + time;
        			time = time.subSequence(0, 2) + ":" + time.subSequence(2, 4);
        			Log.e("ScheduleUpdateService", "time after fix = " + time);
        			int important = Integer.parseInt(element[3]);
        			int flag = Integer.parseInt(element[4]);
        			
        			if(command == GlobalConstants.ADD) {
        				long scheduleId = db.createScheduleData(time, taskId, important);
        				Cursor c = db.fetchTaskData(taskId);
        				AddScheduleActivity.setAlarm(context, scheduleId, taskId, 
        						c.getString(c.getColumnIndex(DataDbAdapter.KEY_TASK_NAME)), time);
        				c.close();
        			} else if(command == GlobalConstants.DELETE){
        				db.deleteScheduleData(taskId, time, flag, important);
        			}
        		}
        		httpConnection.disconnect();
        	} else {
        		// do something for connection failed
        	}
        } catch(MalformedURLException e) {
        	Log.e("HTTPCONNECTION", e.getMessage());
        } catch(IOException e) {
        	Log.e("HTTPCONNECTION", e.getMessage());
        } 
	}
	
	/**
	 * Connect to the web to update web's task and schedule which was cached due to connection failure
	 */
	public void connectToDrainCacheToWeb(Context context) {
		try{
			SharedPreferences myPrefs = context.getSharedPreferences("myPrefs", Context.MODE_WORLD_READABLE);
			String cache = myPrefs.getString("webUpdateCache", "");
			Log.e("cached data: ", cache);
			// if there is no data to update, return.
			if(cache.compareTo("") == 0)
				return;
			
			String[] cachedData = cache.split(";");
			
			for(String data : cachedData) {
			
	        	URL u = new URL(GlobalConstants.SERVER_URL + data + ";!");
	        	
	        	URLConnection connection = u.openConnection();
	        	HttpURLConnection httpConnection = (HttpURLConnection)connection;
	        	httpConnection.setConnectTimeout(GlobalConstants.CONNECTION_TIMEOUT);
	        	httpConnection.setReadTimeout(GlobalConstants.CONNECTION_TIMEOUT);
	            
	        	int responseCode = httpConnection.getResponseCode();
	        	
	        	httpConnection.disconnect();
	        	//if connection fails return
	        	if(responseCode != HttpURLConnection.HTTP_OK)
	        		return;
			}
			// drain cache
			SharedPreferences.Editor prefsEditor = myPrefs.edit();
    		prefsEditor.putString("webUpdateCache", "");
    		prefsEditor.commit();
			
        } catch(MalformedURLException e) {
        	Log.e("HTTPCONNECTION", e.getMessage());
        } catch(IOException e) {
        	Log.e("HTTPCONNECTION", e.getMessage());
        } 
	}
}
