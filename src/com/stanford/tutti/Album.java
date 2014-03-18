package com.stanford.tutti;

import java.util.ArrayList;

import org.json.*; 

public class Album {
	private String title;
	private Artist artist;
	private ArrayList<Song> songList = new ArrayList<Song>();
	private boolean local;
	
	public Album(String title, Artist artist, boolean local) {
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
	
	public void setSongs(ArrayList<Song> songList) {
		this.songList = songList;
	}
	
	public String getTitle() {
		return title;
	}
	
	
	public boolean isLocal() {
		return local;
	}
	
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return json;
	}

}
