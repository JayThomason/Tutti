package com.stanford.tutti;
 
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
 
/**
 * Displays the settings page, accessible from the home screen, 
 * and from anywhere in the app via the action bar. 
 */
public class SettingsMenuActivity extends PreferenceActivity {
 
	private SharedPreferences.OnSharedPreferenceChangeListener listener; 
	
	/**
	 * Creates the Settings menu activity. 
	 * 
	 * @see android.app.PreferenceActivity#onCreate(android.os.Bundle)
	 */
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
        			Globals g = (Globals) getApplication(); 
        			g.jam.setIPUsername(g.getIpAddr(), g.getUsername());
        		} 
        	}
        };

        prefs.registerOnSharedPreferenceChangeListener(listener);	
    }
}