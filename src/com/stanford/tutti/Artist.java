package com.stanford.tutti;

import java.util.ArrayList;

import org.json.*; 

public class Artist {
	private String name;
	private ArrayList<Album> albumList = new ArrayList<Album>();
	private boolean local;
	
	public Artist(String name, boolean local) {
		this.name = name;
		this.local = local;
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
	
	public boolean isLocal() {
		return local;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof Album))
            return false;
		Artist a = (Artist) obj;
		if (!a.getName().equals(this.name))
			return false;
		return true;
	}
}
