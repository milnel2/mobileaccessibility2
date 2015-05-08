package com.dtt.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.dtt.R;
import com.dtt.objs.GlobalConstants;

/**
 * Activity for care takers. This activity has link to Schedule builder and
 * task builder.
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class CareTakerActivity extends Activity{
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.care_taker);
        setTitle(getString(R.string.app_name) + " > Care Taker Menu");
        
        Button buildSchedule = (Button) findViewById(R.id.care_taker_build_schedule);
        Button modifyTasks = (Button) findViewById(R.id.care_taker_tasks_modifier);
        Button help = (Button) findViewById(R.id.care_taker_help);
        Button goBack = (Button) findViewById(R.id.care_taker_exit);
        
        buildSchedule.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		// direct to schedule builder
    			Intent i = new Intent(CareTakerActivity.this, BuildScheduleTabActivity.class);
    			startActivity(i);
        	}
        });
        modifyTasks.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		// direct to task builder
    			Intent i = new Intent(CareTakerActivity.this, TasksModifierActivity.class);
    			startActivity(i);
        	}
        });
        help.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		showDialog(GlobalConstants.DIALOG_HELP_ID);
        	}
        }); 
        goBack.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		finish();
        	}
        });
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		// disable back button
	    if (keyCode == KeyEvent.KEYCODE_BACK) {
	        return true;
	    }

	    return super.onKeyDown(keyCode, event);
	}
	
	/**
     * create dialog for direction
     */
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog;
    	switch(id) {
    		case GlobalConstants.DIALOG_HELP_ID:
    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			builder.setMessage(R.string.care_taker_help)
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