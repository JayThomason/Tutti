package com.stanford.tutti;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.DnsSdServiceResponseListener;
import android.net.wifi.p2p.WifiP2pManager.DnsSdTxtRecordListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Globals g;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		g = (Globals) getApplicationContext(); 

		loadLocalMusic(); 
		initializeJam(); 
		setWelcomeText();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		System.out.println("Main Activity Restarted.");
		g.jam.endServerKeepAlive();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(MainActivity.this, SettingsMenuActivity.class); 
			startActivity(intent); 
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	public void makeNewJam(View view) {
		showNameJamDialog();
	}

	public void joinJam(View view) {
		Intent intent = new Intent(this, JoinJamActivity.class);
		startActivity(intent);
	}

	public void settingsMenu(View view) {
		Intent intent = new Intent(this, SettingsMenuActivity.class);
		startActivity(intent);
	}

	public void helpMenu(View view) {
		Intent intent = new Intent(this, HelpMenuActivity.class);
		startActivity(intent);
	}

	private void loadLocalMusic() {
		g.db.dropTable("songs"); 
		g.localLoaderThread = new MusicLibraryLoaderThread(this);
		g.localLoaderThread.start();	
	}

	private void initializeJam() {
		Globals g = (Globals) getApplicationContext(); 
		g.db.dropTable("jam"); 
	}

	private void setWelcomeText() {
		Globals g = (Globals) getApplicationContext(); 
		TextView welcomeText = (TextView) findViewById(R.id.welcome_message); 
		if (g.getUsername().equals("anonymous")) {
			welcomeText.setText("Set your username in the Settings menu!");
		} else {
			welcomeText.setText("Welcome back " + g.getUsername() + "!"); 
		}
	}

	private void showNameJamDialog() {
		final EditText input = new EditText(MainActivity.this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		input.setLayoutParams(lp);

		final AlertDialog nameDialog = new AlertDialog.Builder(this)
		.setTitle("Choose a name")
		.setMessage("Enter a name below or just press create jam to use a default name.")
		.setPositiveButton("Create", null)
		.setNegativeButton("Cancel", null)
		.setView(input)
		.create();
		setNameDialogShowListener(nameDialog, input);
		nameDialog.show();
	}

	private void setNameDialogShowListener(final AlertDialog nameDialog, final EditText input) {
		nameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Button b = nameDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String jamName = input.getText().toString();
						if (jamName.contains(" ")) {
							Toast.makeText(getApplicationContext(), 
									"The jam name may not contain spaces.", Toast.LENGTH_LONG).show();
						}
						else {
							startJam(jamName, nameDialog);
						}
					}
				});
			}
		});
	}

	// return true if jam created in db, false if definite failure
	private boolean startJam(String jamName, final AlertDialog nameDialog) {
		try {
			Server s = new Server(g);
			s.start();
			g.db.updatePortForLocalSongs();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		int port = g.getServerPort();
		if (port > 0) {
			startJamBroadcast(jamName);
			//createJamInDatabase(jamName.isEmpty() ? null : jamName, nameDialog, port);
			return true;
		}
		else {
			Toast.makeText(this, "Unable to create jam in database -- bad port number!", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	private void createJamInDatabase(String name, final AlertDialog nameDialog, int port) {
		final String serverHostname = getString(R.string.ec2_server);
		Uri.Builder builder = Uri.parse("http://" + serverHostname).buildUpon();
		builder.path("/createJam");
		builder.appendQueryParameter("private",  g.getIpAddr());
		builder.appendQueryParameter("ssid",  g.getWifiSSID());
		builder.appendQueryParameter("gateway", g.getGatewayIpAddr());
		builder.appendQueryParameter("port", String.valueOf(g.getServerPort()));

		if (name != null) {
			builder.appendQueryParameter("name", name);
		}

		AsyncHttpClient client = new AsyncHttpClient();
		getCreateJam(name, client, builder.build().toString(), serverHostname, nameDialog);
	}

	private void getCreateJam(final String jamName, AsyncHttpClient client, String url,
			final String serverHostname, final AlertDialog nameDialog) {
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				if (statusCode == 200) {
					System.out.println("Successfully created jam on server.");
					g.jam.startServerKeepAlive(serverHostname);
					g.jam.startMasterClientPingThread();
					g.jam.setMaster(true);
					if (jamName != null && !jamName.equals("")) {
						g.jam.setJamName(jamName);
					} else {
						g.jam.setJamName("Jam-" + g.getIpAddr());
					}
					nameDialog.dismiss();

					try {
						g.localLoaderThread.join();
					} catch (InterruptedException e) {
						e.printStackTrace();
					} 

					Intent intent = new Intent(MainActivity.this, BrowseMusicActivity.class);
					startActivity(intent);
				}
				else {
					nameDialog.dismiss();
					System.out.println("Failed to create jam on server.");
					System.out.println("Response body: " + new String(responseBody));
					Toast.makeText(MainActivity.this,
							"Unable to create jam on server." , Toast.LENGTH_SHORT)
							.show();				
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				System.out.println("Failed to create jam on server.");
			}
		});
	}

	// starts broadcasting the jam by making 
	private void startJamBroadcast(String jamName) {
		g.jam.startMasterClientPingThread();
		g.jam.setMaster(true);
		if (jamName != null && !jamName.equals("")) {
			g.jam.setJamName(jamName);
		} else {
			g.jam.setJamName("Jam-" + g.getIpAddr());
		}
		startRegistration();
		//Intent intent = new Intent(MainActivity.this, BrowseMusicActivity.class);
		//startActivity(intent);
	}

	private void startRegistration() {
		final WifiP2pManager mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		final Channel mChannel = mManager.initialize(this, getMainLooper(), null);	

		//  Create a string map containing information about your service.
		Map<String, String> record = new HashMap<String, String>();
		record.put("listenport", String.valueOf(1234));
		record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
		record.put("available", "visible");

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
				// Command successful! Code isn't necessarily needed here,
				// Unless you want to update the UI or add logging statements.
				System.out.println("created local service!");
			}

			@Override
			public void onFailure(int arg0) {
				// Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
				System.out.println("failed to create local service :(((");
			}
		});


		final HashMap<String, String> buddies = new HashMap<String, String>();

		DnsSdTxtRecordListener txtListener = new DnsSdTxtRecordListener() {

			@Override
			public void onDnsSdTxtRecordAvailable(String arg0,
					Map<String, String> arg1, WifiP2pDevice arg2) {
				System.out.println("DnsSdTxtRecord available -" + arg1.toString());
				buddies.put(arg2.deviceAddress, arg1.get("buddyname"));				
			}
		};
		
		DnsSdServiceResponseListener servListener = new DnsSdServiceResponseListener() {
	        @Override
	        public void onDnsSdServiceAvailable(String instanceName, String registrationType,
	                WifiP2pDevice resourceType) {

	                // Update the device name with the human-friendly version from
	                // the DnsTxtRecord, assuming one arrived.
	                resourceType.deviceName = buddies
	                        .containsKey(resourceType.deviceAddress) ? buddies
	                        .get(resourceType.deviceAddress) : resourceType.deviceName;

	                // Add to the custom adapter defined specifically for showing
	                // wifi devices.

	                        
	                System.out.println("onBonjourServiceAvailable " + instanceName);
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

}
