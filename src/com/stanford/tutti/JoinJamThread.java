package com.stanford.tutti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.stanford.tutti.NanoHTTPD.Response.Status;

import android.net.Uri;
import android.os.Message;

class JoinJamThread extends Thread {

	private String ipAddress;
	private final int PORT = 1234;
	private Globals g; 
	boolean isMasterPhone;

	public JoinJamThread(String ip, boolean isMasterPhone) {
		ipAddress = ip; 
		g = (Globals) Globals.getAppContext(); 
		this.isMasterPhone = isMasterPhone;
	}

	public void run() {
		HttpClient httpClient = new DefaultHttpClient();
		if (isMasterPhone || requestJoinJam(httpClient)) {
			getRemoteLibrary(httpClient);
		}
		else {
			// display to user: unable to join jam in pop up box and go back to main menu
		}
	}

	/*
	 * Asks the master phone if it can join the jam. Returns true if the master 
	 * phone returns OK.
	 */
	private boolean requestJoinJam(HttpClient httpClient) {
		String path = "/joinJam";
		String uri = "http://" + ipAddress + ":" + PORT + path;
		HttpGet get = new HttpGet(uri.toString());
		try {
			System.out.println("JoinJamThread: Requesting to join jam");
			HttpResponse response = httpClient.execute(get);
			System.out.println(response.toString());
			return response.getStatusLine().getStatusCode() == HttpStatus.SC_OK;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * Requests and parses the library from the master phone of the jam.
	 */
	private boolean getRemoteLibrary(HttpClient httpClient) {
		try {
			String path = "/getLocalLibrary";
			String uri = "http://" + ipAddress + ":" + PORT + path;
			HttpGet get = new HttpGet(uri);
			HttpResponse response = httpClient.execute(get);
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
			String serverArtistList = reader.readLine();
			JSONObject jsonArtistList = new JSONObject(serverArtistList);
			JSONArray artists = jsonArtistList.getJSONArray("artists");     			
			loadMusicFromJSON(artists); 
			System.out.println(response.toString());
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}

	/*
	 * Load new music into the database library by
	 * parsing the JSON response from another phone. 
	 * 
	 */
	public void loadMusicFromJSON(JSONArray artists) {    	
		for (int i = 0; i < artists.length(); i++) {
			try {
				JSONObject jsonArtist = artists.getJSONObject(i); 
				String artistName = (String)jsonArtist.get("name"); 
				JSONArray albums = jsonArtist.getJSONArray("albums"); 
				for (int j = 0; j < albums.length(); j++) {
					JSONObject jsonAlbum = albums.getJSONObject(j); 
					String albumTitle = (String)jsonAlbum.get("title");
					JSONArray songs = jsonAlbum.getJSONArray("songs"); 
					for (int k = 0; k < songs.length(); k++) {
						JSONObject jsonSong = songs.getJSONObject(k); 
						String songTitle = (String)jsonSong.get("title"); 
						String songPath = (String)jsonSong.get("path");
						Song song = new Song(songTitle, songPath, false);
						song.setArtist(artistName); 
						song.setAlbum(albumTitle); 
						
						g.db.addSong(song); 
						if (g.uiUpdateHandler != null) {
							Message msg = g.uiUpdateHandler.obtainMessage();
							msg.what = 0; // fix this later to be constant
							g.uiUpdateHandler.sendMessage(msg);
						}
					}
				}
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} 
		}
	}
}