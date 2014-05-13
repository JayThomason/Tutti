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
    	                
        System.out.println("BINDING VIEW: " + cursor.getString(cursor.getColumnIndex("title"))); 
        
        ImageView artView = (ImageView) view.findViewById(R.id.browserArt);
        String artPath = cursor.getString(cursor.getColumnIndex("art")); 
        if (artPath != null && !artPath.equals("")) {
        	artView.setImageURI(Uri.parse(artPath)); 
        } else {
            artView.setImageResource(noArtImgID);
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
        TextView albumView = (TextView) view.findViewById(R.id.browserAlbum); 
        
		albumView.setVisibility(View.GONE); 

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
        TextView albumView = (TextView) view.findViewById(R.id.browserAlbum); 
    	
        String title = cursor.getString(cursor.getColumnIndex("title")); 

    	String album = cursor.getString(cursor.getColumnIndex("album"));
    	System.out.println("BINDING SONGS VIEW, ALBUM: " + album + " LAST ALBUM: " + lastAlbum); 
    	if (!album.equals(lastAlbum)) {
    		System.out.println("SETTING NEW ALBUM: " + album + " LAST ALBUM: " + lastAlbum); 
            albumView.setText("NEW ALBUM: " + album + " " + title); 
            lastAlbum = album; 
    	} else {
    		System.out.println("HIDING ALBUM: " + album + " LAST ALBUM: " + lastAlbum); 
    		albumView.setVisibility(View.GONE); 
    		//albumView.setBackgroundColor(Color.rgb(0, 0, 0));
    	}
        String username = g.jam.getIPUsername(cursor.getString(cursor.getColumnIndex("_ip"))); 
        if (username == null) 
        	username = ""; 
        titleView.setText(title); 
        ownerView.setText(username); 
    }
    
    
    private void bindJamView(View view, Cursor cursor) {
        TextView titleView = (TextView) view.findViewById(R.id.browserText); 
        TextView ownerView = (TextView) view.findViewById(R.id.ownerText); 
        TextView albumView = (TextView) view.findViewById(R.id.browserAlbum); 
    	
		albumView.setVisibility(View.GONE); 
    	
        String songTitle = cursor.getString(cursor.getColumnIndex("title")); 
        int songIndex = cursor.getInt(cursor.getColumnIndex("jamIndex")); 
        String text = ""; 
        if (g.jam != null) {
        	if (songIndex == g.jam.getCurrentSongIndex()) {
        		text += "Now playing: "; 
        	} else if (songIndex < g.jam.getCurrentSongIndex()) {
        		view.setBackgroundColor(Color.rgb(0, 0, 0));
        	}
        }
        text += songTitle; 
        titleView.setText(text); 
        
    	String addedBy = cursor.getString(cursor.getColumnIndex("addedBy")); 
    	ownerView.setText("Added by: " + addedBy);
    }
	
}
