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
	
	private void configureStartButton() {
		Button startButton = (Button) this.findViewById(R.id.song_media_player_start_btn);
		startButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mediaPlayer.start();
			}
		});
	}
	
	private void configurePauseButton() {
		Button pauseButton = (Button) this.findViewById(R.id.song_media_player_pause_btn);
		pauseButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mediaPlayer.pause();
			}
		});
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.song_media_player);
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
	
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {       
    	mediaPlayer.stop(); // stop playing song when back button pressed
        onBackPressed(); 
        return true;
    }
}
