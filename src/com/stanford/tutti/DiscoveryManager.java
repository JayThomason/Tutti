package com.stanford.tutti;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;

public class DiscoveryManager {
	private Globals g;
	private final WifiP2pManager mManager;
	private final Channel mChannel;


	public DiscoveryManager(Globals g) {
		this.g = g;

		final Context appContext = g.getApplicationContext();
		mManager = (WifiP2pManager) g.getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(appContext, appContext.getMainLooper(), null);	
	}

	/*
	 * Makes the current jam discoverable by broadcasting a local DnsSd service over Wifi P2P.
	 */
	public void makeJamDiscoverable(String jamName) {
		Map<String, String> record = new HashMap<String, String>();
		int port = g.getServerPort();
		record.put("port", String.valueOf(port));
		record.put("ipAddr", g.getIpAddr());
		//record.put("TuttiJam", "true");
		record.put("name", jamName);

		// Service information.  Pass it an instance name, service type
		// _protocol._transportlayer , and the map containing
		// information other devices will want once they connect to this one.
		WifiP2pDnsSdServiceInfo serviceInfo =
				WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);

		// Add the local service, sending the service info, network channel,
		// and listener that will be used to indicate success or failure of
		// the request.
		mManager.addLocalService(mChannel, serviceInfo, new ActionListener() {
			@Override
			public void onSuccess() {
				System.out.println("created local service!");
			}

			@Override
			public void onFailure(int arg0) {
				System.out.println("failed to create local service :(((");

				// What to do if we can't broadcast? -- display message to user saying unable to host jam...
			}
		});
	}

	public void stopJamDiscoverable() {
		mManager.clearLocalServices(mChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				System.out.println("removed local service!");
			}

			@Override
			public void onFailure(int arg0) {
				System.out.println("failed to remove local service :(");
				// shouldn't be a real problem -- closing the app will cause the service to be undiscoverable in a couple minutes
			}
		});
	}

	public void startJamDiscovery() {

		// map from name to ip:port
		final HashMap<String, String> jams = new HashMap<String, String>();

		DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {

			@Override
			public void onDnsSdTxtRecordAvailable(String arg0,
					Map<String, String> arg1, WifiP2pDevice arg2) {
				System.out.println("DnsSdTxtRecord available -" + arg1.toString());
				// should ping /exists endpoint here before adding to list
				jams.put(arg1.get("name"), arg1.get("ipAddr") + arg1.get("port"));				
			}
		};

		DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
			@Override
			public void onDnsSdServiceAvailable(String instanceName, String registrationType,
					WifiP2pDevice resourceType) {
				System.out.println("device: " + resourceType);
			}
		};

		mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

		WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

		mManager.addServiceRequest(mChannel,
				serviceRequest,
				new ActionListener() {
			@Override
			public void onSuccess() {
				// Success!
				System.out.println("added service request for discovery");
			}

			@Override
			public void onFailure(int code) {
				// Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
				System.out.println("failed to add service request for discovery :(");
			}
		});

		mManager.discoverServices(mChannel, new ActionListener() {

			@Override
			public void onSuccess() {
				System.out.println("success discover services...");
			}

			@Override
			public void onFailure(int code) {
				System.out.println("failed to discover services... :(((");
			}
		});	
	}

	public boolean stopJamDiscovery() {
		// not sure if this works or not
		mManager.clearServiceRequests(mChannel, new ActionListener() {
			@Override
			public void onSuccess() {
				System.out.println("clearing discovery service requests");
			}

			@Override
			public void onFailure(int code) {
				System.out.println("could not clear discovery service requests");
			}
		});

		return true;
	}

}
