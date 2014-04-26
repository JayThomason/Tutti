package com.stanford.tutti;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.os.Handler;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RequestLocalJamThread extends Thread {
	private String serverHostname;
	private final String path = "/discoverJams";
	private Activity joinJamActivity;
	
	public RequestLocalJamThread(String serverHostname, Activity joinJamActivity) {
		this.serverHostname = serverHostname;
		this.joinJamActivity = joinJamActivity;
	}
	
	public void run() {
		HttpClient httpClient = new DefaultHttpClient();
		String uri = "http://" + serverHostname + path;
		HttpGet get = new HttpGet(uri.toString());
		try {
			System.out.println("Request to get local jams for public ip addr.");
			HttpResponse response = httpClient.execute(get);
			System.out.println("Server response: "+ response.getStatusLine().getStatusCode());
			if (response.getStatusLine().getStatusCode() == 200) {
				String jamListStr = EntityUtils.toString(response.getEntity());
				response.getEntity().consumeContent();
				final String ipList[] = jamListStr.split("\n");
				joinJamActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() { // update ui with jam list
						ListView jamListView = (ListView) joinJamActivity.findViewById(R.id.jamListView);
						ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
								joinJamActivity, android.R.layout.simple_list_item_1,
								ipList);
						jamListView.setAdapter(arrayAdapter);
					}
				});
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}