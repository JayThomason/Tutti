package com.stanford.tutti;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;

/**
 * Requests the music library metadata, jam information,
 * and encoded album art from a remote phone, 
 * and loads it into the local database asynchronously.  
 */
class RequestLibraryThread extends Thread {
	private Globals g;
	private Client client; 
	
	/**
	 * Constructor. 
	 * 
	 * Creates a new thread to request from the given Client. 
	 * 
	 * @param Globals g
	 * @param Client client
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	public RequestLibraryThread(Globals g, Client client) {
		this.g = g; 
		this.client = client;
	}

	/**
	 * Runs the thread to request music library metadata, 
	 * jam information, and album art from a remote client phone. 
	 * 
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		client.requestRemoteLibrary(new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				try {					
					ByteArrayInputStream is = new ByteArrayInputStream(responseBody); 
					BufferedReader reader = new BufferedReader(
							new InputStreamReader(is));
					
					String remoteLibrary = reader.readLine();
					JSONObject jsonLibrary;
					jsonLibrary = new JSONObject(remoteLibrary);

					String username = jsonLibrary.getString("username"); 
					g.jam.setIPUsername(client.getIpAddress(), username); 

					JSONArray artists = jsonLibrary.getJSONArray("artists");
					JSONObject jam = jsonLibrary.getJSONObject("jam"); 
					
					g.db.loadMusicFromJSON(artists);
					g.logger.updateNumberSongs();
					
					if (!g.jam.checkMaster()) {
						g.jam.loadJamFromJSON(jam); 
					}
					
					if (g.jam.checkMaster()) {
						for (Client c : g.jam.getClientSet()) {
							if (c != client) {
								c.updateLibrary(jsonLibrary, new AsyncHttpResponseHandler() {
									
								});
							}
						}
					}
					
					
					client.requestAlbumArt(new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
							
							ByteArrayInputStream is = new ByteArrayInputStream(responseBody); 
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(is));
							
							String remoteAlbumArt;
							JSONObject jsonAlbumArt = null; 
							try {
								remoteAlbumArt = reader.readLine();
								jsonAlbumArt = new JSONObject(remoteAlbumArt);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} catch (JSONException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							} 
							
							if (jsonAlbumArt != null) {
								g.db.loadAlbumArtFromJSON(jsonAlbumArt); 
								
								if (g.jam.checkMaster()) {
									for (Client c : g.jam.getClientSet()) {
										if (c != client) {
											c.updateAlbumArt(jsonAlbumArt, new AsyncHttpResponseHandler() {
												
											});
										}
									}
								}
							}
						}
					}); 
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}); 
	}
	
}