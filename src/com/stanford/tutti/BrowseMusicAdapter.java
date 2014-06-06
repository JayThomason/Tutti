package com.stanford.tutti;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Adapter for the music browser. Associates songs with elements in
 * the list view for the artists, songs, and jam fragments.
 */
public class BrowseMusicAdapter extends SimpleCursorAdapter {
    private int layout;
    private String[] columns; 
    private final LayoutInflater inflater;
    private Globals g; 
    private int noArtImgID;
            
    /**
     * @param context 
     * @param layout
     * @param c
     * @param from
     * @param to
     */
    public BrowseMusicAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
        super(context,layout,c,from,to);
        this.layout=layout;
        this.mContext = context;
        this.columns = from; 
        this.inflater=LayoutInflater.from(context);        
        this.g = (Globals) context.getApplicationContext(); 
    	this.noArtImgID = context.getResources().getIdentifier("musicnote", "drawable", context.getPackageName());
    }

    /**
     * (non-Javadoc)
     * @see android.support.v4.widget.ResourceCursorAdapter#newView(android.content.Context, android.database.Cursor, android.view.ViewGroup)
     */
    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(layout, null);
    }

    /**
     * Binds a song from the library database to a view in the list view.
     * 
     * @param view A view from the ListView to bind to
     * @param context The context to use
     * @param cursor Cursor representing a song from the music library database.
     * 
     * (non-Javadoc)
     * @see android.support.v4.widget.SimpleCursorAdapter#bindView(android.view.View, android.content.Context, android.database.Cursor)
     */
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
            TextView ownerView = (TextView) view.findViewById(R.id.ownerText); 
            ownerView.setText("");
            return; 
        } else {
        	
        	setTopMargin(view, 0); 
        	
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
    
    /**
     * Sets the top margin for a view.
     * 
     * @param view The view to set the margin for
     * @param pixels The number of pixels for the margin
     */
    private void setTopMargin(View view, int pixels) {
    	ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) view.findViewById(R.id.browserArt).getLayoutParams();
        mlp.setMargins(0, pixels, 0, 0);
    }
    
    /**
     * Binds an artist from the database to a view.
     * Used in the artists fragment list view.
     * 
     * @param view The view to bind the artist to.
     * @param cursor A cursor representing an artist.
     */
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
    
    /**
     * Binds a song from the database to a view.
     * Used in the songs fragment list view.
     * 
     * @param view The view to bind the song to.
     * @param cursor A cursor representing a song from the library database.
     */
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
    
    /**
     * Binds a song from the jam to a view.
     * Used in the jam fragment list view.
     * 
     * @param view The view to bind the song to.
     * @param cursor A cursor representing a song from the jam database.
     */
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
        		view.setBackgroundColor(Color.parseColor("#ff0927"));
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
    			JSONObject jsonJam = new JSONObject(); 
    			g.jamLock.lock(); 
    			try {
    				g.jam.shuffle(); 
        			g.sendUIMessage(0); 
    				jsonJam = g.jam.toJSON(); 
    			} finally {
    				g.jamLock.unlock(); 
    			}
    			
    			
    			if (g.jam.checkMaster()) {
    				g.jam.broadcastJamUpdate(jsonJam); 
    			} else {
    				//g.jam.requestShuffleJam(); 
    			}
    		}
    	});
    }
    

	/**
	 * (non-Javadoc)
	 * @see android.widget.BaseAdapter#isEnabled(int)
	 */
    @Override
    public boolean isEnabled(int position) {
        return true;
    }
}
