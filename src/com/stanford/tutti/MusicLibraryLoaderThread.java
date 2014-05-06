package com.stanford.tutti;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.provider.MediaStore;

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
public class MusicLibraryLoaderThread extends Thread {
	
	private ArrayList<String> artists; 
	Activity activity; 
	
	public MusicLibraryLoaderThread(Activity activity) {
		this.activity = activity; 
	}

	public void run() {
		loadMusic(activity); 
	}

	
	/*
	 * Loads all of the music into the Globals music metadata store.
	 */
    public void loadMusic(Activity activity) {
    	Globals g = (Globals) activity.getApplication();
		loadAllArtists(activity);
		loadAllSongs(activity, g);
	}
	
    /*
     * Loads all of the artists from the music store into the
     * Globals metadata store.
     */
	private void loadAllArtists(Activity activity) {
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
	            
	            String albumTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)); 
	            song.setAlbum(albumTitle);
	            song.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
	            
	            if (albumTitle != "") {
		            Cursor artCursor = activity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, 
		                    new String[] {MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_ART}, 
		                    MediaStore.Audio.Albums.ALBUM + "=?", 
		                    new String[] { albumTitle }, 
		                    null);
	
		            String artPath = ""; 
		            if (artCursor.moveToFirst() && artCursor.getString(1) != null) {
		            	artPath = artCursor.getString(1);
		            }
		            
		            song.setAlbumArt(artPath); 
		            artCursor.close(); 
	            }
	            
	            song.setIpAddr(g.getIpAddr());
	            
	            g.db.addSongToLibrary(song); 
	        }
	        cursor.close(); 
		}	
	}
}
