package com.stanford.tutti;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class ViewJamActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_jam);
	//	jam = (Jam)getIntent().getExtras().getSerializable("jam");
		
		ListView listView = (ListView) findViewById(R.id.listView3);
		ArrayList<String> songList = MainActivity.jam.getSongList();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songList);
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			  @Override
			  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				  String item = ((TextView)view).getText().toString();
				  new AlertDialog.Builder(view.getContext())
		            .setMessage("Now playing "+item).show();
				  MainActivity.jam.addSong(item);
			  }
			});
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_jam, menu);
		return true;
	}

	public void popupJoinRequest(View view) {
		new AlertDialog.Builder(view.getContext()).setMessage("Harrison wants to join your jam")
		.setNegativeButton("Decline", null)
	  	.setPositiveButton("Accept", null).show();
	}
	
	public void addSongs(View view) {
		Intent intent = new Intent(this, NewJamActivity.class);
		startActivity(intent);
	}
}