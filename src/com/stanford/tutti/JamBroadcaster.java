package com.stanford.tutti;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pUpnpServiceInfo;
import android.util.Log;


/*
 * Periodically broadcasts the jam information using UDP. 
 * 
 * Should only be performed by the master phone.
 */
public class JamBroadcaster {
	
	private final String TAG = "JamBroadcaster";
	private WifiP2pManager manager;
	private Channel channel;
	private Globals g;
	
	// TXT RECORD properties
	public static final String TXTRECORD_PROP_AVAILABLE = "available";
	public static final String SERVICE_INSTANCE = "_tutti_test";
	public static final String SERVICE_REG_TYPE = "_http._tcp";

	public static final int MESSAGE_READ = 0x400 + 1;
	public static final int MY_HANDLE = 0x400 + 2;

	public JamBroadcaster(Globals g) {
		this.g = g;
        manager = (WifiP2pManager) Globals.getAppContext().getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(Globals.getAppContext(), Globals.getAppContext().getMainLooper(), null);
        manager.clearLocalServices(channel, null);
		startRegistration();
	}
	
	private void startRegistration() {
        Map<String, String> record = new HashMap<String, String>();
        record.put(TXTRECORD_PROP_AVAILABLE, "visible");
        record.put("ip", g.getIpAddr());

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(
                SERVICE_INSTANCE, SERVICE_REG_TYPE, record);
        /*ArrayList<String> services = new ArrayList<String>();
        services.add("c");
        WifiP2pUpnpServiceInfo service = 
        		WifiP2pUpnpServiceInfo.newInstance(
        				"uuid:6859dede-8574-59ab-9332-123456789012::urn:schemas-upnp-org:device:", 
        				"b", services);
        				*/
        manager.addLocalService(channel, service, new ActionListener() {

            @Override
            public void onSuccess() {
                Log.d(TAG, "Added Local Service");
            }

            @Override
            public void onFailure(int error) {
                Log.d(TAG, "Failed to add a service");
            }
        });
    }
	
	public void stop() {
		manager.clearLocalServices(channel, new ActionListener() {
            @Override
            public void onSuccess() {
            	System.out.println("Unregistered p2p services");
            }

            @Override
            public void onFailure(int arg0) {
            	System.out.println("ERROR: failure to unregister p2p services in broadcaster.");
            }
		});
	}
	
}
