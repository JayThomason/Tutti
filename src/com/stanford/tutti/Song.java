package com.stanford.tutti;

/*
 * Stores all metadata associated with a given song. Also includes references
 * to the album which includes the song and the artist who plays the song. This
 * information is not currently very relevant but it could be later.
 */
public class Song {
	
	/* The title of the song. */
	private String title;
	
	/* A reference to the artist whose song this is. */
	private Artist artist;
	
	/* A reference to the album the song is on. */
	private Album album;
	
	/* The path on the disk/sdcard to the raw song data or file. */
	private String path;
	
	/*
	 * Constructor
	 * 
	 * @param String title
	 */
	public Song(String title) {
		this.title = title;
	}
	
	/*
	 * Constructor
	 * 
	 * @param String title, String path
	 */
	public Song(String title, String path) {
		this.title = title;
		this.path = path;
	}
	
	/*
	 * Returns the title of the song.
	 * 
	 * @return String
	 */
	public String getTitle() {
		return title;
	}
	
	/*
	 * Returns the artist of the song.
	 * 
	 * @return Artist
	 */
	public Artist getArtist() {
		return artist;
	}
	
	/*
	 * Returns the album the song is on.
	 * 
	 * @return Album
	 */
	public Album getAlbum() {
		return album;
	}
	
	/*
	 * Sets the artist for the song.
	 * 
	 * @param Artist artist
	 */
	public void setArtist(Artist artist) {
		this.artist = artist;
	}
	
	/*
	 * Sets the album for the song.
	 * 
	 * @param Album album
	 */
	public void setAlbum(Album album) {
		this.album = album;
	}
	
	/*
	 * Gets the path for the song data or file.
	 * 
	 * @return String
	 */
	public String getPath() {
		return path;
	}
}
