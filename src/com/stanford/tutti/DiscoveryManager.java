package com.stanford.tutti;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.os.Message;

public class DiscoveryManager {
	private Globals g;
	private final WifiP2pManager mManager;
	private final Channel mChannel;
	private AtomicBoolean shouldBroadcastJam;
	
	private static final String DNS_SERVICE_NAME = "_tutti_jam";
	private static final String DNS_PROTOCOL_NAME = "_presence";
	private static final String DNS_TRANSPORT_NAME = "_tcp";
	private static final String DNS_FULL_NAME = DNS_SERVICE_NAME + "." 
			+	DNS_PROTOCOL_NAME + "." + DNS_TRANSPORT_NAME + ".local.";
	
	private static final String IP_PORT_KEY = "ip_port";
	private static final String JAM_NAME_KEY = "jam_name";

	public DiscoveryManager(Globals g) {
		this.g = g;

		final Context appContext = g.getApplicationContext();
		mManager = (WifiP2pManager) g.getSystemService(Context.WIFI_P2P_SERVICE);
		mChannel = mManager.initialize(appContext, appContext.getMainLooper(), null);
		shouldBroadcastJam = new AtomicBoolean(false);
	}

	/*
	 * Makes the current jam discoverable by broadcasting a local DnsSd service over Wifi P2P.
	 */
	public void makeJamDiscoverable() {
		Map<String, String> record = new HashMap<String, String>();
		String ipPortValue = g.getIpAddr() + ":" + String.valueOf(g.getServerPort());
		String jamNameValue = g.jam.getJamName();
		System.out.println("jam name: " + jamNameValue + " ip port: " + ipPortValue);
		record.put(IP_PORT_KEY, ipPortValue);
		record.put(JAM_NAME_KEY,  jamNameValue);

		// Service information.  Pass it an instance name, service type
		// _protocol._transportlayer , and the map containing
		// information other devices will want once they connect to this one.
		WifiP2pDnsSdServiceInfo serviceInfo =
				WifiP2pDnsSdServiceInfo.newInstance(DNS_SERVICE_NAME, 
						DNS_PROTOCOL_NAME + "." + DNS_TRANSPORT_NAME, record);
		System.out.println("DNS_FULL_NAME: " + DNS_FULL_NAME);

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
		
		
		// not sure why we need this code, but discovery doesn't seem to work without it.
		// essentially the same code as in startJamDiscovery
		
		DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {
			@Override
			public void onDnsSdTxtRecordAvailable(String arg0,
					Map<String, String> arg1, WifiP2pDevice arg2) {}
		};

		DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
			@Override
			public void onDnsSdServiceAvailable(String instanceName, String registrationType,
					WifiP2pDevice resourceType) {}
		};

		mManager.setDnsSdResponseListeners(mChannel, servListener, txtListener);

		WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();

		mManager.addServiceRequest(mChannel,
				serviceRequest,
				new ActionListener() {
			@Override
			public void onSuccess() {
				System.out.println("added service request for discovery");
			}

			@Override
			public void onFailure(int code) {
				System.out.println("failed to add service request for discovery :(");
				// what to do here? -- display message to user unable to broadcast jam?
			}
		});

		shouldBroadcastJam.set(true);
		
		(new Thread() {
			public void run() {
				while (true) {
					if (shouldBroadcastJam.get()) {
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
						
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e) {
							System.out.println("unable to sleep in discovery broadcast thread...\n");
							break;
						}
					}
					else {
						break;
					}
				}
			}
		}).start();
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
		
		shouldBroadcastJam.set(false);
	}

	public void startJamDiscovery () {
		DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {
			@Override
			public void onDnsSdTxtRecordAvailable(String dnsName,
					final Map<String, String> record, WifiP2pDevice arg2) {
				
				if (dnsName.equals(DNS_FULL_NAME)) {
					String ipPort = record.get(IP_PORT_KEY);
					if (ipPort == null) {
						return;
					}
					String split[] = ipPort.split(":");
					final String ip = split[0];
					final String port = split[1];
					final String jamName = record.get(JAM_NAME_KEY);
					if (ip == null || port == null || jamName == null) {
						return;
					}
					Client client = new Client(g, null, ip, Integer.parseInt(port));
					client.isMaster(new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
							System.out.println("response from jam active...: " + statusCode);
							if (statusCode == 200) { // jam is hosted here
								System.out.println("discovered jam: " + jamName);
								if (g.joinJamHandler != null) {
									Message msg = g.joinJamHandler.obtainMessage(1);
									msg.obj = jamName + ":" + ip + ":" + port;
									g.joinJamHandler.sendMessage(msg);
								}
							}
						}
						
						@Override
						public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
							System.out.println("statusCode: " + statusCode);
							System.out.println("error: " + error);
						}						
					});
					System.out.println("checking if jam is active...");
				}
			}
		};

		DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
			@Override
			public void onDnsSdServiceAvailable(String instanceName, String registrationType,
					WifiP2pDevice resourceType) {
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
