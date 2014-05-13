package com.stanford.tutti;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class BrowseMusicAdapter extends SimpleCursorAdapter {

    private int layout;
    private Cursor cr;
    private String[] columns; 
    private final LayoutInflater inflater;
    
    private Globals g; 
    private int noArtImgID;
    
    private String lastAlbum = ""; 

    public BrowseMusicAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context,layout,c,from,to);
        this.layout=layout;
        this.mContext = context;
        this.columns = from; 
        this.inflater=LayoutInflater.from(context);
        this.cr=c;
        
        this.g = (Globals) context.getApplicationContext(); 
    	this.noArtImgID = context.getResources().getIdentifier("musicnote", "drawable", context.getPackageName());
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
    	if (cursor == null)
    		return; 
    	
        super.bindView(view, context, cursor);
                
        ImageView artView = (ImageView) view.findViewById(R.id.browserArt);
        String artPath = cursor.getString(cursor.getColumnIndex("art")); 
        if (artPath != null && !artPath.equals("")) {
        	artView.setImageURI(Uri.parse(artPath)); 
        } else {
            artView.setImageResource(noArtImgID);
        }

        TextView titleView = (TextView) view.findViewById(R.id.browserText); 
        TextView ownerView = (TextView) view.findViewById(R.id.ownerText); 
        
        // SHOULD BE DOING THIS BY HASH CODE
        String songTitle = cursor.getString(cursor.getColumnIndex(columns[1])); 
        String text = ""; 
        if (g.jam != null && g.jam.getCurrentSong() != null && g.jam.getCurrentSong().getTitle().equals(songTitle)) {
        	text += "Now playing: "; 
        }
        text += songTitle; 
        titleView.setText(text); 
        
        if (columns.length == 2) { 
        	bindLibraryView(titleView, ownerView, cursor); 
        } else {
        	bindJamView(view, titleView, ownerView, cursor); 
        }
    }
    
    
    private void bindLibraryView(TextView titleView, TextView ownerView, Cursor cursor) {
        String username = g.jam.getIPUsername(cursor.getString(cursor.getColumnIndex("_ip"))); 
        if (username == null) 
        	username = ""; 
        String title = cursor.getString(cursor.getColumnIndex(columns[1])); 
        titleView.setText(title); 
        ownerView.setText(username); 
    }
    
    
    private void bindJamView(View mainView, TextView titleView, TextView ownerView, Cursor cursor) {
        String songTitle = cursor.getString(cursor.getColumnIndex(columns[1])); 
        int songIndex = cursor.getInt(cursor.getColumnIndex("jamIndex")); 
        String text = ""; 
        if (g.jam != null) {
        	if (songIndex == g.jam.getCurrentSongIndex()) {
        		text += "Now playing: "; 
        	} else if (songIndex < g.jam.getCurrentSongIndex()) {
        		mainView.setBackgroundColor(Color.rgb(0, 0, 0));
        	}
        }
        text += songTitle; 
        titleView.setText(text); 
        
    	String addedBy = cursor.getString(cursor.getColumnIndex(columns[2])); 
    	ownerView.setText("Added by: " + addedBy);
    }
	
}
