package com.stanford.tutti; 

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
import android.widget.AdapterView.OnItemClickListener;
 
public class BrowseArtistsFragment extends Fragment {
	
	private Cursor cursor = null; 
	private Globals g; 
	private View rootView; 
	private ListView listView; 
	private ViewPager viewPager; 
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        rootView = inflater.inflate(R.layout.fragment_browse_artists, container, false);
        listView = (ListView) rootView.findViewById(R.id.artistListView);
        
        g = (Globals) rootView.getContext().getApplicationContext(); 
         
        viewPager = (ViewPager) container.findViewById(R.id.pager);
        
        initializeArtistList(); 
        initializeSearchBar(); 
        setupHandler(); 
        
        return rootView;
    }
    

	private void initializeArtistList() {		
	    if (cursor != null) 
	    	cursor.close(); 
		cursor = g.db.getAllArtists(); 
		
		String[] columns = new String[] { "art", "artist" };
	    int[] to = new int[] { R.id.browserArt, R.id.browserText };

	    
	    //SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, columns, to, 0);
	    MusicBrowserAdapter adapter = new MusicBrowserAdapter(g, R.layout.list_layout, cursor, columns, to);
	    listView.setAdapter(adapter);
	    listView.setFastScrollEnabled(true);
	    listView.setTextFilterEnabled(true);

	    adapter.setFilterQueryProvider(new FilterQueryProvider() {
	        public Cursor runQuery(CharSequence constraint) {
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
				
				TextView textView = (TextView) view.findViewById(R.id.browserText); 
				String artist = textView.getText().toString();
				
				g.currentArtistView = artist; 
				g.currentAlbumView = ""; 
				
				if (g.albumUpdateHandler != null) {
					Message msg = g.albumUpdateHandler.obtainMessage();
					msg.what = 0; // fix this later to be constant
					g.albumUpdateHandler.sendMessage(msg);
				}				
				
		        //viewPager.setCurrentItem(1);
			}
		});
	}
	
	private void initializeSearchBar() {
	    EditText searchBox = (EditText) rootView.findViewById(R.id.artist_search_box);
	    searchBox.addTextChangedListener(new TextWatcher() {
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
	
	/*
	 * Initializes the handler. The handler is used to receive messages from
	 * the server and to update the UI accordingly.
	 */
	private void setupHandler() {
		g.artistUpdateHandler = new Handler() {
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
}