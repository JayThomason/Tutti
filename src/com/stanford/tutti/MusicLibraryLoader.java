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
	
	/*
	 * Loads all of the music into the Globals music metadata store.
	 */
    public void loadMusic(Activity activity) {
    	Globals g = (Globals) activity.getApplication();
		loadAllArtists(activity, g);
		loadAllAlbums(activity, g);
		loadAllSongs(activity, g);
	}
	
    /*
     * Loads all of the artists from the music store into the
     * Globals metadata store.
     */
	private void loadAllArtists(Activity activity, Globals g) {
        Cursor cursor = activity.getContentResolver().query(
        	    MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, 
        	    null, 
        	    null, 
        	    null, 
        	    MediaStore.Audio.Artists.ARTIST + " ASC");
        
        while (cursor.moveToNext()) {
            String artistName = cursor.getString(
            		cursor.getColumnIndex(MediaStore.Audio.Artists.ARTIST));
            Artist artist = new Artist(artistName, true);
            g.addArtist(artist);
        }
	}
	
	/*
	 * Loads all of the albums for each artist into the Globals
	 * metadata store.
	 */
	private void loadAllAlbums(Activity activity, Globals g) {
		ArrayList<Artist> artistList = g.getArtistList();
		for (int i = 0; i < artistList.size(); ++i) {
			Artist artist = artistList.get(i);
			final String where = MediaStore.Audio.AlbumColumns.ARTIST
        		+ "='" + artist.getName().replace("'",  "''") + "'";
        
	        Cursor cursor = activity.getContentResolver().query(
	        	    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, 
	        	    null, 
	        	    where, 
	        	    null, 
	        	    MediaStore.Audio.Albums.ALBUM + " ASC");
	        
	        while (cursor.moveToNext()) {
	            String albumTitle = cursor.getString(
	            		cursor.getColumnIndex(MediaStore.Audio.Albums.ALBUM));
	            Album album = new Album(albumTitle, artist, true);
	            artist.addAlbum(album);
	            g.addAlbum(album);
	        }
		}
	}
	
	/*
	 * Loads every song in the music store on the phone into
	 * the Globals' metadata store.
	 */
	private void loadAllSongs(Activity activity, Globals g) {
		ArrayList<Album> albumList = g.getAlbumList();
		for (int i = 0; i < albumList.size(); ++i) {
			Album album = albumList.get(i);
	    	final String where = MediaStore.Audio.Media.ALBUM
	        		+ "='" + album.getTitle().replace("'", "''") + "'";
	        
	        Cursor cursor = activity.getContentResolver().query(
	        	    MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, 
	        	    null, 
	        	    where, 
	        	    null, 
	        	    MediaStore.Audio.Albums.ALBUM + " ASC");
	        
	        while (cursor.moveToNext()) {
	            String songTitle = cursor.getString(
	            		cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));
	            String path = cursor.getString(
	            		cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
	            Song song = new Song(songTitle, path, true);
	            System.out.println(song.getPath());
	            song.setAlbum(album);
	            song.setArtist(album.getArtist());
	            g.addSong(song);
	            album.addSong(song);
			}
		}
	}
}
