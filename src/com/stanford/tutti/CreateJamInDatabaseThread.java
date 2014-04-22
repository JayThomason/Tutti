package com.stanford.tutti;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

public class CreateJamInDatabaseThread extends Thread {
	private String serverIpAddr;
	private String privateIpAddr;
	private final String path = "/createJam";
	private final String query = "?private=";

	public CreateJamInDatabaseThread(String serverIpAddr, String privateIpAddr) {
		this.serverIpAddr = serverIpAddr;
		try {
			this.privateIpAddr = URLEncoder.encode(privateIpAddr, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		HttpClient httpClient = new DefaultHttpClient();
		String uri = "http://" + serverIpAddr + path + query + privateIpAddr;
		HttpGet get = new HttpGet(uri.toString());
		try {
			System.out.println("Request to create jam on DB server.");
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
