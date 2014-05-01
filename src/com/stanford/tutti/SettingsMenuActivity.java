package com.stanford.tutti;
 
import android.os.Bundle;
import android.preference.PreferenceActivity;
 
public class SettingsMenuActivity extends PreferenceActivity {
 
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        getActionBar().hide(); 
 
        addPreferencesFromResource(R.xml.settings);
 
    }
}