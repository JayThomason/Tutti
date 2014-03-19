package com.stanford.tutti;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ViewJamActivity extends Activity {

	ImageButton startButton;
	ImageButton pauseButton;
	ImageButton backButton;
	ImageButton nextButton;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_jam);
		
		// setupActionBar();
		
	//	jam = (Jam)getIntent().getExtras().getSerializable("jam");
		backButton = (ImageButton) this.findViewById(R.id.song_media_player_back_btn);
		startButton =  (ImageButton)this.findViewById(R.id.song_media_player_start_btn);
		pauseButton = (ImageButton) this.findViewById(R.id.song_media_player_pause_btn);
		
		nextButton = (ImageButton) this.findViewById(R.id.song_media_player_next_btn);
		configureBackButton();
		configureStartButton(); 
		configurePauseButton(); 
		//configureNextButton();
				
		ListView listView = (ListView) findViewById(R.id.listView3);
		
        Globals g = (Globals) getApplication();  
		ArrayList<Song> songList = g.jam.getSongList(); 
		
		// Eventually want to abstract this so the Jam is maintaining its own string list
		ArrayList<String> songStringList = new ArrayList<String>(); 
		for (int i = 0; i < songList.size(); i++) {
			songStringList.add(songList.get(i).getArtist().getName() + ": " + songList.get(i).getTitle()); 
		}
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, songStringList);
		listView.setAdapter(adapter);
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			  @Override
			  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				  String item = ((TextView)view).getText().toString();
				  
	              Toast.makeText(
	                        getApplicationContext(),
	                        "Now playing: " + item, Toast.LENGTH_SHORT)
	                        .show();
				  
	              Globals g = (Globals) getApplication();  
	              g.jam.setCurrentSongByIndex(position);
	              g.jam.playCurrentSong(); 
	              //setCurrentSongIndex? 
	                
				  // MainActivity.jam.addSong(item);
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
	
	
	/*
	 * Sets the OnClickListener for the play button.
	 */
	private void configureStartButton() {
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                Globals g = (Globals) getApplication();
				g.jam.start();
			}
		});
	}
	
	
	/*
	 * Sets the OnClickListener for the pause button.
	 */
	private void configurePauseButton() {
		pauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
                Globals g = (Globals) getApplication();
				g.jam.pause();
			}
		});
	}


	private void configureBackButton() {
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
	            Globals g = (Globals) getApplication();
				g.jam.seekTo(0);
				
			}
		});
	}
	

	/*private void configureNextButton() {
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
	            Globals g = (Globals) getApplication();
	            g.mediaPlayer.seekTo(g.mediaPlayer.getDuration());
	            
				
			}
		});
	}*/

}