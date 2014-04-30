package com.stanford.tutti;

import java.io.IOException;
import java.util.*;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.Toast;

/*
 * The NewJam Activity is started when a new jam is created on a master
 * phone or a jam is connected to from a client phone (thus it is probably
 * a bit of a misnomer for now). 
 * It displays a artist/song dropdown list. When a song is clicked on, it is
 * added to the jam playlist on both phones and, if a song is not currently
 * playing, it begins playing from the master.
 */
public class NewJamActivity extends Activity {
	private ExpandableListAdapter listAdapter;
	private ExpandableListView expListView;
	private List<String> listDataHeader;
	private HashMap<String, List<String>> listDataChild;
	private final int PORT = 1234;
	private Server server;
	private Globals g; 
	private boolean master; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_jam);
		setupActionBar();
		
		getMasterBoolFromBundle();
		g = (Globals) getApplicationContext(); 
		g.jam.setMaster(master); 

		// Show the unique code (ip) for "join jam" requests
		EditText editText = (EditText) this.findViewById(R.id.ip_address);
		editText.setText("Your Jam ID is: " + g.getIpAddr());

		if (master) {
			setUpServer();
			(new CreateJamInDatabaseThread(getString(R.string.ec2_server), g.getIpAddr())).start();
		}

		Intent intent = new Intent(this, BrowseMusicActivity.class);
		startActivity(intent);
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
