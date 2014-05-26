package com.stanford.tutti;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class JoinJamActivity extends Activity {
	private ListView jamListView;
	private Server server;
	private Globals g;
	private final String path = "/discoverJams";
	private Map<String, String> ipMap;
	private Map<String, String> requestedMap; 
	private ArrayList<String> nameList = new ArrayList<String>();


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join_jam);

		g = (Globals) getApplication();

		ipMap = new HashMap<String, String>();
		requestedMap = new HashMap<String, String>(); 

		server = new Server(g);

		try {
			server.start();
			g.db.updatePortForLocalSongs();
		} catch (IOException e) {
			// unable to start server
			// should display a message to the user or back out to main menu
			e.printStackTrace();
		}

		setupHandler(); 
		configureJamListView(); 
		g.discoveryManager.startJamDiscovery();
	}

	private void configureJamListView() {
		jamListView = (ListView) this.findViewById(R.id.jamListView);
		jamListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				Globals g = (Globals) getApplication();
				String jamName = ((TextView) arg1).getText().toString();
				final String ipPortString = ipMap.get(jamName);

				if (requestedMap.containsKey(ipPortString)) { 
					Toast.makeText(g, "Already sent request to join " + jamName, Toast.LENGTH_SHORT).show(); 
					return; 
				} else {
					requestedMap.put(ipPortString, "true"); 
					Toast.makeText(g, "Requested to join " + jamName, Toast.LENGTH_SHORT).show(); 
				}

				String split[] = ipPortString.split(":");
				String ip = split[0];
				String port = split[1];

				final Client masterClient = new Client(g, "", ip, Integer.parseInt(port)); 
				masterClient.requestJoinJam(g.getUsername(), g.getServerPort(), new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						System.out.println("response for join jam: " + statusCode);
					}
				});
			}
		});

	}

	private void setupHandler() {
		g.joinJamHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				if (msg.what == 0) {		// accepted or rejected
					String message = (String) msg.obj; 
					if (message != null) {
						String[] tokens = message.split("//"); 
						final String ipAddr = tokens[1]; 
						if (tokens[0].equals("ACCEPTED")) {
							System.out.println("tokens: ");
							for (String str : tokens) System.out.println(str);
							final String username = tokens[1]; 
							final int masterPort = Integer.parseInt(tokens[2]);
							final String jamName = tokens[3]; 						

							g.jam.setJamName(jamName);

							g.jam.setMaster(false); 
							g.jam.setMasterIp(ipAddr);
							g.jam.setIPUsername(ipAddr, username);
							g.jam.setMasterPort(masterPort);

							Client masterClient = new Client(g, username, ipAddr, masterPort);
							g.jam.addClient(masterClient);

							try {
								g.localLoaderThread.join();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}

							Thread getLibraryThread = new RequestLibraryThread(g, ipAddr, masterPort);
							getLibraryThread.start();

							Intent intent = new Intent(JoinJamActivity.this, BrowseMusicActivity.class);
							startActivity(intent);
							finish();
						} 
						else if (tokens[0].equals("REJECTED")) {
							requestedMap.remove(ipAddr); 
						}
					}
				}
				else if (msg.what == 1) { // new jam discovered -> refresh list
					String message = (String) msg.obj;
					// message has format jamName:ipAdddr:port
					String split[] = message.split(":");
					String jamName = split[0];
					String ipAddr = split[1];
					String port = split[2];
					ipMap.put(jamName,  ipAddr + ":" + port);

					// update list
					nameList.add(jamName);
					ListView jamListView = (ListView) findViewById(R.id.jamListView);
					ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
							JoinJamActivity.this, android.R.layout.simple_list_item_1,
							nameList);
					jamListView.setAdapter(arrayAdapter);
				}
			}
		};
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.join_jam, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item.getItemId() == R.id.action_settings) {
			Intent intent = new Intent(JoinJamActivity.this, SettingsMenuActivity.class); 
			startActivity(intent); 
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
