package com.stanford.tutti;

import java.util.ArrayList;
import java.util.HashMap;

import android.os.Bundle;
import android.provider.MediaStore;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;


public class ArtistListView extends Activity {
    ListView listView;    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.artist_list_view);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        Globals g = (Globals) getApplication();
        ArrayList<Artist> artists = g.getArtistList();
        ArrayList<String> artistNames = new ArrayList<String>();
        for (int i = 0; i < artists.size(); ++i) {
        	artistNames.add(artists.get(i).getName());
        }
                 
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.artist_list);

        // TODO: implement custom adapter for artists
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
          android.R.layout.simple_list_item_1, android.R.id.text1, artistNames);

        // Assign adapter to ListView
        listView.setAdapter(adapter); 
        
        // ListView Item Click Listener
        listView.setOnItemClickListener(new OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view,
            		  int position, long id) {     
            	  String  artist = (String) listView.getItemAtPosition(position);
            	  if (artist != null) {
            		  Intent intent = new Intent(getApplicationContext(), ArtistAlbumListView.class);
            		  Globals g = (Globals) getApplication();
            		  g.setCurrentArtist(g.getArtistList().get(position));
            		  startActivity(intent); 
            	  }
              }
        }); 
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {       
        onBackPressed(); 
        return true;
    }
}