package com.dtt.preferences;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.PreferenceActivity;

import com.dtt.R;

/**
 * Preference activity class to store and update information needed for 
 * this application setting, such as user, care taker phone numbers, password
 * and user name
 * 
 * @author Moon Hwan Oh, Amanda Shen
 * 
 */
public class CareTakerPreferences extends PreferenceActivity implements
        OnSharedPreferenceChangeListener {

	public static String KEY_CARE_TAKER_PASSWORD = "care_taker_password";
    public static String KEY_CARE_TAKER_PHONE_NUMBER = "care_taker_phone_num";
    public static String KEY_USER_NAME = "user_name";
    public static String KEY_USER_PHONE_NUMBER = "user_phone_num";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.care_taker_preferences);
        setTitle(getString(R.string.app_name) + " > Care Taker Preferences");
        updateCTPassword();
        updateCTPhoneNum();
        updateUserName();
        updateUserPhoneNum();
    }


    @Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                this);
    }


    @Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    /**
     * if anything changed, update shared preferences
     */
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(KEY_CARE_TAKER_PASSWORD)) {
        	updateCTPassword();
        } else if (key.equals(KEY_CARE_TAKER_PHONE_NUMBER)) {
        	updateCTPhoneNum();
        } else if (key.equals(KEY_USER_NAME)) {
        	updateUserName();
        } else if (key.equals(KEY_USER_PHONE_NUMBER))
        	updateUserPhoneNum();
    }

    /**
     * update password shared preference
     */
    private void updateCTPassword() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_CARE_TAKER_PASSWORD);
        etp.setSummary(etp.getText());
    }
    
    /**
     * update care taker phone number shared preference
     */   
    private void updateCTPhoneNum() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_CARE_TAKER_PHONE_NUMBER);
        etp.setSummary(etp.getText());
    }
    
    /**
     * update user name shared preference
     */
    private void updateUserName() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_USER_NAME);
        etp.setSummary(etp.getText());
    }
    
    /**
     * update user phone number shared preference
     */
    private void updateUserPhoneNum() {
        EditTextPreference etp =
                (EditTextPreference) this.getPreferenceScreen().findPreference(KEY_USER_PHONE_NUMBER);
        etp.setSummary(etp.getText());
    }
}
