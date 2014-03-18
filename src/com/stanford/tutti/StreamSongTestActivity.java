package com.stanford.tutti;

import java.io.IOException;
import org.json.JSONArray;


import android.app.ActionBar;
import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

/*
 * Test Class for networking between phones. 
 */
public class StreamSongTestActivity extends Activity {
	
	private final int PORT = 1234;
	private Server server;
	private Globals g;
	
	private Button clientButton;
	private Button serverButton;
	private EditText editText;
	
	public String getIpAddr() {
		WifiManager wifiManager = 
				(WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format(
				"%d.%d.%d.%d",
				(ip & 0xff),
				(ip >> 8 & 0xff),
				(ip >> 16 & 0xff),
				(ip >> 24 & 0xff));

		return ipString;
	}
	
	private void configureClientButton() {
		clientButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				//(new ClientThread()).start();
			}
		});		
	}

	private void configureServerButton() {
		serverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
		        server = new Server(PORT, g, null);
		        try {
					server.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_stream_song_test);
		System.out.println(getIpAddr());
		clientButton = (Button) this.findViewById(R.id.activity_stream_song_test_client_btn);
		serverButton = (Button) this.findViewById(R.id.activity_stream_song_test_server_btn);
		editText = (EditText) this.findViewById(R.id.ip_address);
		editText.setText(getIpAddr());
		g = (Globals) getApplication();
		configureClientButton();
		configureServerButton();
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {       
        onBackPressed(); 
        return true;
    }
    
    /*
    class ClientThread extends Thread {
    	public void run() {
    		try {
    			String ipAddr = editText.getText().toString();
    			String path = "/getLocalLibrary";
    			Uri uri = Uri.parse("http://" + ipAddr + ":" + PORT + path);
    			HttpClient httpClient = new DefaultHttpClient();
    			HttpGet get = new HttpGet(uri.toString());
    			HttpResponse response = httpClient.execute(get);
    			BufferedReader reader = new BufferedReader(
    					new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
    			
    			String serverArtistList = reader.readLine();
    			JSONObject jsonArtistList = new JSONObject(serverArtistList);
    			JSONArray artists = jsonArtistList.getJSONArray("artists");     			
    			loadMusicFromJSON(artists); 			
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    		} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    }*/
    
    /*
     * Load new music into the global library by
     * parsing the JSON response from another phone. 
     */
    public void loadMusicFromJSON(JSONArray artists) {
    	/*
    	Globals g = (Globals) getApplication(); 
    	
    	for (int i = 0; i < artists.length(); i++) {
    		try {
        		JSONObject jsonArtist = artists.getJSONObject(i); 
        		String artistName = (String)jsonArtist.get("name"); 
				Artist artist = new Artist(artistName);
				JSONArray albums = jsonArtist.getJSONArray("albums"); 
				for (int j = 0; j < albums.length(); j++) {
	        		JSONObject jsonAlbum = albums.getJSONObject(j); 
					String albumTitle = (String)jsonAlbum.get("title"); 
					Album album = new Album(albumTitle, artist); 
					JSONArray songs = jsonAlbum.getJSONArray("songs"); 
					for (int k = 0; k < songs.length(); k++) {
						JSONObject jsonSong = songs.getJSONObject(k); 
						String songTitle = (String)jsonSong.get("title"); 
						String songPath = (String)jsonSong.get("path"); 
						Song song = new Song(songTitle, songPath);
						song.setArtist(artist); 
						song.setAlbum(album); 
						album.addSong(song); 
					}
					artist.addAlbum(album); 
					g.addAlbum(album); 
				}
				g.addArtist(artist);
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
    	}
    */
    }
}


