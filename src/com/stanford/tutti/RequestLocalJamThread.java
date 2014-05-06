package com.stanford.tutti;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;

import android.widget.ArrayAdapter;
import android.widget.ListView;

public class RequestLocalJamThread extends Thread {
	private String serverHostname;
	private final String path = "/discoverJams";
	private Activity joinJamActivity;
	private HashMap<String, String> ipMap = new HashMap<String, String>();

	public RequestLocalJamThread(String serverHostname, Activity joinJamActivity) {
		this.serverHostname = serverHostname;
		this.joinJamActivity = joinJamActivity;
	}
	
	public String getIpForName(String name) {
		return ipMap.get(name);
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
				if (jamListStr.length() <= 1)
					return;
				response.getEntity().consumeContent();
				// each name/ip pair is delimited by a \n and the names/ips are divided by a space
				final ArrayList<String> nameList = new ArrayList<String>();
				String nameIpPairList[] = jamListStr.split("\n");
				for (String str : nameIpPairList) {
					String nameIpPair[] = str.split(" ");
					nameList.add(nameIpPair[0]);
					ipMap.put(nameIpPair[0],  nameIpPair[1]);
				}
				joinJamActivity.runOnUiThread(new Runnable() {
					@Override
					public void run() { // update ui with jam list
						ListView jamListView = (ListView) joinJamActivity.findViewById(R.id.jamListView);
						ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
								joinJamActivity, android.R.layout.simple_list_item_1,
								nameList);
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