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
	
	public void updateLibrary(JSONObject jsonLibrary, AsyncHttpResponseHandler responseHandler) {
		String url = getUrl("/updateLibrary", "");
		try {
			StringEntity entity = new StringEntity(jsonLibrary.toString());
			client.post(g.getBaseContext(), url, entity, "application/json", responseHandler);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public String getIpAddress() {
		return ipAddress;
	}
	
	private String getUrl(String path, String query) {
	    return "http://" + ipAddress + ":" + port + path + query; 
	}
}