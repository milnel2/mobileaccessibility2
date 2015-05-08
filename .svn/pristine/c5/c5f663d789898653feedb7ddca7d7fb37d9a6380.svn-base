package edu.washington.cs.kittens;

/*
 * This activity is the opening page of our application, where the user enters
 * either user or editor mode.
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.TextView;


	
public class Intro extends Activity {
	final static String TAG = "Pic2Speech"; // For error logging

	private CatApplication application;
	private Context context;
	private SharedPreferences prefs;
	private SharedPreferences.Editor prefseditor;
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.intro);
		this.context = this;
		this.application = (CatApplication) this.getApplication();
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		prefseditor = prefs.edit();
		if(!prefs.contains("isFirstTime")) {
			prefseditor.putBoolean("isFirstTime", false);
			AlertDialog d = new AlertDialog.Builder(this).create();
			d.setTitle("Welcome to Pic2Speech");
			d.setMessage("The default password is 1234!\nIt can be changed in the settings.");
			d.setButton("OK", new DialogInterface.OnClickListener() {
			   public void onClick(DialogInterface dialog, int which) {
			   }
			});
			d.show();
		}
		
		View userButton = findViewById(R.id.intro_button_user);
		userButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent i = new Intent(context, Home.class);
				prefseditor.putBoolean("isEditor", false);
				prefseditor.commit();
				startActivity(i);
			}
		});	
		
		View editorButton = findViewById(R.id.intro_button_editor);
		editorButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				String password = prefs.getString("opt_password", "1234");
				TextView box = (TextView) findViewById(R.id.intro_password);
				String text = box.getText().toString();
				if(text.equals(password)) {
					Intent i = new Intent(context, Home.class);
					prefseditor.putBoolean("isEditor", true);
					prefseditor.commit();
					box.setText("");
					startActivity(i);
				}
			}
		});	

	}

}