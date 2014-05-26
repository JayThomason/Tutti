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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private Globals g;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		g = (Globals) getApplicationContext(); 

		loadLocalMusic(); 
		initializeJam(); 
		setWelcomeText();
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		System.out.println("Main Activity Restarted.");
		g.jam.endServerKeepAlive();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(MainActivity.this, SettingsMenuActivity.class); 
			startActivity(intent); 
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	public void makeNewJam(View view) {
		showNameJamDialog();
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

	private void loadLocalMusic() {
		g.db.dropTable("songs"); 
		g.localLoaderThread = new MusicLibraryLoaderThread(this);
		g.localLoaderThread.start();	
	}

	private void initializeJam() {
		Globals g = (Globals) getApplicationContext(); 
		g.db.dropTable("jam"); 
	}

	private void setWelcomeText() {
		Globals g = (Globals) getApplicationContext(); 
		TextView welcomeText = (TextView) findViewById(R.id.welcome_message); 
		if (g.getUsername().equals("anonymous")) {
			welcomeText.setText("Set your username in the Settings menu!");
		} else {
			welcomeText.setText("Welcome back " + g.getUsername() + "!"); 
		}
	}

	private void showNameJamDialog() {
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
							startJam(jamName, nameDialog);
						}
					}
				});
			}
		});
	}

	// return true if jam created in db, false if definite failure
	private boolean startJam(String jamName, final AlertDialog nameDialog) {
		try {
			Server s = new Server(g);
			s.start();
			g.db.updatePortForLocalSongs();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		int port = g.getServerPort();
		if (port > 0) {
			startJamBroadcast(jamName);
			//createJamInDatabase(jamName.isEmpty() ? null : jamName, nameDialog, port);
			return true;
		}
		else {
			Toast.makeText(this, "Unable to create jam in database -- bad port number!", Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	// starts broadcasting the jam by making 
	private void startJamBroadcast(String jamName) {
		g.jam.startMasterClientPingThread();
		g.jam.setMaster(true);
		if (jamName != null && !jamName.equals("")) {
			g.jam.setJamName(jamName);
		} else {
			g.jam.setJamName(g.getUsername() + "'s Jam");
		}
		g.discoveryManager.makeJamDiscoverable();
		Intent intent = new Intent(MainActivity.this, BrowseMusicActivity.class);
		startActivity(intent);
	}

}
