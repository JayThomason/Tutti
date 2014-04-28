package com.stanford.tutti; 
 
import java.util.ArrayList;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
 
public class BrowseJamFragment extends Fragment {
	
	private ImageButton startButton;
	private ImageButton pauseButton;
	private ImageButton backButton;
	private ImageButton nextButton;
	private ListView listView;
	private final int port = 1234;
	
	private View rootView; 
	private Globals g; 
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
 
        rootView = inflater.inflate(R.layout.fragment_browse_jam, container, false);
         
        g = (Globals) rootView.getContext().getApplicationContext(); 
        
        assignButtons();
		configureButtons();
		listView = (ListView) rootView.findViewById(R.id.jamListView);
		initializeJamList();
        
        return rootView;
    }
    

	/*
	 * Initializes the play, pause, back, and next buttons on the page.
	 */
	private void assignButtons() {
		backButton = (ImageButton) rootView.findViewById(R.id.song_media_player_back_btn);
		startButton =  (ImageButton) rootView.findViewById(R.id.song_media_player_start_btn);
		pauseButton = (ImageButton) rootView.findViewById(R.id.song_media_player_pause_btn);
		nextButton = (ImageButton) rootView.findViewById(R.id.song_media_player_next_btn);
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
				g.jam.start();
				if (!g.jam.checkMaster()) {
					(new PassMessageThread(g.jam.getMasterIpAddr(), port, "/jam/start", "")).start(); 					
				}
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
				g.jam.pause();
				if (!g.jam.checkMaster()) {
					(new PassMessageThread(g.jam.getMasterIpAddr(),
							port, "/jam/pause", "")).start(); 
				}
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
				g.jam.seekTo(0);
				if (!g.jam.checkMaster()) {
					(new PassMessageThread(g.jam.getMasterIpAddr(), 
							port, "/jam/restart", "")).start();
				}
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
	            g.jam.iterateCurrentSong();
	            g.jam.playCurrentSong();
			}
		});
	}
 
	/*
	 * Initializes the listView with a list of the current songs in the jam.
	 */
	private void initializeJamList() {
		int jamSize = g.jam.getJamSize();
		
		// Eventually want to abstract this so the Jam is maintaining its own string list
		ArrayList<String> songStringList = new ArrayList<String>(); 
		for (int i = 0; i < jamSize; i++) {
			String songText = "";  
			if (i == g.jam.getCurrentSongIndex()) {
				songText = "Now Playing: "; 
			}
			songText += g.jam.getSongByIndex(i).getArtist() + 
						": " + g.jam.getSongByIndex(i).getTitle();
			songStringList.add(songText); 
		}
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(g, 
				android.R.layout.simple_list_item_1, songStringList);
		listView.setAdapter(adapter);
		//setJamListItemClickListener();
	}
	
	/*
	 * Adds an onItemClickListener to the items in the listView that will
	 * play the song which is clicked on.
	 */
	/*
	private void setJamListItemClickListener() {
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
				if (!g.jam.checkMaster()) {
									new PassMessageThread(g.jam.getMasterIpAddr(), port,
						"/jam/set/" + Integer.toString(song.hashCode()), "").start(); 
				}
				initializeJamList(); 
			}
		});
	}
	*/
}