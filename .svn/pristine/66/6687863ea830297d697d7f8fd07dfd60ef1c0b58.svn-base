package com.dtt.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.dtt.R;
import com.dtt.db.DataDbAdapter;

/**
 * This activity has functionality for searching specific task from list of tasks
 * by querying. If this activity is for care taker, it returns task selected to 
 * AddScheduleActivity. Otherwise, guide user through step by step view
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class SearchTaskActivity extends ListActivity implements TextToSpeech.OnInitListener {
	
	public static final String CARE_TAKER = "care taker";
	public static final int USER_VAL = 0;
	public static final int CARE_TAKER_VAL = 1;
	
	// dialog id for direction for care taker
	private static final int DIALOG_CARE_TAKER_HELP_ID = 0;
	// dialog id for direction for user	
	private static final int DIALOG_USER_HELP_ID = 1;


	private TextView mSearchTextView;
	// to access the database 
	private DataDbAdapter mDbHelper;
	
	private Cursor mCursor;

	private int mCareTaker;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.search_task);
		setTitle(getString(R.string.app_name) + " > Search Task");

		mCareTaker = getIntent().getIntExtra(CARE_TAKER, USER_VAL); 

		// Open Database
		mDbHelper = new DataDbAdapter(this);
		mDbHelper.open();
		
		// Capture View Elements
		mSearchTextView = (TextView) findViewById(R.id.search_task_query);
		ImageButton search = (ImageButton) findViewById(R.id.search_task_button);
		Button help = (Button) findViewById(R.id.search_task_help);
		Button goBack = (Button) findViewById(R.id.search_task_goback);
		
		// for care taker give a little direction when this activity starts
		if(mCareTaker == CARE_TAKER_VAL){
			Toast.makeText(SearchTaskActivity.this, "Choose a task you want to add to the schedule!",
					Toast.LENGTH_SHORT).show();
		}

		// fill out all list
		fillListData(false);

		search.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// fill out list after filtering by search key from user
				fillListData(true);
			}
		});
		help.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if(mCareTaker == USER_VAL) {
					// dialog for user direction
					showDialog(DIALOG_USER_HELP_ID);
				} else {
					// dialog for care taker direction
					showDialog(DIALOG_CARE_TAKER_HELP_ID);
				}
			}
		}); 
		goBack.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				// when this activity was for care taker, return null val to 
				//previous activity
				if(mCareTaker == CARE_TAKER_VAL) {
					Intent i = new Intent();
					i.putExtra(DataDbAdapter.KEY_TASK_ID, -1);
					setResult(RESULT_OK, i);
				}
				finish();
			}
		});
	}


	/**
	 * set up list of tasks either with filtering by search key or without it
	 * 
	 * @param searchFlag - ture for filtering by search key; false for listing all task available 
	 */
	private void fillListData(boolean searchFlag) {
		if(searchFlag)
			mCursor = mDbHelper.fetchSearchedTaskData(mSearchTextView.getText().toString());
		else
			mCursor = mDbHelper.fetchAllTasksDatum();
		startManagingCursor(mCursor);

		// Create an array to specify the fields to display in the list
		String[] from = new String[]{DataDbAdapter.KEY_TASK_NAME, DataDbAdapter.KEY_PICTURE};

		// and an array of the fields to bind those fields to
		int[] to = new int[]{R.id.task_name, R.id.task_picture};

		// Now create a simple cursor adapter and set it to display
		SimpleCursorAdapter datum = 
			new SimpleCursorAdapter(this, R.layout.task_list_row, mCursor, from, to);
		setListAdapter(datum);
		getListView().setTextFilterEnabled(true);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, final long id) {
		super.onListItemClick(l, v, position, id);
		// when this activity was for care taker, return selected task to 
		//previous activity 
		if(mCareTaker == CARE_TAKER_VAL){
			Intent i = new Intent();
			i.putExtra(DataDbAdapter.KEY_TASK_ID, id);
			setResult(RESULT_OK, i);
			finish();
		// otherwise, start TaskStepViewActivity with task selected
		} else {
			Intent i = new Intent(SearchTaskActivity.this, TaskStepViewActivity.class);
			i.putExtra(TaskStepViewActivity.CARE_TAKER, TaskStepViewActivity.CARE_TAKER_VAL);
			i.putExtra(DataDbAdapter.KEY_TASK_ID, id);
			startActivity(i);
		}		
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if(mCursor != null)
			mCursor.close();
		mDbHelper.close();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)  {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	/**
	 * create dialogs for user direction and care taker direction
	 */
	 protected Dialog onCreateDialog(int id) {
		 Dialog dialog;
		 switch(id) {
		 case DIALOG_CARE_TAKER_HELP_ID:
			 AlertDialog.Builder builder1 = new AlertDialog.Builder(this);
			 builder1.setMessage(R.string.search_task_care_taker_help)
			 .setCancelable(false)
			 .setTitle("Direction")
			 .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface dialog, int id) {
					 dialog.dismiss();

				 }
			 });
			 dialog = builder1.create();
			 break;
		 case DIALOG_USER_HELP_ID:
			 AlertDialog.Builder builder2 = new AlertDialog.Builder(this);
			 builder2.setMessage(R.string.search_task_user_help)
			 .setCancelable(false)
			 .setTitle("Direction")
			 .setNeutralButton(R.string.ok, new DialogInterface.OnClickListener() {
				 public void onClick(DialogInterface dialog, int id) {
					 dialog.dismiss();
				 }
			 });
			 dialog = builder2.create();
			 break;

		 default:
			 dialog = null;
		 }
		 return dialog;
	 }



	public void onInit(int status) {
		// TODO Auto-generated method stub
		
	}

}


