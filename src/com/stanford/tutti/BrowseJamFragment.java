package com.stanford.tutti; 
 
import java.util.ArrayList;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
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
	
	private Cursor cursor; 
	private View rootView; 
	private Globals g; 
 
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
     	
        rootView = inflater.inflate(R.layout.fragment_browse_jam, container, false);
         
        g = (Globals) rootView.getContext().getApplicationContext(); 
        
		listView = (ListView) rootView.findViewById(R.id.jamListView);
		
        assignButtons();
		configureButtons();
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
	public void initializeJamList() {
		if (cursor != null) 
			cursor.close(); 

		cursor = g.db.getSongsInJam(); 
		
		String[] columns = new String[] { "art", "title", "addedBy" };
		int[] to = new int[] { R.id.browserArt, R.id.browserText, R.id.ownerText };

		BrowseMusicAdapter adapter = new BrowseMusicAdapter(g, R.layout.list_layout, cursor, columns, to); 
		listView.setAdapter(adapter);
		listView.setFastScrollEnabled(true);
		
		setJamListItemClickListener();
	}
	
	/*
	 * Adds an onItemClickListener to the items in the listView that will
	 * play the song which is clicked on.
	 */
	private void setJamListItemClickListener() {
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, 
					int position, long id) {
				
				TextView textView = (TextView) view.findViewById(R.id.browserText); 
				String title = textView.getText().toString();
								
				Toast.makeText(
						g, 
						"Now playing: " + title, Toast.LENGTH_SHORT)
						.show();
				g.jam.setCurrentSongByIndex(position);
				Song song = g.jam.getSongByIndex(position); 
				if (g.jam.checkMaster()) {
					g.jam.playCurrentSong(); 
					// THIS IS DUPLICATE CODE FROM THE SERVER
					// NEED BETTER ENCAPSULATION
					for (Client client : g.jam.getClientSet()) {
						if (client.getIpAddress().equals(g.getIpAddr())) 
							continue; 
						client.requestSetSong(Integer.toString(song.hashCode()), new AsyncHttpResponseHandler() {
							@Override
							public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
								System.out.println("request to add song to client returned: " + statusCode);
							}
						});
					}
				} else {
									new PassMessageThread(g.jam.getMasterIpAddr(), port,
						"/jam/set/" + Integer.toString(song.hashCode()), "").start(); 
				}
				initializeJamList(); 
			}
		});
	}
}