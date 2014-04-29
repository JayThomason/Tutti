package com.stanford.tutti;

import java.io.IOException;

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
		// Show the Up button in the action bar.
		setupActionBar();

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
				// TODO Auto-generated method stub
			    server = new Server(PORT, g);
			    String jamName = ((TextView) arg1).getText().toString();
			    String ip = requestLocalJamThread.getIpForName(jamName);
				Globals g = (Globals) getApplication();
				g.jam.setMasterIp(ip);
				Thread joinJamThread = new JoinJamThread(ip, false);
				try {
					server.start();
					joinJamThread.start();
					/*joinJamThread.join();
				} catch (InterruptedException e) {
					// probably want to log some message to user: unable to join jam
					e.printStackTrace();*/
				} catch (IOException e) {
					// unable to start server
					// in either failure case we can't join the jam and thus we should display
					// a message to the user and back out to the main menu or just stay here...
					e.printStackTrace();
				}
				
				// Load the music browser as a client phone
				Intent intent = new Intent(JoinJamActivity.this, MusicBrowserActivity.class);
				g.jam.setMaster(false); 
				startActivity(intent);
				finish();
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
