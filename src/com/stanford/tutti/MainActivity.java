package com.stanford.tutti;

import com.example.myfirstapp.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends Activity {

    public final static String EXTRA_MESSAGE = "com.example.myfirstapp.MESSAGE";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button browseArtistsBtn = (Button) this.findViewById(R.id.browse_artists_btn);
		browseArtistsBtn.setOnClickListener( new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	
		      	Intent intent = new Intent(getApplicationContext(), ArtistListView.class);
		      	startActivity(intent);
		    }
		  });

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
