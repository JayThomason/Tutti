package com.stanford.tutti;

import java.util.ArrayList;

public class Album {
	private String title;
	private String artist;
	private boolean local;
	private ArrayList<Song> songList = new ArrayList<Song>();
	
	public Album(String title, String artist, boolean local) {
		this.title = title;
		this.artist = artist;
		this.local = local;
	}
	
	public String getArtist() {
		return artist;
	}
	
	public void addSong(Song song) {
		songList.add(song);
	}
	
	public ArrayList<Song> getSongList() {
		return songList;
	}
	
	public void setSongList(ArrayList<Song> songList) {
		this.songList = songList;
	}
	
	public String getTitle() {
		return title;
	}
	
	
	public boolean isLocal() {
		return local;
	}
	
	public boolean equals(Object obj) {
		if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof Album))
            return false;
        Album a = (Album) obj;
        if (!a.getTitle().equals(this.title))
        	return false;
        if (!a.getArtist().equals(this.artist))
        	return false;
        return true;
	}

}
