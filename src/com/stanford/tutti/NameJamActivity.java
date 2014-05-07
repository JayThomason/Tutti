package com.stanford.tutti;

import java.io.IOException;

import org.apache.http.Header;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class NameJamActivity extends Activity {
	private Globals g;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_name_jam);

		getActionBar().setDisplayShowHomeEnabled(false);              
		getActionBar().setDisplayShowTitleEnabled(false);
		getActionBar().hide(); 

		g = (Globals) getApplication();
		g.jam.setMaster(true);
		try {
			(new Server(1234, g)).start();
		} catch (IOException e) {
			// failed to create server, abort back to main activity
			e.printStackTrace();
		}
	}

	/* In the future we can autogenerate a cool name here! */
	public void createQuickJam(View view) {
		createJamInDatabase(null);
		Intent intent = new Intent(this, BrowseMusicActivity.class);
		startActivity(intent);
	}

	public void createNamedJam(View view) {
		EditText nameField = (EditText) findViewById(R.id.jam_name);
		String name = nameField.getText().toString();
		if (name.length() <= 0 || name.equals(nameField.getHint().toString())) {
			Toast.makeText(getApplicationContext(), "Please enter a name for the jam.", Toast.LENGTH_LONG).show();
		}
		else if (name.contains(" ")) {
			Toast.makeText(getApplicationContext(), "The jam name may not contain spaces.", Toast.LENGTH_LONG).show();
		}
		else {
			createJamInDatabase(name);
			Intent intent = new Intent(this, BrowseMusicActivity.class);
			startActivity(intent);
		}
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
