package com.stanford.tutti;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Application;

public class Globals extends Application {
	private ArrayList<Artist> artistList = new ArrayList<Artist>();
	private ArrayList<Album> albumList = new ArrayList<Album>();
	private HashMap<String, Artist> artistMap = new HashMap<String, Artist>();
		
	public ArrayList<Artist> getArtistList() {
		return artistList;
	}
	
	public void addArtist(Artist artist) {
		artistList.add(artist);
		artistMap.put(artist.getName(), artist);
	}	
	
	public Artist getArtistByName(String artistName) {
		return artistMap.get(artistName);
	}
	
	public ArrayList<Album> getAlbumList() {
		return albumList;
	}
	
	public void addAlbum(Album album) {
		albumList.add(album);
	}
}
