package com.stanford.tutti; 

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

/**
 * Fragment that allows the user to scroll through a list on screen that includes
 * every musical artist in the library. Clicking on an artist will switch to the 
 * BrowseSongsFragment.
 */
public class BrowseArtistsFragment extends Fragment {
	
	private Globals g; 
	private View rootView; 
	private String columns[]; 
	private int views[]; 
	private BrowseMusicAdapter adapter; 
	
	public ListView listView; 
		
	/**
	 * (non-Javadoc)
	 * @see android.support.v4.app.Fragment#onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)
	 */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
     	
        rootView = inflater.inflate(R.layout.fragment_browse_artists, container, false);
		listView = (ListView) rootView.findViewById(R.id.artistListView);

        g = (Globals) rootView.getContext().getApplicationContext(); 

        initializeArtistList(); 
        
        return rootView;
    }
    

    /**
     * Initializes the list of artists to be displayed.
     */
	public void initializeArtistList() {		
		columns = new String[] { "art", "artist" };
	    views = new int[] { R.id.browserArt, R.id.browserText };
	    
		Cursor cursor = g.db.getAllArtists(); 
	    adapter = new BrowseMusicAdapter(g, R.layout.list_layout, cursor, columns, views);
	    
	    
	    //adapter.setFilterQueryProvider(g.searchFilterProvider);
	    
	    
	    listView.setAdapter(adapter); 
	    
	    listView.setFastScrollEnabled(true);
	    listView.setTextFilterEnabled(true);	
	    
	    setArtistListItemClickListener();
	}  
	
	/**
	 * Refreshes the list of artists being displayed.
	 */
	public void refreshArtistList() {
		Cursor newCursor = g.db.getAllArtists(); 
	    Cursor oldCursor = adapter.swapCursor(newCursor);
	    oldCursor.close(); 
	}
	
	/**
	 * Searches the list of artists for a specific artist given a query string
	 * and updates the UI to display artists whose names contain the provided
	 * query.
	 * 
	 * @param query String used to search for artist by name
	 */
	public void searchArtistList(String query) {
		Cursor newCursor = g.db.searchArtists(query); 
	    Cursor oldCursor = adapter.swapCursor(newCursor);
	    oldCursor.close(); 
	}
	
	
	/**
	 * Adds an onItemClickListener to the items in the listView that will
	 * move to the ViewAlbumsActivity and filter on the selected artist. 
	 */
	private void setArtistListItemClickListener() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				
				TextView textView = (TextView) 
						view.findViewById(R.id.browserText); 
				String artist = textView.getText().toString();
								
				if (artist.equals("Unknown Artist")) {
					artist = "<unknown>"; 
				}
				
				g.currentArtistView = artist; 
				
				g.sendUIMessage(3); 
			}
		});
	}
}