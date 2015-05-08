package com.dtt.activities;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;

import com.dtt.R;

/**
 * Tab activity for building schedule for care taker. AddScheduleActivity
 * for adding an instance of schedule and ScheduleListActivity for deleting
 * an instance of schedule are embedded to this activity
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class BuildScheduleTabActivity extends TabActivity {
	private static TextView mTVLF;
	private static TextView mTVRF;
	private static final String ADD_TAB = "add_tab";
	private static final String DELETE_TAB = "delete_tab";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(getString(R.string.app_name) + " > Build Schedule");

		final TabHost tabHost = getTabHost();
		tabHost.setBackgroundColor(Color.WHITE);
		tabHost.getTabWidget().setBackgroundColor(Color.BLACK);
		// set intent to ADD_TAB
		Intent add = new Intent(this, AddScheduleActivity.class);
		tabHost.addTab(tabHost.newTabSpec(ADD_TAB).setIndicator(getString(R.string.add))
				.setContent(add));
		// set intent to DELETE_TAB
		Intent delete = new Intent(this, ScheduleListActivity.class);
		delete.putExtra(ScheduleListActivity.CARE_TAKER, ScheduleListActivity.CARE_TAKER_VAL);
		tabHost.addTab(tabHost.newTabSpec(DELETE_TAB).setIndicator(getString(R.string.delete))
				.setContent(delete));

		LinearLayout ll = (LinearLayout) tabHost.getChildAt(0);
		TabWidget tw = (TabWidget) ll.getChildAt(0);
		// set ADD_TAB's layout
		RelativeLayout rllf = (RelativeLayout) tw.getChildAt(0);
		mTVLF = (TextView) rllf.getChildAt(1);
		mTVLF.setTextSize(19);
		mTVLF.setPadding(0, 0, 0, 6);
		// set DELETE_TAB's layout
		RelativeLayout rlrf = (RelativeLayout) tw.getChildAt(1);
		mTVRF = (TextView) rlrf.getChildAt(1);
		mTVRF.setTextSize(19);
		mTVRF.setPadding(0, 0, 0, 6);
	}

	/**
	 * set header text into each tab
	 * @param string
	 * @param tab
	 */
	public static void setTabHeader(String string, String tab) {
		if (tab.equals(ADD_TAB)) {
			mTVLF.setText(string);
		} else if (tab.equals(DELETE_TAB)) {
			mTVRF.setText(string);
		}
	}


}
