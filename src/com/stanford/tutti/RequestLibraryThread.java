package com.stanford.tutti;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.Header;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.loopj.android.http.AsyncHttpResponseHandler;




class RequestLibraryThread extends Thread {
	private String ip;
	private int port;
	private Globals g; 
	
	public RequestLibraryThread(Globals g, String ip, int port) {
		this.g = g; 
		this.ip = ip;
		this.port = port;
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		final Client client = new Client(g, "", ip, port); 
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
					g.jam.setIPUsername(ip, username); 

					JSONArray artists = jsonLibrary.getJSONArray("artists");
					JSONObject jam = jsonLibrary.getJSONObject("jam"); 
					g.db.loadMusicFromJSON(artists);
					
					if (!g.jam.checkMaster()) {
						g.jam.loadJamFromJSON(jam); 
					}
					
					
					client.requestAlbumArt(new AsyncHttpResponseHandler() {
						@Override
						public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
							
							ByteArrayInputStream is = new ByteArrayInputStream(responseBody); 
							BufferedReader reader = new BufferedReader(
									new InputStreamReader(is));
							
							String remoteAlbumArt = reader.readLine();
							JSONObject jsonAlbumArt = new JSONObject(remoteAlbumArt); 
							
							g.db.loadAlbumArtFromJSON(jsonAlbumArt); 
							
							if (g.jam.checkMaster()) {
								for (Client client : g.jam.getClientSet()) {
									if (client.getIpAddress() != ip) {
										client.updateAlbumArt(jsonLibrary, new AsyncHttpResponseHandler() {
											
										});
									}
								}
							}
						}
					}); 
					
					
					if (g.jam.checkMaster()) {
						for (Client client : g.jam.getClientSet()) {
							if (client.getIpAddress() != ip) {
								client.updateLibrary(jsonLibrary, new AsyncHttpResponseHandler() {
									
								});
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		}); 
	}
	
}