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
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

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
public class StreamSongTestActivity extends Activity {
	
	private final int PORT = 1234;
	
	private Button clientButton;
	private Button serverButton;
	private EditText editText;
	
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
	
	private void configureClientButton() {
		clientButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				(new ClientThread()).start();
			}
		});		
	}

	private void configureServerButton() {
		serverButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				(new ServerThread()).start();
			}
		});
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
		setContentView(R.layout.activity_stream_song_test);
		System.out.println(getIpAddr());
		clientButton = (Button) this.findViewById(R.id.activity_stream_song_test_client_btn);
		serverButton = (Button) this.findViewById(R.id.activity_stream_song_test_server_btn);
		editText = (EditText) this.findViewById(R.id.ip_address);
		editText.setText(getIpAddr());
		configureClientButton();
		configureServerButton();
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {       
        onBackPressed(); 
        return true;
    }
    
    class ServerThread extends Thread {
    	public void run() {
    		try {
    			ServerSocket serverSocket = new ServerSocket(PORT);
    			Socket connection = serverSocket.accept();

    			BufferedReader in = new BufferedReader(  
    					new InputStreamReader(  
    							connection.getInputStream()));  
    			StringBuilder str = new StringBuilder();
    			String line = null;
    			while ((line = in.readLine()) != null) {
    				str.append(line);
    			}
    			final String msg = str.toString();
    			runOnUiThread(new Runnable() {
    				public void run() {
        				editText.setText(msg);
    				}
    			});
    			connection.close();
    			serverSocket.close();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    class ClientThread extends Thread {
    	public void run() {
    		try {
    			String ipAddr = editText.getText().toString();
    			Socket socket = new Socket(ipAddr, PORT);
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
    }
}


