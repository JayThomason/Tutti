package com.stanford.tutti;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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

/**
 * Stores any state which must be globally accessible, eg. variables which cannot
 * be passed to activities using the intent. 
 */
public class Globals extends Application {
	public Jam jam = new Jam(this); 

	private int serverPort;
	
	public String currentArtistView = ""; 
	public Handler uiUpdateHandler; 
	public Handler joinJamHandler; 
	
	public int playerDuration = 0; 
	public OnPreparedListener playerListener; 
	
	public MusicLibraryLoaderThread localLoaderThread; 
	
	public Lock jamLock = new ReentrantLock(); 
	
	public DiscoveryManager discoveryManager;
	
	public Server server;
	
	public Logger logger;

	DatabaseHandler db; 
	
	private static Context context; 
	private LoggerAlarmReceiver loggerAlarm;
	
	/**
	 * Initializes the global variables for Tutti. 
	 * 
	 * @see android.app.Application#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate() {
		super.onCreate();
		Globals.context = getApplicationContext(); 
		db = new DatabaseHandler(this);
		jam.setIPUsername(getIpAddr(), getUsername());
		discoveryManager = new DiscoveryManager(this);
		logger = new Logger(this);
		loggerAlarm = new LoggerAlarmReceiver();
		loggerAlarm.setAlarm(context, false);
	}

	/**
	 * Helper method to get the global context
	 * from anywhere in the app. 
	 */
	public static Context getAppContext() {
		return Globals.context;
	}

	/**
	 * Returns a string representation of the current device's IP address. 
	 * 
	 * @return String ipAddress
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

	/**
	 * Returns a string representation of the current device's gateway IP address.
	 * 
	 * @return String gatewayIpAddress
	 */
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

	/**
	 * Returns the SSID of the current WiFi network.
	 * 
	 * @returns String wifiSSID
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

	/**
	 * Returns the username from the user preferences. 
	 * 
	 * @returns String username
	 */
	public String getUsername() {
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		return preferences.getString("prefUsername", "anonymous"); 
	}
	
	/**
	 * Returns the current system timestamp,
	 * in a punctuationless format better suited for IDs than printing. 
	 * 
	 * @returns String timestamp
	 */
	public String getTimestamp() {
		Date date = new Date();
		SimpleDateFormat sdf = new SimpleDateFormat("ddhhmmssSSSa");
		return sdf.format(date);
	}
	
	/**
	 * Sends a String message to the global UI update handler.
	 * 
	 * @param String message
	 */
	public void sendUIMessage(String message) {
		if (uiUpdateHandler != null) {
			Message msg = uiUpdateHandler.obtainMessage();
			msg.obj = message; 
			uiUpdateHandler.sendMessage(msg);
		}
	}
	
	/**
	 * Sends an int message to the global UI update handler.
	 * 
	 * @param int message
	 */
	public void sendUIMessage(int message) {
		if (uiUpdateHandler != null) {
			Message msg = uiUpdateHandler.obtainMessage();
			msg.what = message; 
			uiUpdateHandler.sendMessage(msg);
		}
	}
	
	
	/**
	 * Returns the server port. 
	 * 
	 * @return int servePort
	 */
	public int getServerPort() {
		return serverPort;
	}
	
	/**
	 * Sets the server port. 
	 * 
	 * @param int port
	 */
	public void setServerPort(int port) {
		this.serverPort = port;
	}

}
