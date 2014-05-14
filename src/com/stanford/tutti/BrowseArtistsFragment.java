package com.stanford.tutti; 

import android.database.Cursor;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.app.Fragment;
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
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.AsyncTaskLoader;
 
public class BrowseArtistsFragment extends Fragment {
	
	private Globals g; 
	private View rootView; 
	private ListView listView; 
	private EditText searchBar;
	
	private String columns[]; 
	private int views[]; 
	private BrowseMusicAdapter adapter; 
	
	private FilterQueryProvider searchFilter;
	
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
     	
        rootView = inflater.inflate(R.layout.fragment_browse_artists, container, false);
		listView = (ListView) rootView.findViewById(R.id.artistListView);
	    searchBar = (EditText) rootView.findViewById(R.id.artist_search_box);

        g = (Globals) rootView.getContext().getApplicationContext(); 
        
        initializeArtistList(); 
	    initializeQueryFilter(); 
        initializeSearchBar(); 
        
        return rootView;
    }
    

	public void initializeArtistList() {		
		columns = new String[] { "art", "artist" };
	    views = new int[] { R.id.browserArt, R.id.browserText };
	    
		Cursor cursor = g.db.getAllArtists(); 
	    adapter = new BrowseMusicAdapter(g, R.layout.list_layout, cursor, columns, views);
	    adapter.setFilterQueryProvider(searchFilter);
	    listView.setAdapter(adapter); 
	    
	    listView.setFastScrollEnabled(true);
	    listView.setTextFilterEnabled(true);	
	    
	    setArtistListItemClickListener();
	}  
	
	
	public void refreshArtistList() {
		Cursor newCursor = g.db.getAllArtists(); 
	    Cursor oldCursor = adapter.swapCursor(newCursor);
	    oldCursor.close(); 
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
				
				g.sendUIMessage(3); 
			}
		});
	}
	
    public void initializeQueryFilter() {
    	searchFilter = new FilterQueryProvider() {
	        public Cursor runQuery(CharSequence constraint) {
	            return g.db.searchArtists(constraint);
	        }
	    }; 
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