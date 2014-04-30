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
	private EditText searchBar;
	
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
     	
        rootView = inflater.inflate(R.layout.fragment_browse_artists, container, false);
        listView = (ListView) rootView.findViewById(R.id.artistListView);
	    searchBar = (EditText) rootView.findViewById(R.id.artist_search_box);

        
        g = (Globals) rootView.getContext().getApplicationContext(); 
         
        viewPager = (ViewPager) container.findViewById(R.id.pager);
        
        initializeArtistList(); 
        initializeSearchBar(); 
        
        return rootView;
    }
    

	public void initializeArtistList() {		
	    if (cursor != null) 
	    	cursor.close(); 
		cursor = g.db.getAllArtists(); 
		
		String[] columns = new String[] { "art", "artist" };
	    int[] to = new int[] { R.id.browserArt, R.id.browserText };

	    
	    //SimpleCursorAdapter adapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, columns, to, 0);
	    BrowseMusicAdapter adapter = new BrowseMusicAdapter(g, R.layout.list_layout, cursor, columns, to);
	    listView.setAdapter(adapter);
	    listView.setFastScrollEnabled(true);
	    listView.setTextFilterEnabled(true);

	    adapter.setFilterQueryProvider(new FilterQueryProvider() {
	        public Cursor runQuery(CharSequence constraint) {
	            return g.db.searchArtists(constraint);
	        }
	    });
		
		setArtistListItemClickListener();
		
		searchBar.setText("");
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
				
				if (g.uiUpdateHandler != null) {
					Message msg = g.uiUpdateHandler.obtainMessage();
					msg.what = 2; 
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