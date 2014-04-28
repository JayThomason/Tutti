package com.stanford.tutti;
 
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
 
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
}