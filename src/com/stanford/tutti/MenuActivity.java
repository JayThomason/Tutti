package com.stanford.tutti;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;


public class MenuActivity extends Activity {
	private String[] menuItems = { null, null };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		menuItems[0] = getString(R.string.browse_artists);
		menuItems[1] = getString(R.string.add_fake_music);
		setContentView(R.layout.activity_menu);
		final ListView listView = (ListView) findViewById(R.id.menu_list);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
		          android.R.layout.simple_list_item_1, android.R.id.text1, menuItems);
        listView.setAdapter(adapter);
        
        listView.setOnItemClickListener(new OnItemClickListener() {
        	@Override
            public void onItemClick(AdapterView<?> parent, View view,
            		int position, long id) {
        		String item = (String) listView.getItemAtPosition(position);
        		if (item.equals(menuItems[0])) {
        			Intent intent = new Intent(getApplicationContext(), ArtistListView.class);
	        		startActivity(intent);
        		}
        		else if (item.equals(menuItems[1])) {
        			UserJoiningRoomSimulator sim = new UserJoiningRoomSimulator();
        			sim.addUserMusic((Globals) getApplication());
        		}        			
        	}
        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
