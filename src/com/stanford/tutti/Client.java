package com.stanford.tutti; 

import java.io.UnsupportedEncodingException;

import org.apache.http.entity.StringEntity;
import org.json.JSONObject;

import com.loopj.android.http.*;

/**
 * 
 * Object that encapsulates the information for a remote phone: 
 * ip address, username, port, and activity boolean. 
 * 
 */
public class Client {
	
	private AsyncHttpClient client; 
	private Globals g; 
	private String username; 
	private String ipAddress; 
	private int port; 
	private boolean isActive;
	
	public AsyncHttpResponseHandler loadRemoteLibraryResponse; 
	
	/**
	 * Constructor. 
	 * Creates a Client object that stores basic information
	 * about a remote phone, and has methods for persistent
	 * communication of messages with that phone. 
	 * 
	 * @param Globals g
	 * @param String username
	 * @param String ipAddress
	 * @param int port
	 */
	public Client(Globals g, String username, String ipAddress, int port) {
		client = new AsyncHttpClient();
		this.g = g; 
		this.username = username; 
		this.ipAddress = ipAddress;
		this.port = port;
		this.isActive = false;
	} 
	
	/**
	 * Sends a request to join a jam at the Client on the specified port. 
	 * Request includes the local username, for purposes of displaying to the remote client. 
	 * 
	 * @param String username
	 * @param port
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void requestJoinJam(String username, int port, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/joinJam", "?username=" + username + "&port=" + String.valueOf(port));
		client.get(url, null, responseHandler); 
	}
	
	/**
	 * Sends a request to notify the Client that its prior join jam request has been accepted. 
	 * 
	 * @param String jamName
	 * @param int port
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void acceptJoinJam(String jamName, int port, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/acceptJoinJam", "?jamName=" + jamName + "&port=" + String.valueOf(port)); 
		client.get(url, null, responseHandler); 
	}
	
	/**
	 * Sends a request to notify this Client that its prior join jam request has been rejected.
	 * 
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void rejectJoinJam(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/rejectJoinJam/", ""); 
		client.get(url, null, responseHandler); 
	}
	
	/**
	 * Sends a request for the Client's music library metadata. 
	 * 
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void requestRemoteLibrary(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/getLocalLibrary/", ""); 
		client.get(url, null, responseHandler); 
	}
	
	/**
	 * Sends a request for the Client's album art. 
	 * 
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void requestAlbumArt(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/getAlbumArt/", ""); 
		client.get(url, null, responseHandler); 
	}

	/**
	 * Sends a request to the Client to add a song 
	 * to its local version of the jam. 
	 * 
	 * @param String songHash
	 * @param String addedBy
	 * @param String jamSongId
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void requestAddSong(String songHash, String addedBy, String jamSongId, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/add/", "?songId=" + songHash + "&jamSongId=" + jamSongId + "&addedBy=" + addedBy); 
		client.get(url, null, responseHandler); 
	}
	
	/**
	 * Sends a request to the Client to set the currently-playing song
	 * in its local version of the jam. 
	 * 
	 * @param String jamSongId
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void requestSetSong(String jamSongId, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/set/?jamSongId=", jamSongId); 
		client.get(url, null, responseHandler); 
	}
	
	/**
	 * Sends a request to the Client to move the given song
	 * to a new index in its local version of the jam. 
	 * 
	 * @param String jamSongId
	 * @param int to
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void requestMoveSong(String jamSongId, int to, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/move/", "?jamSongId=" + jamSongId + "&to=" + to); 
		client.get(url, null, responseHandler); 
	}
	
	/**
	 * Sends a request to the Client to remove the given song
	 * from its local version of the jam. 
	 * 
	 * @param String jamSongId
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void requestRemoveSong(String jamSongId, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/remove/", "?jamSongId=" + jamSongId); 
		client.get(url, null, responseHandler); 
	}
	
	/**
	 * Sends a request to the Client to get
	 * its local version of the jam. 
	 * 
	 * @param String jamSongId
	 * @param int to
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void refreshJam(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/getJam/", ""); 
		client.get(url, null, responseHandler); 
	}
	
	/**
	 * Sends a post request to the Client containing
	 * new music metadata. 
	 * 
	 * @param JSONObject jsonLibrary
	 * @param AsyncHttpResponseHandler responseHandler
	 */
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
	
	/**
	 * Sends a post request to the Client containing
	 * new encoded album art. 
	 * 
	 * @param JSONObject jsonAlbumArt
	 * @param AsyncHttpResponseHandler responseHandler
	 */
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
	
	/**
	 * Sends a post request to the Client containing
	 * an updated version of the jam. 
	 * 
	 * @param JSONObject jsonJam
	 * @param AsyncHttpResponseHandler responseHandler
	 */
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
	
	/**
	 * Sends a request to the Client to start playing the 
	 * current song in the jam. 
	 * 
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void startPlaying(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/start", "");
		client.get(url, responseHandler);
	}
	
	/**
	 * Sends a request to the Client to pause the 
	 * current song in the jam. 
	 * 
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void pauseSong(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/pause", "");
		client.get(url, responseHandler);
	}
	
	/**
	 * Sends a request to the Client to restart
	 * the current song in the jam. 
	 * 
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void restartSong(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/restart", "");
		client.get(url,  responseHandler);
	}
	
	/**
	 * Returns the ip address of the Client. 
	 * 
	 * @returns String ipAddress
	 */
	public String getIpAddress() {
		return ipAddress;
	}
	
	/**
	 * Returns the username of the Client. 
	 * 
	 * @returns String username
	 */
	public String getUsername() {
		return username; 
	}
	
	/**
	 * Sets this client to be active. 
	 */
	public void isActive() {
		this.isActive = true;
	}
	
	/**
	 * Sends a message to the client telling it to remove clientToRemove from the jam.
	 * 
	 * @param Client clientToRemove
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void removeAllFrom(Client clientToRemove, 
			AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/removeAllFrom", "?ip=" + clientToRemove.getIpAddress());
		client.get(url, responseHandler);
	}
	
	/**
	 * Sends a ping message to the client to check if it is still there.
	 * 
	 * The message is only sent if the client is 'active,' ie. we have already exchanged 
	 * music with this client. This is to guarantee that the client has set its masterIp
	 * variable.
	 * 
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void ping(AsyncHttpResponseHandler responseHandler) {
		if (isActive) {
			String url = getUrl("/ping", "");
			client.get(url,  responseHandler);
		}
	}
	
	/**
	 * Sends a jamActive message to the client to check if it is hosting a jam.
	 * 
	 * @param AsyncHttpResponseHandler responseHandler
	 */
	public void isMaster(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/isMaster", "");
		client.get(url, responseHandler);
	}
	
	/**
	 * Concatenates and returns a request URL for the client. 
	 * 
	 * @param String path
	 * @param String query
	 * @returns String fullURL
	 */
	private String getUrl(String path, String query) {
	    return "http://" + ipAddress + ":" + port + path + query; 
	}
}