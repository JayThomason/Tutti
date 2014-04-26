package com.stanford.tutti;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class MusicBrowserAdapter extends SimpleCursorAdapter {

    private Context mContext;
    private Context appContext;
    private int layout;
    private Cursor cr;
    private String[] columns; 
    private final LayoutInflater inflater;

    public MusicBrowserAdapter(Context context,int layout, Cursor c,String[] from,int[] to) {
        super(context,layout,c,from,to);
        this.layout=layout;
        this.mContext = context;
        this.columns = from; 
        this.inflater=LayoutInflater.from(context);
        this.cr=c;
    }

    @Override
    public View newView (Context context, Cursor cursor, ViewGroup parent) {
            return inflater.inflate(layout, null);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        super.bindView(view, context, cursor);
        
        TextView titleView = (TextView) view.findViewById(R.id.browserText); 
        ImageView artView = (ImageView) view.findViewById(R.id.browserArt);
        
        titleView.setText(cursor.getString(cursor.getColumnIndex(columns[1]))); 
        
        String artPath = cursor.getString(cursor.getColumnIndex("art")); 
        if (artPath != null && !artPath.equals("")) {
        	artView.setImageURI(Uri.parse(artPath)); 
        } else {
        	int noArtImgID = context.getResources().getIdentifier("music_note", "drawable", context.getPackageName());
            artView.setImageResource(noArtImgID);
        }
    }
	
}
