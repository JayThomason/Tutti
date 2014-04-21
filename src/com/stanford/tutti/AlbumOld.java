package com.stanford.tutti;

import java.util.ArrayList;

import org.json.*; 

public class AlbumOld {
	private String title;
	private Artist artist;
	private boolean local;
	private ArrayList<Song> songList = new ArrayList<Song>();
	
	public AlbumOld(String title, Artist artist, boolean local) {
		this.title = title;
		this.artist = artist;
		this.local = local;
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
	
	public void setSongList(ArrayList<Song> songList) {
		this.songList = songList;
	}
	
	public String getTitle() {
		return title;
	}
	
	
	public boolean isLocal() {
		return local;
	}
	
	/*
	 * Converts this album and its list
	 * of songs into a JSONObject. 
	 * Used for sending album metadata to remote phones. 
	 * 
	 * @return JSONObject album
	 */
	public JSONObject toJSON(boolean justLocal) {
		JSONObject json = new JSONObject(); 
		try {
			json.put("title", title);
			JSONArray songArray = new JSONArray(); 
			for (int i = 0; i < songList.size(); i++) {
				Song song = songList.get(i);
				if (justLocal && song.isLocal())
					songArray.put(song.toJSON());
				else if (!justLocal)
					songArray.put(song.toJSON());
			}
			json.put("songs", songArray); 
		} catch (JSONException e) {
			e.printStackTrace();
		} 
		return json;
	}
	
	public boolean equals(Object obj) {
		if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof AlbumOld))
            return false;
        AlbumOld a = (AlbumOld) obj;
        if (!a.getTitle().equals(this.title))
        	return false;
        if (!a.getArtist().equals(this.artist))
        	return false;
        return true;
	}

}
