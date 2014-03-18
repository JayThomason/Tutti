package com.stanford.tutti;

import java.io.Serializable;
import java.util.ArrayList;

public class Jam implements Serializable {
	
	ArrayList<Song> songs;
	private Song currentSong = null;
	private int currentSongIndex = -1; 
	
	Jam() {
		songs = new ArrayList<Song>();
	}
	
	public void addSong(Song song) {
		songs.add(song);
	}
	
	public ArrayList<Song> getSongList() {
		return songs;
	}
	
	public Song getCurrentSong() {
		return currentSong; 
	}
	
	public int getCurrentSongIndex() {
		return currentSongIndex; 
	}
	
	public void iterateCurrentSong() {
		currentSongIndex++; 
    	currentSong = songs.get(currentSongIndex); 
	}
	
	public void setCurrentSong(Song song) {
		currentSong = song; 
		currentSongIndex = songs.indexOf(song); 
	}
	
	public void setCurrentSongByIndex(int index) {
		if (index < songs.size()) {
			currentSongIndex = index; 
			currentSong = songs.get(index); 
		}
	}
}
