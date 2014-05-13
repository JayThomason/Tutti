package com.stanford.tutti;

import com.loopj.android.http.AsyncHttpResponseHandler;

import android.app.ActionBar.Tab;
import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

public class BrowseMusicActivity extends FragmentActivity implements ActionBar.TabListener {

    public ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    // Tab titles
    private String[] tabs = { "Artists", "Songs", "Jam" };
    
    private BrowseArtistsFragment artistsFragment;
    private BrowseSongsFragment songsFragment; 
    private BrowseJamFragment jamFragment; 
    
    private Globals g; 
    private int PORT = 1234; 
 
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_browser);
 
        // Initilization
        viewPager = (ViewPager) findViewById(R.id.pager);
        actionBar = getActionBar();
        mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
 
        viewPager.setAdapter(mAdapter);
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);        
         
        // MenuItem item = actionBar.findItem(R.id.action_settings);

        
        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }
        
        getActionBar().setDisplayShowHomeEnabled(false);              
        getActionBar().setDisplayShowTitleEnabled(false);
        
    	g = (Globals) getApplicationContext(); 
    	    	
        setupTabHighlightListener(); 
        setupHandler(); 
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.music_browser, menu);
		return true;
	}
	
	/*
	@Override
	public void onBackPressed() {
		int index = actionBar.getSelectedNavigationIndex(); 
		int newIndex = index - 1; 
		if (newIndex == -1) {
			newIndex = 0; 
		}
	    viewPager.setCurrentItem(newIndex); 
	}
	*/

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onTabReselected(Tab tab, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
	}

	@Override
	public void onTabSelected(Tab tab, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
		int index = tab.getPosition(); 
		viewPager.setCurrentItem(index);
	}

	@Override
	public void onTabUnselected(Tab tab, FragmentTransaction arg1) {
		// TODO Auto-generated method stub
	}
	
	private void setupTabHighlightListener() {
	   	/**
    	 * on swiping the viewpager make respective tab selected
    	 * */
    	viewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
    	 
    	    @Override
    	    public void onPageSelected(int position) {
				int index = actionBar.getSelectedNavigationIndex(); 
    	    	if (position == 0 && position < index && !g.currentArtistView.equals("")) {
    	    		g.currentArtistView = ""; 
    				if (g.uiUpdateHandler != null) {
    					Message msg = g.uiUpdateHandler.obtainMessage();
    					msg.what = 5; 
    					g.uiUpdateHandler.sendMessage(msg);
    				}
    	    	} else if (position == 1 && position < index && !g.currentAlbumView.equals("")) {
    	    		g.currentAlbumView = ""; 
    				if (g.uiUpdateHandler != null) {
    					Message msg = g.uiUpdateHandler.obtainMessage();
    					msg.what = 6; 
    					g.uiUpdateHandler.sendMessage(msg);
    				}
    	    	}
    	        // on changing the page
    	        // make respected tab selected
    	        actionBar.setSelectedNavigationItem(position);
    	    }
    	 
    	    @Override
    	    public void onPageScrolled(int arg0, float arg1, int arg2) {
    	    }
    	 
    	    @Override
    	    public void onPageScrollStateChanged(int arg0) {
    	    }
    	});
    	
	}
	
	/*
	 * Initializes the handler. The handler is used to receive messages from
	 * the server and to update the UI accordingly (new songs, join jam requests, etc.)
	 */
	private void setupHandler() {
		g.uiUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				String message = (String)msg.obj; 
				if (message != null) {
					// We've received a String message containing a username
					// Need to display a "Join Jam?" alert dialog					
					displayJoinJamRequest(message); 
				}
				
				if (msg.what == 0) {
					int index = actionBar.getSelectedNavigationIndex(); 
					if (index == 0) {
						if (artistsFragment != null) 
							artistsFragment.refreshArtistList(); 
					} else if (index == 1) {
						if (songsFragment != null)
							songsFragment.refreshSongList(); 
					} else if (index == 2) {
						if (jamFragment != null) 
							jamFragment.refreshJamList(); 
					}
				} else if (msg.what == 1) {
					artistsFragment.refreshArtistList(); 
				} else if (msg.what == 2) {

				} else if (msg.what == 3) {
					if (songsFragment != null)
						songsFragment.refreshSongList(); 
			        viewPager.setCurrentItem(1); 
				} else if (msg.what == 4) {
					if (jamFragment != null)
						jamFragment.refreshJamList(); 
					viewPager.setCurrentItem(2); 
				} else if (msg.what == 5) {
					
				} else if (msg.what == 6) {
					if (songsFragment != null)
						songsFragment.refreshSongList(); 
				} else if (msg.what == 7) {
					if (jamFragment != null) 
						jamFragment.refreshJamList(); 
				} 
				super.handleMessage(msg);
			}
		};		
	}
	
	public void displayJoinJamRequest(String message) {
		final String ipAddr = message.split("//")[0]; 
		final String username = message.split("//")[1]; 
		
		View currView = viewPager.getFocusedChild(); 
		
		new android.app.AlertDialog.Builder(currView.getContext())
	    .setTitle("Join Jam Request Received")
	    .setMessage("Accept Join Jam request from " + username + "?")
	    .setPositiveButton("Accept", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	        	// Accept join jam request and request new client's music library. 
	        	Client newClient = new Client(g, username, ipAddr, 1234);
				g.jam.addClient(newClient);
				g.jam.setIPUsername(ipAddr, username);
				newClient.acceptJoinJam(new AsyncHttpResponseHandler() { 
					
				}); 
		    	Thread getLibraryThread = new RequestLibraryThread(g, ipAddr, PORT);
		    	getLibraryThread.start();
	        }
	    }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
	        public void onClick(DialogInterface dialog, int whichButton) {
	            // Reject join jam request. 
	        	Client newClient = new Client(g, username, ipAddr, 1234); 
	        	newClient.rejectJoinJam(new AsyncHttpResponseHandler() { 
	        		
	        	}); 
	        }
	    }).show();
	}
	
	public class TabsPagerAdapter extends FragmentPagerAdapter {
		 
	    public TabsPagerAdapter(FragmentManager fm) {
	        super(fm);
	    }
	 
	    @Override
	    public Fragment getItem(int index) {
	 
	        switch (index) {
		        case 0:
		            return new BrowseArtistsFragment();
		        case 1:
		            return new BrowseSongsFragment();
		        case 2: 
		        	return new BrowseJamFragment();
	        }
	 
	        return null;
	    }
	 
	    @Override
	    public int getCount() {
	        // get item count - equal to number of tabs
	        return 3;
	    }
	    
	    @Override
	    public Object instantiateItem(ViewGroup container, int position) {
	    	Object fragment = super.instantiateItem(container, position); 
	    	if (fragment instanceof BrowseArtistsFragment) {
    			artistsFragment = (BrowseArtistsFragment) fragment; 
	    	} else if (fragment instanceof BrowseSongsFragment) {
    			songsFragment = (BrowseSongsFragment) fragment; 
	    	} else if (fragment instanceof BrowseJamFragment) {
    			jamFragment = (BrowseJamFragment) fragment; 
    			g.playerListener = (BrowseJamFragment) fragment; 
	    	}
	    	return fragment; 
	    }
	}
}
