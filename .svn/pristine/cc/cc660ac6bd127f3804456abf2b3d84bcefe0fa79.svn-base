package com.dtt.activities;


import java.io.File;
import java.io.FileNotFoundException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.dtt.R;
import com.dtt.db.DataDbAdapter;
import com.dtt.objs.GlobalConstants;

/**
 * This activity is for creating or editing one step for a task.
 * Care taker can input instruction, picture, audio, video aid
 * for user.
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class TaskStepModifierActivity extends Activity{
	
	// request code for taking picture activity
	private static final int ACTIVITY_TAKE_PICTURE = 0;
	// request code for recording audio activity
	private static final int ACTIVITY_RECORD_AUDIO = 1;
	// request code for taking video activity
	private static final int ACTIVITY_TAKE_VIDEO = 2;
	
	// dialog id of alerting if there was no data recorded
	private static final int DIALOG_ALERT_ID = 3;
	
	private Long mStepId;
	private Long mTaskId;
	private Long mStepNum;
	
	private String mPictureUri = "";
	private String mAudioUri = "";
	private String mVideoUri = "";
	
	private TextView mStepInst;
	private TextView mStepNumView;
	
	private ImageButton mPictureTaken;
	private ImageButton mVideoTaken;
	private ImageButton mAudioTaken;
	
	private DataDbAdapter mDbHelper;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_step_modifier);
        setTitle(getString(R.string.app_name) + " > Edit Step");
        
        mStepInst = (TextView)findViewById(R.id.task_step_modifier_inst);
        mStepNumView = (TextView)findViewById(R.id.task_step_modifier_num);
        
        // open DB
        mDbHelper = new DataDbAdapter(this);
        mDbHelper.open();
        
        Button takePicture = (Button)findViewById(R.id.task_step_modifier_take_picture);
        Button recordAudio = (Button)findViewById(R.id.task_step_modifier_record_audio);
        Button takeVideo = (Button)findViewById(R.id.task_step_modifier_take_video);
        
        mPictureTaken = (ImageButton)findViewById(R.id.task_step_modifier_picture_taken);
        mVideoTaken = (ImageButton)findViewById(R.id.task_step_modifier_video_taken);
        mAudioTaken = (ImageButton)findViewById(R.id.task_step_modifier_audio_taken);
        
        Button help = (Button)findViewById(R.id.task_step_help);
        Button done = (Button)findViewById(R.id.task_step_done);
        
        // restore mTaskId
        restoreFields(savedInstanceState);        
        
        // start image capture activity
        takePicture.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        		String filePath = "/sdcard/tmps.jpg";
        		i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(filePath)));
        		startActivityForResult(i, ACTIVITY_TAKE_PICTURE);
        	}
        }); 
        // start audio capture activity
        recordAudio.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		Intent i = new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
        		startActivityForResult(i, ACTIVITY_RECORD_AUDIO);
        	}
        }); 
        // start video capture activity
        takeVideo.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		Intent i = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        		startActivityForResult(i, ACTIVITY_TAKE_VIDEO);
        	}
        });
        // show dialog for care taker direction
        help.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		showDialog(GlobalConstants.DIALOG_HELP_ID);
        	}
        });
        // finish this activity if there is at least one data recorded
        done.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if( mStepInst.getText().toString().compareTo("") != 0
        				|| mPictureUri.compareTo("") != 0
        				|| mAudioUri.compareTo("") != 0
        				|| mVideoUri.compareTo("") != 0)
        			finish();
        		else
        			showDialog(DIALOG_ALERT_ID);
        	}
        });
	}
	
	/**
     * restore mStepId from bundle
     * @param savedInstanceState
     */
    private void restoreFields(Bundle savedInstanceState) {
    	mStepId = savedInstanceState != null ? savedInstanceState.getLong(DataDbAdapter.KEY_ID) 
    			: null;
    	if (mStepId == null) {
    		Bundle extras = getIntent().getExtras();            
    		mStepId = extras != null ? extras.getLong(DataDbAdapter.KEY_ID) 
    				: null;
    	}
    }
        
    /**
     * display contents to the screen
     */
    private void populateFields() {
    	if (mStepId != null) {
    		Cursor data = null;
    		try {
    			data = mDbHelper.fetchStepData(mStepId);	
    			mTaskId = data.getLong(
    				data.getColumnIndexOrThrow(DataDbAdapter.KEY_TASK_ID));
    			mStepNum = data.getLong(
					data.getColumnIndexOrThrow(DataDbAdapter.KEY_STEP_NUM));
    			mStepInst.setText(data.getString(
    				data.getColumnIndexOrThrow(DataDbAdapter.KEY_INSTRUCTION)));
    			mPictureUri = data.getString(
    				data.getColumnIndexOrThrow(DataDbAdapter.KEY_PICTURE));
    			mAudioUri = data.getString(
    				data.getColumnIndexOrThrow(DataDbAdapter.KEY_AUDIO));
    			mVideoUri = data.getString(
    				data.getColumnIndexOrThrow(DataDbAdapter.KEY_VIDEO));
    		
    			mStepNumView.setText("STEP " + mStepNum);
    			if(mPictureUri.compareTo("") != 0)
    				mPictureTaken.setBackgroundResource(R.drawable.ic_check);
    			else
    				mPictureTaken.setBackgroundResource(R.drawable.ic_check_bw);
    			if(mVideoUri.compareTo("") != 0)
    				mVideoTaken.setBackgroundResource(R.drawable.ic_check);
    			else
    				mVideoTaken.setBackgroundResource(R.drawable.ic_check_bw);
    			if(mAudioUri.compareTo("") != 0)
    				mAudioTaken.setBackgroundResource(R.drawable.ic_check);
    			else
    				mAudioTaken.setBackgroundResource(R.drawable.ic_check_bw);
    		} finally {
    			if(data != null)
    				data.close();
    		}
    	} 
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        populateFields();        
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }
    
    @Override
    protected void onDestroy() {
    	mDbHelper.close();
        super.onDestroy();
    }
    
    /**
     * save contents of data into database
     */
    private void saveState() {
    	String stepInst = mStepInst.getText().toString();
    	if(mStepId == null) {
    		long id = mDbHelper.createStepsData(mTaskId, stepInst, 
        			mPictureUri, mAudioUri, mVideoUri);
            if (id > 0) {
            	mTaskId = id;
            }
    	} else 
    		mDbHelper.updateStepsData(mStepId, mTaskId, mStepNum, stepInst, 
    			mPictureUri, mAudioUri, mVideoUri);
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putLong(DataDbAdapter.KEY_ID, mStepId);
    }
    
    /**
     * create dialogs
     */
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog;
    	switch(id) {
    		// alerting if there was no data recorded
    		case DIALOG_ALERT_ID:
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setMessage("Please record at least one of data type!")
    			       .setCancelable(false)
    			       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   dialog.dismiss();
    			           }
    			       });
    			dialog = builder.create();
    			break;
    		// care taker direction
    		case GlobalConstants.DIALOG_HELP_ID:
    			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
    			builder1.setMessage(R.string.edit_step_help)
    			       .setCancelable(false)
    			       .setTitle("Direction")
    			       .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   dialog.dismiss();
    			        	   
    			           }
    			       });
    			dialog = builder1.create();
    			break;
    		default:
    			dialog = null;
    	}
    	return dialog;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
    	super.onActivityResult(requestCode, resultCode, intent);
    	switch(requestCode) {
    		// store uri of the taken picture from taking picture activity
    		case ACTIVITY_TAKE_PICTURE:
    			File f = new File("/sdcard/tmps.jpg");
    			try {
    				mPictureUri = android.provider.MediaStore.Images.Media.insertImage(getContentResolver(),
    						f.getAbsolutePath(), null, null);
    			} catch (FileNotFoundException e) {
    				e.printStackTrace();
    			}
    			break;
    		// store uri of the recorded audio from recording audio activity
    		case ACTIVITY_RECORD_AUDIO:
    			mAudioUri = intent.getDataString();
    			break;
    		// store uri of the recorded video from recording video activity
    		case ACTIVITY_TAKE_VIDEO:
    			try {
    				mVideoUri = intent.getDataString();
    			} catch(Exception e) {
    				
    			}
    			break;
		}
    	// update database after returning from activities
    	saveState();
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
     

