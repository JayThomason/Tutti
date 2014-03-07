package com.stanford.tutti;

import android.app.ActionBar;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;


public class SongMediaPlayer extends Activity {
	MediaPlayer mediaPlayer = new MediaPlayer();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.song_media_player);
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
    	mediaPlayer.stop();
        onBackPressed(); 
        return true;
    }
}
