package com.stanford.tutti;

import java.util.ArrayList;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ViewSongsActivity extends Activity {
	private ListView listView;
	private Cursor cursor; 
	private final int port = 1234;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_songs);
		listView = (ListView) findViewById(R.id.songListView);
		initializeSongList();
		setUpHandler(); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_jam, menu);
		return true;
	}

	private void initializeSongList() {
		Globals g = (Globals) getApplication();  
		
	    if (cursor != null) 
	    	cursor.close(); 
	    
		if (g.currentAlbumView != "") {
			cursor = g.db.getSongsByAlbum(g.currentAlbumView); 
		} else if (g.currentArtistView != "") {
			cursor = g.db.getSongsByArtist(g.currentArtistView); 
		} else {
			cursor = g.db.getAllSongs(); 
		}
				
		String[] columns = new String[] { "title" };
	    int[] to = new int[] { android.R.id.text1 };

	    SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, columns, to, 0);
	    listView.setAdapter(adapter);
	    listView.setFastScrollEnabled(true);
	    listView.setTextFilterEnabled(true);
	    
	    EditText etext = (EditText)findViewById(R.id.song_search_box);
	    etext.addTextChangedListener(new TextWatcher() {
	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        }

	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        }

	        public void afterTextChanged(Editable s) {
	            ListView lv = (ListView)findViewById(R.id.songListView);
	            SimpleCursorAdapter filterAdapter = (SimpleCursorAdapter)lv.getAdapter();
	            filterAdapter.getFilter().filter(s.toString());
	        }
	    });

	    adapter.setFilterQueryProvider(new FilterQueryProvider() {
	        public Cursor runQuery(CharSequence constraint) {
	        	Globals g = (Globals) getApplication(); 
	            return g.db.searchSongs(constraint);
	        }
	    });
		
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
				String title = ((TextView)view).getText().toString();
				Globals g = (Globals) getApplication(); 
				
				// IN THE LONG TERM
				// WE NEED TO BE USING GETSONGBYID
				Song song = g.db.getSongByTitle(title); 
				
				g.jam.addSong(song); 
				Toast.makeText(getApplicationContext(),
						song.getArtist()
						+ " : " + song.getTitle()
						+ " added to Jam", Toast.LENGTH_SHORT).show();                
				if (g.master) {
					if (g.jam.getCurrentSong() == null) {
						g.jam.setCurrentSong(song);
						g.jam.playCurrentSong();
					}          
				} 
				new PassMessageThread(g.jam.getOtherIP(), port,
						"/jam/add/", Integer.toString(song.hashCode())).start(); 
			}
		});
	}
	
	/*
	 * Initializes the handler. The handler is used to receive messages from
	 * the server and to update the UI accordingly.
	 */
	private void setUpHandler() {
		Globals g = (Globals) getApplicationContext(); 
		g.uiUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				/*
				 * When we get a message from another phone that we have new
				 * non-local music, we can update the list-view for the library.
				 */
				if (msg.what == 0) {
					initializeSongList(); 
				}
				super.handleMessage(msg);
			}
		};		
	}
	
	
	public void viewJam(View view) {
		Intent intent = new Intent(this, ViewJamActivity.class);
		Bundle bundle = new Bundle();    	
		intent.putExtras(bundle);
		startActivity(intent);
	}
}