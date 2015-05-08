package com.dtt.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dtt.R;
import com.dtt.db.DataDbAdapter;
import com.dtt.objs.GlobalConstants;
import com.dtt.objs.HTTPConnection;
import com.dtt.preferences.CareTakerPreferences;

/**
 * This activity shows users steps for specific task step by step
 * with instruction, picture, video, audio guide
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class TaskStepViewActivity extends Activity {
	
	public static final String CARE_TAKER = "care taker";
    public static final int USER_VAL = 0;
    public static final int CARE_TAKER_VAL = 1;
    
	private Long mScheduleId;
	private Long mStepId;
	private Long mStepNum;
	private Long mTaskId;
	
	private String mPictureUri = "";
	private String mAudioUri = "";
	private String mVideoUri = "";
	
	private TextView mStepNumView;
	private TextView mStepInstView;
	private ImageView mPictureView;
	
	Button mPlayAudio;
    Button mPlayVideo;
    Button mPrev;
    Button mDone;
    Button mNext;
	
	private DataDbAdapter mDbHelper; 
	private Cursor mCursor;
	
	private int mCareTaker;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_step_view);
		setTitle(getString(R.string.app_name) + " > Step by Step");
		
        mStepNumView = (TextView)findViewById(R.id.task_step_view_num);
        mStepInstView = (TextView)findViewById(R.id.task_step_view_inst);
        mPictureView = (ImageView)findViewById(R.id.task_step_view_picture);
        
        // open DB
        mDbHelper = new DataDbAdapter(this);
        mDbHelper.open();
        
        // restore fiels from budle
        restoreFields(savedInstanceState);
        
        mPlayAudio = (Button)findViewById(R.id.task_step_view_play_audio);
        mPlayVideo = (Button)findViewById(R.id.task_step_view_play_video);
        mDone = (Button)findViewById(R.id.task_step_view_done);
        mPrev = (Button)findViewById(R.id.task_step_view_prev);
        mNext = (Button)findViewById(R.id.task_step_view_next);
        
        mPlayAudio.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if(mAudioUri.compareTo("") != 0) {
        			Intent i = new Intent(Intent.ACTION_VIEW);
        			i.setData(Uri.parse(mAudioUri));
        			startActivity(i);
        		}
        	}
        });
        mPlayVideo.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if(mVideoUri.compareTo("") != 0) {
        			Intent i = new Intent(Intent.ACTION_VIEW);
        			i.setData(Uri.parse(mVideoUri));
        			Log.e("stepid", ""+ mStepId);
        			startActivity(i);
        		}
        	}
        });
        // if this activity started from user after he/she saw alarm, update server as this
        //schedule finished
        mDone.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		Log.e("done: ", "0");
        		if(mCareTaker == USER_VAL) {
        			Log.e("done: ", "1");
        			mDbHelper.updateScheduleData(mScheduleId, GlobalConstants.DONE);
        			String time = "";
        			int sms = 0;
        			Cursor cursor = null;
        			try {
        				Log.e("done: ", "2");
        				cursor = mDbHelper.fetchScheduleData(mScheduleId);
        				cursor.moveToFirst();
        				time = cursor.getString(cursor.getColumnIndex(DataDbAdapter.KEY_TIME));
        				sms = cursor.getInt(cursor.getColumnIndex(DataDbAdapter.KEY_IMPORTANT));
        			} finally {
        				if(cursor != null)
        					cursor.close();
        			}
        			Log.e("done: ", "3");
        			SharedPreferences settings =
		                   PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		        	String pNum = settings.getString(CareTakerPreferences.KEY_USER_PHONE_NUMBER, "");
		        	if(pNum.length() == 10) {
		        		// update web server to indicate this schedule is done by user
		        		HTTPConnection c = new HTTPConnection(pNum, GlobalConstants.SCHEDULE, GlobalConstants.UPDATE_SERVER, 
		        				GlobalConstants.UPDATE, mTaskId, time, GlobalConstants.DONE, sms);
		        		c.connect(getBaseContext());
		        	}
		        	Log.e("done: ", "4");
        		}
        		Log.e("done: ", "5");
        		finish();
        		Log.e("done: ", "6");
        	}
        });
        // move to previous step
        mPrev.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		mCursor.moveToPrevious();
        		mStepId = mCursor.getLong(mCursor.getColumnIndexOrThrow(DataDbAdapter.KEY_ID));
        		populateFields();
        	}
        });
        // move to next step
        mNext.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		mCursor.moveToNext();
        		mStepId = mCursor.getLong(mCursor.getColumnIndexOrThrow(DataDbAdapter.KEY_ID));
        		populateFields();
        	}
        });
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
    		mStepNum = savedInstanceState.getLong(DataDbAdapter.KEY_STEP_NUM);
    		mCareTaker = savedInstanceState.getInt(CARE_TAKER);
    	} else if (extras != null) {
    		mScheduleId = extras.getLong(DataDbAdapter.KEY_ID);
    		mTaskId = extras.getLong(DataDbAdapter.KEY_TASK_ID);
    		mStepNum = extras.getLong(DataDbAdapter.KEY_STEP_NUM);
    		mCareTaker = extras.getInt(CARE_TAKER);
    	}
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	if(mTaskId != null) {
    		mCursor = mDbHelper.fetchAllStepsDatumForTask(mTaskId);
    		startManagingCursor(mCursor);
    		mCursor.moveToFirst();
    		if(mStepNum != 0) 
    			mCursor.move(mStepNum.intValue() - 1);
    		mStepId = mCursor.getLong(mCursor.getColumnIndexOrThrow(DataDbAdapter.KEY_ID));
    	}
    	populateFields();
    }

    /**
     * display contents to the screen
     */
    private void populateFields() {
    	if(mStepId != null) {
    		Cursor data = null;
    		try{
        		data = mDbHelper.fetchStepData(mStepId);
            	mStepNum = data.getLong(
        				data.getColumnIndexOrThrow(DataDbAdapter.KEY_STEP_NUM));
            	mPictureUri = data.getString(
            			data.getColumnIndexOrThrow(DataDbAdapter.KEY_PICTURE));
           		mAudioUri = data.getString(
            			data.getColumnIndexOrThrow(DataDbAdapter.KEY_AUDIO));
            	mVideoUri = data.getString(
           					data.getColumnIndexOrThrow(DataDbAdapter.KEY_VIDEO));
           		mStepNumView.setText("STEP " + mStepNum);
           		mStepInstView.setText(data.getString(
        				data.getColumnIndexOrThrow(DataDbAdapter.KEY_INSTRUCTION)));
           		
           		mPictureView.setImageURI(Uri.parse(mPictureUri));
           		
           		if (mAudioUri.compareTo("") == 0)
           			mPlayAudio.setVisibility(Button.INVISIBLE);
           		else
           			mPlayAudio.setVisibility(Button.VISIBLE);
           		
           		if (mVideoUri.compareTo("") == 0)
           			mPlayVideo.setVisibility(Button.INVISIBLE);
           		else
           			mPlayVideo.setVisibility(Button.VISIBLE);
           		
           		if (mCursor.isFirst())
           			mPrev.setVisibility(Button.INVISIBLE);
           		else
           			mPrev.setVisibility(Button.VISIBLE);
           		if (mCursor.isLast()) {
           			mNext.setVisibility(Button.INVISIBLE);
           			mDone.setVisibility(Button.VISIBLE);
           		} else {
           			mNext.setVisibility(Button.VISIBLE);
           			mDone.setVisibility(Button.INVISIBLE);
           		}
    		} finally {
    			if(data != null)
    				data.close();
    		}
    	}
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DataDbAdapter.KEY_ID, mScheduleId);
        outState.putLong(DataDbAdapter.KEY_TASK_ID, mTaskId);
        outState.putLong(DataDbAdapter.KEY_STEP_NUM, mStepNum);
        outState.putInt(CARE_TAKER, mCareTaker);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	if(mCursor != null)
    		mCursor.close();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	mDbHelper.close();
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
