package com.stanford.tutti;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ViewAlbumsActivity extends Activity {
	private ListView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_albums);
		listView = (ListView) findViewById(R.id.listView5);
		initializeAlbumList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_jam, menu);
		return true;
	}

	private void initializeAlbumList() {
		Globals g = (Globals) getApplication();  
		
		String artist; 
		Bundle b = getIntent().getExtras();
		if (b != null && b.containsKey("artist")) {
			artist = b.getString("artist");
		} else {
			artist = ""; 
		}
		
		Cursor cursor = g.db.getAlbumsByArtist(artist); 
		
		String[] columns = new String[] { "album" };
	    int[] to = new int[] { android.R.id.text1 };

	    SimpleCursorAdapter mAdapter = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1, cursor, columns, to, 0);
	    listView.setAdapter(mAdapter);
		
		setAlbumListItemClickListener();
	}
	
	/*
	 * Adds an onItemClickListener to the items in the listView that will
	 * move to the ViewSongsActivity and filter on the selected album. 
	 */
	
	private void setAlbumListItemClickListener() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				
				String album = ((TextView)view).getText().toString();
				
		    	Intent intent = new Intent(ViewAlbumsActivity.this, ViewSongsActivity.class);
				Bundle b = new Bundle();
				b.putString("album", album); //Your id
				intent.putExtras(b);
				startActivity(intent);
			}
		});
	}
	
	
	public void viewJam(View view) {
		Intent intent = new Intent(this, ViewJamActivity.class);
		Bundle bundle = new Bundle();    	
		intent.putExtras(bundle);
		startActivity(intent);
	}
}