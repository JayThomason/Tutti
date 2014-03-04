package com.stanford.tutti;

import java.util.ArrayList;

public class Artist {
	private String name;
	private ArrayList<Album> albumList = new ArrayList<Album>();
	
	public Artist(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void addAlbum(Album album) {
		albumList.add(album);
	}
	
	public ArrayList<Album> getAlbumList() {
		return albumList;
	}
}
