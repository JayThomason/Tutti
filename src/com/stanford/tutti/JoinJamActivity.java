package com.stanford.tutti;

import java.io.IOException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class JoinJamActivity extends Activity {
	private Button joinButton; 
	private static final int PORT = 1234;
	private EditText editText;
	private Server server;
	private Globals g;
	private NsdHelper mNsdHelper;
	private Handler h;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_join_jam);
		// Show the Up button in the action bar.
		setupActionBar();
		editText = (EditText) this.findViewById(R.id.ip_address);
		g = (Globals) getApplication();
		configureJoinJamButton(); 
		h = new Handler() {
		    @Override
		    public void handleMessage(Message msg) {
		       if (msg.getData().getInt("what") == 0) {
		    	   String ip = msg.getData().getString("ip");
		    	   int port = msg.getData().getInt("port");
		    	   joinDiscoveredJam(ip, port);
		       }
		    }
		};
		mNsdHelper = new NsdHelper(Globals.getAppContext(), h, false);
		mNsdHelper.initializeNsd();
        mNsdHelper.discoverServices();
	}
	
	private void joinDiscoveredJam(String ip, int port) {
		Globals g = (Globals) getApplication();
	    server = new Server(PORT, g, null);
	    g.server = server;
		g.jam.setOtherIP(ip);
		Thread joinJamThread = new JoinJamThread(ip, false);
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
	
	private void configureJoinJamButton() {
		joinButton = (Button) this.findViewById(R.id.join_jam_btn);
		joinButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String ip = editText.getText().toString(); 
				joinDiscoveredJam(ip, PORT);
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
	
	@Override
	protected void onPause() {
		if (mNsdHelper != null) {
			mNsdHelper.stopDiscovery();
		}
		super.onPause();
	}
	
    @Override
    protected void onResume() {
        super.onResume();
        if (mNsdHelper != null) {
            mNsdHelper.discoverServices();
        }
    }

	
	@Override
    protected void onDestroy() {
        mNsdHelper.tearDown();
        super.onDestroy();
    }
}
