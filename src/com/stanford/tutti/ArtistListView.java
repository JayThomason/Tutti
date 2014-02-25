package com.stanford.tutti;

import java.util.ArrayList;
import java.util.HashMap;

import com.example.myfirstapp.R;

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
        
        ArrayList<String> artists = new ArrayList<String>();

        Cursor cursor = getContentResolver().query(
        	    MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, 
        	    null, 
        	    null, 
        	    null, 
        	    MediaStore.Audio.Artists.ARTIST + " ASC");
        
        while (cursor.moveToNext()) {
            String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            artists.add(0, artist);
        }
                
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.artist_list);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
          android.R.layout.simple_list_item_1, android.R.id.text1, artists);


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
            		  intent.putExtra(getString(R.string.artist), artist);
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