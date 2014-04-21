package com.stanford.tutti;

import org.json.*; 

/*
 * Stores all metadata associated with a given song. Also includes references
 * to the album which includes the song and the artist who plays the song. 
 */
public class SongOld {
	
	/* The title of the song. */
	private String title;
	
	/* A reference to the artist whose song this is. */
	private Artist artist;
	
	/* A reference to the album the song is on. */
	private Album album;
	
	/* The path on the disk/sdcard to the raw song data or file. */
	private String path; 
	
	/* Indicates whether the song is local to this device. */
	private boolean local; 
	
	/*
	 * Constructor
	 * 
	 * @param String title
	 */
	public SongOld(String title, boolean local) {
		this.title = title;
		this.local = local;
	}
	
	/*
	 * Constructor
	 * 
	 * @param String title, String path
	 */
	public SongOld(String title, String path, boolean local) {
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
	
	/*
	 * Gets whether the song is local to this phone. 
	 * 
	 * @return boolean local
	 */
	public boolean isLocal() {
		return local; 
	}
	
	/*
	 * Converts this song and its data to a JSONObject. 
	 * Used for sending song metadata to remote phones.  
	 * 
	 * @return JSONObject artist
	 */
	public JSONObject toJSON() {
		JSONObject json = new JSONObject(); 
		try {
			json.put("title", title); 
			json.put("path", path); 
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		return json; 
	}
	
	@Override
	public int hashCode() {
		StringBuilder keyBuilder = new StringBuilder("");
		Artist artist = getArtist();
		if (artist != null)
			keyBuilder.append(artist.getName());
		Album album = getAlbum();
		if (album != null) 
			keyBuilder.append(album.getTitle());
		keyBuilder.append(getTitle());
		return keyBuilder.toString().hashCode();
	}
	
	@Override
	public boolean equals(Object o) {
		SongOld s = (SongOld) o;
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
