package com.stanford.tutti;

import java.util.ArrayList;
import org.json.*; 

public class Artist {
	private String name;
	private ArrayList<Album> albumList = new ArrayList<Album>();
	private boolean local;
	
	public Artist(String name, boolean local) {
		this.name = name;
		this.local = local;
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
	
	public boolean isLocal() {
		return local;
	}
	
	public JSONObject toJSON(boolean justLocal) {
		JSONObject json = new JSONObject(); 
		try {
			json.put("name", name);
			JSONArray albumArray = new JSONArray(); 
			for (int i = 0; i < albumList.size(); i++) {
				Album album = albumList.get(i);
				if (justLocal && album.isLocal())
					albumArray.put(album.toJSON(justLocal));
				else if (!justLocal)
					albumArray.put(album.toJSON(justLocal));
			}
			json.put("albums", albumArray); 
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return json; 
	}
}
