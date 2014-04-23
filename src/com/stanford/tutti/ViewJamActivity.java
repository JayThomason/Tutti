package com.stanford.tutti;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
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

/*
 * When a user views the current songs in the jam this activity is used. It
 * displays a list of the songs in the jam and a bottom control menu with 
 * play, pause, prev, and next buttons. When a song in the list is clicked on
 * it begins to play.
 */
public class ViewJamActivity extends Activity {
	private ImageButton startButton;
	private ImageButton pauseButton;
	private ImageButton backButton;
	private ImageButton nextButton;
	private ListView listView;
	private final int port = 1234;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_view_jam);
		assignButtons();
		configureButtons();
		listView = (ListView) findViewById(R.id.listView3);
		initializeArtistSongList();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.view_jam, menu);
		return true;
	}

	/*
	 * Initializes the listView with a list of the current songs in the jam.
	 */
	private void initializeArtistSongList() {
		Globals g = (Globals) getApplication();  
		int jamSize = g.jam.getJamSize();
		
		// Eventually want to abstract this so the Jam is maintaining its own string list
		ArrayList<String> songStringList = new ArrayList<String>(); 
		for (int i = 0; i < jamSize; i++) {
			songStringList.add(g.jam.getSongByIndex(i).getArtist() + 
					": " + g.jam.getSongByIndex(i).getTitle()); 
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, 
				android.R.layout.simple_list_item_1, songStringList);
		listView.setAdapter(adapter);
		setSongListItemClickListener();
	}
	
	/*
	 * Adds an onItemClickListener to the items in the listView that will
	 * play the song which is clicked on.
	 */
	private void setSongListItemClickListener() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				String item = ((TextView)view).getText().toString();
				Toast.makeText(
						getApplicationContext(),
						"Now playing: " + item, Toast.LENGTH_SHORT)
						.show();
				Globals g = (Globals) getApplication();  
				g.jam.setCurrentSongByIndex(position);
				g.jam.playCurrentSong(); 
				Song song = g.jam.getSongByIndex(position); 
				new PassMessageThread(g.jam.getOtherIP(), port,
						"/jam/set/" + Integer.toString(song.hashCode()), "").start(); 
			}
		});
	}
	
	/*
	 * Initializes the play, pause, back, and next buttons on the page.
	 */
	private void assignButtons() {
		backButton = (ImageButton) this.findViewById(R.id.song_media_player_back_btn);
		startButton =  (ImageButton)this.findViewById(R.id.song_media_player_start_btn);
		pauseButton = (ImageButton) this.findViewById(R.id.song_media_player_pause_btn);
		nextButton = (ImageButton) this.findViewById(R.id.song_media_player_next_btn);
	}

	/*
	 * Configures each individual button.
	 */
	private void configureButtons() {
		configureBackButton();
		configureStartButton(); 
		configurePauseButton(); 
		configureNextButton();
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
				(new PassMessageThread(g.jam.getOtherIP(), port, "/jam/start", "")).start(); 
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
				(new PassMessageThread(g.jam.getOtherIP(), port, "/jam/pause", "")).start(); 
			}
		});
	}

	/*
	 * Sets the OnClickListener for the back (prev song) button.
	 */
	private void configureBackButton() {
		backButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Globals g = (Globals) getApplication();
				g.jam.seekTo(0);
				(new PassMessageThread(g.jam.getOtherIP(), port, "/jam/restart", "")).start(); 
			}
		});
	}

	/*
	 * Sets the OnClickListener for the next (next song) button.
	 */
	private void configureNextButton() {
		nextButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
	            Globals g = (Globals) getApplication();
	            g.jam.iterateCurrentSong();
	            g.jam.playCurrentSong();
			}
		});
	}

	/*
	 * This is an onclick being set in the layout XML itself. 
	 * We should configure it here like the other buttons eventually. 
	 */
 	public void addSongs(View view) {
  		finish(); 
  	}
}