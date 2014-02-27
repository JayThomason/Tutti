package com.stanford.tutti;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;

public class Globals extends Application {
	private ArrayList<String> artists = new ArrayList<String>();
	private HashMap<String, ArrayList<String>> artistAlbumMap = new HashMap<String, ArrayList<String>>();
	private HashMap<String, ArrayList<String>> albumSongMap = new HashMap<String, ArrayList<String>>();
	// I assume album names are unique. Will fix later.
		
	public ArrayList<String> getArtists() {
		return artists;
	}
	
	public void setArtists(ArrayList<String> newArtists) {
		artists = newArtists;
	}
	
	public void addArtist(String artist) {
		artists.add(artist);
	}

	
	public ArrayList<String> getAlbumsForArtist(String artist) {
		return artistAlbumMap.get(artist);
	}
	
	public void addAlbumForArtist(String artist, String album) {
		if (artistAlbumMap.get(artist) == null) {
			artistAlbumMap.put(artist, new ArrayList<String>());
		}
		artistAlbumMap.get(artist).add(album);
	}
	
	public ArrayList<String> getSongsForAlbum(String album) {
		return albumSongMap.get(album);
	}
	
	public void addSongToAlbum(String album, String song) {
		if (albumSongMap.get(album) == null) {
			albumSongMap.put(album, new ArrayList<String>());
		}
		albumSongMap.get(album).add(song);
	}

	public void setSongsForAlbum(String album, ArrayList<String> songs) {
		albumSongMap.put(album, songs);
	}
	
	public void setAlbumsForArtist(String artist, ArrayList<String> albums) {
		artistAlbumMap.put(artist,  albums);
	}
}
