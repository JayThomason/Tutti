package com.stanford.tutti;

import java.util.Locale;

import org.json.*; 

/**
 * Stores all metadata associated with a given song. 
 */
public class Song {
	
	/* The title of the song. */
	private String title;
	
	/* A reference to the artist whose song this is. */
	private String artist = ""; 
	
	/* A reference to the album the song is on. */
	private String album = ""; 
	
	/* The path on the disk/sdcard to the raw song data or file. */
	private String path; 
	
	/* Indicates whether the song is local to this device. */
	private boolean local; 
	
	/* Path to the album art for the song, if any */
	private String albumArt = ""; 
	
	/* IP address for the phone containing the song. */
	private String ipAddr = "";
	
	/* Port for the phone's server which has the song. */
	private int port = 0;
	
	/* Username that added this song to the Jam */
	private String addedBy = ""; 
	
	/* Track number of this song on its album, if any */
	private int trackNum = 0; 
	
	/* The ID (i.e. timestamp) if this song within the Jam, if any */
	private String jamID = ""; 
	
	
	/**
	 * Constructor
	 * 
	 * @param String title
	 * @param String path
	 */
	public Song(String title, String path, boolean local) {
		this.title = title;
		this.path = path;
		this.local = local;
	}
	
	/**
	 * Returns the title of the song.
	 * 
	 * @return String title
	 */
	public String getTitle() {
		return title;
	}
	
	/**
	 * Returns the artist of the song.
	 * 
	 * @return String artist
	 */
	public String getArtist() {
		return artist;
	}
	
	/**
	 * Returns the album the song is on.
	 * 
	 * @return String album
	 */
	public String getAlbum() {
		return album;
	}
	
	/**
	 * Sets the artist for the song.
	 * 
	 * @param String artist
	 */
	public void setArtist(String artist) {
		this.artist = artist;
	}
	
	/**
	 * Sets the album for the song.
	 * 
	 * @param String album
	 */
	public void setAlbum(String album) {
		this.album = album;
	}
	
	/**
	 * Gets the path for the song data or file.
	 * 
	 * @return String path
	 */
	public String getPath() {
		return path;
	}

	/**
	 * Gets whether the song is local to this phone. 
	 * 
	 * @return boolean local
	 */
	public boolean isLocal() {
		return local; 
	}
	
	/**
	 * Sets the album art path for this song. 
	 * 
	 * @param String albumArtPath
	 */
	public void setAlbumArt(String path) {
		albumArt = path; 
	}
	
	/**
	 * Gets the album art path for this song. 
	 * 
	 * @return String albumArtPath
	 */
	public String getAlbumArt() {
		return albumArt; 
	}
	
	/**
	 * Sets the ip address for this song. 
	 * 
	 * @param String ipAddr
	 */
	public void setIpAddr(String ipAddr) {
		this.ipAddr = ipAddr;
	}
	
	/**
	 * Sets the port number for this song. 
	 * 
	 * @param int port
	 */
	public void setPort(int port) {
		this.port = port;
	}
	
	/**
	 * Gets the ip address for this song. 
	 * 
	 * @return String ipAddr
	 */
	public String getIpAddr() {
		return ipAddr;
	}
	
	/**
	 * Gets the port number for this song. 
	 * 
	 * @return int port
	 */
	public int getPort() {
		return port;
	}
	
	/**
	 * Gets the user who added this song to the jam (if any). 
	 * 
	 * @return String addedBy
	 */
	public String getAddedBy() {
		return addedBy; 
	}
	
	/**
	 * Sets the user who added this song to the jam (if any). 
	 * 
	 * @param String addedBy
	 */
	public void setAddedBy(String addedBy) {
		this.addedBy = addedBy; 
	}
	
	/**
	 * Gets the track number for this song. 
	 * 
	 * @return int trackNum
	 */
	public int getTrackNum() {
		return trackNum; 
	}
	
	/**
	 * Sets the track number for this song. 
	 * 
	 * @param int trackNum
	 */
	public void setTrackNum(int num) {
		trackNum = num; 
	}
	
	/**
	 * Gets the jam ID for this song (if any). 
	 * 
	 * @return String jamID
	 */
	public String getJamID() {
		return jamID; 
	}
	
	/**
	 * Sets the jam ID for this song. 
	 * 
	 * @param String jamID
	 */
	public void setJamID(String jamID) {
		this.jamID = jamID; 
	}
	
	/**
	 * Returns an integer hashCode, formed by 
	 * concatenating the artist, album, and title of the song. 
	 * 
	 * @return int hashCode
	 */
	@Override
	public int hashCode() {
		StringBuilder keyBuilder = new StringBuilder("");
		String artist = getArtist();
		if (artist != null) {
			artist = artist.toLowerCase(Locale.ENGLISH); 
			artist = artist.replaceAll("[.'\"()]", ""); 
			keyBuilder.append(artist);
		}
		String album = getAlbum();
		if (album != null) {
			album = album.toLowerCase(Locale.ENGLISH); 
			album = album.replaceAll("[.'\"()]", ""); 
			keyBuilder.append(album);
		}
		String title = getTitle(); 
		if (title != null) {
			title = title.toLowerCase(Locale.ENGLISH); 
			title = title.replaceAll("[.'\"()]", ""); 
			keyBuilder.append(title);
		}
		return keyBuilder.toString().hashCode();
	}

	/**
	 * Tests two songs for equality: 
	 * i.e., same album, artist, and title. 
	 * 
	 * @return boolean isEqual
	 */
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
	
	/**
	 * Returns a JSON representation of this song. 
	 * 
	 * @return JSONObject jsonSong
	 */
	public JSONObject toJSON() {
		JSONObject song = new JSONObject(); 
		try {
			song.put("title", title); 
			song.put("artist", artist);
			song.put("album", album); 
			song.put("path", path); 
			song.put("ip", ipAddr); 
			song.put("port", port);
			song.put("addedBy", addedBy);
			song.put("jamID", jamID); 
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		
		return song; 
	}

}
