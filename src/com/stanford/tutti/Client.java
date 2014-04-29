package com.stanford.tutti; 

import com.loopj.android.http.*;

public class Client {
	
	private AsyncHttpClient client; 
	private Globals g; 
	private String username; 
	private String ipAddress; 
	private int port; 
	
	public Client(Globals g, String username, String ipAddress, int port) {
		client = new AsyncHttpClient();
		this.g = g; 
		this.username = username; 
		this.ipAddress = ipAddress;
		this.port = port; 
	} 

	public void requestAddSong(String songHash, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/jam/add/", songHash); 
		client.get(url, null, responseHandler); 
	}
	
	public void refreshJam(AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/getJam/", ""); 
		client.get(url, null, responseHandler); 
	}
	
	private String getUrl(String path, String query) {
	    return "http://" + ipAddress + ":" + port + path + query; 
	}
	
}