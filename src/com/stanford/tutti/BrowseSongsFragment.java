package com.stanford.tutti; 

import java.util.Set;

import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseSongsFragment extends Fragment {

	private Globals g; 
	private View rootView; 
	public ListView listView; 
	
	private String columns[]; 
	private int views[]; 
	public BrowseMusicAdapter adapter; 
	
	private final int port = 1234;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.fragment_browse_songs, container, false);
		listView = (ListView) rootView.findViewById(R.id.songListView); 

		g = (Globals) rootView.getContext().getApplicationContext(); 
		
		initializeSongList(); 

		return rootView;
	}

	public void initializeSongList() {
		Cursor cursor = g.db.getAllSongs(); 
		
		columns = new String[] { "art", "artist", "title" };
		views = new int[] { R.id.browserArt, R.id.browserText };
		adapter = new BrowseMusicAdapter(g, R.layout.list_layout, cursor, columns, views); 
		
		
		//adapter.setFilterQueryProvider(searchFilter);
		
		
		listView.setAdapter(adapter);
		
		listView.setFastScrollEnabled(true);
		listView.setTextFilterEnabled(true);
		
		setSongListItemClickListener();
	}
	
	
	public void refreshSongList() {
		Cursor cursor; 
		
		if (!g.currentArtistView.equals("")) {
			cursor = g.db.getSongsByArtist(g.currentArtistView); 
		} else {
			cursor = g.db.getAllSongs(); 
		}

		Cursor oldCursor = adapter.swapCursor(cursor); 
		oldCursor.close(); 
	}
	
	
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


	/*
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
				
				String[] tokens = textView.getText().toString().split(":");
				String title = ""; 
				if (tokens[0].equals("Now playing")) {
					title = tokens[2].substring(1); 
				} else if (tokens.length > 1){
					title = tokens[1].substring(1); 
				} else {
					if (Character.isDigit(tokens[0].charAt(0))) {
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
               
				Toast.makeText(g,
						song.getArtist()
						+ ": " + song.getTitle()
						+ " added to Jam", Toast.LENGTH_SHORT).show(); 
				
				if (g.jam.checkMaster()) {
					song.setAddedBy(g.getUsername());
					g.jam.addSong(song); 
					if (g.jam.getCurrentSong() == null) {
						g.jam.setCurrentSong(g.jam.getJamSize() - 1);
						g.jam.playCurrentSong();
					}
					// will fix to a higher-level abstraction, ie. sendMessageToAllClients(ip, port, path, etc.)
					Set<Client> clientSet = g.jam.getClientSet();
					for (Client client : clientSet) {
						client.requestAddSong(Integer.toString(song.hashCode()), g.getUsername(), new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
								System.out.println("request to add song to client returned: " + statusCode);
							}
						});
					}
				}
				else {
					Client masterClient = new Client(g, g.jam.getIPUsername(g.jam.getMasterIpAddr()), g.jam.getMasterIpAddr(), port); 
					masterClient.requestAddSong(Integer.toString(song.hashCode()), g.getUsername(), new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
							System.out.println("request to add song to master returned: " + statusCode);
						}
					});
				}

				g.sendUIMessage(0); 
			}
		});
	}
}