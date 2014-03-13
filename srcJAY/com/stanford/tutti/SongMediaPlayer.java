package com.stanford.tutti;

import android.app.ActionBar;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

/*
 * class SongMediaPlayer
 * 
 * SongMediaPlayer is a simple activity which plays the current song selected
 * and stored in Globals using a MediaPlayer. 
 * 
 * Note: right now, nothing special is being done when the song is not 'real,'
 * aka not actually stored locally on the phone. This means that the media player
 * will log errors when attempting to play and pause these non-existent songs.
 */
public class SongMediaPlayer extends Activity {
	MediaPlayer mediaPlayer = new MediaPlayer();
	Button startButton;
	Button pauseButton;


	
	/*
	 * Sets the OnClickListener for the play button.
	 */
	private void configureStartButton() {
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mediaPlayer.start();
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
				mediaPlayer.pause();
			}
		});
	}

	/*
	 * Initializes the media player by setting up the action bar, configuring
	 * the start and pause buttons, and then loading and playing the current
	 * song.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.song_media_player);
		startButton = (Button) this.findViewById(R.id.song_media_player_start_btn);
		pauseButton = (Button) this.findViewById(R.id.song_media_player_pause_btn);
		configureStartButton();
		configurePauseButton();
		Globals g = (Globals) getApplication();
		Song selectedSong = g.getCurrentSong();
		try {
			Uri myUri = Uri.parse(g.getCurrentSong().getPath());
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setDataSource(getApplicationContext(), myUri);
			mediaPlayer.prepare();
			mediaPlayer.start();
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}
	
	/*
	 * Sets up the back button and ensures that the media player stops
	 * playing when the back button is pressed.
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {       
    	mediaPlayer.stop(); // stop playing song when back button pressed
        onBackPressed(); 
        return true;
    }
}
