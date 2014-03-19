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

class PassMessageThread extends Thread {

	private String ipAddress;
	private final int PORT = 1234;
	private Globals g; 
	private String path; 

	public PassMessageThread(String ip, String path) {
		ipAddress = ip; 
		g = (Globals) Globals.getAppContext(); 
		this.path = path; 
	}

	public void run() {
		HttpClient httpClient = new DefaultHttpClient();
		// String path = method + "/" + Utils.getUniqueKeyForSong(song);
		String uri = "http://" + g.jam.getOtherIP() + ":" + PORT + path;
		HttpGet get = new HttpGet(uri.toString());
		try {
			System.out.println("Sending message to other phone at " + g.jam.getOtherIP() + " : " + path);
			System.out.println(uri.toString()); 
			HttpResponse response = httpClient.execute(get);
			System.out.println("RESPONSE:"); 
			System.out.println(response.toString());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
