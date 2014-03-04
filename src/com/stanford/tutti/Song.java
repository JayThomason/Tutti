package com.stanford.tutti;

public class Song {
	private String title;
	private Artist artist;
	private Album album;
	
	public Song(String title) {
		this.title = title;
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
}
