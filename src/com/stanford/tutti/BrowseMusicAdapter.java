package com.stanford.tutti;

import com.loopj.android.http.AsyncHttpResponseHandler;

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
    
    private int port = 1234; 
        
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

        ImageView artView = (ImageView) view.findViewById(R.id.browserArt);
        String artPath = cursor.getString(cursor.getColumnIndex("art")); 
 
        if (artPath != null && !artPath.equals("")) {
        	artView.setImageURI(Uri.parse(artPath)); 
        } else {
            artView.setImageResource(noArtImgID);
        }
        
        String title = cursor.getString(cursor.getColumnIndex("title")); 
        
        if (title.equals("DISPLAY_ALBUM")) {
            String artist = cursor.getString(cursor.getColumnIndex("artist")); 
            if (artist.equals("<unknown>")) {
            	artist = "Unknown Artist"; 
            }
        	
            TextView titleView = (TextView) view.findViewById(R.id.browserText); 
            titleView.setText(artist + ": " + cursor.getString(cursor.getColumnIndex("album")));  
            
            LayoutParams layoutParams = artView.getLayoutParams(); 
            layoutParams.width = (int) g.getResources().getDimension(R.dimen.image_dimen_large);
            layoutParams.height = (int) g.getResources().getDimension(R.dimen.image_dimen_large); 
            artView.setLayoutParams(layoutParams); 
            artView.requestLayout(); 
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
        String artist = cursor.getString(cursor.getColumnIndex("artist")); 
        if (artist.equals("<unknown>")) {
        	artist = "Unknown Artist"; 
        }
        titleView.setText(artist); 
        ownerView.setText(username); 
    }
    
    private void bindSongsView(View view, Cursor cursor) {
        TextView titleView = (TextView) view.findViewById(R.id.browserText); 
        TextView ownerView = (TextView) view.findViewById(R.id.ownerText); 
    	
        String title = cursor.getString(cursor.getColumnIndex("title")); 
        String artist = cursor.getString(cursor.getColumnIndex("artist")); 
        if (artist.equals("<unknown>")) {
        	artist = "Unknown Artist"; 
        }
        
        int trackNum = cursor.getInt(cursor.getColumnIndex("trackNum")); 
        if (trackNum > 1000) {
        	trackNum %= 1000; 
        }
        
        String titleText = ""; 
        if (trackNum != 0) {
        	titleText += trackNum + ". "; 
        }
        if (g.currentArtistView.equals("")) {
        	titleText += artist + ": "; 
        }
        titleText += title; 
    	titleView.setText(titleText); 
    	
    	Song song = g.db.rowToSong(cursor); 
    	if (g.jam.containsSong(song)) {
    		view.setBackgroundColor(Color.rgb(0, 0, 0));
    	} else {
    		view.setBackgroundColor(Color.TRANSPARENT); 
    	}
    	
        String username = g.jam.getIPUsername(cursor.getString(cursor.getColumnIndex("_ip"))); 
        if (username == null) 
        	username = ""; 
        ownerView.setText(username); 
    }
    
    
    private void bindJamView(View view, Cursor cursor) {
        TextView titleView = (TextView) view.findViewById(R.id.browserText); 
        TextView ownerView = (TextView) view.findViewById(R.id.ownerText); 
    	
        String songTitle = cursor.getString(cursor.getColumnIndex("title"));
        String artist = cursor.getString(cursor.getColumnIndex("artist")); 
        if (artist.equals("<unknown>")) {
        	artist = "Unknown Artist"; 
        }

        final int songIndex = cursor.getInt(cursor.getColumnIndex("jamIndex")); 
        int displayIndex = songIndex + 1; 
        String text = displayIndex + ". "; 
        if (g.jam != null) {
        	if (songIndex == g.jam.getCurrentSongIndex()) {
        		text += "Now playing: "; 
        	} else if (songIndex < g.jam.getCurrentSongIndex()) {
        		view.setBackgroundColor(Color.rgb(0, 0, 0));
        	} else {
        		view.setBackgroundColor(Color.TRANSPARENT); 
        	}
        }
        text += artist + ": " + songTitle; 
        titleView.setText(text); 
        
    	String addedBy = cursor.getString(cursor.getColumnIndex("addedBy")); 
    	ownerView.setText(addedBy);
    	
        ImageView artView = (ImageView) view.findViewById(R.id.browserArt);
    	artView.setOnClickListener(new View.OnClickListener() {
    		@Override
    		public void onClick(View view) {
    			//g.jam.removeSong(songIndex);
    			if (g.jam.isShuffled()) {
    				g.jam.unShuffle(); 
    			} else {
        			g.jam.shuffle(); 
    			}
    			g.sendUIMessage(0); 
    			if (g.jam.checkMaster()) {
    				for (Client client : g.jam.getClientSet()) {
    					client.requestRemoveSong(Integer.toString(songIndex), new AsyncHttpResponseHandler() {
    						
    					}); 
    				}
    			} else {
    				Client masterClient = new Client(g, "", g.jam.getMasterIpAddr(), port); 
    				masterClient.requestRemoveSong(Integer.toString(songIndex), new AsyncHttpResponseHandler() {
    					
    				});
    			}
    		}
    	});
    }
    

	
    @Override
    public boolean isEnabled(int position) {
        /*if(YOUR CONDTITION){
            return false;
        }*/
        return true;
    }
}
