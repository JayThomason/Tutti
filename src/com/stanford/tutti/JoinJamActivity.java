package com.stanford.tutti;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

public class JoinJamActivity extends Activity {
	private Button joinButton; 
	private static final int PORT = 1234;
	private EditText editText;
	private Server server;
	private Globals g;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join_jam);
		// Show the Up button in the action bar.
		setupActionBar();
		editText = (EditText) this.findViewById(R.id.ip_address);
		g = (Globals) getApplication();
		configureJoinJamButton(); 
		
		/*
		ListView listView2 = (ListView) findViewById(R.id.listView2);
		String[] items = { "Audrey's Galaxy Note", "Jay's Nexus 7" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, items);
		listView2.setAdapter(adapter);
		
		listView2.setOnItemClickListener(new OnItemClickListener() {
			  @Override
			  public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				  String item = ((TextView)view).getText().toString();
				  new AlertDialog.Builder(view.getContext())
		            .setMessage("Join "+item+"?")
		            .setNegativeButton("No", null)
				  	.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int id) {
							Intent intent = new Intent(JoinJamActivity.this, ViewJamActivity.class);  
					        startActivity(intent);
						}
					}) .show();
			  }
			});
		*/
	}
	
	
	private void configureJoinJamButton() {
		joinButton = (Button) this.findViewById(R.id.join_jam_btn);
		joinButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
			    server = new Server(PORT, g, null);
				String ip = editText.getText().toString(); 
				Globals g = (Globals) getApplication(); 
				g.otherIP = ip; 
				Thread joinJamThread = new JoinJamThread(ip, g, false);
				try {
					server.start();
					joinJamThread.start();
					joinJamThread.join();
				} catch (InterruptedException e) {
					// probably want to log some message to user: unable to join jam
					e.printStackTrace();
				} catch (IOException e) {
					// unable to start server
					// in either failure case we can't join the jam and thus we should display
					// a message to the user and back out to the main menu or just stay here...
					e.printStackTrace();
				}
				
				// Load the new jam screen as a slave
				Intent intent = new Intent(JoinJamActivity.this, NewJamActivity.class);
				Bundle b = new Bundle();
				b.putInt("host", 0); //Your id
				intent.putExtras(b);
				startActivity(intent);
				finish();
			}
		});		
	}

	
	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.join_jam, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
