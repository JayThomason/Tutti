package com.stanford.tutti;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends Activity {	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
                
        setContentView(R.layout.activity_main);
                
        getActionBar().hide(); 
        
        loadLocalMusic(); 
        
        initializeJam(); 
        
        setWelcomeText(); 
    }
    
    private void loadLocalMusic() {
        Globals g = (Globals) getApplicationContext(); 
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
		Intent intent = new Intent(this, NewJamActivity.class);
		startActivity(intent);
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

}
