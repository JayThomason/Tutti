package com.stanford.tutti;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.Toast;



public class NewJamActivity extends Activity {

	ExpandableListAdapter listAdapter;
	ExpandableListView expListView;
	List<String> listDataHeader;
	HashMap<String, List<String>> listDataChild;

	private final int PORT = 1234;
	private Server server;
	private Globals g; 
	private Handler handler;
	
	private boolean master; 

	// We should really be building this up as a global
	HashMap<String, Song> songMap; 


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_jam);
		// Show the Up button in the action bar.
		setupActionBar();

		Bundle b = getIntent().getExtras();
		if (b != null && b.containsKey("host")) {
			int value = b.getInt("host");
			master = (value == 1); 
		} else {
			master = false; 
		}
		
		g = (Globals) getApplication(); 
		g.jam.setMaster(master); 

		// Show the unique code for "join jam" requests
		EditText editText = (EditText) this.findViewById(R.id.ip_address);
		editText.setText("Your Jam ID is: " + getIpAddr());

		// get the listview
		expListView = (ExpandableListView) findViewById(R.id.listView1);

		// preparing list data
		prepareListData();

		listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);

		// setting list adapter
		expListView.setAdapter(listAdapter);

		expListView.setOnChildClickListener(new OnChildClickListener() {

			@Override
			public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

				String songName = listDataChild.get(listDataHeader.get(groupPosition)).get(childPosition); 
				Song song = songMap.get(songName); 
				Globals g = (Globals) getApplication();
				g.jam.addSong(song); 
				Toast.makeText(
						getApplicationContext(),
						listDataHeader.get(groupPosition)
						+ " : "
						+ songName 
						+ " added to Jam", Toast.LENGTH_SHORT)
						.show();                

				if (master) {
					if (g.jam.getCurrentSong() == null) {
						g.jam.setCurrentSong(song);
						g.jam.playCurrentSong();
					}          
				} 
				
				// Send a message to the other phone to add the song to its version of the jam
				new PassMessageThread(g.jam.getOtherIP(), "/jam/add/" + song.getUniqueKey()).start(); 

				return false;
			}
		});

		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				/*
				 * When we get a message from the server that we have new non-local music, we can
				 * update the list-view for the library.
				 */
				if (msg.what == 0) {
					System.out.println("ATTEMPTING TO UPDATE LIST VIEWWWWWWW\n\n\n");
					prepareListData();
					listAdapter = new ExpandableListAdapter(getBaseContext(), listDataHeader, listDataChild);
					expListView.setAdapter(listAdapter);
				}
				super.handleMessage(msg);
			}
		};
		
		server = new Server(PORT, g, handler);
		try {
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Return a string representation of the current device's IP address. 
	 */
	public String getIpAddr() {
		WifiManager wifiManager = 
				(WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format(
				"%d.%d.%d.%d",
				(ip & 0xff),
				(ip >> 8 & 0xff),
				(ip >> 16 & 0xff),
				(ip >> 24 & 0xff));

		return ipString;
	}


	/*
	 * Set up the nested/expandable ListView
	 */
	private void prepareListData() {
		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<String>>();
		songMap = new HashMap<String, Song>(); 

		Globals g = (Globals) getApplication();
		ArrayList<Artist> artists = g.getArtistList();

		for (int i = 0; i < artists.size(); ++i) {
			listDataHeader.add(artists.get(i).getName());
			ArrayList<Album> albums = artists.get(i).getAlbumList(); 
			ArrayList<String> songs = new ArrayList<String>(); 
			for (int j = 0; j < albums.size(); j++) {
				ArrayList<Song> albumSongs = albums.get(j).getSongList(); 
				for (int k = 0; k < albumSongs.size(); k++) {
					Song song = albumSongs.get(k); 
					songs.add(song.getTitle()); 
					songMap.put(song.getTitle(), song);
				}
			}
			listDataChild.put(listDataHeader.get(i), songs);
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

	public void viewJam(View view) {
		Intent intent = new Intent(this, ViewJamActivity.class);
		Bundle bundle = new Bundle();    	
		intent.putExtras(bundle);
		startActivity(intent);
	}
}
