package com.stanford.tutti;

import java.util.ArrayList;

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

public class ArtistAlbumListView extends Activity {
    ListView listView;
    
    private ArrayList<String> readAlbumsForArtistFromSDCard(String artist) {
    	ArrayList<String> albums = new ArrayList<String>();
    	
    	final String where = MediaStore.Audio.AlbumColumns.ARTIST
        		+ "='" 
        		+ artist
        		+ "'";
        
        Cursor cursor = getContentResolver().query(
        	    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, 
        	    null, 
        	    where, 
        	    null, 
        	    MediaStore.Audio.Albums.ALBUM + " ASC");
        
        while (cursor.moveToNext()) {
            String album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
            
            albums.add(0, album);
        }
        return albums;
	}
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.artist_album_list_view);
        
        String artist = getIntent().getStringExtra(getString(R.string.artist));
        Globals g = (Globals) getApplication();
        ArrayList<String> albums = g.getAlbumsForArtist(artist);
        if (albums == null || albums.size() == 0) {
        	albums = readAlbumsForArtistFromSDCard(artist);
        	g.setAlbumsForArtist(artist, albums);
        }
        
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.artist_album_list);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
          android.R.layout.simple_list_item_1, android.R.id.text1, albums);


        // Assign adapter to ListView
        listView.setAdapter(adapter); 
        
        // ListView Item Click Listener
        listView.setOnItemClickListener(new OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view,
            		  int position, long id) {
            	  String  album = (String) listView.getItemAtPosition(position); 
        		  Intent intent = new Intent(getApplicationContext(), ArtistAlbumSongListView.class);
        		  intent.putExtra(getString(R.string.album), album);
            	  startActivity(intent);
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