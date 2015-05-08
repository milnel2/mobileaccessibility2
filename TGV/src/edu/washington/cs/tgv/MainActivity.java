// Copyright (c) 2012-2013 University of Washington
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// - Redistributions of source code must retain the above copyright notice,
// this list of conditions and the following disclaimer.
// - Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
// - Neither the name of the University of Washington nor the names of its
// contributors may be used to endorse or promote products derived from this
// software without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE UNIVERSITY OF WASHINGTON AND CONTRIBUTORS
// "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
// TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
// PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE UNIVERSITY OF WASHINGTON OR
// CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
// EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
// PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS;
// OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
// WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR
// OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
// ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
package edu.washington.cs.tgv;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;

public class MainActivity extends TabActivity {
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
 
        TabHost tabHost = getTabHost();
 
        // Tab for Results
        TabSpec resultspec = tabHost.newTabSpec("Results");
        // setting Title and Icon for the Tab
        resultspec.setIndicator("Results", getResources().getDrawable(R.drawable.icon_results_tab));
        Intent resultsIntent = new Intent(this, resultsActivity.class);
        resultspec.setContent(resultsIntent);
 
        // Tab for Scan
        TabSpec scanspec = tabHost.newTabSpec("Scan");
        scanspec.setIndicator("Scan", getResources().getDrawable(R.drawable.icon_scan_tab));
        Intent scanIntent = new Intent(this, scanActivity.class);
        scanspec.setContent(scanIntent);
 
        // Tab for Settings
        TabSpec settingspec = tabHost.newTabSpec("Settings");
        settingspec.setIndicator("Settings", getResources().getDrawable(R.drawable.icon_settings_tab));
        Intent settingsIntent = new Intent(this, settingsActivity.class);
        settingspec.setContent(settingsIntent);
 
        // Adding all TabSpec to TabHost
        tabHost.addTab(resultspec); // Adding results tab
        tabHost.addTab(scanspec); // Adding scans tab
        tabHost.addTab(settingspec); // Adding settings tab
    }
    
}
