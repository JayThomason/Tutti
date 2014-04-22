package com.stanford.tutti;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.os.Handler;

public class RequestLocalJamThread extends Thread {
	private String serverIpAddr;
	private final String path = "/discoverJams";
	private Handler h;
	
	public RequestLocalJamThread(String serverIpAddr, Handler h) {
		this.serverIpAddr = serverIpAddr;
		this.h = h;
	}
	
	public void run() {
		HttpClient httpClient = new DefaultHttpClient();
		String uri = "http://" + serverIpAddr + path;
		HttpGet get = new HttpGet(uri.toString());
		try {
			System.out.println("Request to get local jams for public ip addr.");
			System.out.println(get.toString());
			HttpResponse response = httpClient.execute(get);
			System.out.println(response.toString());
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
