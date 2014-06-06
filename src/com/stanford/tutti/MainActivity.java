package com.stanford.tutti;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/** 
 * This activity displays the application's main menu when the app is booted.
 */
public class MainActivity extends Activity {
	private Globals g;

	/**
	 * Loads the music from disk, resets the jam, sets the Welcome Text.
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		g = (Globals) getApplicationContext(); 

		loadLocalMusic(); 
		initializeJam(); 
		setWelcomeText();
	}

	/**
	 * Stops logging a jam, resets the welcome text, and stops all discovery
	 * related tasks.
	 * 
	 * @see android.app.Activity#onRestart()
	 */
	@Override
	protected void onRestart() {
		super.onRestart();
		System.out.println("Main Activity Restarted.");
		setWelcomeText();
		
		g.discoveryManager.stopJamDiscoverable();
		g.discoveryManager.stopJamDiscovery();
		g.logger.endCurrentJam();
	}

	/**
	 * (non-Javadoc)
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * (non-Javadoc)
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Intent intent = new Intent(MainActivity.this, 
					SettingsMenuActivity.class); 
			startActivity(intent); 
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * Shows the name jam dialog so a new jam can be created. Binded to the
	 * create jam button.
	 * 
	 * @param View view
	 */
	public void makeNewJam(View view) {
		showNameJamDialog();
	}

	/**
	 * Starts a new Join Jam activity. Bound to join local jam button.
	 * 
	 * @param View view
	 */
	public void joinJam(View view) {
		Intent intent = new Intent(this, JoinJamActivity.class);
		startActivity(intent);
	}

	/**
	 * Starts a new SettingsMenuActivity. Bound to settings button.
	 * 
	 * @param View view
	 */
	public void settingsMenu(View view) {
		Intent intent = new Intent(this, SettingsMenuActivity.class);
		startActivity(intent);
	}

	/**
	 * Starts a new HelpMenuActivity. Bound to help button.
	 * 
	 * @param View view
	 */
	public void helpMenu(View view) {
		Intent intent = new Intent(this, HelpMenuActivity.class);
		startActivity(intent);
	}

	/**
	 * Starts a new thread that loads the music on the phone into a local 
	 * database.
	 */
	private void loadLocalMusic() {
		g.db.dropTable("songs"); 
		g.localLoaderThread = new MusicLibraryLoaderThread(this);
		g.localLoaderThread.start();	
	}

	/**
	 * Drops the jam table to ensure no songs are preloaded in the jam.
	 */
	private void initializeJam() {
		g.db.dropTable("jam"); 
	}

	/* *
	 * Sets the welcome text to either welcome the user by their username or to
	 * ask the user to set his or her username in the settings.
	 */
	private void setWelcomeText() {
		Globals g = (Globals) getApplicationContext(); 
		TextView welcomeText = (TextView) findViewById(R.id.welcome_message); 
		if (g.getUsername().equals("anonymous")) {
			welcomeText.setText("Set your username in the Settings menu!");
		} else {
			welcomeText.setText("Welcome back " + g.getUsername() + "!"); 
		}
	}

	/**
	 * Displays the name jam dialog on screen, allowing the user to create a
	 * jam with a certain name.
 	 */
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
		
		input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
		    @Override
		    public void onFocusChange(View v, boolean hasFocus) {
		        if (hasFocus) {
		            nameDialog.getWindow().setSoftInputMode(
		            		WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
		        }
		    }
		});
	}

	/**
	 * Sets the listener for the name jam dialog. Starts the jam if the name
	 * provided is valid.
	 * 
	 * @param nameDialog The name jam dialog being displayed
	 * @param input The EditText from the name jam dialog input box
	 */
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
									"The jam name may not contain spaces.", 
									Toast.LENGTH_LONG).show();
						}
						else {
							startJam(jamName);
						}
					}
				});
			}
		});
	}

	/**
	 * Starts a new jam by booting a server, starting the jam broadcast,
	 * 
	 * @param jamName The name of the jam to be started
	 * @returns True if jam created successfully and false on failure
	 */
	private boolean startJam(String jamName) {
		try {
			Server s = new Server(g);
			s.start();
			g.server = s;
			g.db.updatePortForLocalSongs();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		int port = g.getServerPort();
		if (port > 0) {
			startJamBroadcast(jamName);
			return true;
		}
		else {
			Toast.makeText(this, 
					"Unable to create jam in database -- bad port number!", 
					Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	/**
	 * Starts the master client ping thread, sets this phone as the jam master,
	 * sets the jam name, makes the jam discoverable, starts a new logger, and 
	 * 
	 * @param jamName The name of the jam being started
	 */
	private void startJamBroadcast(String jamName) {
		g.jam.startMasterClientPingThread();
		g.jam.setMaster(true);
		if (jamName != null && !jamName.equals("")) {
			g.jam.setJamName(jamName);
		} else {
			g.jam.setJamName(g.getUsername() + "'s Jam");
		}
		g.discoveryManager.makeJamDiscoverable();
		g.logger.startNewJam();
		Intent intent = new Intent(MainActivity.this, BrowseMusicActivity.class);
		startActivity(intent);
	}
}
