package com.stanford.tutti;

import java.io.IOException;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {
	private Globals g;
	private final int port = 1234;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getActionBar().hide(); 
		g = (Globals) getApplicationContext(); 

		loadLocalMusic(); 
		initializeJam(); 
		setWelcomeText(); 
	}

	private void loadLocalMusic() {
		g.db.dropTable("songs"); 
		MusicLibraryLoaderThread loaderThread = new MusicLibraryLoaderThread(this);
		loaderThread.run();	
	}

	private void initializeJam() {
		Globals g = (Globals) getApplicationContext(); 
		g.db.dropTable("jam"); 
	}

	private void setWelcomeText() {
		Globals g = (Globals) getApplicationContext(); 
		TextView welcomeText = (TextView) findViewById(R.id.welcome_message); 
		if (g.getUsername().equals("anonymous")) {
			welcomeText.setText("Set your username in the Settings menu so your friends can see which music is yours!");
		} else {
			welcomeText.setText("Welcome back " + g.getUsername() + "!"); 
		}
	}


	/*
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
	 */

	public void makeNewJam(View view) {
		g.jam.setMaster(true); 
		try {
			(new Server(port, g)).start();
			createJamInDatabase();
			Intent intent = new Intent(this, BrowseMusicActivity.class);
			startActivity(intent);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void joinJam(View view) {
		Intent intent = new Intent(this, JoinJamActivity.class);
		startActivity(intent);
	}

	public void settingsMenu(View view) {
		Intent intent = new Intent(this, SettingsMenuActivity.class);
		startActivity(intent);
	}

	public void helpMenu(View view) {
		Intent intent = new Intent(this, HelpMenuActivity.class);
		startActivity(intent);
	}

	private void createJamInDatabase() {
		AsyncHttpClient client = new AsyncHttpClient();
		String serverHostname = getString(R.string.ec2_server);
		String localIpAddr = g.getIpAddr();
		String url = "http://" + serverHostname + "/createJam?private=" + localIpAddr;
		client.get(url, new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				if (statusCode == 200) {
					System.out.println("Successfully created jam on server.");
				}
				else {
					System.out.println("Failed to create jam on server.");
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				System.out.println("Failed to create jam on server.");
			}
		});
	}
}
