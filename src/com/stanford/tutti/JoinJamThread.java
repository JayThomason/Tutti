package com.stanford.tutti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
		/*
		if (isMasterPhone) {
			Set<String> clientIpAddrSet = g.jam.getClientIpSet();
			for (String clientIpAddr : clientIpAddrSet) {
				// send message to ask them to refresh library
			}
		}
		*/
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
			
			JSONObject jsonLibrary = new JSONObject(serverArtistList);
			JSONArray artists = jsonLibrary.getJSONArray("artists");
			
			JSONObject jam = jsonLibrary.getJSONObject("jam"); 
			
			String username = jsonLibrary.getString("username"); 
			g.jam.setIPUsername(ipAddress, username); 
			
			g.db.loadMusicFromJSON(artists, ipAddress); 
			g.jam.loadJamFromJSON(jam, ipAddress); 
			
			return true;
		}
		catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return false;
	}
}