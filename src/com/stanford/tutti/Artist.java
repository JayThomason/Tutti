package com.stanford.tutti;

import java.util.ArrayList;
import org.json.*; 

public class Artist {
	private String name;
	private ArrayList<Album> albumList = new ArrayList<Album>();
	
	public Artist(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
	
	public void addAlbum(Album album) {
		albumList.add(album);
	}
	
	public ArrayList<Album> getAlbumList() {
		return albumList;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject(); 
		try {
			json.put("name", name);
			JSONArray albumArray = new JSONArray(); 
			for (int i = 0; i < albumList.size(); i++) {
				albumArray.put(albumList.get(i).toJSON()); 
			}
			json.put("albums", albumArray); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return json; 
	}
}
