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

public class ViewArtistsActivity extends Activity {
	private ListView all; 
	private ListView listView;
	private EditText searchBox; 
	private Cursor cursor; 

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_artists);
		all = (ListView) findViewById(R.id.allArtists);
		initializeAllButton(); 
		listView = (ListView) findViewById(R.id.artistListView);
		initializeArtistList();
		setUpHandler(); 
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_jam, menu);
		return true;
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
				g.currentArtistView = ""; 
		    	Intent intent = new Intent(ViewArtistsActivity.this, ViewAlbumsActivity.class);
				startActivity(intent);
			}
		});
	}


	private void initializeArtistList() {		
	    Globals g = (Globals) getApplicationContext(); 
	    if (cursor != null) 
	    	cursor.close(); 
		cursor = g.db.getAllArtists(); 
		
		String[] columns = new String[] { "artist" };
	    int[] to = new int[] { android.R.id.text1 };

	    SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, columns, to, 0);
	    listView.setAdapter(adapter);
	    listView.setFastScrollEnabled(true);
	    listView.setTextFilterEnabled(true);
	    
	    searchBox = (EditText)findViewById(R.id.artist_search_box);
	    searchBox.addTextChangedListener(new TextWatcher() {
	        public void onTextChanged(CharSequence s, int start, int before, int count) {
	        }

	        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
	        }

	        public void afterTextChanged(Editable s) {
	            ListView lv = (ListView)findViewById(R.id.artistListView);
	            SimpleCursorAdapter filterAdapter = (SimpleCursorAdapter)lv.getAdapter();
	            filterAdapter.getFilter().filter(s.toString());
	        }
	    });
	    

	    adapter.setFilterQueryProvider(new FilterQueryProvider() {
	        public Cursor runQuery(CharSequence constraint) {
	        	Globals g = (Globals) getApplication(); 
	            return g.db.searchArtists(constraint);
	        }
	    });
		
		setArtistListItemClickListener();
	}
	
	/*
	 * Adds an onItemClickListener to the items in the listView that will
	 * move to the ViewAlbumsActivity and filter on the selected artist. 
	 */
	private void setArtistListItemClickListener() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				String artist = ((TextView)view).getText().toString();
				Globals g = (Globals) getApplication(); 
				g.currentArtistView = artist; 
				
		    	Intent intent = new Intent(ViewArtistsActivity.this, ViewAlbumsActivity.class);
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
					initializeArtistList(); 
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