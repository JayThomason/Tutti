package com.stanford.tutti; 

import org.apache.http.Header;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.database.Cursor;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class BrowseJamFragment extends Fragment implements OnPreparedListener {

	private ImageButton startButton;
	private ImageButton pauseButton;
	private ImageButton backButton;
	private ImageButton nextButton;

	//private ListView listView;
	private DragSortListView listView; 

	private View rootView; 
	private Globals g; 

	private int current = 0;  
	private boolean running = true;  
	private SeekBar seekBar;  
	private TextView mediaTimeCurrent;
	private TextView mediaTimeEnd;
	private Client masterClient;	

	private BrowseMusicAdapter adapter; 

	private DragSortListView.DropListener onDrop = new DragSortListView.DropListener()
	{
		@Override
		public void drop(int from, int to)
		{
			if (from != to)
			{
				String timestamp = ""; 
				JSONObject jsonJam = new JSONObject(); 
				g.jamLock.lock(); 
				try {
					timestamp = g.jam.getSongIdByIndex(from); 
					g.jam.changeSongIndexInJam(timestamp, to); 
					refreshJamList(); 
					
					if (g.jam.checkMaster()) {
						jsonJam = g.jam.toJSON(); 							
					}
				} finally {
					g.jamLock.unlock(); 
				}
				
				if (g.jam.checkMaster()) {
					g.jam.broadcastJamUpdate(jsonJam); 
				} else {
					g.jam.requestMoveSong(timestamp, to);
				}
			}
		}
	};

	private DragSortListView.RemoveListener onRemove = new DragSortListView.RemoveListener() {
		@Override
		public void remove(int index) {
			String songJamId = ""; 
			JSONObject jsonJam = new JSONObject(); 
			g.jamLock.lock(); 
			try {
				songJamId = g.jam.getSongIdByIndex(index); 
    			g.jam.removeSong(songJamId);
    			/*
    			if (g.jam.isShuffled()) {
    				g.jam.unShuffle(); 
    			} else {
        			g.jam.shuffle(); 
    			}
    			*/
    			g.sendUIMessage(0); 
				jsonJam = g.jam.toJSON(); 
			} finally {
				g.jamLock.unlock(); 
			}
			
			
			if (g.jam.checkMaster()) {
				g.jam.broadcastJamUpdate(jsonJam); 
			} else {
				g.jam.requestRemoveSong(songJamId); 
			}
		}
	};



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		rootView = inflater.inflate(R.layout.fragment_browse_jam, container, false);
		listView = (DragSortListView) rootView.findViewById(R.id.jamListView);
		seekBar = (SeekBar) rootView.findViewById(R.id.progress_bar);  
		mediaTimeCurrent = (TextView) rootView.findViewById(R.id.progress_time); 
		mediaTimeEnd = (TextView) rootView.findViewById(R.id.progress_time_end); 

		g = (Globals) rootView.getContext().getApplicationContext(); 


		g.playerListener = this; 
		g.jam.mediaPlayer.setOnPreparedListener(this); 

		masterClient = new Client(g, "", g.jam.getMasterIpAddr(), g.jam.getMasterPort()); 

		if (g.jam.checkMaster()) {
			initializeSeekBar(); 
		} else {
			RelativeLayout progressBar = (RelativeLayout) rootView.findViewById(R.id.player_progress); 
			progressBar.setVisibility(View.GONE); 
			listView.setPadding(0, 0, 0, 120);
		}
		
		listView.setFastScrollEnabled(true); 

		assignButtons();
		configureButtons();
		initializeJamList();

		return rootView;
	}


	private void initializeSeekBar() {
		seekBar.setMax(g.playerDuration);  
		seekBar.postDelayed(onEverySecond, 1000);  
		seekBar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {  
			@Override  
			public void onStopTrackingTouch(SeekBar seekBar) {
				g.jam.mediaPlayer.seekTo(seekBar.getProgress());
				updateTime(); 
			}  

			@Override  
			public void onStartTrackingTouch(SeekBar seekBar) { }  

			@Override  
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) { }  
		});  
	}

	private Runnable onEverySecond = new Runnable() {  
		@Override  
		public void run(){  
			if (running == true){  
				if (seekBar != null) {  
					seekBar.setProgress(g.jam.mediaPlayer.getCurrentPosition());  
				}  

				if (g.jam.mediaPlayer.isPlaying()) {  
					seekBar.postDelayed(onEverySecond, 1000);  
					updateTime();  
				}  
			}  
		}  
	};  

	private void updateTime(){  
		current = g.jam.mediaPlayer.getCurrentPosition();  

		int dSeconds = (int) (g.playerDuration / 1000) % 60 ;  
		int dMinutes = (int) ((g.playerDuration / (1000*60)) % 60);  
		int dHours   = (int) ((g.playerDuration / (1000*60*60)) % 24);  

		int cSeconds = (int) (current / 1000) % 60 ;  
		int cMinutes = (int) ((current / (1000*60)) % 60);  
		int cHours   = (int) ((current / (1000*60*60)) % 24);  

		if(dHours == 0){  
			mediaTimeCurrent.setText(String.format("%02d:%02d", cMinutes, cSeconds));
			mediaTimeEnd.setText(String.format("%02d:%02d", dMinutes, dSeconds));
		}
		else{  
			mediaTimeCurrent.setText(String.format("%02d:%02d:%02d", cHours, cMinutes, cSeconds));
			mediaTimeEnd.setText(String.format("%02d:%02d:%02d", dHours, dMinutes, dSeconds));
		}  
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
					masterClient.startPlaying(new AsyncHttpResponseHandler() {
						public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
							System.out.println("Requested start playing on master, returned code: " + statusCode);
						}
					});
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
					masterClient.pauseSong(new AsyncHttpResponseHandler() {
						public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
							System.out.println("Requested pause song on master, returned code: " + statusCode);
						}
					});
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
					masterClient.restartSong(new AsyncHttpResponseHandler() {
						public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
							System.out.println("Requested previous song on master, returned code: " + statusCode);
						}
					});
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
				int index = g.jam.getCurrentSongIndex();
				index++;
				String songTimestampId = g.jam.getSongIdByIndex(index);
				if (!g.jam.checkMaster()) { // client
					masterClient.requestSetSong(songTimestampId, new AsyncHttpResponseHandler() {
						public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
							System.out.println("Requested next song on master, returned code: " + statusCode);
						}
					});
				}
				else { // master
					JSONObject jsonJam;
					g.jamLock.lock(); 
					try {
						g.jam.setCurrentSong(songTimestampId);
						g.jam.playCurrentSong(); 
						
						g.sendUIMessage(7); 
						
						jsonJam = g.jam.toJSON(); 
					} finally {
						g.jamLock.unlock(); 
					}
					g.jam.broadcastJamUpdate(jsonJam); 
				}
			}
		});
	}


	/*
	 * Initializes the listView with a list of the current songs in the jam.
	 */
	public void initializeJamList() {
		listView.setDropListener(onDrop);
		listView.setRemoveListener(onRemove);


		DragSortController controller = new DragSortController(listView);
		controller.setDragHandleId(R.id.browserText);
		controller.setDragInitMode(2);
		controller.setFlingHandleId(R.id.browserText);
		controller.setRemoveMode(1);

		controller.setRemoveEnabled(true);
		controller.setSortEnabled(true);

		listView.setFloatViewManager(controller);
		listView.setOnTouchListener(controller);
		listView.setDragEnabled(true);


		Cursor cursor = g.jam.getSongs(); 

		String[] columns = new String[] { "art", "artist", "title", "addedBy" };
		int[] to = new int[] { R.id.browserArt, R.id.browserText, R.id.ownerText };

		adapter = new BrowseMusicAdapter(g, R.layout.list_layout, cursor, columns, to); 
		listView.setAdapter(adapter);
		listView.setFastScrollEnabled(true);

		setJamListItemClickListener();
	}
	

	public void refreshJamList() {
		Cursor newCursor = g.jam.getSongs(); 
		Cursor oldCursor = adapter.swapCursor(newCursor);
		oldCursor.close(); 
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

				final String title; 
				String[] tokens = textView.getText().toString().split(":"); 
				if (tokens[0].equals("Now playing")) {
					title = tokens[2]; 
				} else {
					title = tokens[1]; 
				}

				final int index = position; 

				String songJamId = ""; 
				JSONObject jsonJam = new JSONObject(); 
				g.jamLock.lock(); 
				try {
					if (g.jam.checkMaster()) {
						songJamId = g.jam.setCurrentSongIndex(index);
						g.jam.playCurrentSong(); 
						refreshJamList(); 
						Toast.makeText(
								g, 
								"Now playing: " + title, Toast.LENGTH_SHORT)
								.show();
						
						jsonJam = g.jam.toJSON();
					} else {
						songJamId = g.jam.getSongIdByIndex(index); 
					}
				} finally {
					g.jamLock.unlock(); 
				}
				
				if (g.jam.checkMaster()) {
					g.jam.broadcastJamUpdate(jsonJam); 
				} else {
					g.jam.requestSetSong(songJamId, title);
				}
			}
		});
	}

	
	@Override  
	public void onPrepared(MediaPlayer mp) { 
		if (g.jam.checkMaster()) {
			mp.start(); 
		}
		g.playerDuration = g.jam.mediaPlayer.getDuration();  
		seekBar.setMax(g.playerDuration);  
		seekBar.postDelayed(onEverySecond, 1000);  
	}  
}