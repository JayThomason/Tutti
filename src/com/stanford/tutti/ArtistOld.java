package com.stanford.tutti;

import java.util.ArrayList;

import org.json.*; 

public class ArtistOld {
	private String name;
	private ArrayList<Album> albumList = new ArrayList<Album>();
	private boolean local;
	
	public ArtistOld(String name, boolean local) {
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
	
	
	/*
	 * Converts this artist and their albums
	 * and songs into a nested JSONObject. 
	 * Used for sending artist metadata to remote phones.  
	 * 
	 * @return JSONObject artist
	 */
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
			e.printStackTrace();
		} 
		return json; 
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null)
            return false;
        if (obj == this)
            return true;
        if (!(obj instanceof Album))
            return false;
		ArtistOld a = (ArtistOld) obj;
		if (!a.getName().equals(this.name))
			return false;
		return true;
	}
}
