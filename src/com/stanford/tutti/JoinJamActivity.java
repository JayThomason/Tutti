package com.stanford.tutti;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pUpnpServiceRequest;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class JoinJamActivity extends Activity implements ConnectionInfoListener {
	private Button joinButton; 
	private static final int PORT = 1234;
	private EditText editText;
	private Server server;
	private Globals g;
	private Handler h;

	static final String TAG = "JoinJamListener";
	private WifiP2pManager manager;
	private Channel channel;
	final HashMap<String, String> buddies = new HashMap<String, String>();
	private final IntentFilter intentFilter = new IntentFilter();
	private BroadcastReceiver receiver = null;
	//private WifiP2pUpnpServiceRequest serviceRequest;

	private WifiP2pDnsSdServiceRequest serviceRequest;

	// TXT RECORD properties
	public static final String TXTRECORD_PROP_AVAILABLE = "available";
	public static final String SERVICE_INSTANCE = "_tutti_test";
	public static final String SERVICE_REG_TYPE = "_http._tcp";

	public static final int MESSAGE_READ = 0x400 + 1;
	public static final int MY_HANDLE = 0x400 + 2;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join_jam);
		// Show the Up button in the action bar.
		setupActionBar();
		editText = (EditText) this.findViewById(R.id.ip_address);
		g = (Globals) getApplication();
		configureJoinJamButton(); 
		h = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.getData().getInt("what") == 0) {
					String ip = msg.getData().getString("ip");
					int port = msg.getData().getInt("port");
					joinDiscoveredJam(ip, port);
				}
			}
		};
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
		intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
		intentFilter
		.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
		intentFilter
		.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

		manager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = manager.initialize(this, getMainLooper(), null);
		manager.clearServiceRequests(channel,  null);
		discoverService();
	}

	private void discoverService() {

		/*
		manager.setUpnpServiceResponseListener(channel, 
				new WifiP2pManager.UpnpServiceResponseListener() {

					@Override
					public void onUpnpServiceAvailable(
							List<String> uniqueServiceNames,
							WifiP2pDevice srcDevice) {
						Log.d(TAG, uniqueServiceNames.toString() + " " + srcDevice.toString());
					}
			
		});
		
		serviceRequest = WifiP2pUpnpServiceRequest.newInstance();
		manager.addServiceRequest(channel, serviceRequest,
				new ActionListener() {

			@Override
			public void onSuccess() {
				Log.d(TAG, "Added service discovery request");
			}

			@Override
			public void onFailure(int arg0) {
				Log.d(TAG, "Failed adding service discovery request");
			}
		});
		manager.discoverServices(channel, new ActionListener() {

			@Override
			public void onSuccess() {
				Log.d(TAG, "Service discovery initiated");
			}

			@Override
			public void onFailure(int arg0) {
				Log.d(TAG, "Service discovery failed");
			}
		});
		*/
		/*
		 * Register listeners for DNS-SD services. These are callbacks invoked
		 * by the system when a service is actually discovered.
		 */

		
		manager.setDnsSdResponseListeners(channel,
				new DnsSdServiceResponseListener() {

			@Override
			public void onDnsSdServiceAvailable(String instanceName,
					String registrationType, WifiP2pDevice srcDevice) {

				// A service has been discovered. Is this our app?
				Log.d(TAG, "discovery : " + instanceName);
				if (instanceName.equalsIgnoreCase(SERVICE_INSTANCE)) {
					Log.d(TAG, "onBonjourServiceAvailable "
							+ instanceName);
				}
			}
		}, new DnsSdTxtRecordListener() {

			/**
			 * A new TXT record is available. Pick up the advertised
			 * buddy name.
			 */
		
			@Override
			public void onDnsSdTxtRecordAvailable(
					String fullDomainName, Map<String, String> record,
					WifiP2pDevice device) {
				Log.d(TAG, "fullDomainName: " + fullDomainName);
				for (String str : record.keySet()) {
					Log.d(TAG, device.deviceName + " " + str + " : " + record.get(str));
				}
				if (fullDomainName.startsWith(SERVICE_INSTANCE + "." + SERVICE_REG_TYPE)) {
					joinDiscoveredJam(record.get("ip"), 1234);
				}
			}
		});

		// After attaching listeners, create a service request and initiate
		// discovery.
		serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
		manager.addServiceRequest(channel, serviceRequest,
				new ActionListener() {

			@Override
			public void onSuccess() {
				Log.d(TAG, "Added service discovery request");
			}

			@Override
			public void onFailure(int arg0) {

				Log.d(TAG, "Failed adding service discovery request");
			}
		});
		manager.discoverServices(channel, new ActionListener() {

			@Override
			public void onSuccess() {
				Log.d(TAG, "Service discovery initiated");
			}

			@Override
			public void onFailure(int arg0) {
				Log.d(TAG, "Service discovery failed");
			}
		});
		
	}

	private void joinDiscoveredJam(String ip, int port) {
		Globals g = (Globals) getApplication();
		server = new Server(PORT, g, null);
		g.server = server;
		g.jam.setOtherIP(ip);
		Thread joinJamThread = new JoinJamThread(ip, false);
		try {
			server.start();
			joinJamThread.start();
			joinJamThread.join();
		} catch (InterruptedException e) {
			// probably want to log some message to user: unable to join jam
			e.printStackTrace();
		} catch (IOException e) {
			// unable to start server
			// in either failure case we can't join the jam and thus we should display
			// a message to the user and back out to the main menu or just stay here...
			e.printStackTrace();
		}

		// Load the new jam screen as a slave
		Intent intent = new Intent(JoinJamActivity.this, NewJamActivity.class);
		Bundle b = new Bundle();
		b.putInt("host", 0); //Your id
		intent.putExtras(b);
		startActivity(intent);
		finish();
	}

	private void configureJoinJamButton() {
		joinButton = (Button) this.findViewById(R.id.join_jam_btn);
		joinButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String ip = editText.getText().toString(); 
				joinDiscoveredJam(ip, PORT);
			}
		});		
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.join_jam, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(receiver);
	}

	@Override
	public void onResume() {
		super.onResume();
		receiver = new WiFiDirectBroadcastReceiver(manager, channel, this);
		registerReceiver(receiver, intentFilter);
	}

	@Override
	public void onConnectionInfoAvailable(WifiP2pInfo info) {
		System.out.println(info.toString());
	}
}
