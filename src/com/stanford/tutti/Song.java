package com.stanford.tutti;

public class Song {
	private String title;
	private Artist artist;
	private Album album;
	private String path;
	
	public Song(String title) {
		this.title = title;
	}
	
	public Song(String title, String path) {
		this.title = title;
		this.path = path;
	}
	
	public String getTitle() {
		return title;
	}
	
	public Artist getArtist() {
		return artist;
	}
	
	public Album getAlbum() {
		return album;
	}
	
	public void setArtist(Artist artist) {
		this.artist = artist;
	}
	
	public void setAlbum(Album album) {
		this.album = album;
	}
	
	public String getPath() {
		return path;
	}
}
