package com.stanford.tutti;
 
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
 
public class SettingsMenuActivity extends PreferenceActivity {
 
	private SharedPreferences.OnSharedPreferenceChangeListener listener; 
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        getActionBar().setHomeButtonEnabled(false);
        getActionBar().setDisplayShowHomeEnabled(false);              

 
        addPreferencesFromResource(R.xml.settings);
        
        SharedPreferences prefs = 
        	    PreferenceManager.getDefaultSharedPreferences(this);
        
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        		if (key.equals("prefUsername")) {
        			
        		} 
        	}
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);	
    }
}