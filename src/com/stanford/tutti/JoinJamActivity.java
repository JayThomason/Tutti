package com.stanford.tutti;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class JoinJamActivity extends Activity {
	private ListView jamListView;
	private static final int PORT = 1234;
	private Server server;
	private Globals g;
	private Handler h;
	private RequestLocalJamThread requestLocalJamThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join_jam);
		
        getActionBar().setDisplayShowHomeEnabled(false);              
        getActionBar().setDisplayShowTitleEnabled(false);
        getActionBar().hide(); 

		g = (Globals) getApplication();
		configureJamListView(); 
		requestLocalJams();
	}
	
	private void requestLocalJams() {
		String serverHostname = getString(R.string.ec2_server);
		requestLocalJamThread = new RequestLocalJamThread(serverHostname, this);
		requestLocalJamThread.start();
	}
	
	private void configureJamListView() {
		jamListView = (ListView) this.findViewById(R.id.jamListView);
		jamListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				Globals g = (Globals) getApplication();
			    String jamName = ((TextView) arg1).getText().toString();
			    final String ip = requestLocalJamThread.getIpForName(jamName);
				final Client masterClient = new Client(g, "", ip, PORT); 
				masterClient.requestJoinJam(g.getUsername(), new AsyncHttpResponseHandler() {
					@Override
					public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
						Globals g = (Globals) getApplication(); 
						g.jam.setMaster(false); 
						g.jam.setMasterIp(ip);
						// ADD
						// CLIENT
						// OBJECT
						// FOR 
						// MASTER??? 
						server = new Server(PORT, g);
						try {
							server.start();
						} catch (IOException e) {
							// unable to start server
							// should display a message to the user or back out to main menu
							e.printStackTrace();
						}
						
						masterClient.requestRemoteLibrary(new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
								try {
									
									Globals g = (Globals) getApplication(); 
									
									ByteArrayInputStream is = new ByteArrayInputStream(responseBody); 
									BufferedReader reader = new BufferedReader(
											new InputStreamReader(is));
									String remoteLibrary = reader.readLine();
									
									JSONObject jsonLibrary = new JSONObject(remoteLibrary);

									String username = jsonLibrary.getString("username"); 
									g.jam.setIPUsername(ip, username); 

									JSONArray artists = jsonLibrary.getJSONArray("artists");
									JSONObject jam = jsonLibrary.getJSONObject("jam"); 
									g.db.loadMusicFromJSON(artists); 
									g.jam.loadJamFromJSON(jam); 
									
									// Load the music browser as a client phone
									Intent intent = new Intent(JoinJamActivity.this, BrowseMusicActivity.class);
									startActivity(intent);
									finish();
								}
								catch (IOException e) {
									e.printStackTrace();
								} catch (JSONException e) {
									e.printStackTrace();
								}
								
							}
						});
					}
				});
			}
		});
	
	}

	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
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
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
