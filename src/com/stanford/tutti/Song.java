package com.stanford.tutti;

import org.json.*; 

/*
 * Stores all metadata associated with a given song. Also includes references
 * to the album which includes the song and the artist who plays the song. 
 */
public class Song {
	
	/* The title of the song. */
	private String title;
	
	/* A reference to the artist whose song this is. */
	private String artist;
	
	/* A reference to the album the song is on. */
	private String album;
	
	/* The path on the disk/sdcard to the raw song data or file. */
	private String path; 
	
	/* Indicates whether the song is local to this device. */
	private boolean local; 
	
	/* Path to the album art for the song, if any */
	private String albumArt = ""; 
	
	/* IP address for the phone containing the song. Empty if local. */
	private String ipAddr = "";
	
	/*
	 * Constructor
	 * 
	 * @param String title, String path
	 */
	public Song(String title, String path, boolean local) {
		this.title = title;
		this.path = path;
		this.local = local;
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
	public String getArtist() {
		return artist;
	}
	
	/*
	 * Returns the album the song is on.
	 * 
	 * @return Album
	 */
	public String getAlbum() {
		return album;
	}
	
	/*
	 * Sets the artist for the song.
	 * 
	 * @param Artist artist
	 */
	public void setArtist(String artist) {
		this.artist = artist;
	}
	
	/*
	 * Sets the album for the song.
	 * 
	 * @param Album album
	 */
	public void setAlbum(String album) {
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

	/*
	 * Gets whether the song is local to this phone. 
	 * 
	 * @return boolean local
	 */
	public boolean isLocal() {
		return local; 
	}
	
	public void setAlbumArt(String path) {
		albumArt = path; 
	}
	
	public String getAlbumArt() {
		return albumArt; 
	}
	
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}
	
	public String getIpAddr() {
		return ipAddr;
	}
	
	@Override
	public int hashCode() {
		StringBuilder keyBuilder = new StringBuilder("");
		String artist = getArtist();
		if (artist != null)
			keyBuilder.append(artist);
		String album = getAlbum();
		if (album != null) 
			keyBuilder.append(album);
		keyBuilder.append(getTitle());
		return keyBuilder.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		Song s = (Song) o;
		if (o == null)
			return false;
		if (!s.getTitle().equals(this.title))
			return false;
		if (!s.getArtist().equals(this.artist))
			return false;
		if (!s.getAlbum().equals(this.album))
			return false;
		return true;
	}

}
