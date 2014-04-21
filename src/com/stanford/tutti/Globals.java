package com.stanford.tutti;

import java.util.*; 

import org.json.*; 

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

/* 
 * Stores any state which must be globally accessible, eg. variables which cannot
 * be passed to activities using the intent. 
 * 
 * We are currently storing all of the music library metadata in-memory using this
 * class. It supports accessing and searching the library metadata as well as
 * checking and setting the current artist, album, and song.
 */
public class Globals extends Application {
	private ArrayList<Artist> artistList = new ArrayList<Artist>();
	private ArrayList<Album> albumList = new ArrayList<Album>();
	private HashMap<String, Artist> artistMap = new HashMap<String, Artist>();
	private HashMap<String, Song> songMap = new HashMap<String, Song>();
	public Jam jam = new Jam(); 
	
	private int nextSongId = 1; 
	
	private static Context context; 
	
	@Override
	public void onCreate() {
		super.onCreate();
		Globals.context = getApplicationContext(); 
	}
	
	public static Context getAppContext() {
        return Globals.context;
    }


	/*
	 * Returns a list of all artists.
	 * 
	 * @return ArrayList<Artist>
	 */
	public ArrayList<Artist> getArtistList() {
		return artistList;
	}

	/*
	 * Adds an artist to the list.
	 * 
	 * @param Artist
	 */
	public void addArtist(Artist artist) {
		artistList.add(artist);
		artistMap.put(artist.getName(), artist);
	}	

	/*
	 * Returns an Artist given the artist's name.
	 * 
	 * @param String artistName
	 */
	public Artist getArtistByName(String artistName) {
		return artistMap.get(artistName);
	}

	/*
	 * Returns a list of all albums.
	 * 
	 * @return ArrayList<Album>
	 */
	public ArrayList<Album> getAlbumList() {
		return albumList;
	}

	/*
	 * Adds an album to the album list.
	 * 
	 * @param Album album
	 */
	public void addAlbum(Album album) {
		albumList.add(album);
	}

	/*
	 * Associates a song with a unique key in the song map.
	 */
	public void addSong(Song song) {
		songMap.put(Integer.toString(song.hashCode()), song);
	}

	/*
	 * Gets a song associated with a specific key.
	 */
	public Song getSongForUniqueKey(String key) {
		return songMap.get(key);
	}
	
	/*
	 * Get the integer primary key for the next song
	 * to be stored in the database, and increment. 
	 */
	public int nextSongId() {
		return nextSongId++; 
	}

	/*
	 * Gets the current library of artists/albums/songs as JSON. 
	 * 
	 * @param JSONObject object
	 * @param boolean (whether we should just get music that is local) 
	 */
	public JSONObject getArtistsAsJSON(boolean justLocal) {
		JSONObject json = new JSONObject(); 
		JSONArray artistArray = new JSONArray(); 
		for (int i = 0; i < artistList.size(); i++) {
			Artist artist = artistList.get(i);
			if (justLocal && artist.isLocal())
				artistArray.put(artist.toJSON(justLocal));
			else if (!justLocal) 
				artistArray.put(artist.toJSON(justLocal));
		}
		try {
			json.put("artists", artistArray);
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		return json; 
	}
	
	/*
	 * Return a string representation of the current device's IP address. 
	 */
	public String getIpAddr() {
		WifiManager wifiManager = 
				(WifiManager) getSystemService(WIFI_SERVICE);
		WifiInfo wifiInfo = wifiManager.getConnectionInfo();
		int ip = wifiInfo.getIpAddress();

		String ipString = String.format(
				"%d.%d.%d.%d",
				(ip & 0xff),
				(ip >> 8 & 0xff),
				(ip >> 16 & 0xff),
				(ip >> 24 & 0xff));

		return ipString;
	}
}
