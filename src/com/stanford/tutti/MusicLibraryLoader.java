package com.stanford.tutti;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;

/**
 * LoadMusicLibraryActivity
 * 
 * Loads all music meta data from the SDCard into the global
 * in-memory music store.
 * 
 * If we ever want to keep track of more metadata about each
 * artist, album, or song then we can load that memory into
 * the Global music objects using the sqllite queries here.
 */
public class MusicLibraryLoader {
	
	private ArrayList<String> artists; 
	
	/*
	 * Loads all of the music into the Globals music metadata store.
	 */
    public void loadMusic(Activity activity) {
    	Globals g = (Globals) activity.getApplication();
		loadAllArtists(activity, g);
		loadAllSongs(activity, g);
	}
	
    /*
     * Loads all of the artists from the music store into the
     * Globals metadata store.
     */
	private void loadAllArtists(Activity activity, Globals g) {
        artists = new ArrayList<String>();
		
		Cursor cursor = activity.getContentResolver().query(
        	    MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, 
        	    null, 
        	    null, 
        	    null, 
        	    MediaStore.Audio.Artists.ARTIST + " ASC");
        
        while (cursor.moveToNext()) {
            String artistName = cursor.getString(
            		cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            artists.add(artistName); 
        }
	}
	
	
	private void loadAllSongs(Activity activity, Globals g) {
		for (int i = 0; i < artists.size(); i++) {
			
			String artist = artists.get(i);
			final String where = MediaStore.Audio.AlbumColumns.ARTIST
        		+ "='" + artist.replace("'",  "''") + "'";
			
	        Cursor cursor = activity.getContentResolver().query(
	        	    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
	        	    null, 
	        	    where,
	        	    null, 
	        	    MediaStore.Audio.Albums.ARTIST + " ASC");
	        
	        while (cursor.moveToNext()) {
	            String songTitle = cursor.getString(
	            		cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
	            String path = cursor.getString(
	            		cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
	            Song song = new Song(songTitle, path, true);
	            song.setAlbum(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
	            song.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
	            
	            g.db.addSong(song); 
	        }
		}
	
	}
}
