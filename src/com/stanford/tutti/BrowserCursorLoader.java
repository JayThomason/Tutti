package com.stanford.tutti; 

import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;

public class BrowserCursorLoader extends CursorLoader {

	private Globals g; 
	
    public BrowserCursorLoader(Context context, Globals g) {
        super(context);
        this.g = g; 
    }
 
    @Override
    public Cursor loadInBackground() {
        // this is just a simple query, could be anything that gets a cursor
        return g.db.getAllSongs(); 
    }
}