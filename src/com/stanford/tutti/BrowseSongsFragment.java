package com.stanford.tutti; 

import org.json.JSONObject;

import android.database.Cursor;
import android.os.Bundle;

import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Fragment that allows the user to scroll through a list on screen that includes
 * every song in the library. When this activity is started from the artists fragment
 * it only includes songs by the artist selected.
 */
public class BrowseSongsFragment extends Fragment {
	private Globals g; 
	private View rootView; 	
	private String columns[]; 
	private int views[]; 
	
	public ListView listView; 
	public BrowseMusicAdapter adapter; 
	

	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.fragment_browse_songs, container, false);
		listView = (ListView) rootView.findViewById(R.id.songListView); 

		g = (Globals) rootView.getContext().getApplicationContext(); 
		
		initializeSongList(); 

		return rootView;
	}

	/**
	 * Initializes the list of songs for the list view.
	 */
	public void initializeSongList() {
		Cursor cursor = g.db.getAllSongs(); 
		
		columns = new String[] { "art", "artist", "title" };
		views = new int[] { R.id.browserArt, R.id.browserText };
		adapter = new BrowseMusicAdapter(g, R.layout.list_layout, 
				cursor, columns, views); 
		
		listView.setAdapter(adapter);
		listView.setFastScrollEnabled(true);
		listView.setTextFilterEnabled(true);
		
		setSongListItemClickListener();
	}
	
	/**
	 * Refreshes the list of songs displayed.
	 */
	public void refreshSongList() {
		Cursor cursor; 
		
		if (!g.currentArtistView.equals("")) {
			cursor = g.db.getSongsByArtist(g.currentArtistView); 
		} else {
			cursor = g.db.getAllSongs(); 
		}

		Cursor oldCursor = adapter.swapCursor(cursor); 
		oldCursor.close(); 
		
		listView.setFastScrollEnabled(true);
	}
	
	/**
	 * Searches the songs for a song with a name that includes the query string.
	 * 
	 * @param query The substring to query for
	 */
	public void searchSongList(String query) {
		if (g.currentArtistView.equals("")) {
			Cursor newCursor = g.db.searchSongs(query); 
		    Cursor oldCursor = adapter.swapCursor(newCursor);
		    oldCursor.close(); 
		} else {
			Cursor newCursor = g.db.searchSongsByArtist(query, g.currentArtistView); 
		    Cursor oldCursor = adapter.swapCursor(newCursor);
		    oldCursor.close(); 
		}
	}


	/**
	 * Adds an onItemClickListener to the items in the listView that will
	 * move to the ViewAlbumsActivity and filter on the selected artist. 
	 */
	private void setSongListItemClickListener() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				TextView textView = (TextView) view.findViewById(R.id.browserText); 
				
				// super janky
				if (view.getHeight() > 200) {
					return; 
				}
				
				// unacceptably janky
				String[] tokens = textView.getText().toString().split(":");
				String title = ""; 
				if (tokens[0].equals("Now playing")) {
					title = tokens[2].substring(1); 
				} else if (tokens.length > 1){
					title = tokens[1].substring(1); 
				} else {
					if (Character.isDigit(tokens[0].charAt(0)) && tokens[0].indexOf(".") != -1 && tokens[0].indexOf(".") < 3) {
						title = tokens[0].substring(tokens[0].indexOf(" ") + 1);
					} else {
						title = tokens[0]; 
					}
				}
				
				// IN THE LONG TERM
				// WE NEED TO BE USING GETSONGBYID
				// OR GETSONGBY UNIQUE HASH
				// AND NOT ASSUMING THAT THE SONG TITLE DOES NOT CONTAIN A COLON
				final Song song = g.db.getSongByTitle(title); 
				song.setAddedBy(g.getUsername());
				
				String songJamId = ""; 
				JSONObject jsonJam = new JSONObject(); 
				g.jamLock.lock(); 
				try {
					songJamId = 
				    g.jam.addSong(song); 
					
					g.sendUIMessage(0);
					
					if (g.jam.checkMaster()) {
						jsonJam = g.jam.toJSON(); 
												
						Toast.makeText(g,
								song.getArtist()
								+ ": " + song.getTitle()
								+ " added to Jam", Toast.LENGTH_SHORT).show(); 
						
						if (!g.jam.hasCurrentSong()) {
							g.jam.setCurrentSong(songJamId);
							g.jam.playCurrentSong();
						}
					} 
				} finally {
					g.jamLock.unlock(); 
				}
				
				if (g.jam.checkMaster()) {
					g.jam.broadcastJamUpdate(jsonJam); 
				} else {
					g.jam.requestAddSong(Integer.toString(song.hashCode()), song.getTitle(), g.getUsername(), songJamId); 
				}
			}
		});
	}
}