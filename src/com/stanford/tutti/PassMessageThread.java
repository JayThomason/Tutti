package com.stanford.tutti;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

/*
 * Creates a networking thread which sends an HTTP GET request
 * to the path at the specified ip address.
 */
class PassMessageThread extends Thread {
	private String ipAddress;
	private final int PORT = 1234;
	private String path; 

	/*
	 * Creates a new PassMessageThread for the ip/path pair.
	 * Note: the path should begin with a '/' (it must be
	 * an absolute path on the server).
	 */
	public PassMessageThread(String ip, String path) {
		ipAddress = ip;
		this.path = path; 
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Thread#run()
	 */
	public void run() {
		HttpClient httpClient = new DefaultHttpClient();
		String uri = "http://" + ipAddress + ":" + PORT + path;
		HttpGet get = new HttpGet(uri.toString());
		try {
			// Right now we're not doing anything with this response. 
			HttpResponse response = httpClient.execute(get);
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
