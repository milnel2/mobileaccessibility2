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
 * Tab activity for displaying schedule to user. There are three tabs
 * within this activity: Up-coming tab for tasks scheduled near future,
 * Passed tab for task passed scheduled time, and all tab for all schedule
 * 
 * @author Moon Hwan Oh, Amanda Shen
 *
 */
public class MyScheduleListTabActivity extends TabActivity {
	private static TextView mTVLF;
	private static TextView mTVMF;
    private static TextView mTVRF;
    private static final String UPCOMING_TAB = "upcoming_tab";
    private static final String PASSED_TAB = "passed_tab";
    private static final String ALL_TAB = "all_tab";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.app_name) + " > My Schedule");
        
        final TabHost tabHost = getTabHost();
        tabHost.setBackgroundColor(Color.WHITE);
        tabHost.getTabWidget().setBackgroundColor(Color.BLACK);
        // set intent to UPCOMING_TAB
        Intent upcoming = new Intent(this, ScheduleListActivity.class);
        upcoming.putExtra(ScheduleListActivity.SCHEDULE_LIST_MODE, ScheduleListActivity.UPCOMING_SCHEDULE_MODE);
        tabHost.addTab(tabHost.newTabSpec(UPCOMING_TAB).setIndicator(getString(R.string.upcoming))
                .setContent(upcoming));
        // set intent to PASSED_TAB
        Intent passed = new Intent(this, ScheduleListActivity.class);
        passed.putExtra(ScheduleListActivity.SCHEDULE_LIST_MODE, ScheduleListActivity.PASSED_SCHEDULE_MODE);
        tabHost.addTab(tabHost.newTabSpec(PASSED_TAB).setIndicator(getString(R.string.passed))
                .setContent(passed));
        // set intent to ALL_TAB
        Intent all = new Intent(this, ScheduleListActivity.class);
        tabHost.addTab(tabHost.newTabSpec(ALL_TAB).setIndicator(getString(R.string.all))
                .setContent(all));
        
        LinearLayout ll = (LinearLayout) tabHost.getChildAt(0);
        TabWidget tw = (TabWidget) ll.getChildAt(0);
        // set up layout for UPCOMING_TAB
        RelativeLayout rllf = (RelativeLayout) tw.getChildAt(0);
        mTVLF = (TextView) rllf.getChildAt(1);
        mTVLF.setTextSize(19);
        mTVLF.setPadding(0, 0, 0, 6);
        // set up layout for PASSED_TAB
        RelativeLayout rlmf = (RelativeLayout) tw.getChildAt(1);
        mTVMF = (TextView) rlmf.getChildAt(1);
        mTVMF.setTextSize(19);
        mTVMF.setPadding(0, 0, 0, 6);
        // set up layout for ALL_TAB
        RelativeLayout rlrf = (RelativeLayout) tw.getChildAt(2);
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
        if (tab.equals(UPCOMING_TAB))
            mTVLF.setText(string);
        else if (tab.equals(PASSED_TAB))
            mTVMF.setText(string);
        else
        	mTVRF.setText(string);
        	
        	
    }
}
