package com.stanford.tutti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ActionBar;
import android.app.Activity;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.net.http.AndroidHttpClient;
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
 */
public class StreamSongTestActivity extends Activity {
	
	private final int PORT = 1234;
	private Server server;
	private Globals g;
	
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
		        server = new Server(PORT, g);
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
		g = (Globals) getApplication();
		configureClientButton();
		configureServerButton();
	}
	
    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem)
    {       
        onBackPressed(); 
        return true;
    }
    
    class ClientThread extends Thread {
    	public void run() {
    		try {
    			String ipAddr = editText.getText().toString();
    			String path = "/getLocalLibrary";
    			Uri uri = Uri.parse("http://" + ipAddr + ":" + PORT + path);
    			HttpClient httpClient = new DefaultHttpClient();
    			HttpGet get = new HttpGet(uri.toString());
    			HttpResponse response = httpClient.execute(get);
    			BufferedReader reader = new BufferedReader(
    					new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
    			String serverArtistList = reader.readLine();
    			JSONObject jsonArtistList = new JSONObject(serverArtistList);
    			System.out.println(jsonArtistList.toString(2));
    		}
    		catch (IOException e) {
    			e.printStackTrace();
    		} catch (JSONException e) {
				e.printStackTrace();
			}
    	}
    }
}


