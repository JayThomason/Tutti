package com.stanford.tutti;

import java.util.ArrayList;

public class Album {
	private String title;
	private Artist artist;
	private ArrayList<Song> songList = new ArrayList<Song>();
	
	public Album(String title, Artist artist) {
		this.title = title;
		this.artist = artist;
	}
	
	public Artist getArtist() {
		return artist;
	}
	
	public void addSong(Song song) {
		songList.add(song);
	}
	
	public ArrayList<Song> getSongList() {
		return songList;
	}
	
	public void setSongs(ArrayList<Song> songList) {
		this.songList = songList;
	}
	
	public String getTitle() {
		return title;
	}
}
