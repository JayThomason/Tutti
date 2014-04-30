package com.stanford.tutti;

import android.app.ActionBar.Tab;
import android.app.Activity;
import android.app.ActionBar;
import android.support.v4.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.os.Build;

public class BrowseMusicActivity extends FragmentActivity implements ActionBar.TabListener {

    public ViewPager viewPager;
    private TabsPagerAdapter mAdapter;
    private ActionBar actionBar;
    // Tab titles
    private String[] tabs = { "Artists", "Albums", "Songs", "Jam" };
    
    private BrowseArtistsFragment artistsFragment;
    private BrowseAlbumsFragment albumsFragment; 
    private BrowseSongsFragment songsFragment; 
    private BrowseJamFragment jamFragment; 
    
    private Globals g; 
 
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
         
        // Adding Tabs
        for (String tab_name : tabs) {
            actionBar.addTab(actionBar.newTab().setText(tab_name)
                    .setTabListener(this));
        }
        
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
	
	@Override
	public void onBackPressed() {
		int index = actionBar.getSelectedNavigationIndex(); 
		int newIndex = index - 1; 
		if (newIndex == -1) {
			newIndex = 0; 
		}
	    viewPager.setCurrentItem(newIndex); 
	}

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
	 * the server and to update the UI accordingly.
	 */
	private void setupHandler() {
		g.uiUpdateHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				/*
				 * When we get a message from another phone that we have new
				 * non-local music, we can update the list-view for the library.
				 */
				if (msg.what == 0) {
					int index = actionBar.getSelectedNavigationIndex(); 
					if (index == 0) {
						artistsFragment.initializeArtistList(); 
					} else if (index == 1) {
						albumsFragment.initializeAlbumList(); 
					} else if (index == 2) {
						songsFragment.initializeSongList(); 
					} else if (index == 3) {
						jamFragment.initializeJamList(); 
					}
				} else if (msg.what == 1) {
					artistsFragment.initializeArtistList(); 
				} else if (msg.what == 2) {
					albumsFragment.initializeAlbumList(); 
					viewPager.setCurrentItem(1);
				} else if (msg.what == 3) {
					songsFragment.initializeSongList(); 
			        viewPager.setCurrentItem(2); 
				} else if (msg.what == 4) {
					jamFragment.initializeJamList(); 
					viewPager.setCurrentItem(3); 
				}
				super.handleMessage(msg);
			}
		};		
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
		            return new BrowseAlbumsFragment();
		        case 2:
		            return new BrowseSongsFragment();
		        case 3: 
		        	return new BrowseJamFragment();
	        }
	 
	        return null;
	    }
	 
	    @Override
	    public int getCount() {
	        // get item count - equal to number of tabs
	        return 4;
	    }
	    
	    @Override
	    public Object instantiateItem(ViewGroup container, int position) {
	    	Object fragment = super.instantiateItem(container, position); 
	    	if (fragment instanceof BrowseArtistsFragment) {
    			artistsFragment = (BrowseArtistsFragment) fragment; 
	    	} else if (fragment instanceof BrowseAlbumsFragment) {
    			albumsFragment = (BrowseAlbumsFragment) fragment; 
	    	} else if (fragment instanceof BrowseSongsFragment) {
    			songsFragment = (BrowseSongsFragment) fragment; 
	    	} else if (fragment instanceof BrowseJamFragment) {
    			jamFragment = (BrowseJamFragment) fragment; 
	    	}
	    	return fragment; 
	    }
	}
}
