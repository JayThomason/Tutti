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
	private Handler handler;
	private boolean master; 
	// We should really be building this up as a global
	HashMap<String, Song> songMap; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_new_jam);
		setupActionBar();
		getMasterBoolFromBundle();
		g = (Globals) getApplication(); 
		g.jam.setMaster(master); 
		
		// Show the unique code (ip) for "join jam" requests
		EditText editText = (EditText) this.findViewById(R.id.ip_address);
		editText.setText("Your Jam ID is: " + g.getIpAddr());
		
		/*
		// get the listview
		expListView = (ExpandableListView) findViewById(R.id.listView1);
		// preparing list data
		prepareListData();
		listAdapter = new ExpandableListAdapter(this, listDataHeader, listDataChild);
		// setting list adapter
		expListView.setAdapter(listAdapter);
		setItemOnClickListener();
		setUpHandler();*/
		if (master) {
			setUpServer();
			System.out.println("booted server!");
			(new CreateJamInDatabaseThread(getString(R.string.ec2_server), g.getIpAddr())).start();
		}
		
		Intent intent = new Intent(this, ViewArtistsActivity.class);
		startActivity(intent);
	}
	
	/*
	 * Initializes the handler. The handler is used to receive messages from
	 * the server and to update the UI accordingly.
	 */
	private void setUpHandler() {
		handler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				/*
				 * When we get a message from another phone that we have new
				 * non-local music, we can update the list-view for the library.
				 */
				if (msg.what == 0) {
					prepareListData();
					listAdapter = new ExpandableListAdapter(
							getBaseContext(), listDataHeader, listDataChild);
					expListView.setAdapter(listAdapter);
				}
				super.handleMessage(msg);
			}
		};		
	}
	
	/*
	 * Starts the embedded NanoHttpd server.
	 */
	private void setUpServer() {
		g.uiUpdateHandler = handler;
		server = new Server(PORT, g, handler);
		try {
			server.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Sets the onClickListener for the items (songs) in the artist/song 
	 * drop down list.
	 */
	private void setItemOnClickListener() {
		expListView.setOnChildClickListener(new OnChildClickListener() {
			@Override
			public boolean onChildClick(ExpandableListView parent, View v, 
					int groupPosition, int childPosition, long id) {
				String songName = listDataChild.get(
						listDataHeader.get(groupPosition)).get(childPosition); 
				Song song = songMap.get(songName); 
				Globals g = (Globals) getApplication();
				g.jam.addSong(song); 
				Toast.makeText(getApplicationContext(),
						listDataHeader.get(groupPosition)
						+ " : " + songName 
						+ " added to Jam", Toast.LENGTH_SHORT).show();                
				if (master) {
					if (g.jam.getCurrentSong() == null) {
						g.jam.setCurrentSong(song);
						g.jam.playCurrentSong();
					}          
				} 
				new PassMessageThread(g.jam.getOtherIP(), 1234, 
						"/jam/add/" + Integer.toString(song.hashCode()), "").start(); 
				return false;
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
	 * Set up the nested/expandable ListView of artist/songs.
	 */
	private void prepareListData() {
		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<String>>();
		songMap = new HashMap<String, Song>(); 
		Globals g = (Globals) getApplication();
		
		//ArrayList<Artist> artists = g.getArtistList();

		List<Song> songs = new ArrayList<Song>(); // g.db.getAllSongs(); 
		
		/*
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
		*/
		
		for (int i = 0; i < songs.size(); ++i) {
			listDataHeader.add(songs.get(i).getArtist()); 
			songMap.put(songs.get(i).getTitle(), songs.get(i));
			List<String> songList = new ArrayList<String>(); 
			songList.add(songs.get(i).getTitle()); 
			listDataChild.put(listDataHeader.get(i), songList);
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
