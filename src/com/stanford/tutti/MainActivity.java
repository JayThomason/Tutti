package com.stanford.tutti;

import java.io.IOException;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
		final EditText input = new EditText(MainActivity.this);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT);
		input.setLayoutParams(lp);

		final AlertDialog nameDialog = new AlertDialog.Builder(this)
		.setTitle("Choose a name")
		.setMessage("Enter a name below or just press create jam to use a default name.")
		.setPositiveButton("Create", null)
		.setNegativeButton("Cancel", null)
		.setView(input)
		.create();
		setNameDialogShowListener(nameDialog, input);
		nameDialog.show();
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
	
	private void setNameDialogShowListener(final AlertDialog nameDialog, final EditText input) {
		nameDialog.setOnShowListener(new DialogInterface.OnShowListener() {
			@Override
			public void onShow(DialogInterface dialog) {
				Button b = nameDialog.getButton(AlertDialog.BUTTON_POSITIVE);
				b.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String jamName = input.getText().toString();
						if (jamName.contains(" ")) {
							Toast.makeText(getApplicationContext(), 
									"The jam name may not contain spaces.", Toast.LENGTH_LONG).show();
						}
						else {
					        try {
					    		(new Server(1234, g)).start();
								createJamInDatabase(jamName.isEmpty() ? null : jamName);
								g.jam.setMaster(true);
								nameDialog.dismiss();
								Intent intent = new Intent(MainActivity.this, BrowseMusicActivity.class);
								startActivity(intent);
					        }
					        catch (IOException e) {
					        	e.printStackTrace();
					        }
						}
					}
				});
			}
		});
	}

	private void createJamInDatabase(String name) {
		String serverHostname = getString(R.string.ec2_server);
		Uri.Builder builder = Uri.parse("http://" + serverHostname).buildUpon();
		builder.path("/createJam");
		builder.appendQueryParameter("private",  g.getIpAddr());
		builder.appendQueryParameter("ssid",  g.getWifiSSID());
		builder.appendQueryParameter("gateway", g.getGatewayIpAddr());

		if (name != null) {
			builder.appendQueryParameter("name", name);
		}

		AsyncHttpClient client = new AsyncHttpClient();
		client.get(builder.build().toString(), new AsyncHttpResponseHandler() {
			@Override
			public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
				if (statusCode == 200) {
					System.out.println("Successfully created jam on server.");
				}
				else {
					System.out.println("Failed to create jam on server.");
					System.out.println("Response body: " + new String(responseBody));
				}
			}

			@Override
			public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
				System.out.println("Failed to create jam on server.");
			}
		});
	}
}
