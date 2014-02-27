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
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;


public class ArtistAlbumSongListView extends Activity {
    ListView listView;
    
    private ArrayList<String> readSongsForAlbumFromSDCard(String album) {
    	ArrayList<String> songs = new ArrayList<String>();
    	
    	final String where = MediaStore.Audio.Media.ALBUM
        		+ "='" 
        		+ album
        		+ "'";
        
        Cursor cursor = getContentResolver().query(
        	    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
        	    null, 
        	    where, 
        	    null, 
        	    MediaStore.Audio.Albums.ALBUM + " ASC");
        
        while (cursor.moveToNext()) {
            String song = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
            songs.add(0, song);
        }
        return songs;  
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.artist_album_list_view);
        
        String album = getIntent().getStringExtra(getString(R.string.album));
        Globals g = (Globals) getApplication();
        ArrayList<String> songs = g.getSongsForAlbum(album);
        if (songs == null || songs.size() == 0) {
        	songs = readSongsForAlbumFromSDCard(album);
        	g.setSongsForAlbum(album, songs);
        }      
                
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.artist_album_list);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
          android.R.layout.simple_list_item_1, android.R.id.text1, songs);


        // Assign adapter to ListView
        listView.setAdapter(adapter); 
        
        // ListView Item Click Listener
        listView.setOnItemClickListener(new OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view,
            		  int position, long id) {
            	  String song = (String) listView.getItemAtPosition(position); 
            	  System.out.println(song);
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