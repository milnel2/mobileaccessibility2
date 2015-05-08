package com.dtt.activities;

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
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.dtt.R;
import com.dtt.db.DataDbAdapter;
import com.dtt.objs.GlobalConstants;
import com.dtt.objs.HTTPConnection;
import com.dtt.preferences.CareTakerPreferences;

/**
 * Activity for listing all available tasks. This activity gives
 * functionalities of adding, editing and removing tasks.
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class TasksModifierActivity extends ListActivity {
	// dialog id for showing options of editing and removing task selected
	private static final int DIALOG_SELECT_OPTION_ID = 1;
	// dialog id for alerting to delete a selected task
	private static final int DIALOG_ALERT_DELETE_ID = 2;
	
	private Long mTaskId;
	private DataDbAdapter mDbHelper;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tasks_modifier);
        setTitle(getString(R.string.app_name) + " > Task List");
        
        // open DB
        mDbHelper = new DataDbAdapter(this);
        mDbHelper.open();
        
        Button newBtn = (Button)findViewById(R.id.tasks_modifier_new);
        Button helpBtn = (Button)findViewById(R.id.tasks_modifier_help);
        Button doneBtn = (Button)findViewById(R.id.tasks_modifier_done);
        
        // start activity to create new task
        newBtn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		Intent i = new Intent(TasksModifierActivity.this, TaskModifierActivity.class);
        		startActivity(i);
        	}	
        });
        // showing dialog for care taker direction
        helpBtn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		showDialog(GlobalConstants.DIALOG_HELP_ID);
        	}	
        });
        // finish this activity
        doneBtn.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		finish();
        	}	
        });
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		fillTasksList();
	}
	
	/**
	 * display all lists of task available
	 */
	private void fillTasksList() {
		Cursor cursor = mDbHelper.fetchAllTasksDatum();
        startManagingCursor(cursor);
        
        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{DataDbAdapter.KEY_TASK_NAME, DataDbAdapter.KEY_PICTURE};
        
        // and an array of the fields we want to bind those fields to
        int[] to = new int[]{R.id.task_name, R.id.task_picture};
        
        // create a simple cursor adapter and set it to display
        SimpleCursorAdapter datum = 
        	    new SimpleCursorAdapter(this, R.layout.task_list_row, cursor, from, to);
        setListAdapter(datum);
        getListView().setTextFilterEnabled(true);
	}
	

    /**
     * create dialogs for showing options of editing and removing task selected
     * and for alerting to delete a selected task
     */
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog;
    	switch(id) {
    		// showing options of editing and removing task selected
    		case DIALOG_SELECT_OPTION_ID:
    			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
    			builder1.setMessage("Do you want to modify this task, or delete it")
    			       .setCancelable(false)
    			       .setPositiveButton(R.string.modify, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   dialog.dismiss();
    			        	   Intent i = new Intent(TasksModifierActivity.this, TaskModifierActivity.class);
    			               i.putExtra(DataDbAdapter.KEY_ID, mTaskId);
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
    			builder2.setMessage("Are you sure to delete this task?")
    			       .setCancelable(false)
    			       .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   dialog.dismiss();
    			        	   mDbHelper.deleteTasksData(mTaskId);
    			        	   fillTasksList();
    			        	   SharedPreferences settings =
    			                   PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    			        	   String pNum = settings.getString(CareTakerPreferences.KEY_USER_PHONE_NUMBER, "");
    			        	   if(pNum.length() == 10) {
	    			        	   HTTPConnection c = new HTTPConnection(pNum, GlobalConstants.TASK, 
	    			        			   GlobalConstants.UPDATE_SERVER, GlobalConstants.DELETE,
	    			        			   mTaskId, "go to school");
	    			        	   c.connect(getBaseContext());
    			        	   } else {
    			        		   // do something for failure of http connection
    			        	   }
    			           }
    			       })
    			       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			                dialog.cancel();
    			           }
    			       });
    			dialog = builder2.create();
    			break;
    		// care taker direction
    		case GlobalConstants.DIALOG_HELP_ID:
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setMessage(R.string.task_list_help)
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
    
    @Override
    protected void onDestroy() {
    	mDbHelper.close();
        super.onDestroy();
    }
	
	@Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
    	super.onListItemClick(l, v, position, id);
    	mTaskId = id;
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
