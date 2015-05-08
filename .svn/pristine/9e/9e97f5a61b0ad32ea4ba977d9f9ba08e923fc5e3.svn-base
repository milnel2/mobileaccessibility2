package com.dtt.activities;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
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
public class OtherToolsActivity extends Activity implements TextToSpeech.OnInitListener {
	
	private TextToSpeech mTts;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.other_tools);
        setTitle(getString(R.string.app_name) + " > Other Tools");
        
        // initialize TTS
        mTts = new TextToSpeech(this, this);
    	mTts.setLanguage(Locale.US);
        
        Button searchTasks = (Button) findViewById(R.id.other_tools_search);
        Button help = (Button) findViewById(R.id.other_tools_help);
        Button goBack = (Button) findViewById(R.id.other_tools_goback);
        
        
        searchTasks.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		// direct to SearchTaskActivity
    			Intent i = new Intent(OtherToolsActivity.this, SearchTaskActivity.class);
    			startActivity(i);
        	}
        });
        help.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		// show direction
        		mTts.speak(getString(R.string.other_tools_help), TextToSpeech.QUEUE_FLUSH, null);
        		showDialog(GlobalConstants.DIALOG_HELP_ID);
        	}
        }); 
        goBack.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View view) {
        		// finish this activity
        		finish();
        	}
        });
	}
	
	@Override
	protected void onDestroy() {
		// shut down TTS
		mTts.shutdown();
		super.onDestroy();
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
    			builder.setMessage(R.string.other_tools_help)
    			       .setCancelable(false)
    			       .setTitle("Direction")
    			       .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
    			           public void onClick(DialogInterface dialog, int id) {
    			        	   dialog.dismiss();
    			        	   mTts.stop();
    			           }
    			       });
    			dialog = builder.create();
    			break;
    		default:
    			dialog = null;
    	}
    	return dialog;
    }

    /**
     * unimplemented func for TTS
     */
	public void onInit(int status) {
		// TODO Auto-generated method stub
		
	}

}