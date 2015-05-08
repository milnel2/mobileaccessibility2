package com.dtt.activities;

import java.util.ArrayList;

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
import android.widget.TextView;

import com.dtt.R;
import com.dtt.adapters.StepListAdapter;
import com.dtt.db.DataDbAdapter;
import com.dtt.objs.GlobalConstants;
import com.dtt.objs.HTTPConnection;
import com.dtt.objs.TaskStep;
import com.dtt.preferences.CareTakerPreferences;

/**
 * Activity for listing all steps for a specific task. This activity gives
 * functionalities of adding, editing and removing steps from list. 
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class TaskStepsModifierActivity extends ListActivity {
	// dialog id for showing options of editing and removing step selected
	private static final int DIALOG_SELECT_OPTION_ID = 0;
	// dialog id for alerting to delete a selected task
	private static final int DIALOG_ALERT_DELETE_ID = 1;
	// dialog id for alerting to start preview activity if there is no steps
	// available
	private static final int DIALOG_ALERT_PREVIEW_ID = 2;
	
	private Long mTaskId;
	private Long mStepId;
	
	private TextView mTaskNameView;
	
	private DataDbAdapter mDbHelper;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.task_steps_modifier);
        setTitle(getString(R.string.app_name) + " > Step List");
        
        // open DB
        mDbHelper = new DataDbAdapter(this);
        mDbHelper.open();
        
        mTaskNameView = (TextView)findViewById(R.id.task_steps_modifier_task_name);
        Button newBtn = (Button)findViewById(R.id.task_steps_modifier_new);
        Button previewBtn = (Button)findViewById(R.id.task_steps_modifier_preview);
        Button doneBtn = (Button)findViewById(R.id.task_steps_modifier_done);
        
        // restore mTaskId
        restoreFields(savedInstanceState);
        
        // start TaskStepModifierActivity activity to create new step 
        newBtn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		Intent i = new Intent(TaskStepsModifierActivity.this, TaskStepModifierActivity.class);
        		i.putExtra(DataDbAdapter.KEY_ID, mDbHelper.createStepsData(mTaskId, "", "", "", ""));
        		startActivity(i);
        	}	
        });
        // start TaskStepViewActivity to show step by step view
        previewBtn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		int count = 0;
        		Cursor c = null;
        		try{
        			c = mDbHelper.fetchAllStepsDatumForTask(mTaskId);
        			count = c.getCount();
        		} finally {
        			if(c != null)
        				c.close();
        		}
        		if (count > 0) { 
        			Intent i = new Intent(TaskStepsModifierActivity.this, TaskStepViewActivity.class);
        			i.putExtra(TaskStepViewActivity.CARE_TAKER, TaskStepViewActivity.CARE_TAKER_VAL);
            		i.putExtra(DataDbAdapter.KEY_TASK_ID, mTaskId);
            		startActivity(i);
        		} else {
        			showDialog(DIALOG_ALERT_PREVIEW_ID);
        		}
        	}	
        });
        // update web for this task and finish this activity
        doneBtn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		if(hasSteps(mTaskId)) {
        			SharedPreferences settings =
		                   PreferenceManager.getDefaultSharedPreferences(getBaseContext());
		        	String pNum = settings.getString(CareTakerPreferences.KEY_USER_PHONE_NUMBER, "");
		        	if(pNum.length() == 10) {
		        		// update web for this task
		        		HTTPConnection c = new HTTPConnection(pNum, GlobalConstants.TASK, 
		        				GlobalConstants.UPDATE_SERVER, GlobalConstants.ADD,
		        				mTaskId, mTaskNameView.getText().toString());
		        		c.connect(getBaseContext());
		        	} 
        			finish();	
        		} else
        			showDialog(DIALOG_ALERT_PREVIEW_ID);
        			
        	}	
        });
    }
	
	/**
	 * true if there is at least one step for the task passed; 
	 * otherwise return false
	 * @param taskId - for this list of steps
	 * @return
	 */
	private boolean hasSteps(long taskId) {
		boolean result = false;
		Cursor c = null;
		try {
			c = mDbHelper.fetchAllStepsDatumForTask(taskId);
			if(c.getCount() != 0)
				result = true;
		} finally {
			if(c != null)
				c.close();
		}
		return result;
	}
	
	/**
     * restore task id from bundle
     * @param savedInstanceState
     */
    private void restoreFields(Bundle savedInstanceState) {
    	mTaskId = savedInstanceState != null ? savedInstanceState.getLong(DataDbAdapter.KEY_TASK_ID) 
    			: null;
    	if (mTaskId == null) {
    		Bundle extras = getIntent().getExtras();            
    		mTaskId = extras != null ? extras.getLong(DataDbAdapter.KEY_TASK_ID) 
    				: null;
    	}
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
		fillStepsList();
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
    		} finally {
    			if(data != null)
    				data.close();
    		}
    	}
	}
	
	/**
	 * display lists of the task specfied to the screen
	 */
	private void fillStepsList() {
        ArrayAdapter<TaskStep> stepListAdapter =
			new StepListAdapter(this, R.layout.step_list_row, getStepList());
		setListAdapter(stepListAdapter);
	}
	
	/**
	 * return step list of the task specified from database 
	 */
	private ArrayList<TaskStep> getStepList() {
		Cursor cursor = mDbHelper.fetchAllStepsDatumForTask(mTaskId);
		cursor.moveToFirst();
		ArrayList<TaskStep> stepList = new ArrayList<TaskStep>(cursor.getCount());
		while(!cursor.isAfterLast()) {
			Long stepId = cursor.getLong(cursor.getColumnIndex(DataDbAdapter.KEY_ID));
			Long taskId = cursor.getLong(cursor.getColumnIndex(DataDbAdapter.KEY_TASK_ID));
			Long stepNum = cursor.getLong(cursor.getColumnIndex(DataDbAdapter.KEY_STEP_NUM));
			String inst = cursor.getString(cursor.getColumnIndex(DataDbAdapter.KEY_INSTRUCTION));
			String pictureUri = cursor.getString(cursor.getColumnIndex(DataDbAdapter.KEY_PICTURE));
			String audioUri = cursor.getString(cursor.getColumnIndex(DataDbAdapter.KEY_AUDIO));
			String videoUri = cursor.getString(cursor.getColumnIndex(DataDbAdapter.KEY_VIDEO));
			stepList.add(new TaskStep(stepId, taskId, stepNum, inst, pictureUri, audioUri, videoUri));
			cursor.moveToNext();
		}
		cursor.close();
		return stepList;
	}
	
	/**
     * create dialogs
     */
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog;
    	switch(id) {
    		// showing options of editing and removing step selected
    		case DIALOG_SELECT_OPTION_ID:
    			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
    			builder1.setMessage("Do you want to modify this step, or delete it")
    			       .setCancelable(false)
    			       .setPositiveButton(R.string.modify, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   dialog.dismiss();
    			        	   Intent i = new Intent(TaskStepsModifierActivity.this, TaskStepModifierActivity.class);
    			               i.putExtra(DataDbAdapter.KEY_ID, mStepId);
    			               startActivity(i);
    			           }
    			       })
    			       .setNeutralButton(R.string.delete, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   dialog.dismiss();
    			        	   showDialog(DIALOG_ALERT_DELETE_ID);
    			           }
    			       })
    			       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			                dialog.cancel();
    			           }
    			       });
    			dialog = builder1.create();
    			break;
    		// alerting to delete a selected task
    		case DIALOG_ALERT_DELETE_ID:
    			AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
    			builder2.setMessage("Are you sure to delete this step?")
    			       .setCancelable(false)
    			       .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   dialog.dismiss();
    			        	   // delete selected step from database
    			        	   mDbHelper.deleteStepsDataAndUpdateStepNum(mStepId, mTaskId);
    			        	   fillStepsList();
    			           }
    			       })
    			       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			                dialog.cancel();
    			           }
    			       });
    			dialog = builder2.create();
    			break;
    		// alerting to start preview activity if there is no steps available
    		case DIALOG_ALERT_PREVIEW_ID:
    			AlertDialog.Builder builder3 = new AlertDialog.Builder(this);
    			builder3.setMessage("Please add steps!")
    			       .setCancelable(false)
    			       .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			                dialog.cancel();
    			           }
    			       });
    			dialog = builder3.create();
    			break;
    		default:
    			dialog = null;
    	}
    	return dialog;
    }
    
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(DataDbAdapter.KEY_TASK_ID, mTaskId);
    }
    
    @Override
    protected void onDestroy() {
    	mDbHelper.close();
        super.onDestroy();
    }
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	TaskStep s = (TaskStep) getListAdapter().getItem(position);
    	mStepId = s.getStepId();
    	// show dialog for showing options of editing and removing step selected
    	showDialog(DIALOG_SELECT_OPTION_ID);
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
