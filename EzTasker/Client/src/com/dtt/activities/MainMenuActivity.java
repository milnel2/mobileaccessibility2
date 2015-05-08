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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dtt.R;
import com.dtt.objs.GlobalConstants;
import com.dtt.preferences.CareTakerPreferences;

/**
 * Activity for main view. This activity includes my schedule and other tools activities
 * for user. It also contains care taker menu and care taker setting(hided under menu button)   
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class MainMenuActivity extends Activity implements TextToSpeech.OnInitListener {
	// memu id for care taker menu activity
	public static final int CARE_TAKER_MENU = 0;
	// memu id for care taker setting activity
	public static final int CARE_TAKER_SETTING = 1;
	// code for checking installation of TTS
	private static final int MY_DATA_CHECK_CODE = 2;

	private TextToSpeech mTts;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setTitle(getString(R.string.app_name) + " > Main Menu");
		// check if phone has ttl library inculded
		Intent checkIntent = new Intent();
		checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
		startActivityForResult(checkIntent, MY_DATA_CHECK_CODE);

		Button mySchedule = (Button) findViewById(R.id.main_schedule);
		Button otherTools = (Button) findViewById(R.id.main_other_tools);
		Button help = (Button) findViewById(R.id.main_help);
		Button exit = (Button) findViewById(R.id.main_exit);

		mySchedule.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// Go to Schedule tab activity for user
				Intent i = new Intent();
				i.setClass(MainMenuActivity.this, MyScheduleListTabActivity.class);
				startActivity(i);
			}
		});
		otherTools.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// go to Other Tools Menu for user
				Intent i = new Intent();
				i.setClass(MainMenuActivity.this, OtherToolsActivity.class);
				startActivity(i);
			}
		});
		help.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// show direction
				mTts.speak(getString(R.string.main_menu_help), TextToSpeech.QUEUE_FLUSH, null);
				showDialog(GlobalConstants.DIALOG_HELP_ID);
			}
		}); 
		exit.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// finish this activity
				finish();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		// reinitialize tts
		mTts = new TextToSpeech(this, this);
		mTts.setLanguage(Locale.US);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// shut down tts
		mTts.shutdown();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// add a menu instance for care taker menu activity
		menu.add(0, CARE_TAKER_MENU, 0, getString(R.string.care_taker_menu)).setIcon(
				R.drawable.ic_care_taker);
		// add a menu instance for care taker setting activity
		menu.add(1, CARE_TAKER_SETTING, 0, "CareTaker Setting").setIcon(
				R.drawable.ic_setting);

		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		// guide care taker to put password. if the password is correct, direct to
		// care taker menu activity
		case CARE_TAKER_MENU:
			Intent i = new Intent(this, TypePasswordActivity.class);
			startActivityForResult(i, CARE_TAKER_MENU);
			return true;
		// guide care taker to put password. if the password is correct, direct to
		// care taker setting activity
		case CARE_TAKER_SETTING:
			Intent i1 = new Intent(this, TypePasswordActivity.class);
			startActivityForResult(i1, CARE_TAKER_SETTING);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		// if the password was correct, direct to care taker menu activity
		switch(requestCode) {
		case CARE_TAKER_MENU:
			if(resultCode == RESULT_OK) {
				Intent i = new Intent(this, CareTakerActivity.class);
				startActivity(i);
			} else {
				Toast.makeText(this, "Your password is not correct!", Toast.LENGTH_SHORT).show();
			}
			break;
		// if the password is correct, direct to care taker setting activity
		case CARE_TAKER_SETTING:
			if(resultCode == RESULT_OK) {
				Intent i1 = new Intent(this, CareTakerPreferences.class);
				startActivity(i1);
			} else {
				Toast.makeText(this, "Your password is not correct!", Toast.LENGTH_SHORT).show();
			}
			break;
		// if TTL hasn't installed send user to market to download it
		case MY_DATA_CHECK_CODE:
			if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
            	mTts = new TextToSpeech(this, this);
            	mTts.setLanguage(Locale.US);
            } else {
                // missing data, install it
                Intent installIntent = new Intent();
                installIntent.setAction(
                    TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }
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

	/**
	 * create dialog for direction
	 */
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case GlobalConstants.DIALOG_HELP_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setMessage(R.string.main_menu_help)
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
	 * unimplemented for tts
	 */
	public void onInit(int status) {
		// TODO Auto-generated method stub
	}

}

