package edu.uw_cs.ma.deviceidentifier;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.widget.TextView;

public class DeviceIdentifier extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        TextView tv = (TextView)findViewById(R.id.device);
        TelephonyManager mTeleMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);;
    	tv.setText(mTeleMgr.getDeviceId());
    }
}