package com.example.tuttiui;

import java.io.Serializable;
import java.util.ArrayList;

public class Jam implements Serializable {
	ArrayList<String> songs;
	
	Jam(){
		songs = new ArrayList<String>();
	}
	
	public void addSong(String song) {
		songs.add(song);
	}
	
	public ArrayList<String> getSongList() {
		return songs;
	}
}
