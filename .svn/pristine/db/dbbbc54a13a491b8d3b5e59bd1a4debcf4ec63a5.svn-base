package com.dtt.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.EditText;

import com.dtt.R;
import com.dtt.preferences.CareTakerPreferences;

/**
 * This activity forces to enter password for starting care taker menu
 * and setting. When it fails, it will prompt short notification about
 * failing and then go back to the main menu. Otherwise, start proper
 * care taker activity
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class TypePasswordActivity extends Activity{
	
	// dialog id for entering password
	public static final int DIALOG_INPUT_PASSWORD_ID = 0;
	private EditText mPw;
	
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.app_name) + " > Type Password");
        showDialog(DIALOG_INPUT_PASSWORD_ID);
    }
	
	/**
     * create dialogs
     */
    protected Dialog onCreateDialog(int id) {
    	Dialog dialog;
    	switch(id) {
    		case DIALOG_INPUT_PASSWORD_ID:
    			AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
    			builder1.setMessage("Type the password!")
    			       .setCancelable(false)
    			       .setView(mPw = new EditText(this))
    			       .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   dialog.dismiss();
    			        	   SharedPreferences settings =
    			                   PreferenceManager.getDefaultSharedPreferences(getBaseContext());
    			        	   String pw = settings.getString(CareTakerPreferences.KEY_CARE_TAKER_PASSWORD, "");
    			        	   if(pw.compareTo(mPw.getText().toString()) == 0) {
    			        		   Intent i = new Intent();
    			        		   setResult(RESULT_OK, i);
    			        		   finish();
    			        	   } else {
    			        		   Intent i = new Intent();
    			        		   setResult(RESULT_CANCELED, i);
    			        		   finish();
    			        	   }
    			        		   
    			           }
    			       })
    			       .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			                dialog.dismiss();
    			                Intent i = new Intent();
 			        		   	setResult(RESULT_CANCELED, i);
    			                finish();
    			           }
    			       });
    			dialog = builder1.create();
    			break;
    		default:
    			dialog = null;
    	}
    	return dialog;
    }
}
