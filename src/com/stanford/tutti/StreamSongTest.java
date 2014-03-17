package com.stanford.tutti;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.app.ActionBar;
import android.app.Activity;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;

/*
 * Test Class for networking between phones. 
 * 
 * Current goal is to stream a song from one phone to another, however
 * right now all that this class does is open a socket from one phone to
 * another and send a message through.
 * 
 * I want to bind the startServer and startClient methods to buttons in the
 * layout file but I haven't gotten there yet.
 */
public class StreamSongTest extends Activity {
	
	private final String SERVER_IP = "1.1.1.1"; // set to ip address of one of the phones
	private final int PORT = 1234;
	
	public String getIpAddr() {
		   WifiManager wifiManager = 
				   (WifiManager) getSystemService(WIFI_SERVICE);
		   WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		   int ip = wifiInfo.getIpAddress();

		   String ipString = String.format(
		   "%d.%d.%d.%d",
		   (ip & 0xff),
		   (ip >> 8 & 0xff),
		   (ip >> 16 & 0xff),
		   (ip >> 24 & 0xff));

		   return ipString;
		}
	
	private void startServer() {
		try {
			ServerSocket serverSocket = new ServerSocket(PORT);
			Socket connection = serverSocket.accept();
			
			BufferedReader in = new BufferedReader(  
                    new InputStreamReader(  
                            connection.getInputStream()));  
            String line = null;  
            while ((line = in.readLine()) != null) {  
            	System.out.println(line);
            } 
            connection.close();
            serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void startClient() {
		try {
			Socket socket = new Socket(SERVER_IP, PORT);
			PrintWriter out = new PrintWriter(
					new BufferedWriter(new OutputStreamWriter(
							socket.getOutputStream())), true);
			out.println("TEST MESSAGE");
			socket.close();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_stream_song_test);
		System.out.println(getIpAddr());
	}
	
}
