package com.stanford.tutti;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
        
        String title = cursor.getString(cursor.getColumnIndex("title")); 
        String artist = cursor.getString(cursor.getColumnIndex("artist")); 
        if (title.equals("DISPLAY_ALBUM")) {
            TextView titleView = (TextView) view.findViewById(R.id.browserText); 
            titleView.setText(artist + ": " + cursor.getString(cursor.getColumnIndex("album")));  
            
            LayoutParams layoutParams = artView.getLayoutParams(); 
            layoutParams.width = (int) g.getResources().getDimension(R.dimen.image_dimen_large);
            layoutParams.height = (int) g.getResources().getDimension(R.dimen.image_dimen_large); 
            artView.setLayoutParams(layoutParams); 
            artView.requestLayout(); 
            
            if (view != null) {
            	/*
            	if (p != null){
		            p.setMargins(0, 50, 0, 0);
		            view.requestLayout();
            	}
            	*/
            }
           
           
            return; 
        } else {
            LayoutParams layoutParams = artView.getLayoutParams(); 
            layoutParams.width = (int) g.getResources().getDimension(R.dimen.image_dimen_small);
            layoutParams.height = (int) g.getResources().getDimension(R.dimen.image_dimen_small); 
            artView.setLayoutParams(layoutParams); 
            artView.requestLayout(); 
        }
        
        if (columns.length == 2) { 
        	bindArtistsView(view, cursor); 
        } else if (columns.length == 3) {
        	bindSongsView(view, cursor); 
        } else {
        	bindJamView(view, cursor); 
        }
    }
    
    
    private void bindArtistsView(View view, Cursor cursor) {
        TextView titleView = (TextView) view.findViewById(R.id.browserText); 
        TextView ownerView = (TextView) view.findViewById(R.id.ownerText); 

        String username = g.jam.getIPUsername(cursor.getString(cursor.getColumnIndex("_ip"))); 
        if (username == null) 
        	username = ""; 
        String title = cursor.getString(cursor.getColumnIndex("artist")); 
        titleView.setText(title); 
        ownerView.setText(username); 
    }
    
    private void bindSongsView(View view, Cursor cursor) {
        TextView titleView = (TextView) view.findViewById(R.id.browserText); 
        TextView ownerView = (TextView) view.findViewById(R.id.ownerText); 
    	
        String title = cursor.getString(cursor.getColumnIndex("title")); 
        String artist = cursor.getString(cursor.getColumnIndex("artist")); 

        String username = g.jam.getIPUsername(cursor.getString(cursor.getColumnIndex("_ip"))); 
        if (username == null) 
        	username = ""; 
        titleView.setText(artist + ": " + title); 
        ownerView.setText(username); 
    }
    
    
    private void bindJamView(View view, Cursor cursor) {
        TextView titleView = (TextView) view.findViewById(R.id.browserText); 
        TextView ownerView = (TextView) view.findViewById(R.id.ownerText); 
    	
        String songTitle = cursor.getString(cursor.getColumnIndex("title"));
        String artist = cursor.getString(cursor.getColumnIndex("artist")); 

        int songIndex = cursor.getInt(cursor.getColumnIndex("jamIndex")); 
        String text = ""; 
        if (g.jam != null) {
        	if (songIndex == g.jam.getCurrentSongIndex()) {
        		text += "Now playing: "; 
        	} else if (songIndex < g.jam.getCurrentSongIndex()) {
        		view.setBackgroundColor(Color.rgb(0, 0, 0));
        	}
        }
        text += artist + ": " + songTitle; 
        titleView.setText(text); 
        
    	String addedBy = cursor.getString(cursor.getColumnIndex("addedBy")); 
    	ownerView.setText("Added by: " + addedBy);
    }
	
}
