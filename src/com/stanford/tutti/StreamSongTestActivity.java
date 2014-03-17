package com.stanford.tutti;

import java.io.IOException;
import java.util.Map;

import android.app.ActionBar;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
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
	private Server server;
	
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
		        server = new Server();
		        try {
					server.start();
				} catch (IOException e) {
					e.printStackTrace();
				}
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
    
    class Server extends NanoHTTPD {
    	public Server() {
    		super(PORT);
    	}
    	
        @Override
        public Response serve(String uri, Method method, 
                              Map<String, String> header,
                              Map<String, String> parameters,
                              Map<String, String> files)  {
        	System.out.println(uri);
        	System.out.println(method.toString());
        	for (String str : header.keySet())
        		System.out.println(str + ": " + header.get(str));
            for (String str : parameters.keySet())
            	System.out.println(str + " : " + parameters.get(str));
            return new NanoHTTPD.Response("");
        }
    }

    class ClientThread extends Thread {
    	public void run() {
    		try {
    			String ipAddr = editText.getText().toString();
    			Uri uri = Uri.parse("http://" + ipAddr + ":" + PORT);
    			System.out.println(uri.toString());
    			MediaPlayer mp = new MediaPlayer();
    			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
    			mp.setDataSource(getApplicationContext(), uri);
    			mp.prepare();
    			mp.start();
    			mp.stop();
    			mp.release();
    			mp = null;
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
}


