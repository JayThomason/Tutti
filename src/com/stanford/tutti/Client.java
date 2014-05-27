package com.stanford.tutti; 

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import com.loopj.android.http.*;

public class Client {
	
	private AsyncHttpClient client; 
	private Globals g; 
	private String username; 
	private String ipAddress; 
	private int port; 
	private boolean isActive;
	
	public AsyncHttpResponseHandler loadRemoteLibraryResponse; 
	
	public Client(Globals g, String username, String ipAddress, int port) {
		client = new AsyncHttpClient();
		this.g = g; 
		this.username = username; 
		this.ipAddress = ipAddress;
		this.port = port;
		this.isActive = false;
	} 
	
	public void requestJoinJam(String username, int port, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/joinJam", "?username=" + username + "&port=" + String.valueOf(port));
		System.out.println("url: " + url);
		client.get(url, null, responseHandler); 
	}
	
	public void acceptJoinJam(String jamName, int port, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/acceptJoinJam", "?jamName=" + jamName + "&port=" + String.valueOf(port)); 
		client.get(url, null, responseHandler); 
	}
	
	public void rejectJoinJam(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/rejectJoinJam/", ""); 
		client.get(url, null, responseHandler); 
	}
	
	public void requestRemoteLibrary(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/getLocalLibrary/", ""); 
		client.get(url, null, responseHandler); 
	}
	
	public void requestAlbumArt(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/getAlbumArt/", ""); 
		client.get(url, null, responseHandler); 
	}

	public void requestAddSong(String songHash, String addedBy, String jamSongId, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/add/", "?songId=" + songHash + "&jamSongId=" + jamSongId + "&addedBy=" + addedBy); 
		client.get(url, null, responseHandler); 
	}
	
	public void requestSetSong(String jamSongId, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/set/?jamSongId=", jamSongId); 
		client.get(url, null, responseHandler); 
	}
	
	public void requestMoveSong(String jamSongId, int to, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/move/", "?jamSongId=" + jamSongId + "&to=" + to); 
		client.get(url, null, responseHandler); 
	}
	
	public void requestRemoveSong(String jamSongId, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/remove/", "?jamSongId=" + jamSongId); 
		client.get(url, null, responseHandler); 
	}
	
	public void refreshJam(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/getJam/", ""); 
		client.get(url, null, responseHandler); 
	}
	
	public void updateLibrary(JSONObject jsonLibrary, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/updateLibrary", "");
		try {
			StringEntity entity = new StringEntity(jsonLibrary.toString());
			client.setMaxRetriesAndTimeout(3, 5000);
			client.post(g.getBaseContext(), url, entity, "application/json", responseHandler);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void updateAlbumArt(JSONObject jsonAlbumArt, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/updateAlbumArt", ""); 
		try {
			StringEntity entity = new StringEntity(jsonAlbumArt.toString());
			client.setMaxRetriesAndTimeout(3, 5000);
			client.post(g.getBaseContext(), url, entity, "application/json", responseHandler);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void updateJam(JSONObject jsonJam, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/updateJam", ""); 
		try {
			StringEntity entity = new StringEntity(jsonJam.toString());
			client.setMaxRetriesAndTimeout(3, 5000);
			client.post(g.getBaseContext(), url, entity, "application/json", responseHandler);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void startPlaying(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/start", "");
		client.get(url, responseHandler);
	}
	
	public void pauseSong(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/pause", "");
		client.get(url, responseHandler);
	}
	
	public void restartSong(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/restart", "");
		client.get(url,  responseHandler);
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	public String getUsername() {
		return username; 
	}
	
	public void isActive() {
		this.isActive = true;
	}
	
	/*
	 * Sends a message to the client telling it to remove clientToRemove from the jam.
	 */
	public void removeAllFrom(Client clientToRemove, 
			AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/removeAllFrom", "?ip=" + clientToRemove.getIpAddress());
		client.get(url, responseHandler);
	}
	
	/*
	 * Sends a ping message to the client to check if it is still there.
	 * 
	 * The message is only sent if the client is 'active,' ie. we have already exchanged 
	 * music with this client. This is to guarantee that the client has set its masterIp
	 * variable.
	 */
	public void ping(AsyncHttpResponseHandler responseHandler) {
		if (isActive) {
			String url = getUrl("/ping", "");
			client.get(url,  responseHandler);
		}
	}
	
	/*
	 * Sengs a jamActive message to the client to check if it is hosting a jam.
	 */
	public void isMaster(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/isMaster", "");
		client.get(url, responseHandler);
	}
	
	private String getUrl(String path, String query) {
	    return "http://" + ipAddress + ":" + port + path + query; 
	}
}