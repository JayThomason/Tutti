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

public class ViewAlbumsActivity extends Activity {
	private ListView all; 
	private ListView listView;
	private Cursor cursor; 
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_albums);
		all = (ListView) findViewById(R.id.allAlbums);
		initializeAllButton(); 
		listView = (ListView) findViewById(R.id.albumListView);
		initializeAlbumList();
		setUpHandler(); 
	}
	
	private void initializeAllButton() {
		ArrayList<String> stringList = new ArrayList<String>(); 
		stringList.add("All"); 
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, stringList);
		all.setAdapter(adapter);
		
		setAllClickListener(); 
	}
	
	private void setAllClickListener() {
		all.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				Globals g = (Globals) getApplication(); 
				g.currentAlbumView = ""; 
		    	Intent intent = new Intent(ViewAlbumsActivity.this, ViewSongsActivity.class);
				startActivity(intent);
			}
		});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_jam, menu);
		return true;
	}

	private void initializeAlbumList() {
		Globals g = (Globals) getApplication();  
		
	    if (cursor != null) 
	    	cursor.close(); 
	    
		if (g.currentArtistView != "") {
			cursor = g.db.getAlbumsByArtist(g.currentArtistView); 
		} else {
			cursor = g.db.getAllAlbums(); 
		}
				
		String[] columns = new String[] { "album" };
	    int[] to = new int[] { android.R.id.text1 };

	    SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, columns, to, 0);
	    listView.setAdapter(adapter);
	    listView.setFastScrollEnabled(true);
	    listView.setTextFilterEnabled(true);
	    
	    EditText etext = (EditText)findViewById(R.id.album_search_box);
	    etext.addTextChangedListener(new TextWatcher() {
	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        }

	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        }

	        public void afterTextChanged(Editable s) {
	            ListView lv = (ListView)findViewById(R.id.albumListView);
	            SimpleCursorAdapter filterAdapter = (SimpleCursorAdapter)lv.getAdapter();
	            filterAdapter.getFilter().filter(s.toString());
	        }
	    });

	    adapter.setFilterQueryProvider(new FilterQueryProvider() {
	        public Cursor runQuery(CharSequence constraint) {
	        	Globals g = (Globals) getApplication(); 
	            return g.db.searchAlbums(constraint);
	        }
	    });
		
		setAlbumListItemClickListener();
	}
	
	/*
	 * Adds an onItemClickListener to the items in the listView that will
	 * move to the ViewSongsActivity and filter on the selected album. 
	 */
	
	private void setAlbumListItemClickListener() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				
				String album = ((TextView)view).getText().toString();
				Globals g = (Globals) getApplication(); 
				g.currentAlbumView = album; 
				
		    	Intent intent = new Intent(ViewAlbumsActivity.this, ViewSongsActivity.class);
				startActivity(intent);
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
					initializeAlbumList(); 
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