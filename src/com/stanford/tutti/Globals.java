package com.stanford.tutti;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.widget.FilterQueryProvider;

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
	public Handler uiUpdateHandler; 
	public Handler joinJamHandler; 
	
	public int playerDuration = 0; 
	public OnPreparedListener playerListener; 
	
	public MusicLibraryLoaderThread localLoaderThread; 

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

	public String getGatewayIpAddr() {
		WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		DhcpInfo info = wifiManager.getDhcpInfo();
		int gateway_ip = info.gateway;
		
		String ipString = String.format(
				"%d.%d.%d.%d",
				(gateway_ip & 0xff),
				(gateway_ip >> 8 & 0xff),
				(gateway_ip >> 16 & 0xff),
				(gateway_ip >> 24 & 0xff));
		
		return ipString;
	}

	/*
	 * Returns the SSID of the wifi network.
	 */
	public String getWifiSSID() {
		String ssid = null;
		ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if (networkInfo.isConnected()) {
			final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
			final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
			if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
				ssid = connectionInfo.getSSID();
			}
		}
		return ssid;
	}

	/*
	 * Return the username from the user preferences. 
	 */
	public String getUsername() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString("prefUsername", "anonymous"); 
	}
	
	/*
	 * Return the current system timestamp,
	 * in a punctuationless format better suited for IDs than printing. 
	 */
	public String getTimestamp() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("ddhhmmssSSSa");
		return sdf.format(date);
	}
	
	/*
	 * Sends a String message to the global UI update handler.
	 */
	public void sendUIMessage(String message) {
		if (uiUpdateHandler != null) {
			Message msg = uiUpdateHandler.obtainMessage();
			msg.obj = message; 
			uiUpdateHandler.sendMessage(msg);
		}
	}
	
	/*
	 * Sends an int message to the global UI update handler.
	 */
	public void sendUIMessage(int message) {
		if (uiUpdateHandler != null) {
			Message msg = uiUpdateHandler.obtainMessage();
			msg.what = message; 
			uiUpdateHandler.sendMessage(msg);
		}
	}
}
