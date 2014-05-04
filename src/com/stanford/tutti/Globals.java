package com.stanford.tutti;

import java.util.*; 

import org.json.*; 

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;

/* 
 * Stores any state which must be globally accessible, eg. variables which cannot
 * be passed to activities using the intent. 
 * 
 * We are currently storing all of the music library metadata in-memory using this
 * class. It supports accessing and searching the library metadata as well as
 * checking and setting the current artist, album, and song.
 */
public class Globals extends Application {
	public Jam jam = new Jam(this); 
		
	public String currentArtistView = ""; 
	public String currentAlbumView = ""; 
	public Handler uiUpdateHandler; 
	public Handler joinJamHandler; 
		
	DatabaseHandler db; 
	
	private static Context context; 
		
	@Override
	public void onCreate() {
		super.onCreate();
		Globals.context = getApplicationContext(); 
		db = new DatabaseHandler(this);
		jam.setIPUsername(getIpAddr(), getUsername());
	}
	
	public static Context getAppContext() {
        return Globals.context;
    }

	/*
	 * Return a string representation of the current device's IP address. 
	 */
	public String getIpAddr() {
		WifiManager wifiManager = 
				(WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format(
				"%d.%d.%d.%d",
				(ip & 0xff),
				(ip >> 8 & 0xff),
				(ip >> 16 & 0xff),
				(ip >> 24 & 0xff));

		return ipString;
	}
	
	/*
	 * Return the username from the user preferences. 
	 */
	public String getUsername() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString("prefUsername", "anonymous"); 
	}
}
