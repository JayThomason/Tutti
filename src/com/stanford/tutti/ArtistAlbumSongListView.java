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
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.artist_album_list_view);
                
        Globals g = (Globals) getApplication();
        Album album = g.getCurrentAlbum();
        ArrayList<Song> songList = album.getSongList();
        ArrayList<String> songTitles = new ArrayList<String>();
        for (int i = 0; i < songList.size(); ++i) {
        	songTitles.add(songList.get(i).getTitle());
        }
                
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.artist_album_list);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
          android.R.layout.simple_list_item_1, android.R.id.text1, songTitles);


        // Assign adapter to ListView
        listView.setAdapter(adapter); 
        
        // ListView Item Click Listener
        listView.setOnItemClickListener(new OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view,
            		  int position, long id) {
            	  Globals g = (Globals) getApplication();
                  g.jam.setCurrentSong(g.getCurrentAlbum().getSongList().get(position));
                  Intent intent = new Intent(getApplicationContext(), SongMediaPlayer.class);
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