package com.example.myfirstapp;

import java.util.ArrayList;

import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;


public class ArtistAlbumListView extends Activity {
    ListView listView;
    
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_view_android_example);
        
        ArrayList<String> artists = new ArrayList<String>();

        Cursor cursor = getContentResolver().query(
        	    MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, 
        	    null, 
        	    null, 
        	    null, 
        	    MediaStore.Audio.Artists.ARTIST + " ASC");
        
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            artists.add(0, title);
        }
                
        // Get ListView object from xml
        listView = (ListView) findViewById(R.id.list);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
          android.R.layout.simple_list_item_1, android.R.id.text1, artists);


        // Assign adapter to ListView
        listView.setAdapter(adapter); 
        
        // ListView Item Click Listener
        listView.setOnItemClickListener(new OnItemClickListener() {
              @Override
              public void onItemClick(AdapterView<?> parent, View view,
            		  int position, long id) {
            	  // ListView Clicked item index
            	  int itemPosition     = position;         
            	  // ListView Clicked item value
            	  String  itemValue    = (String) listView.getItemAtPosition(position);     
            	  startActivity(new Intent(this, ArtistAlbumSongView.class));
              }

        }); 
    }

}