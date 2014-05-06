package com.stanford.tutti;

import java.io.IOException;

import org.apache.http.Header;


import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;


/*
 * The NewJam Activity is started when a new jam is created on a master
 * phone or a jam is connected to from a client phone (thus it is probably
 * a bit of a misnomer for now). 
 * It displays a artist/song dropdown list. When a song is clicked on, it is
 * added to the jam playlist on both phones and, if a song is not currently
 * playing, it begins playing from the master.
 */
public class NewJamActivity extends Activity {
	private final int PORT = 1234;
	private Server server;
	private Globals g; 
	private boolean master; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_jam);
		//setupActionBar();
		
        getActionBar().hide(); 
		
		g = (Globals) getApplicationContext(); 
		g.jam.setMaster(true); 
		
		setUpServer();
		createJamInDatabase();
		
		
		/* DEV CODE: */
		/* AUTO-FORWARDS TO MUSIC BROWSER WITH IP-BASED NAME */
		
		// Show the unique code (ip) for "join jam" requests
		EditText editText = (EditText) this.findViewById(R.id.ip_address);
		editText.setText("Your Jam ID is: " + g.getIpAddr());

		Intent intent = new Intent(this, BrowseMusicActivity.class);
		startActivity(intent);
	}
	
	/*
	 * Creates the jam in the database.
	 */
	private void createJamInDatabase() {
		AsyncHttpClient client = new AsyncHttpClient();
		String serverHostname = getString(R.string.ec2_server);
		String localIpAddr = g.getIpAddr();
		String url = "http://" + serverHostname + "/createJam?private=" + localIpAddr;
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				if (statusCode == 200) {
					System.out.println("Successfully created jam on server.");
				}
				else {
					System.out.println("Failed to create jam on server.");
				}
			}
			
			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				System.out.println("Failed to create jam on server.");
			}
		});
	}

	/*
	 * Initializes the master boolean to either true or false depending on
	 * whether the activity was started with a host variable in the 
	 */
	private void getMasterBoolFromBundle() {
		Bundle b = getIntent().getExtras();
		if (b != null && b.containsKey("host")) {
			int value = b.getInt("host");
			master = (value == 1); 
		} else {
			master = false; 
		}
	}
	
	/*
	 * Starts the embedded NanoHttpd server.
	 */
	private void setUpServer() {
		server = new Server(PORT, g);
		try {
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		getMenuInflater().inflate(R.menu.new_jam, menu);
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
