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
import android.widget.ImageView;
import android.widget.TextView;

import com.dtt.R;
import com.dtt.db.DataDbAdapter;
import com.dtt.objs.GlobalConstants;

/**
 * This activity is for creating or editing a task. Care taker
 * can name the task and take a thumnail for it
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class TaskModifierActivity extends Activity {
	
	// request code for taking picture activity
	private static final int ACTIVITY_TAKE_PICTURE=0;
	// request code for step list activity
	private static final int ACTIVITY_STEP_CREATOR=1;
	// id of dialog for alerting to type task name when it's empty
	private static final int DIALOG_ALERT_TYPE_TASK_NAME_ID = 2;
	
	private Long mTaskId;
	private TextView mTaskNameView;
	private String mThumnailUri = "";
	private ImageView mThumnailView;
	private DataDbAdapter mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_modifier);
        setTitle(getString(R.string.app_name) + " > New or Edit Task");

        mTaskNameView = (TextView) findViewById(R.id.task_modifier_task_name);
        mThumnailView = (ImageView) findViewById(R.id.task_modifier_thumnail);
        
        // open DB
        mDbHelper = new DataDbAdapter(this);
        mDbHelper.open();
        
        Button takePictureBtn = (Button) findViewById(R.id.task_modifier_take_picture);
        Button stepsBtn = (Button) findViewById(R.id.task_modifier_steps);
        Button helpButton = (Button) findViewById(R.id.task_modifier_help);
        
        // restore mTaskId from bundle
        restoreRowId(savedInstanceState);
        
        // call camera built in activity to take a picture
        takePictureBtn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        		i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File("/sdcard/tmp.jpg")));
        		startActivityForResult(i, ACTIVITY_TAKE_PICTURE);
        	}	
        });
        // start step list activity
        stepsBtn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if(mTaskNameView.getText().toString().compareTo("") == 0) {
        			showDialog(DIALOG_ALERT_TYPE_TASK_NAME_ID);
        			return;
        		}
        		saveState();
        		Intent i = new Intent(TaskModifierActivity.this, TaskStepsModifierActivity.class);
        		i.putExtra(DataDbAdapter.KEY_TASK_ID, mTaskId);
        		startActivityForResult(i, ACTIVITY_STEP_CREATOR);
        	}
        });
        // show dialog for direction
        helpButton.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		showDialog(GlobalConstants.DIALOG_HELP_ID);
        	}
        		
        });
    }
    
    /**
     * restore taskId from bundle
     * @param savedInstanceState
     */
    private void restoreRowId(Bundle savedInstanceState) {
    	mTaskId = savedInstanceState != null ? savedInstanceState.getLong(DataDbAdapter.KEY_ID) 
    			: null;
    	if (mTaskId == null) {
    		Bundle extras = getIntent().getExtras();            
    		mTaskId = extras != null ? extras.getLong(DataDbAdapter.KEY_ID) 
    				: null;
    	}
    }
        
    /**
     * display contents to the screen
     */
    private void populateFields() {
    	if (mTaskId != null) {
    		Cursor data = null;
    		try{
    			data = mDbHelper.fetchTaskData(mTaskId);
    			mTaskNameView.setText(data.getString(
    				data.getColumnIndexOrThrow(DataDbAdapter.KEY_TASK_NAME)));
    			if(mThumnailUri == "")
    				mThumnailUri = data.getString(
    					data.getColumnIndexOrThrow(DataDbAdapter.KEY_PICTURE));
    			if(mThumnailUri != "")
    				mThumnailView.setImageURI(Uri.parse(mThumnailUri));
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
        String taskName = mTaskNameView.getText().toString();

        if (mTaskId == null) {
            long id = mDbHelper.createTasksData(taskName, mThumnailUri);
            if (id > 0) {
            	mTaskId = id;
            }
        } else {
            mDbHelper.updateTasksData(mTaskId, taskName, mThumnailUri);
        }
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putLong(DataDbAdapter.KEY_ID, mTaskId);
    }
    
    /**
     * create dialogs for alerting to type task id when it's empty and direction
     */
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog;
    	switch(id) {
    		case DIALOG_ALERT_TYPE_TASK_NAME_ID:
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setMessage("Please type this task's name!")
    			       .setCancelable(false)
    			       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   dialog.dismiss();
    			           }
    			       });
    			dialog = builder.create();
    			break;
    		case GlobalConstants.DIALOG_HELP_ID:
    			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
    			builder1.setMessage(R.string.new_or_edit_task_help)
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
    			File f = new File("/sdcard/tmp.jpg");
    			try {
    				mThumnailUri = android.provider.MediaStore.Images.Media.insertImage(getContentResolver(),
    						f.getAbsolutePath(), null, null);
    			} catch (FileNotFoundException e) {
    				e.printStackTrace();
    			}
    			break;
    		// cascading finishes of this activity triggered from step list
    		case ACTIVITY_STEP_CREATOR:
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

}