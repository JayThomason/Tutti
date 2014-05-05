package com.stanford.tutti; 

import java.util.Set;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import org.apache.http.Header;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
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

	private Cursor cursor = null; 
	private Globals g; 
	private View rootView; 
	private ViewPager viewPager; 
	private ListView listView; 
	private EditText searchBar; 
	
	private String columns[]; 
	private int views[]; 
	
	private FilterQueryProvider searchFilter;

	private final int port = 1234;


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		rootView = inflater.inflate(R.layout.fragment_browse_songs, container, false);
		
		listView = (ListView) rootView.findViewById(R.id.songListView); 
		listView.setFastScrollEnabled(true);
		listView.setTextFilterEnabled(true);
		
		searchBar = (EditText) rootView.findViewById(R.id.song_search_box);


		viewPager = (ViewPager) container.findViewById(R.id.pager);

		g = (Globals) rootView.getContext().getApplicationContext(); 
		
		columns = new String[] { "art", "title" };
		views = new int[] { R.id.browserArt, R.id.browserText };

		initializeQueryFilter(); 
		initializeSongList(); 
		initializeSearchBar(); 

		return rootView;
	}
	
	public void initializeQueryFilter() {
		searchFilter = new FilterQueryProvider() {
			public Cursor runQuery(CharSequence constraint) {
				if (!g.currentAlbumView.equals("") && !g.currentArtistView.equals("")) {
					return g.db.searchSongsByArtistAndAlbum(constraint, g.currentArtistView, g.currentAlbumView); 
				} else if (!g.currentArtistView.equals("")) {
					return g.db.searchSongsByArtist(constraint, g.currentArtistView); 
				} else {
					return g.db.searchSongs(constraint); 
				} 
			}
		}; 
	}

	public void initializeSongList() {
		searchBar.setText("");
		
		if (cursor != null) 
			cursor.close(); 

		if (!g.currentAlbumView.equals("") && !g.currentAlbumView.equals("Music") && !g.currentArtistView.equals("")) {
			cursor = g.db.getSongsByArtistAndAlbum(g.currentArtistView, g.currentAlbumView); 
		} else if (!g.currentAlbumView.equals("") && !g.currentAlbumView.equals("Music")) {
			cursor = g.db.getSongsByAlbum(g.currentAlbumView); 
		} else if (!g.currentArtistView.equals("")) {
			cursor = g.db.getSongsByArtist(g.currentArtistView); 
		} else {
			cursor = g.db.getAllSongs(); 
		}

		BrowseMusicAdapter adapter = new BrowseMusicAdapter(g, R.layout.list_layout, cursor, columns, views); 
		adapter.setFilterQueryProvider(searchFilter);
		listView.setAdapter(adapter);

		setSongListItemClickListener();
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
				String title = textView.getText().toString();
				
				// IN THE LONG TERM
				// WE NEED TO BE USING GETSONGBYID
				// OR GETSONGBY UNIQUE HASH
				final Song song = g.db.getSongByTitle(title); 
               
				if (g.jam.checkMaster()) {
					song.setAddedBy(g.getUsername());
					g.jam.addSong(song); 
					Toast.makeText(g,
							song.getArtist()
							+ " : " + song.getTitle()
							+ " added to Jam", Toast.LENGTH_SHORT).show(); 
					if (g.jam.getCurrentSong() == null) {
						g.jam.setCurrentSong(song, g.jam.getJamSize() - 1);
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
					// will want to refactor this to use the Client or a similar Master class
					Client masterClient = new Client(g, g.jam.getIPUsername(g.jam.getMasterIpAddr()), g.jam.getMasterIpAddr(), port); 
					masterClient.requestAddSong(Integer.toString(song.hashCode()), g.getUsername(), new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
							System.out.println("request to add song to master returned: " + statusCode);

							if (statusCode == 200) {
								Toast.makeText(g,
										song.getArtist()
										+ " : " + song.getTitle()
										+ " added to Jam", Toast.LENGTH_SHORT).show(); 
							}
						}
					});
				}

				if (g.uiUpdateHandler != null) {
					Message msg = g.uiUpdateHandler.obtainMessage();
					msg.what = 7; 
					g.uiUpdateHandler.sendMessage(msg);
				}
			}
		});
	}

	private void initializeSearchBar() {
		searchBar.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before, int count) {
			}

			public void beforeTextChanged(CharSequence s, int start, int count, int after) {
			}

			public void afterTextChanged(Editable s) {
				SimpleCursorAdapter filterAdapter = (SimpleCursorAdapter)listView.getAdapter();
				filterAdapter.getFilter().filter(s.toString());
			}
		});
	}
}