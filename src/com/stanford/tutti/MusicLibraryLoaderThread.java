package com.stanford.tutti;

import java.util.ArrayList;

import android.app.Activity;
import android.database.Cursor;
import android.os.Message;
import android.provider.MediaStore;

/**
 * Loads all local music meta data from the SDCard 
 * into the in-app music library database.
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

	
	/**
	 * Loads all of the music into the Globals music metadata store.
	 * 
	 * @param Activity activity
	 */
    public void loadMusic(Activity activity) {
    	Globals g = (Globals) activity.getApplication();
		loadAllArtists(activity);
		loadAllSongs(activity, g);
		g.logger.updateNumberSongs();
	}
	
    /**
     * Loads all of the artists from the music store into the
     * Globals metadata store.
     * 
     * @param Activity activity
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
	
	/**
	 * Loads all metadata from the local music library into the in-app database. 
	 * 
	 * @param Activity activity
	 * @param Globals globals
	 */
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
	        
	        ArrayList<String> albumList = new ArrayList<String>(); 
	        
	        while (cursor.moveToNext()) {
	            String songTitle = cursor.getString(
	            		cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
	            String path = cursor.getString(
	            		cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
	            Song song = new Song(songTitle, path, true);
	            
	            String albumTitle = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)); 
	            song.setAlbum(albumTitle);
	            song.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
	            
	            String artPath = ""; 
	            if (albumTitle != "") {
	            	String trackNumString = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TRACK));
	            	
	            	int trackNum;
	            	
	            	if (trackNumString != null && trackNumString.equals("null")) {
	            		trackNum = Integer.parseInt(trackNumString);
	            	}
	            	else {
	            		trackNum = 0;
	            	}
	            	
	            	song.setTrackNum(trackNum);
	            	
		            Cursor artCursor = activity.getContentResolver().query(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, 
		                    new String[] {MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_ART}, 
		                    MediaStore.Audio.Albums.ALBUM + "=?", 
		                    new String[] { albumTitle }, 
		                    null);
	
		            if (artCursor.moveToFirst() && artCursor.getString(1) != null) {
		            	artPath = artCursor.getString(1);
		            }
		            
		            song.setAlbumArt(artPath); 
		            artCursor.close(); 
	            } else {
	            	song.setTrackNum(0);
	            	
	            	// SET DEFAULT ALBUM ART??
	            }
	            
	            song.setIpAddr(g.getIpAddr());
	            
	            if (!albumList.contains(albumTitle)) {
	            	Song albumSong = new Song("DISPLAY_ALBUM", "", true); 
	            	albumSong.setAlbum(albumTitle);
	            	albumSong.setArtist(cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))); 
	            	albumSong.setTrackNum(-1); 
	            	albumSong.setAlbumArt(artPath);
	            	albumSong.setIpAddr(g.getIpAddr());
	            	g.db.addSongToLibrary(albumSong); 
	            	albumList.add(albumTitle); 
	            }
	            
	            
	            g.db.addSongToLibrary(song); 
	        }
	        cursor.close(); 
	        
	    	g.sendUIMessage(0); 
		}	
	}
}
